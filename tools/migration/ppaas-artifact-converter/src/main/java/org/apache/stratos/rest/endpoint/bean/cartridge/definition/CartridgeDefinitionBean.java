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

package org.apache.stratos.rest.endpoint.bean.cartridge.definition;

import javax.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlRootElement(name = "cartridgeDefinitionBean") public class CartridgeDefinitionBean {

    private String type;
    private String host;
    private String provider;
    private String displayName;
    private String description;
    private String version;
    private boolean multiTenant;
    private DeploymentBean deployment;
    private List<PortMappingBean> portMapping;
    private PersistenceBean persistence;
    private List<IaasProviderBean> iaasProvider;
    private LoadBalancerBean loadBalancer;
    private List<PropertyBean> property;
    private String defaultAutoscalingPolicy;
    private String defaultDeploymentPolicy;
    private String serviceGroup;

    public String toString() {

        return "Type: " + type + ", Provider: " + provider + ", Host: " + host + ", Display Name: " + displayName +
                ", Description: " + description + ", Version: " + version + ", Multitenant " + multiTenant + "\n" +
                getDeploymentDetails() + "\n PortMapping: " + getPortMappings() + "\n IaaS: " + getIaasProviders() +
                "\n LoadBalancer: " + getLoadBalancerInfo() + "\n Properties: " + getProperties()
                + "\n VolumeBean mappings " + persistence.toString();
    }

    private String getDeploymentDetails() {

        if (deployment != null) {
            return deployment.toString();
        }
        return null;
    }

    private String getLoadBalancerInfo() {

        if (loadBalancer != null) {
            return loadBalancer.toString();
        }
        return null;
    }

    private String getPortMappings() {

        StringBuilder portMappingBuilder = new StringBuilder();
        if (portMapping != null && !portMapping.isEmpty()) {
            for (PortMappingBean portMappingBean : portMapping) {
                portMappingBuilder.append(portMappingBean.toString());
            }
        }
        return portMappingBuilder.toString();
    }

    private String getIaasProviders() {

        StringBuilder iaasBuilder = new StringBuilder();
        if (iaasProvider != null && !iaasProvider.isEmpty()) {
            for (IaasProviderBean iaasProviderBean : iaasProvider) {
                iaasBuilder.append(iaasProviderBean.toString());
            }
        }
        return iaasBuilder.toString();
    }

    private String getProperties() {

        StringBuilder propertyBuilder = new StringBuilder();
        if (property != null) {
            for (PropertyBean propertyBean : property) {
                propertyBuilder.append(propertyBean.getName() + " : " + propertyBean.getValue() + " | ");
            }
        }
        return propertyBuilder.toString();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isMultiTenant() {
        return multiTenant;
    }

    public void setMultiTenant(boolean multiTenant) {
        this.multiTenant = multiTenant;
    }

    public DeploymentBean getDeployment() {
        return deployment;
    }

    public void setDeployment(DeploymentBean deployment) {
        this.deployment = deployment;
    }

    public List<PortMappingBean> getPortMapping() {
        return portMapping;
    }

    public void setPortMapping(List<PortMappingBean> portMapping) {
        this.portMapping = portMapping;
    }

    public PersistenceBean getPersistence() {
        return persistence;
    }

    public void setPersistence(PersistenceBean persistence) {
        this.persistence = persistence;
    }

    public List<IaasProviderBean> getIaasProvider() {
        return iaasProvider;
    }

    public void setIaasProvider(List<IaasProviderBean> iaasProvider) {
        this.iaasProvider = iaasProvider;
    }

    public LoadBalancerBean getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(LoadBalancerBean loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public List<PropertyBean> getProperty() {
        return property;
    }

    public void setProperty(List<PropertyBean> property) {
        this.property = property;
    }

    public String getDefaultAutoscalingPolicy() {
        return defaultAutoscalingPolicy;
    }

    public void setDefaultAutoscalingPolicy(String defaultAutoscalingPolicy) {
        this.defaultAutoscalingPolicy = defaultAutoscalingPolicy;
    }

    public String getDefaultDeploymentPolicy() {
        return defaultDeploymentPolicy;
    }

    public void setDefaultDeploymentPolicy(String defaultDeploymentPolicy) {
        this.defaultDeploymentPolicy = defaultDeploymentPolicy;
    }

    public String getServiceGroup() {
        return serviceGroup;
    }

    public void setServiceGroup(String serviceGroup) {
        this.serviceGroup = serviceGroup;
    }
}
