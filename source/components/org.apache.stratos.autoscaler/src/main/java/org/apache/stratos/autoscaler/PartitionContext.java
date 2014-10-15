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
package org.apache.stratos.autoscaler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.stratos.autoscaler.util.ConfUtil;
import org.apache.stratos.cloud.controller.stub.deployment.partition.Partition;
import org.apache.stratos.cloud.controller.stub.pojo.MemberContext;


/**
 * This is an object that inserted to the rules engine.
 * Holds information about a partition.
 * @author nirmal
 *
 */

public class PartitionContext implements Serializable{

	private static final long serialVersionUID = -2920388667345980487L;
	private static final Log log = LogFactory.getLog(PartitionContext.class);
    private String partitionId;
    private String serviceName;
    private String networkPartitionId;
    private Partition partition;
//    private int currentActiveMemberCount = 0;
    private int minimumMemberCount = 0;
    private int pendingMembersFailureCount = 0;
    private final int PENDING_MEMBER_FAILURE_THRESHOLD = 5;

    // properties
    private Properties properties;
    
    // 15 mints as the default
    private long expiryTime = 900000;
    // pending members
    private List<MemberContext> pendingMembers;
    
    // 1 day as default
    private long obsoltedMemberExpiryTime = 1*24*60*60*1000;

    // members to be terminated
    private Map<String, MemberContext> obsoletedMembers;
    
    // Contains the members that CEP notified as faulty members.
//    private List<String> faultyMembers;
    
    // active members
    private List<MemberContext> activeMembers;

    // termination pending members, member is added to this when Autoscaler send grace fully shut down event
    private List<MemberContext> terminationPendingMembers;

    //Keep statistics come from CEP
    private Map<String, MemberStatsContext> memberStatsContexts;
    private int nonTerminatedMemberCount;
//    private int totalMemberCount;

    // for the use of tests
    public PartitionContext(long memberExpiryTime) {

        this.activeMembers = new ArrayList<MemberContext>();
        this.terminationPendingMembers = new ArrayList<MemberContext>();
        expiryTime = memberExpiryTime;
    }
    
    public PartitionContext(Partition partition) {
        this.setPartition(partition);
        this.minimumMemberCount = partition.getPartitionMin();
        this.partitionId = partition.getId();
        this.pendingMembers = new ArrayList<MemberContext>();
        this.activeMembers = new ArrayList<MemberContext>();
        this.terminationPendingMembers = new ArrayList<MemberContext>();
        this.obsoletedMembers = new ConcurrentHashMap<String, MemberContext>();
//        this.faultyMembers = new CopyOnWriteArrayList<String>();
        memberStatsContexts = new ConcurrentHashMap<String, MemberStatsContext>();

        // check if a different value has been set for expiryTime
        XMLConfiguration conf = ConfUtil.getInstance(null).getConfiguration();
        expiryTime = conf.getLong("autoscaler.member.expiryTimeout", 900000);
        if (log.isDebugEnabled()) {
            log.debug("Member expiry time is set to: " + expiryTime);
        }

        Thread th = new Thread(new PendingMemberWatcher(this));
        th.start();
        Thread th2 = new Thread(new ObsoletedMemberWatcher(this));
        th2.start();
    }
    
    public List<MemberContext> getPendingMembers() {
        return pendingMembers;
    }
    
    public void setPendingMembers(List<MemberContext> pendingMembers) {
        this.pendingMembers = pendingMembers;
    }
    
    public int getActiveMemberCount() {
        return activeMembers.size();
    }
    
    public void setActiveMembers(List<MemberContext> activeMembers) {
        this.activeMembers = activeMembers;
    }
    
    public String getPartitionId() {
        return partitionId;
    }
    public void setPartitionId(String partitionId) {
        this.partitionId = partitionId;
    }
//    public int getTotalMemberCount() {
//        // live count + pending count
//        return currentActiveMemberCount + pendingMembers.size();
//    }

//    public void incrementCurrentActiveMemberCount(int count) {
//
//        this.currentActiveMemberCount += count;
//    }
    
//    public void decrementCurrentActiveMemberCount(int count) {
//        this.currentActiveMemberCount -= count;
//    }

    public int getMinimumMemberCount() {
        return minimumMemberCount;
    }

    public void setMinimumMemberCount(int minimumMemberCount) {
        this.minimumMemberCount = minimumMemberCount;
    }

    public Partition getPartition() {
        return partition;
    }

    public void setPartition(Partition partition) {
        this.partition = partition;
    }
    
    public void addPendingMember(MemberContext ctxt) {
        this.pendingMembers.add(ctxt);
    }
    
    public boolean removePendingMember(String id) {
    	if (id == null) {
            return false;
        }
        for (Iterator<MemberContext> iterator = pendingMembers.iterator(); iterator.hasNext();) {
    		MemberContext pendingMember = (MemberContext) iterator.next();
    		if(id.equals(pendingMember.getMemberId())){
    			iterator.remove();
    			return true;
    		}
			
		}
    	
    	return false;
    }
    
    public void movePendingMemberToActiveMembers(String memberId) {
        if (memberId == null) {
            return;
        }
        Iterator<MemberContext> iterator = pendingMembers.listIterator();
        while (iterator.hasNext()) {
            MemberContext pendingMember = iterator.next();
            if(pendingMember == null) {
                iterator.remove();
                continue;
            }
            if(memberId.equals(pendingMember.getMemberId())){
                // member is activated
                // remove from pending list
                iterator.remove();
                // add to the activated list
                this.activeMembers.add(pendingMember);
                pendingMembersFailureCount = 0;
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Pending member is removed and added to the " +
                            "activated member list. [Member Id] %s",memberId));
                }
                break;
            }
        }
    }


    public void moveActiveMemberToTerminationPendingMembers(String memberId) {
        if (memberId == null) {
            return;
        }
        Iterator<MemberContext> iterator = activeMembers.listIterator();
        while ( iterator.hasNext()) {
            MemberContext activeMember = iterator.next();
            if(activeMember == null) {
                iterator.remove();
                continue;
            }
            if(memberId.equals(activeMember.getMemberId())){
                // member is activated
                // remove from pending list
                iterator.remove();
                // add to the activated list
                this.terminationPendingMembers.add(activeMember);
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Active member is removed and added to the " +
                            "termination pending member list. [Member Id] %s", memberId));
                }
                break;
            }
        }
    }
    
    public void addActiveMember(MemberContext ctxt) {
        this.activeMembers.add(ctxt);
    }
    
    public void removeActiveMember(MemberContext ctxt) {
        this.activeMembers.remove(ctxt);
    }

    public boolean removeTerminationPendingMember(String memberId) {
        boolean terminationPendingMemberAvailable = false;
        for (MemberContext memberContext: terminationPendingMembers){
            if(memberContext.getMemberId().equals(memberId)){
                terminationPendingMemberAvailable = true;
                terminationPendingMembers.remove(memberContext);
                break;
            }
        }
        return terminationPendingMemberAvailable;
    }
    
    public long getObsoltedMemberExpiryTime() {
    	return obsoltedMemberExpiryTime;
    }
    
    public void setObsoltedMemberExpiryTime(long obsoltedMemberExpiryTime) {
    	this.obsoltedMemberExpiryTime = obsoltedMemberExpiryTime;
    }
    
    public void addObsoleteMember(MemberContext ctxt) {
    	this.obsoletedMembers.put(ctxt.getMemberId(), ctxt);
    }
    
    public boolean removeObsoleteMember(String memberId) {
    	if(this.obsoletedMembers.remove(memberId) == null) {
    		return false;
    	}
    	return true;
    }
//
//    public void addFaultyMember(String memberId) {
//        this.faultyMembers.add(memberId);
//    }
//
//    public boolean removeFaultyMember(String memberId) {
//        return this.faultyMembers.remove(memberId);
//    }
//
//    public List<String> getFaultyMembers() {
//        return this.faultyMembers;
//    }

    public long getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(long expiryTime) {
        this.expiryTime = expiryTime;
    }
    
    public Map<String, MemberContext> getObsoletedMembers() {
        return obsoletedMembers;
    }
        
    public void setObsoletedMembers(Map<String, MemberContext> obsoletedMembers) {
        this.obsoletedMembers = obsoletedMembers;
    }

    public String getNetworkPartitionId() {
        return networkPartitionId;
    }

    public void setNetworkPartitionId(String networkPartitionId) {
        this.networkPartitionId = networkPartitionId;
    }

    
    public Map<String, MemberStatsContext> getMemberStatsContexts() {
        return memberStatsContexts;
    }

    public MemberStatsContext getMemberStatsContext(String memberId) {
        return memberStatsContexts.get(memberId);
    }

    public void addMemberStatsContext(MemberStatsContext ctxt) {
        this.memberStatsContexts.put(ctxt.getMemberId(), ctxt);
    }

    public void removeMemberStatsContext(String memberId) {
        this.memberStatsContexts.remove(memberId);
    }

    public MemberStatsContext getPartitionCtxt(String id) {
        return this.memberStatsContexts.get(id);
    }

//    public boolean memberExist(String memberId) {
//        return memberStatsContexts.containsKey(memberId);
//    }


    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public List<MemberContext> getTerminationPendingMembers() {
        return terminationPendingMembers;
    }

    public void setTerminationPendingMembers(List<MemberContext> terminationPendingMembers) {
        this.terminationPendingMembers = terminationPendingMembers;
    }

    public int getTotalMemberCount() {

        return activeMembers.size() + pendingMembers.size() + terminationPendingMembers.size();
    }

    public int getNonTerminatedMemberCount() {
        return activeMembers.size() + pendingMembers.size() + terminationPendingMembers.size();
    }
    
    public List<MemberContext> getActiveMembers() {
		return activeMembers;
	}

	public boolean removeActiveMemberById(String memberId) {
        boolean removeActiveMember = false;
        synchronized (activeMembers) {
            Iterator<MemberContext> iterator = activeMembers.listIterator();
            while (iterator.hasNext()) {
                MemberContext memberContext = iterator.next();
                if(memberId.equals(memberContext.getMemberId())){
                    iterator.remove();
                    removeActiveMember = true;

                    break;
                }
            }
        }
        return removeActiveMember;
    }

    public boolean activeMemberExist(String memberId) {

        for (MemberContext memberContext: activeMembers) {
            if(memberId.equals(memberContext.getMemberId())){
                return true;
            }
        }
        return false;
    }

    private class PendingMemberWatcher implements Runnable {
        private PartitionContext ctxt;

        public PendingMemberWatcher(PartitionContext ctxt) {
            this.ctxt = ctxt;
        }

        @Override
        public void run() {

            while (true) {
                long expiryTime = ctxt.getExpiryTime();
                List<MemberContext> pendingMembers = ctxt.getPendingMembers();
                
                synchronized (pendingMembers) {
                    Iterator<MemberContext> iterator = pendingMembers.listIterator();
                    while ( iterator.hasNext()) {
                        MemberContext pendingMember = iterator.next();

                        if (pendingMember == null) {
                            continue;
                        }
                        long pendingTime = System.currentTimeMillis() - pendingMember.getInitTime();
                        if (pendingTime >= expiryTime) {


                            iterator.remove();
                            log.info("Pending state of member: " + pendingMember.getMemberId() +
                                     " is expired. " + "Adding as an obsoleted member.");
                            // member should be terminated
                            ctxt.addObsoleteMember(pendingMember);
                            pendingMembersFailureCount++;
                            if( pendingMembersFailureCount > PENDING_MEMBER_FAILURE_THRESHOLD){
                                setExpiryTime(expiryTime * 2);//Doubles the expiry time after the threshold of failure exceeded
                                //TODO Implement an alerting system: STRATOS-369
                            }
                        }
                    }
                }

                try {
                    // TODO find a constant
                    Thread.sleep(15000);
                } catch (InterruptedException ignore) {
                }
            }
        }

    } 
    
    private class ObsoletedMemberWatcher implements Runnable {
    private PartitionContext ctxt;
    
    public ObsoletedMemberWatcher(PartitionContext ctxt) {
    	this.ctxt = ctxt;
    }
    
    @Override
    public void run() {
    	while (true) {
    		long obsoltedMemberExpiryTime = ctxt.getObsoltedMemberExpiryTime();
    		Map<String, MemberContext> obsoletedMembers = ctxt.getObsoletedMembers();
    		Iterator<Entry<String, MemberContext>> iterator = obsoletedMembers.entrySet().iterator();

    		while (iterator.hasNext()) {
    			Map.Entry<String, MemberContext> pairs = iterator.next();
    			MemberContext obsoleteMember = (MemberContext) pairs.getValue();
    			if (obsoleteMember == null){
    				continue;
    			}
    			
    			long obsoleteTime = System.currentTimeMillis() - obsoleteMember.getInitTime();
    			if (obsoleteTime >= obsoltedMemberExpiryTime) {
    				iterator.remove();
    			}
    		}
    		try {
    			// TODO find a constant
    			Thread.sleep(15000);
    		} catch (InterruptedException ignore) {
    		}
    	}
    }
 }
}
