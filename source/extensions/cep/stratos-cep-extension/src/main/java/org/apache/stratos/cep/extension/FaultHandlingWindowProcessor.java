/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.stratos.cep.extension;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.stratos.messaging.broker.publish.EventPublisher;
import org.apache.stratos.messaging.broker.publish.EventPublisherPool;
import org.apache.stratos.messaging.domain.topology.Cluster;
import org.apache.stratos.messaging.domain.topology.Member;
import org.apache.stratos.messaging.domain.topology.MemberStatus;
import org.apache.stratos.messaging.domain.topology.Service;
import org.apache.stratos.messaging.event.health.stat.MemberFaultEvent;
import org.apache.stratos.messaging.message.receiver.topology.TopologyEventReceiver;
import org.apache.stratos.messaging.message.receiver.topology.TopologyManager;
import org.apache.stratos.messaging.util.Constants;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.core.event.in.InEvent;
import org.wso2.siddhi.core.event.in.InListEvent;
import org.wso2.siddhi.core.persistence.ThreadBarrier;
import org.wso2.siddhi.core.query.QueryPostProcessingElement;
import org.wso2.siddhi.core.query.processor.window.RunnableWindowProcessor;
import org.wso2.siddhi.core.query.processor.window.WindowProcessor;
import org.wso2.siddhi.core.util.collection.queue.scheduler.ISchedulerSiddhiQueue;
import org.wso2.siddhi.core.util.collection.queue.scheduler.SchedulerSiddhiQueue;
import org.wso2.siddhi.core.util.collection.queue.scheduler.SchedulerSiddhiQueueGrid;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;
import org.wso2.siddhi.query.api.expression.constant.IntConstant;
import org.wso2.siddhi.query.api.expression.constant.LongConstant;
import org.wso2.siddhi.query.api.extension.annotation.SiddhiExtension;

@SiddhiExtension(namespace = "stratos", function = "faultHandling")
public class FaultHandlingWindowProcessor extends WindowProcessor implements RunnableWindowProcessor {

    private static final int TIME_OUT = 60 * 1000;
    static final Logger log = Logger.getLogger(FaultHandlingWindowProcessor.class);
    private ScheduledExecutorService eventRemoverScheduler;
    private ScheduledFuture<?> lastSchedule;
    private int subjectedAttrIndex;
    private ThreadBarrier threadBarrier;
    private long timeToKeep;
    private ISchedulerSiddhiQueue<StreamEvent> window;
    private ConcurrentHashMap<String, Long> memberTimeStampMap = new ConcurrentHashMap<String, Long>();
    private ConcurrentHashMap<String, Member> memberIdMap = new ConcurrentHashMap<String, Member>();
    EventPublisher healthStatPublisher = EventPublisherPool.getPublisher(Constants.HEALTH_STAT_TOPIC);
    Map<String, Object> MemberFaultEventMap = new HashMap<String, Object>();
    Map<String, Object> memberFaultEventMessageMap = new HashMap<String, Object>();
    private TopologyEventReceiver topologyEventReceiver;
    private String memberID;
    
    private static final String DATA_BRIDGE_CONFIG_XML = "data-bridge-config.xml";
    private static final String DATA_BRIDGE_DIR = "data-bridge";

    @Override
    protected void processEvent(InEvent event) {
        addDataToMap(event);
    }

    @Override
    protected void processEvent(InListEvent listEvent) {
        for (int i = 0, size = listEvent.getActiveEvents(); i < size; i++) {
            addDataToMap((InEvent) listEvent.getEvent(i));
        }
    }

    protected void addDataToMap(InEvent event) {
        if (memberID != null) {
            String id = (String) event.getData()[subjectedAttrIndex];
            memberTimeStampMap.put(id, event.getTimeStamp());
            log.debug("Event received from [member-id] " + id);
        } else {
            log.error("NULL member ID in the event received");
        }
    }

    @Override
    public Iterator<StreamEvent> iterator() {
        return window.iterator();
    }

    @Override
    public Iterator<StreamEvent> iterator(String predicate) {
        if (siddhiContext.isDistributedProcessingEnabled()) {
            return ((SchedulerSiddhiQueueGrid<StreamEvent>) window).iterator(predicate);
        } else {
            return window.iterator();
        }
    }

    /*
    *  Retrieve the current activated member list from the topology and put them into the
    *  memberTimeStampMap if not already exists. This will allow the system to recover
    *  from any inconsistent state caused by MB/CEP failures.
    */
    private void loadFromTopology() {
        if (TopologyManager.getTopology().isInitialized()) {
            TopologyManager.acquireReadLock();
            memberIdMap.clear();
            long currentTimeStamp = System.currentTimeMillis();
            Iterator<Service> servicesItr = TopologyManager.getTopology().getServices().iterator();
            while (servicesItr.hasNext()) {
                Service service = servicesItr.next();
                Iterator<Cluster> clusterItr = service.getClusters().iterator();
                while (clusterItr.hasNext()) {
                    Cluster cluster = clusterItr.next();
                    Iterator<Member> memberItr = cluster.getMembers().iterator();
                    while (memberItr.hasNext()) {
                        Member member = memberItr.next();
                        if (member.getStatus().equals(MemberStatus.Activated)) {
                            memberTimeStampMap.putIfAbsent(member.getMemberId(), currentTimeStamp);
                            memberIdMap.put(member.getMemberId(), member);
                        }
                    }
                }
            }
            TopologyManager.releaseReadLock();
        }
        if (log.isDebugEnabled()) {
            log.debug("Member TimeStamp Map: " + memberTimeStampMap);
            log.debug("Member ID Map: " + memberIdMap);
        }
    }

    private void publishMemberFault(String memberID) {
        Member member = memberIdMap.get(memberID);
        if (member == null) {
            log.error("Failed to publish MemberFault event. Member having [member-id] " + memberID + " does not exist in topology");
            return;
        }
        MemberFaultEvent memberFaultEvent = new MemberFaultEvent(member.getClusterId(), member.getMemberId(), member.getPartitionId(), 0);
        memberFaultEventMessageMap.put("message", memberFaultEvent);
        Properties headers = new Properties();
        headers.put(Constants.EVENT_CLASS_NAME, memberFaultEvent.getClass().getName());
        healthStatPublisher.publish(MemberFaultEventMap, headers, true);

        if (log.isDebugEnabled()) {
            log.debug("Published MemberFault event for [member-id] " + memberID);
        }
    }


    @Override
    public void run() {
        try {
            threadBarrier.pass();
            loadFromTopology();
            Iterator it = memberTimeStampMap.entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                long currentTime = System.currentTimeMillis();
                Long eventTimeStamp = (Long) pair.getValue();

                if ((currentTime - eventTimeStamp) > TIME_OUT) {
                    log.info("Faulty member detected [member-id] " + pair.getKey() + " with [last time-stamp] " + eventTimeStamp + " [time-out] " + TIME_OUT + " milliseconds");
                    it.remove();
                    publishMemberFault((String) pair.getKey());
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Fault handling processor iteration completed with [time-stamp map length] " + memberTimeStampMap.size() + " [activated member-count] " + memberIdMap.size());
            }
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        } finally {
            if (lastSchedule != null) {
                lastSchedule.cancel(false);
            }
            lastSchedule = eventRemoverScheduler.schedule(this, timeToKeep, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    protected Object[] currentState() {
        return new Object[]{window.currentState()};
    }

    @Override
    protected void restoreState(Object[] data) {
        window.restoreState(data);
        window.restoreState((Object[]) data[0]);
        window.reSchedule();
    }

    @Override
    protected void init(Expression[] parameters, QueryPostProcessingElement nextProcessor, AbstractDefinition streamDefinition, String elementId, boolean async, SiddhiContext siddhiContext) {
        if (parameters[0] instanceof IntConstant) {
            timeToKeep = ((IntConstant) parameters[0]).getValue();
        } else {
            timeToKeep = ((LongConstant) parameters[0]).getValue();
        }

        memberID = ((Variable) parameters[1]).getAttributeName();

        String subjectedAttr = ((Variable) parameters[1]).getAttributeName();
        subjectedAttrIndex = streamDefinition.getAttributePosition(subjectedAttr);

        if (this.siddhiContext.isDistributedProcessingEnabled()) {
            window = new SchedulerSiddhiQueueGrid<StreamEvent>(elementId, this, this.siddhiContext, this.async);
        } else {
            window = new SchedulerSiddhiQueue<StreamEvent>(this);
        }
        MemberFaultEventMap.put("org.apache.stratos.messaging.event.health.stat.MemberFaultEvent", memberFaultEventMessageMap);
        
        // Wait until 
        startTopologyEventReceiver();       

        //Ordinary scheduling
        window.schedule();

    }

    private void startTopologyEventReceiver() {
    	
    	new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				waitUntilPortsActive(loadThriftConfig());
		    	topologyEventReceiver = new TopologyEventReceiver();
		    	Thread thread = new Thread(topologyEventReceiver);
		        thread.start();               
		        log.info("WSO2 CEP topology receiver thread started");
			}
		}).start();   	
		
	}

	@Override
    public void schedule() {
        if (lastSchedule != null) {
            lastSchedule.cancel(false);
        }
        lastSchedule = eventRemoverScheduler.schedule(this, timeToKeep, TimeUnit.MILLISECONDS);
    }

    @Override
    public void scheduleNow() {
        if (lastSchedule != null) {
            lastSchedule.cancel(false);
        }
        lastSchedule = eventRemoverScheduler.schedule(this, 0, TimeUnit.MILLISECONDS);
    }

    @Override
    public void setScheduledExecutorService(ScheduledExecutorService scheduledExecutorService) {
        this.eventRemoverScheduler = scheduledExecutorService;
    }

    @Override
    public void setThreadBarrier(ThreadBarrier threadBarrier) {
        this.threadBarrier = threadBarrier;
    }

    @Override
    public void destroy() {
        this.topologyEventReceiver.terminate();
        window = null;
    }
        
    private DataReceiverConfig loadThriftConfig() {
    	
    	DataReceiverConfig dataReceiverConfig = new DataReceiverConfig();
    	OMElement hostName = null;
    	OMElement securePort = null;
    	OMElement nsPort = null;
    	
            OMElement config = this.loadDataBridgeConfig();
            OMElement thriftDR = (OMElement) config.getChildrenWithLocalName("thriftDataReceiver").next();
            try {
            securePort = (OMElement) thriftDR.getChildrenWithLocalName("securePort").next();
            nsPort = (OMElement) thriftDR.getChildrenWithLocalName("port").next();
            } catch (NoSuchElementException ex) {
            	// ignore
            }
            
            try {
                hostName = (OMElement) thriftDR.getChildrenWithLocalName("hostName").next();
            } catch(NoSuchElementException ex) {
            	String msg = "Thrift Data Receiver hostName must be defined in data-bridge-config.xml ";
            	log.error(msg);
            	throw new RuntimeException(msg, ex);
            }
            
            setHostValue(dataReceiverConfig, hostName);
            dataReceiverConfig.setHost(hostName.getText());
            
            if(securePort != null) {
            	dataReceiverConfig.setSecurePort(Integer.parseInt(securePort.getText())+getPortOffset());
            }
            if(nsPort != null) {
            	dataReceiverConfig.setPort(Integer.parseInt(nsPort.getText())+getPortOffset());
            }
        
    	
		return dataReceiverConfig;
    }
        
    private void setHostValue(DataReceiverConfig dataReceiverConfig,
    		OMElement hostName) {				
    	if(hostName != null) {
    		dataReceiverConfig.setHost(hostName.getText());
    	}
	}

	private int getPortOffset() {
		return Integer.parseInt(System.getProperty("portOffset"));
	}

	private OMElement loadDataBridgeConfig() {
    	
        String path = CarbonUtils.getCarbonConfigDirPath() + File.separator + DATA_BRIDGE_DIR + File.separator + DATA_BRIDGE_CONFIG_XML;
        try {
            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(new File(path)));
            XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            OMElement omElement = builder.getDocumentElement();
            omElement.build();
            return omElement;
        } catch (Exception e) {
        	String msg = "Error in reading data bridge configuration: " ;
            log.error(msg+ e.getMessage(), e);
            throw new RuntimeException(msg, e);
        }
    }
	
    private void waitUntilPortsActive(DataReceiverConfig dataReceiverConfig) {
        long portCheckTimeOut = 1000 * 60 * 10;
        String portCheckTimeOutStr = System.getProperty("port.check.timeout");
        if (StringUtils.isNotBlank(portCheckTimeOutStr)) {
            portCheckTimeOut = Integer.parseInt(portCheckTimeOutStr);
        }
        if (log.isDebugEnabled()) {
            log.debug("Port check timeout: " + portCheckTimeOut);
        }

        long startTime = System.currentTimeMillis();
        boolean active = false;
        while (!active) {
            if(log.isInfoEnabled()) {
                log.info("Waiting for Thrift Data Receiver ports to be active: [ip] "+dataReceiverConfig.getHost()
                		+" [ports] "+ printPorts(dataReceiverConfig));
                		
            }
            List<Integer> portList = new ArrayList<Integer>();
            if(dataReceiverConfig.getPort() != null ) {
            	portList.add(dataReceiverConfig.getPort());	
            }
            if(dataReceiverConfig.getSecurePort() != null) {
            	portList.add(dataReceiverConfig.getSecurePort());
            }
            active = checkPortsActive(dataReceiverConfig.getHost(), portList);
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            if (duration > portCheckTimeOut) {
                return;
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
            }
        }
        if(log.isInfoEnabled()) {
            log.info("Thrift Data Receiver ports activated: [ip] " + dataReceiverConfig.getHost() + " [ports] " +printPorts(dataReceiverConfig));
        }
    }

    private String printPorts(DataReceiverConfig dataReceiverConfig) {
        StringBuilder sb = new StringBuilder();
        if(dataReceiverConfig.getPort() != null) {
        	sb.append(dataReceiverConfig.getPort()).append(",");
        }
        if(dataReceiverConfig.getSecurePort() != null) {
        	sb.append(dataReceiverConfig.getSecurePort());
        }		
		return sb.toString();
	}

	public static boolean checkPortsActive(String ipAddress, List<Integer> ports) {
        if (ports.size() == 0) {
            throw new RuntimeException("No ports found");
        }
        for (int port : ports) {
            Socket socket = null;
            try {
                SocketAddress httpSockaddr = new InetSocketAddress(ipAddress, port);
                socket = new Socket();
                socket.connect(httpSockaddr, 5000);
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Thrift Data Receiver Port %s is active", port));
                }
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Thrift Data Receiver Port %s is not active", port));
                }
                return false;
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        return true;
    }
    

}
