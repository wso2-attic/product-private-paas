/*
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.ppaas;

import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.*;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.axis2.description.Parameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.stratos.messaging.message.receiver.topology.TopologyEventReceiver;
import org.apache.stratos.messaging.message.receiver.topology.TopologyManager;
import org.wso2.carbon.core.clustering.hazelcast.HazelcastCarbonClusterImpl;
import org.wso2.carbon.core.clustering.hazelcast.HazelcastMembershipScheme;
import org.wso2.carbon.core.clustering.hazelcast.HazelcastUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Private PaaS based membership scheme for automating the cluster configuration
 * via the messaging model.
 */
public class PrivatePaaSBasedMembershipScheme implements HazelcastMembershipScheme {

    private static final Log log = LogFactory.getLog(PrivatePaaSBasedMembershipScheme.class);
    private static final String PARAMETER_NAME_CLUSTER_IDS = "clusterIds";

    private final Map<String, Parameter> parameters;
    private final NetworkConfig nwConfig;
    private HazelcastInstance primaryHazelcastInstance;
    private HazelcastCarbonClusterImpl carbonCluster;
    private final List<ClusteringMessage> messageBuffer;
    private boolean shuttingDown;

    public PrivatePaaSBasedMembershipScheme(Map<String, Parameter> parameters,
                                            String primaryDomain,
                                            Config config,
                                            HazelcastInstance primaryHazelcastInstance,
                                            List<ClusteringMessage> messageBuffer) {
        this.parameters = parameters;
        this.primaryHazelcastInstance = primaryHazelcastInstance;
        this.messageBuffer = messageBuffer;
        this.nwConfig = config.getNetworkConfig();
    }

    @Override
    public void setPrimaryHazelcastInstance(HazelcastInstance primaryHazelcastInstance) {
        this.primaryHazelcastInstance = primaryHazelcastInstance;
    }

    @Override
    public void setLocalMember(Member localMember) {
    }

    @Override
    public void setCarbonCluster(HazelcastCarbonClusterImpl hazelcastCarbonCluster) {
        this.carbonCluster = hazelcastCarbonCluster;
    }

    @Override
    public void init() throws ClusteringFault {
        try {
            nwConfig.getJoin().getMulticastConfig().setEnabled(false);
            nwConfig.getJoin().getAwsConfig().setEnabled(false);
            TcpIpConfig tcpIpConfig = nwConfig.getJoin().getTcpIpConfig();
            tcpIpConfig.setEnabled(true);

            Parameter clusterIdsParameter = getParameter(PARAMETER_NAME_CLUSTER_IDS);
            if(clusterIdsParameter == null) {
                throw new RuntimeException(PARAMETER_NAME_CLUSTER_IDS + " parameter not found");
            }
            String clusterIds = (String)clusterIdsParameter.getValue();
            String[] clusterIdArray = clusterIds.split(",");

            if (!waitForTopologyInitialization()) return;

            try {
                TopologyManager.acquireReadLock();
                for (String clusterId : clusterIdArray) {
                    org.apache.stratos.messaging.domain.topology.Cluster cluster =
                            TopologyManager.getTopology().getCluster(clusterId);
                    if (cluster == null) {
                        throw new RuntimeException("Cluster not found in topology: [cluster-id]" + clusterId);
                    }

                    log.info("Reading members of cluster: [cluster-id] " + clusterId);
                    for (org.apache.stratos.messaging.domain.topology.Member member : cluster.getMembers()) {
                        tcpIpConfig.addMember(member.getDefaultPrivateIP());
                        log.info("Member added to cluster configuration: [member-ip] " + member.getDefaultPrivateIP());
                    }
                }
            } finally {
                TopologyManager.releaseReadLock();
            }
        } catch (Throwable t) {
            log.error("Could not initialize stratos membership scheme", t);
        }
    }

    /**
     * Subscribe to topology topic and wait until local topology instance get initialized.
     * @return
     */
    private boolean waitForTopologyInitialization() {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        TopologyEventReceiver topologyEventReceiver = new TopologyEventReceiver();
        topologyEventReceiver.setExecutorService(executorService);
        topologyEventReceiver.execute();
        if (log.isInfoEnabled()) {
            log.info("Topology receiver thread started");
        }

        final Thread currentThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                shuttingDown = true;
                try {
                    currentThread.join();
                } catch (InterruptedException ignore) {
                }
            }
        });

        log.info("Waiting for topology to be initialized...");
        while (!TopologyManager.getTopology().isInitialized()) {
            try {
                if(shuttingDown) {
                    return false;
                }
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {
                return false;
            }
        }
        log.info("Topology initialized");
        return true;
    }

    @Override
    public void joinGroup() throws ClusteringFault {
        primaryHazelcastInstance.getCluster().addMembershipListener(new StratosMembershipListener());
    }

    public Parameter getParameter(String name) {
        return parameters.get(name);
    }

    /**
     * Stratos membership listener.
     */
    private class StratosMembershipListener implements MembershipListener {

        @Override
        public void memberAdded(MembershipEvent membershipEvent) {
            Member member = membershipEvent.getMember();

            // send all cluster messages
            carbonCluster.memberAdded(member);
            log.info("Member joined [" + member.getUuid() + "]: " + member.getInetSocketAddress().toString());
            // Wait for sometime for the member to completely join before replaying messages
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) {
            }
            HazelcastUtil.sendMessagesToMember(messageBuffer, member, carbonCluster);
        }

        @Override
        public void memberRemoved(MembershipEvent membershipEvent) {
            Member member = membershipEvent.getMember();
            carbonCluster.memberRemoved(member);
            log.info("Member left [" + member.getUuid() + "]: " + member.getInetSocketAddress().toString());
        }

        @Override
        public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
            if(log.isDebugEnabled()) {
                log.debug("Member attribute changed: [" + memberAttributeEvent.getKey() + "] " +
                        memberAttributeEvent.getValue());
            }
        }
    }
}