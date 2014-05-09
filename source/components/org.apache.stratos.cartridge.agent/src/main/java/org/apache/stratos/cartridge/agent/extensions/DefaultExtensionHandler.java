/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.stratos.cartridge.agent.extensions;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.stratos.cartridge.agent.artifact.deployment.synchronizer.RepositoryInformation;
import org.apache.stratos.cartridge.agent.artifact.deployment.synchronizer.git.impl.GitBasedArtifactRepository;
import org.apache.stratos.cartridge.agent.config.CartridgeAgentConfiguration;
import org.apache.stratos.cartridge.agent.event.publisher.CartridgeAgentEventPublisher;
import org.apache.stratos.cartridge.agent.util.CartridgeAgentConstants;
import org.apache.stratos.cartridge.agent.util.CartridgeAgentUtils;
import org.apache.stratos.cartridge.agent.util.ExtensionUtils;
import org.apache.stratos.messaging.domain.tenant.Tenant;
import org.apache.stratos.messaging.domain.topology.*;
import org.apache.stratos.messaging.event.instance.notifier.ArtifactUpdatedEvent;
import org.apache.stratos.messaging.event.instance.notifier.InstanceCleanupClusterEvent;
import org.apache.stratos.messaging.event.instance.notifier.InstanceCleanupMemberEvent;
import org.apache.stratos.messaging.event.tenant.CompleteTenantEvent;
import org.apache.stratos.messaging.event.tenant.SubscriptionDomainAddedEvent;
import org.apache.stratos.messaging.event.tenant.SubscriptionDomainRemovedEvent;
import org.apache.stratos.messaging.event.topology.CompleteTopologyEvent;
import org.apache.stratos.messaging.event.topology.MemberActivatedEvent;
import org.apache.stratos.messaging.event.topology.MemberSuspendedEvent;
import org.apache.stratos.messaging.event.topology.MemberTerminatedEvent;
import org.apache.stratos.messaging.message.receiver.tenant.TenantManager;
import org.apache.stratos.messaging.message.receiver.topology.TopologyManager;

import java.lang.reflect.Type;
import java.util.*;

public class DefaultExtensionHandler implements ExtensionHandler {

    private static final Log log = LogFactory.getLog(DefaultExtensionHandler.class);
    private static final Gson gson = new Gson();
    private static final Type memberType = new TypeToken<Collection<Member>>() {
    }.getType();
    private static final Type tenantType = new TypeToken<Collection<Tenant>>() {
    }.getType();
    private static final Type serviceType = new TypeToken<Collection<Service>>() {
    }.getType();

    @Override
    public void onInstanceStartedEvent() {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Processing instance started event...");
            }
            Map<String, String> env = new HashMap<String, String>();
            env.put("STRATOS_PARAM_FILE_PATH", System.getProperty(CartridgeAgentConstants.PARAM_FILE_PATH));
            ExtensionUtils.executeInstanceStartedExtension(env);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Error processing instance started event", e);
            }
        } finally {

        }
    }

    @Override
    public void onInstanceActivatedEvent() {
        ExtensionUtils.executeInstanceActivatedExtension();
    }

    @Override
    public void onArtifactUpdateEvent(ArtifactUpdatedEvent event) {
        ArtifactUpdatedEvent artifactUpdatedEvent = event;
        if (log.isInfoEnabled()) {
            log.info(String.format("Artifact update event received: %s", artifactUpdatedEvent.toString()));
        }
        String clusterIdInPayload = CartridgeAgentConfiguration.getInstance().getClusterId();
        String localRepoPath = CartridgeAgentConfiguration.getInstance().getAppPath();
        String clusterIdInMessage = artifactUpdatedEvent.getClusterId();
        String repoURL = artifactUpdatedEvent.getRepoURL();
        String repoPassword = CartridgeAgentUtils.decryptPassword(artifactUpdatedEvent.getRepoPassword());
        String repoUsername = artifactUpdatedEvent.getRepoUserName();
        String tenantId = artifactUpdatedEvent.getTenantId();
        boolean isMultitenant = CartridgeAgentConfiguration.getInstance().isMultitenant();

        if (StringUtils.isNotEmpty(repoURL) && (clusterIdInPayload != null) &&
                clusterIdInPayload.equals(clusterIdInMessage)) {
            if (log.isInfoEnabled()) {
                log.info("Executing git checkout");
            }
            RepositoryInformation repoInformation = new RepositoryInformation();
            repoInformation.setRepoUsername(repoUsername);
            if (repoPassword == null) {
                repoInformation.setRepoPassword("");
            } else {
                repoInformation.setRepoPassword(repoPassword);
            }
            repoInformation.setRepoUrl(repoURL);
            repoInformation.setRepoPath(localRepoPath);
            repoInformation.setTenantId(tenantId);
            repoInformation.setMultitenant(isMultitenant);
            boolean cloneExists = GitBasedArtifactRepository.getInstance().cloneExists(repoInformation);
            GitBasedArtifactRepository.getInstance().checkout(repoInformation);
            Map<String, String> env = new HashMap<String, String>();
            env.put("STRATOS_CLUSTER_ID", artifactUpdatedEvent.getClusterId());
            env.put("STRATOS_TENANT_ID", artifactUpdatedEvent.getTenantId());
            env.put("STRATOS_REPO_URL", artifactUpdatedEvent.getRepoURL());
            env.put("STRATOS_REPO_PASSWORD", artifactUpdatedEvent.getRepoPassword());
            env.put("STRATOS_REPO_USERNAME", artifactUpdatedEvent.getRepoUserName());
            env.put("STRATOS_STATUS", artifactUpdatedEvent.getStatus());
            ExtensionUtils.executeArtifactsUpdatedExtension(env);

            if (!cloneExists) {
                // Executed git clone, publish instance activated event
                CartridgeAgentEventPublisher.publishInstanceActivatedEvent();
            }

            // Start the artifact update task
            boolean artifactUpdateEnabled = Boolean.parseBoolean(System.getProperty(CartridgeAgentConstants.ENABLE_ARTIFACT_UPDATE));
            if (artifactUpdateEnabled) {

                long artifactUpdateInterval = 10;
                // get update interval
                String artifactUpdateIntervalStr = System.getProperty(CartridgeAgentConstants.ARTIFACT_UPDATE_INTERVAL);

                if (artifactUpdateIntervalStr != null && !artifactUpdateIntervalStr.isEmpty()) {
                    try {
                        artifactUpdateInterval = Long.parseLong(artifactUpdateIntervalStr);

                    } catch (NumberFormatException e) {
                        log.error("Invalid artifact sync interval specified ", e);
                        artifactUpdateInterval = 10;
                    }
                }

                log.info("Artifact updating task enabled, update interval: " + artifactUpdateInterval + "s");
                GitBasedArtifactRepository.getInstance().scheduleSyncTask(repoInformation, artifactUpdateInterval);

            } else {
                log.info("Artifact updating task disabled");
            }

        }
    }

    @Override
    public void onArtifactUpdateEvent(String tenantId){
        Map<String, String> env = new HashMap<String, String>();
        env.put("STRATOS_TENANT_ID", tenantId);
        ExtensionUtils.executeArtifactsUpdatedExtension(env);
    }

    @Override
    public void onInstanceCleanupClusterEvent(InstanceCleanupClusterEvent instanceCleanupClusterEvent) {
        cleanup();
    }

    @Override
    public void onInstanceCleanupMemberEvent(InstanceCleanupMemberEvent instanceCleanupMemberEvent) {
        cleanup();
    }

    private void cleanup() {
        if (log.isInfoEnabled()) {
            log.info("Executing cleaning up the data in the cartridge instance...");
        }
        //sending event on the maintenance mode
        CartridgeAgentEventPublisher.publishMaintenanceModeEvent();

        //cleaning up the cartridge instance's data
        ExtensionUtils.executeCleanupExtension();
        if (log.isInfoEnabled()) {
            log.info("cleaning up finished in the cartridge instance...");
        }
        if (log.isInfoEnabled()) {
            log.info("publishing ready to shutdown event...");
        }
        //publishing the Ready to shutdown event after performing the cleanup
        CartridgeAgentEventPublisher.publishInstanceReadyToShutdownEvent();
    }

    private boolean isRelevantMemberEvent(String serviceName, String clusterId, String memberId, String lbClusterId) {
        String clusterIdInPayload = CartridgeAgentConfiguration.getInstance().getClusterId();
        if (clusterIdInPayload == null) {
            return false;
        }
        Topology topology = TopologyManager.getTopology();
        if (topology == null || !topology.isInitialized()) {
            return false;
        }

        if (clusterIdInPayload.equals(clusterId)) {
            return true;
        }

        if (clusterIdInPayload.equals(lbClusterId)) {
            return true;
        }

        String serviceGroupInPayload = CartridgeAgentConfiguration.getInstance().getServiceGroup();
        if (serviceGroupInPayload != null) {
            Properties serviceProperties = topology.getService(serviceName).getProperties();
            if (serviceProperties == null) {
                return false;
            }
            String memberServiceGroup = serviceProperties.getProperty(CartridgeAgentConstants.SERVICE_GROUP_TOPOLOGY_KEY);
            if (memberServiceGroup != null && memberServiceGroup.equals(serviceGroupInPayload)) {
                return true;
            }

        }

        return false;
    }

    @Override
    public void onMemberActivatedEvent(MemberActivatedEvent memberActivatedEvent) {
        if (log.isInfoEnabled()) {
            log.info(String.format("Member activated event received: [service] %s [cluster] %s",
                    memberActivatedEvent.getServiceName(), memberActivatedEvent.getClusterId()));
        }

        if (log.isDebugEnabled()) {
            String msg = gson.toJson(memberActivatedEvent);
            log.debug("Member activated event msg:" + msg);
        }

        Topology topology = TopologyManager.getTopology();
        Service service = topology.getService(memberActivatedEvent.getServiceName());
        if (service == null) {
            if (log.isErrorEnabled()) {
                log.error(String.format("Member activated event service not found in topology..." +
                        "failed to execute member activated event [service] %s", memberActivatedEvent.getServiceName()));
            }
            return;
        }
        Cluster cluster = service.getCluster(memberActivatedEvent.getClusterId());
        if (cluster == null) {
            if (log.isErrorEnabled()) {
                log.error(String.format("Member activated event cluster id not found in topology..." +
                        "failed to execute member activated event [service] %s", memberActivatedEvent.getClusterId()));
            }
            return;
        }
        Member activatedMember = cluster.getMember(memberActivatedEvent.getMemberId());
        if (activatedMember == null) {
            if (log.isErrorEnabled()) {
                log.error(String.format("Member activated event member id not found in topology..." +
                        "failed to execute member activated event [member] %s", memberActivatedEvent.getMemberId()));
            }
            return;
        }
        String lbClusterId = cluster.getMember(memberActivatedEvent.getMemberId()).getLbClusterId();

        // check whether member activated event is received from the same cluster, lbcluster or service group
        if (isRelevantMemberEvent(memberActivatedEvent.getServiceName(), memberActivatedEvent.getClusterId(),
                memberActivatedEvent.getMemberId(), lbClusterId)) {
            Map<String, String> env = new HashMap<String, String>();
            env.put("STRATOS_MEMBER_IP", memberActivatedEvent.getMemberIp());
            env.put("STRATOS_MEMBER_ID", memberActivatedEvent.getMemberId());
            env.put("STRATOS_CLUSTER_ID", memberActivatedEvent.getClusterId());
            env.put("STRATOS_LB_CLUSTER_ID", lbClusterId);
            env.put("STRATOS_NETWORK_PARTITION_ID", memberActivatedEvent.getNetworkPartitionId());
            env.put("STRATOS_SERVICE_NAME", memberActivatedEvent.getServiceName());
            env.put("STRATOS_PARAM_FILE_PATH", System.getProperty(CartridgeAgentConstants.PARAM_FILE_PATH));
            Collection<Port> ports = memberActivatedEvent.getPorts();
            String ports_str = "";
            for (Port port : ports) {
                ports_str += port.getProtocol() + "," + port.getValue() + "," + port.getProxy() + "|";
            }
            env.put("STRATOS_PORTS", ports_str);

            Collection<Member> members = cluster.getMembers();
            env.put("STRATOS_MEMBER_LIST_JSON", gson.toJson(members, memberType));
            String[] memberIps = getLbMemberIp(lbClusterId);
            if (memberIps != null && memberIps.length > 1) {
                env.put("STRATOS_LB_IP", memberIps[0]);
                env.put("STRATOS_LB_PUBLIC_IP", memberIps[1]);
            }
            env.put("STRATOS_TOPOLOGY_JSON", gson.toJson(topology.getServices(), serviceType));

            Properties serviceProperties = service.getProperties();
            if (serviceProperties != null) {
                for (Map.Entry<Object, Object> entry : serviceProperties.entrySet()) {
                    env.put("STRATOS_SERVICE_PROPERTY_" + entry.getKey().toString(), entry.getValue().toString());
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Service property added: [key] %s [value] %s",
                                entry.getKey().toString(), entry.getValue().toString()));
                    }
                }
            }

            Properties clusterProperties = cluster.getProperties();
            if (clusterProperties != null) {
                for (Map.Entry<Object, Object> entry : clusterProperties.entrySet()) {
                    env.put("STRATOS_CLUSTER_PROPERTY_" + entry.getKey().toString(), entry.getValue().toString());
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Cluster property added: [key] %s [value] %s",
                                entry.getKey().toString(), entry.getValue().toString()));
                    }
                }
            }

            Member member = cluster.getMember(memberActivatedEvent.getMemberId());
            if (member != null && member.getProperties() != null) {
                for (Map.Entry<Object, Object> entry : member.getProperties().entrySet()) {
                    env.put("STRATOS_MEMBER_PROPERTY_" + entry.getKey().toString(), entry.getValue().toString());
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Member property added: [key] %s [value] %s",
                                entry.getKey().toString(), entry.getValue().toString()));
                    }
                }
            }
            ExtensionUtils.executeMemberActivatedExtension(env);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Member activated event is not relevant...skipping agent extension");
            }
        }
    }

    private String[] getLbMemberIp(String lbClusterId) {
        Topology topology = TopologyManager.getTopology();
        Collection<Service> serviceCollection = topology.getServices();

        for (Service service : serviceCollection) {
            Collection<Cluster> clusterCollection = service.getClusters();
            for (Cluster cluster : clusterCollection) {
                Collection<Member> memberCollection = cluster.getMembers();
                for (Member member : memberCollection) {
                    if (member.getClusterId().equals(lbClusterId)) {
                        return new String[]{member.getMemberIp(), member.getMemberPublicIp()};
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void onCompleteTopologyEvent(CompleteTopologyEvent completeTopologyEvent) {
        if (log.isDebugEnabled()) {
            log.debug("Complete topology event received");

        }
        String serviceNameInPayload = CartridgeAgentConfiguration.getInstance().getServiceName();
        String clusterIdInPayload = CartridgeAgentConfiguration.getInstance().getClusterId();
        String lbClusterIdInPayload = CartridgeAgentConfiguration.getInstance().getLbClusterId();
        Topology topology = completeTopologyEvent.getTopology();
        Service service = topology.getService(serviceNameInPayload);
        if (service == null) {
            if (log.isErrorEnabled()) {
                log.error(String.format("Member activated event service not found in topology..." +
                        "failed to execute member activated event [service] %s", serviceNameInPayload));
            }
            return;
        }
        Cluster cluster = service.getCluster(clusterIdInPayload);
        if (cluster == null) {
            if (log.isErrorEnabled()) {
                log.error(String.format("Member activated event cluster id not found in topology..." +
                        "failed to execute member activated event [service] %s", clusterIdInPayload));
            }
            return;
        }
        Map<String, String> env = new HashMap<String, String>();
        Collection<Member> membersInLb = new ArrayList<Member>();
        // if this instance is a LB, get all the members that belong to this LB cluster
        if (cluster.isLbCluster()) {
            Collection<Service> serviceCollection = topology.getServices();
            for (Service s : serviceCollection) {
                Collection<Cluster> clusterCollection = s.getClusters();
                for (Cluster c : clusterCollection) {
                    Collection<Member> memberCollection = c.getMembers();
                    for (Member member : memberCollection) {
                        if (member.getLbClusterId() != null && member.getLbClusterId().equals(clusterIdInPayload)) {
                            membersInLb.add(member);
                        }
                    }
                }
            }
            env.put("STRATOS_MEMBERS_IN_LB_JSON", gson.toJson(membersInLb, memberType));
        }
        String[] memberIps = getLbMemberIp(lbClusterIdInPayload);
        if (memberIps != null && memberIps.length > 1) {
            env.put("STRATOS_LB_IP", memberIps[0]);
            env.put("STRATOS_LB_PUBLIC_IP", memberIps[1]);
        }
        env.put("STRATOS_TOPOLOGY_JSON", gson.toJson(topology.getServices(), serviceType));
        env.put("STRATOS_MEMBER_LIST_JSON", gson.toJson(cluster.getMembers(), memberType));
        env.put("STRATOS_PARAM_FILE_PATH", System.getProperty(CartridgeAgentConstants.PARAM_FILE_PATH));

        Properties serviceProperties = service.getProperties();
        if (serviceProperties != null) {
            for (Map.Entry<Object, Object> entry : serviceProperties.entrySet()) {
                env.put("STRATOS_SERVICE_PROPERTY_" + entry.getKey().toString(), entry.getValue().toString());
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Service property added: [key] %s [value] %s",
                            entry.getKey().toString(), entry.getValue().toString()));
                }
            }
        }

        Properties clusterProperties = cluster.getProperties();
        if (clusterProperties != null) {
            for (Map.Entry<Object, Object> entry : clusterProperties.entrySet()) {
                env.put("STRATOS_CLUSTER_PROPERTY_" + entry.getKey().toString(), entry.getValue().toString());
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Cluster property added: [key] %s [value] %s",
                            entry.getKey().toString(), entry.getValue().toString()));
                }
            }
        }
        ExtensionUtils.executeCompleteTopologyExtension(env);
    }

    @Override
    public void onCompleteTenantEvent(CompleteTenantEvent completeTenantEvent) {
        if (log.isDebugEnabled()) {
            log.debug("Complete tenant event received");
        }
        String tenantListJson = gson.toJson(completeTenantEvent.getTenants(), tenantType);
        if (log.isDebugEnabled()) {
            log.debug("Complete tenants:" + tenantListJson);
        }
        Map<String, String> env = new HashMap<String, String>();
        String lbClusterIdInPayload = CartridgeAgentConfiguration.getInstance().getLbClusterId();
        String[] memberIps = getLbMemberIp(lbClusterIdInPayload);
        if (memberIps != null && memberIps.length > 1) {
            env.put("STRATOS_LB_IP", memberIps[0]);
            env.put("STRATOS_LB_PUBLIC_IP", memberIps[1]);
        }
        env.put("STRATOS_TENANT_LIST_JSON", tenantListJson);
        env.put("STRATOS_PARAM_FILE_PATH", System.getProperty(CartridgeAgentConstants.PARAM_FILE_PATH));
        ExtensionUtils.executeCompleteTenantExtension(env);
    }

    @Override
    public void onMemberTerminatedEvent(MemberTerminatedEvent memberTerminatedEvent) {
        if (log.isInfoEnabled()) {
            log.info(String.format("Member terminated event received: [service] %s [cluster] %s",
                    memberTerminatedEvent.getServiceName(), memberTerminatedEvent.getClusterId()));
        }

        if (log.isDebugEnabled()) {
            String msg = gson.toJson(memberTerminatedEvent);
            log.debug("Member terminated event msg:" + msg);
        }

        Topology topology = TopologyManager.getTopology();
        Service service = topology.getService(memberTerminatedEvent.getServiceName());
        if (service == null) {
            if (log.isErrorEnabled()) {
                log.error(String.format("Member terminated event service not found in topology..." +
                        "failed to execute member terminated event [service] %s", memberTerminatedEvent.getServiceName()));
            }
            return;
        }
        Cluster cluster = service.getCluster(memberTerminatedEvent.getClusterId());
        if (cluster == null) {
            if (log.isErrorEnabled()) {
                log.error(String.format("Member terminated event cluster id not found in topology..." +
                        "failed to execute member terminated event [service] %s", memberTerminatedEvent.getClusterId()));
            }
            return;
        }
        Member terminatedMember = cluster.getMember(memberTerminatedEvent.getClusterId());
        if (terminatedMember == null) {
            if (log.isErrorEnabled()) {
                log.error(String.format("Member terminated event member id not found in topology..." +
                        "failed to execute member terminated event [member] %s", memberTerminatedEvent.getMemberId()));
            }
            return;
        }
        String lbClusterId = cluster.getMember(memberTerminatedEvent.getClusterId()).getLbClusterId();

        // check whether terminated member is within the same cluster, LB cluster or service group
        if (isRelevantMemberEvent(memberTerminatedEvent.getServiceName(), memberTerminatedEvent.getClusterId(),
                memberTerminatedEvent.getMemberId(), lbClusterId)) {

            Collection<Member> members = cluster.getMembers();
            Map<String, String> env = new HashMap<String, String>();
            env.put("STRATOS_MEMBER_IP", cluster.getMember(memberTerminatedEvent.getMemberId()).getMemberIp());
            env.put("STRATOS_MEMBER_ID", memberTerminatedEvent.getMemberId());
            env.put("STRATOS_CLUSTER_ID", memberTerminatedEvent.getClusterId());
            env.put("STRATOS_LB_CLUSTER_ID", lbClusterId);
            env.put("STRATOS_NETWORK_PARTITION_ID", memberTerminatedEvent.getNetworkPartitionId());
            env.put("STRATOS_SERVICE_NAME", memberTerminatedEvent.getServiceName());
            env.put("STRATOS_MEMBER_LIST_JSON", gson.toJson(members, memberType));
            env.put("STRATOS_PARAM_FILE_PATH", System.getProperty(CartridgeAgentConstants.PARAM_FILE_PATH));
            String[] memberIps = getLbMemberIp(lbClusterId);
            if (memberIps != null && memberIps.length > 1) {
                env.put("STRATOS_LB_IP", memberIps[0]);
                env.put("STRATOS_LB_PUBLIC_IP", memberIps[1]);
            }
            env.put("STRATOS_TOPOLOGY_JSON", gson.toJson(topology.getServices(), serviceType));

            Properties serviceProperties = service.getProperties();
            if (serviceProperties != null) {
                for (Map.Entry<Object, Object> entry : serviceProperties.entrySet()) {
                    env.put("STRATOS_SERVICE_PROPERTY_" + entry.getKey().toString(), entry.getValue().toString());
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Service property added: [key] %s [value] %s",
                                entry.getKey().toString(), entry.getValue().toString()));
                    }
                }
            }

            Properties clusterProperties = cluster.getProperties();
            if (clusterProperties != null) {
                for (Map.Entry<Object, Object> entry : clusterProperties.entrySet()) {
                    env.put("STRATOS_CLUSTER_PROPERTY_" + entry.getKey().toString(), entry.getValue().toString());
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Cluster property added: [key] %s [value] %s",
                                entry.getKey().toString(), entry.getValue().toString()));
                    }
                }
            }

            Member member = cluster.getMember(memberTerminatedEvent.getMemberId());
            if (member != null && member.getProperties() != null) {
                for (Map.Entry<Object, Object> entry : member.getProperties().entrySet()) {
                    env.put("STRATOS_MEMBER_PROPERTY_" + entry.getKey().toString(), entry.getValue().toString());
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Member property added: [key] %s [value] %s",
                                entry.getKey().toString(), entry.getValue().toString()));
                    }
                }
            }
            ExtensionUtils.executeMemberTerminatedExtension(env);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Member terminated event is not relevant...skipping agent extension");
            }
        }
    }

    @Override
    public void onMemberSuspendedEvent(MemberSuspendedEvent memberSuspendedEvent) {
        if (log.isInfoEnabled()) {
            log.info(String.format("Member suspended event received: [service] %s [cluster] %s",
                    memberSuspendedEvent.getServiceName(), memberSuspendedEvent.getClusterId()));
        }

        if (log.isDebugEnabled()) {
            String msg = gson.toJson(memberSuspendedEvent);
            log.debug("Member suspended event msg:" + msg);
        }

        String clusterId = memberSuspendedEvent.getClusterId();
        Topology topology = TopologyManager.getTopology();
        Service service = topology.getService(memberSuspendedEvent.getServiceName());
        if (service == null) {
            if (log.isErrorEnabled()) {
                log.error(String.format("Member terminated event service not found in topology..." +
                        "failed to execute member terminated event [service] %s", memberSuspendedEvent.getServiceName()));
            }
            return;
        }
        Cluster cluster = service.getCluster(memberSuspendedEvent.getClusterId());
        if (cluster == null) {
            if (log.isErrorEnabled()) {
                log.error(String.format("Member terminated event cluster id not found in topology..." +
                        "failed to execute member terminated event [service] %s", memberSuspendedEvent.getClusterId()));
            }
            return;
        }
        Member terminatedMember = cluster.getMember(memberSuspendedEvent.getClusterId());
        if (terminatedMember == null) {
            if (log.isErrorEnabled()) {
                log.error(String.format("Member terminated event member id not found in topology..." +
                        "failed to execute member terminated event [member] %s", memberSuspendedEvent.getMemberId()));
            }
            return;
        }
        String lbClusterId = cluster.getMember(memberSuspendedEvent.getClusterId()).getLbClusterId();

        // check whether new member is in the same member cluster or LB cluster of this instance
        if (isRelevantMemberEvent(memberSuspendedEvent.getServiceName(), memberSuspendedEvent.getClusterId(),
                memberSuspendedEvent.getMemberId(), lbClusterId)) {
            Collection<Member> members = cluster.getMembers();
            Map<String, String> env = new HashMap<String, String>();
            env.put("STRATOS_MEMBER_IP", cluster.getMember(clusterId).getMemberIp());
            env.put("STRATOS_MEMBER_ID", memberSuspendedEvent.getMemberId());
            env.put("STRATOS_CLUSTER_ID", memberSuspendedEvent.getClusterId());
            env.put("STRATOS_LB_CLUSTER_ID", lbClusterId);
            env.put("STRATOS_NETWORK_PARTITION_ID", memberSuspendedEvent.getNetworkPartitionId());
            env.put("STRATOS_SERVICE_NAME", memberSuspendedEvent.getServiceName());
            env.put("STRATOS_MEMBER_LIST_JSON", gson.toJson(members, memberType));
            String[] memberIps = getLbMemberIp(lbClusterId);
            if (memberIps != null && memberIps.length > 1) {
                env.put("STRATOS_LB_IP", memberIps[0]);
                env.put("STRATOS_LB_PUBLIC_IP", memberIps[1]);
            }
            env.put("STRATOS_PARAM_FILE_PATH", System.getProperty(CartridgeAgentConstants.PARAM_FILE_PATH));
            env.put("STRATOS_TOPOLOGY_JSON", gson.toJson(topology.getServices(), serviceType));

            Properties serviceProperties = service.getProperties();
            if (serviceProperties != null) {
                for (Map.Entry<Object, Object> entry : serviceProperties.entrySet()) {
                    env.put("STRATOS_SERVICE_PROPERTY_" + entry.getKey().toString(), entry.getValue().toString());
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Service property added: [key] %s [value] %s",
                                entry.getKey().toString(), entry.getValue().toString()));
                    }
                }
            }

            Properties clusterProperties = cluster.getProperties();
            if (clusterProperties != null) {
                for (Map.Entry<Object, Object> entry : clusterProperties.entrySet()) {
                    env.put("STRATOS_CLUSTER_PROPERTY_" + entry.getKey().toString(), entry.getValue().toString());
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Cluster property added: [key] %s [value] %s",
                                entry.getKey().toString(), entry.getValue().toString()));
                    }
                }
            }

            Member member = cluster.getMember(memberSuspendedEvent.getMemberId());
            if (member != null && member.getProperties() != null) {
                for (Map.Entry<Object, Object> entry : member.getProperties().entrySet()) {
                    env.put("STRATOS_MEMBER_PROPERTY_" + entry.getKey().toString(), entry.getValue().toString());
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Member property added: [key] %s [value] %s",
                                entry.getKey().toString(), entry.getValue().toString()));
                    }
                }
            }

            ExtensionUtils.executeMemberSuspendedExtension(env);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Member suspended event is not relevant...skipping agent extension");
            }
        }
    }

    @Override
    public void startServerExtension() {
        boolean active = false;
        while (!active) {
            if (log.isInfoEnabled()) {
                log.info("Waiting for complete topology event...");
            }
            active = TopologyManager.getTopology().isInitialized();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
            }
        }
        if (log.isInfoEnabled()) {
            log.info("Complete topology event received");
        }

        String serviceNameInPayload = CartridgeAgentConfiguration.getInstance().getServiceName();
        String clusterIdInPayload = CartridgeAgentConfiguration.getInstance().getClusterId();
        String lbClusterIdInPayload = CartridgeAgentConfiguration.getInstance().getLbClusterId();
        Topology topology = TopologyManager.getTopology();
        Service service = topology.getService(serviceNameInPayload);
        if (service == null) {
            if (log.isErrorEnabled()) {
                log.error(String.format("Member activated event service not found in topology..." +
                        "failed to execute member activated event [service] %s", serviceNameInPayload));
            }
            return;
        }
        Cluster cluster = service.getCluster(clusterIdInPayload);
        if (cluster == null) {
            if (log.isErrorEnabled()) {
                log.error(String.format("Member activated event cluster id not found in topology..." +
                        "failed to execute member activated event [service] %s", clusterIdInPayload));
            }
            return;
        }
        Map<String, String> env = new HashMap<String, String>();
        Collection<Member> membersInLb = new ArrayList<Member>();
        // if this instance is a LB, get all the members that belong to this LB cluster
        if (cluster.isLbCluster()) {
            Collection<Service> serviceCollection = topology.getServices();
            for (Service s : serviceCollection) {
                Collection<Cluster> clusterCollection = s.getClusters();
                for (Cluster c : clusterCollection) {
                    Collection<Member> memberCollection = c.getMembers();
                    for (Member member : memberCollection) {
                        if (member.getLbClusterId() != null && member.getLbClusterId().equals(clusterIdInPayload)) {
                            membersInLb.add(member);
                        }
                    }
                }
            }
            env.put("STRATOS_MEMBERS_IN_LB_JSON", gson.toJson(membersInLb, memberType));
        }
        String[] memberIps = getLbMemberIp(lbClusterIdInPayload);
        if (memberIps != null && memberIps.length > 1) {
            env.put("STRATOS_LB_IP", memberIps[0]);
            env.put("STRATOS_LB_PUBLIC_IP", memberIps[1]);
        }
        env.put("STRATOS_TOPOLOGY_JSON", gson.toJson(topology.getServices(), serviceType));
        env.put("STRATOS_MEMBER_LIST_JSON", gson.toJson(cluster.getMembers(), memberType));
        env.put("STRATOS_PARAM_FILE_PATH", System.getProperty(CartridgeAgentConstants.PARAM_FILE_PATH));

        Properties serviceProperties = service.getProperties();
        if (serviceProperties != null) {
            for (Map.Entry<Object, Object> entry : serviceProperties.entrySet()) {
                env.put("STRATOS_SERVICE_PROPERTY_" + entry.getKey().toString(), entry.getValue().toString());
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Service property added: [key] %s [value] %s",
                            entry.getKey().toString(), entry.getValue().toString()));
                }
            }
        }

        Properties clusterProperties = cluster.getProperties();
        if (clusterProperties != null) {
            for (Map.Entry<Object, Object> entry : clusterProperties.entrySet()) {
                env.put("STRATOS_CLUSTER_PROPERTY_" + entry.getKey().toString(), entry.getValue().toString());
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Cluster property added: [key] %s [value] %s",
                            entry.getKey().toString(), entry.getValue().toString()));
                }
            }
        }
        ExtensionUtils.executeStartServersExtension(env);
    }

    @Override
    public void volumeMountExtension(String persistenceMappingsPayload) {
        ExtensionUtils.executeVolumeMountExtension(persistenceMappingsPayload);
    }

    @Override
    public void onSubscriptionDomainAddedEvent(SubscriptionDomainAddedEvent subscriptionDomainAddedEvent) {
        String tenantDomain = findTenantDomain(subscriptionDomainAddedEvent.getTenantId());
        if (log.isInfoEnabled()) {
            log.info(String.format("Subscription domain added event received: [tenant-id] %d [tenant-domain] %s " +
                            "[domain-name] %s [application-context] %s",
                    subscriptionDomainAddedEvent.getTenantId(),
                    tenantDomain,
                    subscriptionDomainAddedEvent.getDomainName(),
                    subscriptionDomainAddedEvent.getApplicationContext()
            ));
        }

        if (log.isDebugEnabled()) {
            String msg = gson.toJson(subscriptionDomainAddedEvent);
            log.debug("Subscription domain added event msg:" + msg);
        }

        Map<String, String> env = new HashMap<String, String>();
        env.put("STRATOS_SUBSCRIPTION_SERVICE_NAME", subscriptionDomainAddedEvent.getServiceName());
        env.put("STRATOS_SUBSCRIPTION_DOMAIN_NAME", subscriptionDomainAddedEvent.getDomainName());
        env.put("STRATOS_SUBSCRIPTION_TENANT_ID", Integer.toString(subscriptionDomainAddedEvent.getTenantId()));
        env.put("STRATOS_SUBSCRIPTION_TENANT_DOMAIN", tenantDomain);
        env.put("STRATOS_SUBSCRIPTION_APPLICATION_CONTEXT", subscriptionDomainAddedEvent.getApplicationContext());
        ExtensionUtils.executeSubscriptionDomainAddedExtension(env);
    }

    private String findTenantDomain(int tenantId) {
        try {
            TenantManager.acquireReadLock();
            Tenant tenant = TenantManager.getInstance().getTenant(tenantId);
            if (tenant == null) {
                throw new RuntimeException(String.format("Tenant could not be found: [tenant-id] %d", tenantId));
            }
            return tenant.getTenantDomain();
        } finally {
            TenantManager.releaseReadLock();
        }
    }

    @Override
    public void onSubscriptionDomainRemovedEvent(SubscriptionDomainRemovedEvent subscriptionDomainRemovedEvent) {
        String tenantDomain = findTenantDomain(subscriptionDomainRemovedEvent.getTenantId());
        if (log.isInfoEnabled()) {
            log.info(String.format("Subscription domain added event received: [tenant-id] %d [tenant-domain] %s " +
                            "[domain-name] %s",
                    subscriptionDomainRemovedEvent.getTenantId(),
                    tenantDomain,
                    subscriptionDomainRemovedEvent.getDomainName()
            ));
        }

        if (log.isDebugEnabled()) {
            String msg = gson.toJson(subscriptionDomainRemovedEvent);
            log.debug("Subscription domain removed event msg:" + msg);
        }

        Map<String, String> env = new HashMap<String, String>();
        env.put("STRATOS_SUBSCRIPTION_SERVICE_NAME", subscriptionDomainRemovedEvent.getServiceName());
        env.put("STRATOS_SUBSCRIPTION_DOMAIN_NAME", subscriptionDomainRemovedEvent.getDomainName());
        env.put("STRATOS_SUBSCRIPTION_TENANT_ID", Integer.toString(subscriptionDomainRemovedEvent.getTenantId()));
        env.put("STRATOS_SUBSCRIPTION_TENANT_DOMAIN", tenantDomain);
        ExtensionUtils.executeSubscriptionDomainRemovedExtension(env);
    }

    @Override
    public void onCopyArtifactsExtension(String src, String des) {
        ExtensionUtils.executeCopyArtifactsExtension(src, des);
    }

}