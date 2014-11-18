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
package org.apache.stratos.messaging.event.instance.status;

import java.io.Serializable;
import java.util.Map;

/**
 * This event is fired by instance upon git pull.
 */

public class ArtifactDeploymentCompletedEvent extends InstanceStatusEvent implements Serializable {
    private String serviceName;
    private String clusterId;
    private String networkPartitionId;
    private String partitionId;
    private String memberId;
    private String tenantId;
    private String environment;
    private boolean multitenant;
    private Map<String, Long> modifiedArtifacts;

    public ArtifactDeploymentCompletedEvent(String serviceName, String clusterId, String networkPartitionId,
                                            String partitionId, String memberId, String tenantId, String environment,
                                            boolean multitenant, Map<String, Long> modifiedArtifacts) {
        this.serviceName = serviceName;
        this.clusterId = clusterId;
        this.networkPartitionId = networkPartitionId;
        this.partitionId = partitionId;
        this.memberId = memberId;
        this.tenantId = tenantId;
        this.environment = environment;
        this.multitenant = multitenant;
        this.modifiedArtifacts = modifiedArtifacts;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getNetworkPartitionId() {
        return networkPartitionId;
    }

    public void setNetworkPartitionId(String networkPartitionId) {
        this.networkPartitionId = networkPartitionId;
    }

    public String getPartitionId() {
        return partitionId;
    }

    public void setPartitionId(String partitionId) {
        this.partitionId = partitionId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public boolean isMultitenant() {
        return multitenant;
    }

    public void setMultitenant(boolean multitenant) {
        this.multitenant = multitenant;
    }

    public Map<String, Long> getModifiedArtifacts() {
        return modifiedArtifacts;
    }

    public void setModifiedArtifacts(Map<String, Long> modifiedArtifacts) {
        this.modifiedArtifacts = modifiedArtifacts;
    }

}
