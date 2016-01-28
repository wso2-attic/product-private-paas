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
package org.wso2.ppaas.tools.artifactmigration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.apache.log4j.Logger;
import org.apache.stratos.cloud.controller.stub.pojo.Persistence;
import org.apache.stratos.cloud.controller.stub.pojo.PortMapping;
import org.apache.stratos.cloud.controller.stub.pojo.Volume;
import org.apache.stratos.common.beans.application.ApplicationBean;
import org.apache.stratos.common.beans.application.ComponentBean;
import org.apache.stratos.common.beans.application.SubscribableInfo;
import org.apache.stratos.common.beans.application.domain.mapping.DomainMappingBean;
import org.apache.stratos.common.beans.artifact.repository.ArtifactRepositoryBean;
import org.apache.stratos.common.beans.cartridge.*;
import org.apache.stratos.common.beans.partition.NetworkPartitionBean;
import org.apache.stratos.common.beans.partition.NetworkPartitionReferenceBean;
import org.apache.stratos.common.beans.partition.PartitionBean;
import org.apache.stratos.common.beans.partition.PartitionReferenceBean;
import org.apache.stratos.common.beans.policy.autoscale.*;
import org.apache.stratos.common.beans.policy.deployment.ApplicationPolicyBean;
import org.apache.stratos.common.beans.policy.deployment.DeploymentPolicyBean;
import org.apache.stratos.manager.dto.Cartridge;
import org.apache.stratos.rest.endpoint.bean.CartridgeInfoBean;
import org.apache.stratos.rest.endpoint.bean.autoscaler.partition.Partition;
import org.apache.stratos.rest.endpoint.bean.autoscaler.partition.PartitionGroup;
import org.apache.stratos.rest.endpoint.bean.autoscaler.policy.autoscale.AutoscalePolicy;
import org.apache.stratos.rest.endpoint.bean.autoscaler.policy.deployment.DeploymentPolicy;
import org.apache.stratos.rest.endpoint.bean.cartridge.definition.PropertyBean;
import org.apache.stratos.rest.endpoint.bean.cartridge.definition.ServiceDefinitionBean;
import org.apache.stratos.rest.endpoint.bean.subscription.domain.SubscriptionDomainBean;
import org.wso2.ppaas.tools.artifactmigration.exception.ArtifactLoadingException;
import org.wso2.ppaas.tools.artifactmigration.exception.TransformationException;
import org.wso2.ppaas.tools.artifactmigration.loader.ArtifactLoader400;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Transforms the artifacts from PPaaS 4.0.0 to 4.1.0
 */
class Transformer {

    private static final Logger log = Logger.getLogger(Transformer.class);
    private static final Map<String, List<String>> memoryMap = new HashMap<>();
    private static final Map<String, Boolean> domainMappingAvailabilityMap = new HashMap<>();
    private static final Map<String, String> deploymentPolicyToApplicationPolicyMap = new HashMap<>();
    private static final List<String> networkPartitionList = new ArrayList<>();
    private static final List<String> deploymentPolicyList = new ArrayList<>();
    private static final ExecutorService executorService = Executors.newFixedThreadPool(3);
    private static List<CartridgeInfoBean> subscription400List = null;

    public static Map<String, List<String>> getMemoryMap() {
        return memoryMap;
    }

    public static Map<String, String> getDeploymentPolicyToApplicationPolicyMap() {
        return deploymentPolicyToApplicationPolicyMap;
    }

    public static Map<String, Boolean> getDomainMappingAvailabilityMap() {
        return domainMappingAvailabilityMap;
    }

    /**
     * Method to transform Auto Scale Policies
     */
    public static void transformAutoscalePolicyList() {
        Runnable autoscalePolicyRunnable = new Runnable() {
            @Override public void run() {
                if (log.isInfoEnabled()) {
                    log.info("Started autoscaling policy conversion");
                }
                List<AutoscalePolicy> autoscalePolicy400List;
                AutoscalePolicyBean autoscalePolicy410 = new AutoscalePolicyBean();

                boolean created410autoscalepolicy = false;

                try {
                    autoscalePolicy400List = ArtifactLoader400.fetchAutoscalePolicyList();
                    if (autoscalePolicy400List.isEmpty())
                        log.info("Auto Scale Policies not available from PPaaS 4.0.0");
                    else {
                        log.info("Fetched Auto Scale Policy from PPaaS 4.0.0");

                        File directoryName = new File(Constants.ROOT_DIRECTORY + Constants.DIRECTORY_POLICY_AUTOSCALE);
                        for (AutoscalePolicy autoscalePolicy400 : autoscalePolicy400List) {

                            autoscalePolicy410.setId(autoscalePolicy400.getId());

                            //PPaaS 4.1.0 artifacts
                            LoadThresholdsBean loadThresholds410 = new LoadThresholdsBean();
                            RequestsInFlightThresholdsBean requestsInFlight410 = new RequestsInFlightThresholdsBean();
                            MemoryConsumptionThresholdsBean memoryConsumption410 = new MemoryConsumptionThresholdsBean();
                            LoadAverageThresholdsBean loadAverage410 = new LoadAverageThresholdsBean();

                            requestsInFlight410.setThreshold(
                                    autoscalePolicy400.getLoadThresholds().getRequestsInFlight().getUpperLimit());
                            memoryConsumption410.setThreshold(
                                    autoscalePolicy400.getLoadThresholds().getMemoryConsumption().getUpperLimit());
                            loadAverage410.setThreshold(
                                    autoscalePolicy400.getLoadThresholds().getLoadAverage().getUpperLimit());
                            loadThresholds410.setRequestsInFlight(requestsInFlight410);
                            loadThresholds410.setLoadAverage(loadAverage410);
                            loadThresholds410.setMemoryConsumption(memoryConsumption410);

                            autoscalePolicy410.setLoadThresholds(loadThresholds410);
                            JsonWriter.writeFile(directoryName, autoscalePolicy410.getId() + Constants.JSON_EXTENSION,
                                    getGson().toJson(autoscalePolicy410));
                            if (autoscalePolicy400.getId() != null)
                                created410autoscalepolicy = true;
                        }
                    }
                    if (created410autoscalepolicy)
                        log.info("Created Auto Scale Policy 4.1.0 artifacts");
                } catch (JsonSyntaxException e) {
                    String msg = "JSON syntax error in retrieving auto scale policies";
                    log.error(msg, e);
                } catch (ArtifactLoadingException e) {
                    String msg = "Artifact Loading error in fetching auto scale policies";
                    log.error(msg, e);
                }
            }
        };
        executorService.submit(autoscalePolicyRunnable);
    }

    /**
     * Method to transform network partitions
     */
    public static void transformNetworkPartitionList() {

        Runnable networkPartitionRunnable = new Runnable() {
            @Override public void run() {
                if (log.isInfoEnabled()) {
                    log.info("Started network partition list conversion");
                }
                List<Partition> networkPartition400List;
                NetworkPartitionBean networkPartition410 = new NetworkPartitionBean();
                File directoryName;
                try {
                    networkPartition400List = ArtifactLoader400.fetchPartitionList();
                    if (networkPartition400List.isEmpty())
                        log.info("Network Partitions not available from PPaaS 4.0.0");
                    else {
                        log.info("Fetched Network Partition List from PPaaS 4.0.0");

                        for (Partition networkPartition400 : networkPartition400List) {
                            networkPartition410.setId(networkPartition400.getId());
                            networkPartition410.setProvider(networkPartition400.getProvider());
                            List<PartitionBean> partitionsList410 = new ArrayList<>();
                            PartitionBean partition410 = new PartitionBean();
                            partition410.setId(Constants.NETWORK_PARTITION_ID);
                            if (networkPartition400.getProperty() != null) {
                                List<org.apache.stratos.rest.endpoint.bean.cartridge.definition.PropertyBean> property400List = networkPartition400
                                        .getProperty();
                                List<org.apache.stratos.common.beans.PropertyBean> property410List = new ArrayList<>();

                                for (org.apache.stratos.rest.endpoint.bean.cartridge.definition.PropertyBean property400 : property400List) {
                                    org.apache.stratos.common.beans.PropertyBean property = new org.apache.stratos.common.beans.PropertyBean();
                                    property.setName(property400.getName());
                                    property.setValue(property400.getValue());
                                    property410List.add(property);
                                }
                                partition410.setProperty(property410List);
                                partitionsList410.add(0, partition410);
                                networkPartition410.setPartitions(partitionsList410);
                            }
                            directoryName = new File(
                                    Constants.ROOT_DIRECTORY + Constants.DIRECTORY_NETWORK_PARTITION + File.separator
                                            + networkPartition400.getProvider());
                            JsonWriter.writeFile(directoryName, networkPartition400.getId() + Constants.JSON_EXTENSION,
                                    getGson().toJson(networkPartition410));
                            networkPartitionList.add(networkPartition400.getId());
                        }
                    }
                    memoryMap.put("networkPartitions", networkPartitionList);
                    if (!networkPartitionList.isEmpty())
                        log.info("Created Network Partition List 4.1.0 artifacts");
                } catch (JsonSyntaxException e) {
                    String msg = "JSON syntax error in retrieving network partition lists";
                    log.error(msg, e);
                } catch (ArtifactLoadingException e) {
                    String msg = "Artifact loading error in fetching network partition lists";
                    log.error(msg, e);
                }
            }
        };
        executorService.submit(networkPartitionRunnable);
    }

    /**
     * Method to transform DeploymentPolicy
     */
    public static void transformDeploymentPolicyList() {

        Runnable deploymentPoliciesRunnable = new Runnable() {
            @Override public void run() {

                if (log.isInfoEnabled()) {
                    log.info("Started deployment policy conversion");
                }
                List<DeploymentPolicy> deploymentPolicy400List;
                DeploymentPolicyBean deploymentPolicy410 = new DeploymentPolicyBean();
                try {
                    deploymentPolicy400List = ArtifactLoader400.fetchDeploymentPolicyList();
                    if (deploymentPolicy400List.isEmpty())
                        log.info("Deployment Policies not available from PPaaS 4.0.0");
                    else {
                        log.info("Fetched Deployment Policy from PPaaS 4.0.0");

                        File directoryName = new File(Constants.ROOT_DIRECTORY + Constants.DIRECTORY_POLICY_DEPLOYMENT);
                        for (DeploymentPolicy deploymentPolicy400 : deploymentPolicy400List) {

                            List<String> networkPartitions = new ArrayList<>();

                            deploymentPolicy410.setId(deploymentPolicy400.getId());
                            List<PartitionGroup> partitionGroup400List = deploymentPolicy400.getPartitionGroup();
                            List<NetworkPartitionReferenceBean> networkPartitions410List = new ArrayList<>();

                            int a = 0;
                            for (PartitionGroup partitionGroup : partitionGroup400List) {
                                NetworkPartitionReferenceBean tempNetworkPartition = new NetworkPartitionReferenceBean();
                                tempNetworkPartition.setPartitionAlgo(partitionGroup.getPartitionAlgo());

                                List<Partition> partition400List = partitionGroup.getPartition();
                                List<PartitionReferenceBean> partitions410List = new ArrayList<>();

                                int partitionIterator = 0;
                                for (Partition partition : partition400List) {

                                    networkPartitions.add(partition.getId());

                                    tempNetworkPartition.setId(partition.getId());
                                    PartitionReferenceBean tempPartition = new PartitionReferenceBean();
                                    tempPartition.setId(Constants.NETWORK_PARTITION_ID);
                                    tempPartition.setPartitionMax(partition.getPartitionMax());

                                    if (partition.getProperty() != null) {
                                        List<PropertyBean> property400List = partition.getProperty();
                                        List<org.apache.stratos.common.beans.PropertyBean> property410List = new ArrayList<>();
                                        int c = 0;
                                        for (PropertyBean propertyBean400 : property400List) {
                                            org.apache.stratos.common.beans.PropertyBean tempPropertyBean410 = new org.apache.stratos.common.beans.PropertyBean();
                                            tempPropertyBean410.setName(propertyBean400.getName());
                                            tempPropertyBean410.setValue(propertyBean400.getValue());
                                            property410List.add(c++, tempPropertyBean410);
                                        }
                                        tempPartition.setProperty(property410List);
                                    }
                                    partitions410List.add(partitionIterator++, tempPartition);
                                }
                                tempNetworkPartition.setPartitions(partitions410List);
                                networkPartitions410List.add(a, tempNetworkPartition);
                            }
                            deploymentPolicy410.setNetworkPartitions(networkPartitions410List);

                            //Adding network partitions specific to a deployment policies
                            memoryMap.put(deploymentPolicy400.getId(), networkPartitions);
                            deploymentPolicyList.add(deploymentPolicy410.getId());

                            JsonWriter.writeFile(directoryName, deploymentPolicy410.getId() + Constants.JSON_EXTENSION,
                                    getGson().toJson(deploymentPolicy410));
                        }
                    }
                    memoryMap.put("deploymentPolicies", deploymentPolicyList);
                    if (!deploymentPolicyList.isEmpty())
                        log.info("Created Deployment Policy 4.1.0 artifacts");
                } catch (JsonSyntaxException e) {
                    String msg = "JSON syntax error in retrieving deployment policies";
                    log.error(msg, e);
                } catch (ArtifactLoadingException e) {
                    String msg = "Artifact Loading error in fetching deployment policies";
                    log.error(msg, e);
                }
                if (log.isInfoEnabled()) {
                    log.info("Deployment policy conversion completed");
                }
            }
        };
        executorService.submit(deploymentPoliciesRunnable);
    }

    /**
     * Method to transform cartridge list
     */
    public static void transformCartridgeList() throws TransformationException {
        List<Cartridge> cartridge400List;
        ApplicationBean application410 = new ApplicationBean();
        CartridgeBean cartridge410 = new CartridgeBean();

        try {
            subscription400List = ArtifactLoader400.fetchSubscriptionDataList();
            if (subscription400List.isEmpty())
                log.info("Subscription not available from PPaaS 4.0.0");
            else
                log.info("Fetched Subscription List");

            cartridge400List = ArtifactLoader400.fetchCartridgeList();
            if (cartridge400List.isEmpty())
                log.info("Cartridges not available from PPaaS 4.0.0");
            else {
                log.info("Fetched Cartridge List from PPaaS 4.0.0");
                File outputDirectoryNameCartridge = new File(Constants.ROOT_DIRECTORY + Constants.DIRECTORY_CARTRIDGE);
                for (Cartridge cartridge : cartridge400List) {

                    ComponentBean components = new ComponentBean();
                    List<CartridgeReferenceBean> cartridges = new ArrayList<>();
                    CartridgeReferenceBean cartridgeReference410 = new CartridgeReferenceBean();
                    SubscribableInfo subscribableInfo = new SubscribableInfo();

                    //Getting corresponding subscription data
                    List<ArtifactRepositoryBean> signup410List = new ArrayList<>();
                    List<DomainMappingBean> domainMapping410List = new ArrayList<>();
                    int domainMappingIterator = 0;
                    int a = 0;

                    for (CartridgeInfoBean subscription : subscription400List) {

                        if (cartridge.getCartridgeType().equalsIgnoreCase(subscription.getCartridgeType())) {

                            subscribableInfo.setAutoscalingPolicy(subscription.getAutoscalePolicy());
                            subscribableInfo.setDeploymentPolicy(subscription.getDeploymentPolicy());
                            subscribableInfo.setAlias(subscription.getAlias());

                            //Adding signup details
                            ArtifactRepositoryBean artifactRepository = new ArtifactRepositoryBean();
                            artifactRepository.setAlias(subscription.getAlias());
                            artifactRepository.setPrivateRepo(subscription.isPrivateRepo());
                            artifactRepository.setRepoUrl(subscription.getRepoURL());
                            artifactRepository.setRepoUsername(subscription.getRepoUsername());
                            artifactRepository.setRepoPassword(subscription.getRepoPassword());

                            signup410List.add(a++, artifactRepository);

                            cartridgeReference410.setSubscribableInfo(subscribableInfo);
                            cartridgeReference410.setType(cartridge.getCartridgeType());
                            cartridgeReference410.setCartridgeMax(Constants.CARTRIDGE_MAX_VALUE);
                            cartridgeReference410.setCartridgeMin(Constants.CARTRIDGE_MIN_VALUE);

                            cartridges.add(0, cartridgeReference410);
                            components.setCartridges(cartridges);
                            application410.setComponents(components);
                            application410.setAlias(cartridgeReference410.getSubscribableInfo().getAlias());
                            application410.setName(subscription.getAlias() + Constants.APPLICATION_NAME);
                            application410.setApplicationId(subscription.getAlias() + Constants.APPLICATION_NAME);
                            application410.setDescription(cartridge.getDescription());

                            File outputDirectoryNameApp = new File(
                                    Constants.ROOT_DIRECTORY + Constants.DIRECTORY_APPLICATION + File.separator
                                            + application410.getName() + File.separator
                                            + Constants.DIRECTORY_ARTIFACTS);
                            JsonWriter.writeFile(outputDirectoryNameApp,
                                    application410.getName() + Constants.JSON_EXTENSION,
                                    getGson().toJson(application410));
                            if (application410.getApplicationId() != null)
                                log.info("Created Application " + application410.getApplicationId()
                                        + " 4.1.0 artifacts");

                            JsonWriter.writeFile(outputDirectoryNameApp, Constants.FILENAME_APPLICATION_SIGNUP,
                                    getGson().toJson(signup410List));

                            //Adding domain mapping details
                            List<SubscriptionDomainBean> domainMapping400List = ArtifactLoader400
                                    .fetchDomainMappingList(cartridge.getCartridgeType(), subscription.getAlias());

                            if ((domainMapping400List != null) && (!domainMapping400List.isEmpty())) {
                                domainMappingAvailabilityMap.put(application410.getName(), true);
                                for (SubscriptionDomainBean domainMapping : domainMapping400List) {
                                    DomainMappingBean domainMappingBean = new DomainMappingBean();
                                    domainMappingBean.setCartridgeAlias(subscription.getAlias());
                                    domainMappingBean.setDomainName(domainMapping.getDomainName());
                                    domainMappingBean.setContextPath(domainMapping.getApplicationContext());
                                    domainMapping410List.add(domainMappingIterator++, domainMappingBean);
                                }
                                //Converting domain mapping list string to the standard format
                                String domainMappingJsonString =
                                        "{\"domainMappings\":" + getGson().toJson(domainMapping410List) + "}";
                                JsonWriter.writeFile(outputDirectoryNameApp, Constants.FILENAME_DOMAIN_MAPPING,
                                        domainMappingJsonString);
                            } else {
                                domainMappingAvailabilityMap.put(application410.getName(), false);
                            }
                            ConversionTool.addCommonDeployingScript(
                                    Constants.ROOT_DIRECTORY + Constants.DIRECTORY_OUTPUT_SCRIPT + File.separator
                                            + application410.getName(), subscribableInfo, cartridge.getDisplayName(),
                                    application410.getName());
                            ConversionTool.addCommonUndeployingScript(
                                    Constants.ROOT_DIRECTORY + Constants.DIRECTORY_OUTPUT_SCRIPT + File.separator
                                            + application410.getName(), subscribableInfo, cartridge.getDisplayName(),
                                    cartridge.getCartridgeType(), application410.getName());
                        }
                    }
                    cartridge410.setDisplayName(cartridge.getDisplayName());
                    cartridge410.setDescription(cartridge.getDescription());
                    cartridge410.setCategory(Constants.CARTRIDGE_CATEGORY);
                    cartridge410.setType(cartridge.getCartridgeType());
                    cartridge410.setProvider(cartridge.getProvider());
                    cartridge410.setVersion(cartridge.getVersion());
                    cartridge410.setHost(cartridge.getHostName());
                    cartridge410.setMultiTenant(cartridge.isMultiTenant());

                    //Setting the port mappings details
                    //Use of default values in port mappings
                    List<PortMappingBean> portMappingList = new ArrayList<>();
                    PortMappingBean portMappingBean = new PortMappingBean();
                    portMappingBean.setPort(Integer.parseInt(System.getProperty(Constants.PORT)));
                    portMappingBean.setProxyPort(Integer.parseInt(System.getProperty(Constants.PROXY_PORT)));
                    portMappingBean.setProtocol(System.getProperty(Constants.PROTOCOL));
                    portMappingList.add(0, portMappingBean);

                    cartridge410.setPortMapping(portMappingList);

                    //Overwrite the default mappings if port mappings exist
                    if (cartridge.getPortMappings() != null) {
                        PortMapping[] portMapping400List = cartridge.getPortMappings();

                        int b = 0;
                        for (PortMapping portMapping : portMapping400List) {

                            PortMappingBean portMappingBeanTemp = new PortMappingBean();
                            if (portMapping.getPort() != null)
                                portMappingBeanTemp.setPort(Integer.parseInt(portMapping.getPort()));
                            else
                                portMappingBeanTemp.setPort(Integer.parseInt(System.getProperty(Constants.PORT)));

                            if (portMapping.getProtocol() != null)
                                portMappingBeanTemp.setProtocol(portMapping.getProtocol());
                            else
                                portMappingBeanTemp.setProtocol(System.getProperty(Constants.PROTOCOL));

                            if (portMapping.getProxyPort() != null)
                                portMappingBeanTemp.setProxyPort(Integer.parseInt(portMapping.getProxyPort()));
                            else
                                portMappingBeanTemp
                                        .setProxyPort(Integer.parseInt(System.getProperty(Constants.PROXY_PORT)));
                            portMappingList.add(b++, portMappingBeanTemp);
                        }
                    }
                    cartridge410.setPortMapping(portMappingList);

                    if (cartridge.getPersistence() != null) {
                        Persistence persistence400 = cartridge.getPersistence();
                        PersistenceBean persistenceBean410 = new PersistenceBean();
                        persistenceBean410.setRequired(persistence400.getPersistanceRequired());

                        Volume[] volume400Array = persistence400.getVolumes();
                        List<VolumeBean> volumeBean410List = new ArrayList<>();

                        int b = 0;
                        for (Volume volume : volume400Array) {
                            VolumeBean volumeBeanTemp = new VolumeBean();
                            volumeBeanTemp.setId(volume.getId());
                            volumeBeanTemp.setSize(String.valueOf(volume.getSize()));
                            volumeBeanTemp.setMappingPath(volume.getMappingPath());
                            volumeBeanTemp.setDevice(volume.getDevice());
                            volumeBeanTemp.setRemoveOnTermination(volume.isRemoveOnterminationSpecified());

                            volumeBean410List.add(b++, volumeBeanTemp);
                        }
                        persistenceBean410.setVolume(volumeBean410List);
                        cartridge410.setPersistence(persistenceBean410);
                    }
                    //Setting IaaS provider details
                    List<NetworkInterfaceBean> networkInterfacesList = new ArrayList<>();
                    NetworkInterfaceBean networkInterface = new NetworkInterfaceBean();
                    networkInterface.setNetworkUuid(System.getProperty(Constants.NETWORK_UUID));
                    networkInterfacesList.add(networkInterface);

                    List<IaasProviderBean> iaasProviderList = new ArrayList<>();
                    IaasProviderBean iaasProvider = new IaasProviderBean();
                    iaasProvider.setType(System.getProperty(Constants.IAAS));
                    iaasProvider.setImageId(System.getProperty(Constants.IAAS_IMAGE_ID));
                    iaasProvider.setNetworkInterfaces(networkInterfacesList);
                    iaasProviderList.add(0, iaasProvider);
                    cartridge410.setIaasProvider(iaasProviderList);

                    JsonWriter.writeFile(outputDirectoryNameCartridge,
                            cartridge410.getDisplayName() + Constants.JSON_EXTENSION, getGson().toJson(cartridge410));
                    if (cartridge410.getDisplayName() != null)
                        log.info("Created Cartridge " + cartridge410.getType() + " 4.1.0 artifacts");
                }
            }
        } catch (JsonSyntaxException e) {
            String msg = "JSON syntax error in retrieving cartridges";
            log.error(msg);
            throw new TransformationException(msg, e);
        } catch (ArtifactLoadingException e) {
            String msg = "Artifact loading error in fetching cartridges";
            log.error(msg);
            throw new TransformationException(msg, e);
        }
    }

    /**
     * Method to transform multi tenant cartridge list
     */

    public static void transformMultiTenantCartridgeList() throws TransformationException {
        List<Cartridge> multiTenantCartridge400List;
        List<ServiceDefinitionBean> service400List;
        ApplicationBean multiTenantApplication410 = new ApplicationBean();
        CartridgeBean multiTenantCartridge410 = new CartridgeBean();

        try {
            service400List = ArtifactLoader400.fetchMultiTenantServiceList();
            if (service400List.isEmpty())
                log.info("Services not available from PPaaS 4.0.0");
            else
                log.info("Fetched Service List from PPaaS 4.0.0");

            multiTenantCartridge400List = ArtifactLoader400.fetchMultiTenantCartridgeList();
            if (multiTenantCartridge400List.isEmpty())
                log.info("Multi Tenant Cartridges not available from PPaaS 4.0.0");
            else {
                log.info("Fetched Multi Tenant Cartridge List from PPaaS 4.0.0");

                File outputDirectoryNameCartridge = new File(Constants.ROOT_DIRECTORY + Constants.DIRECTORY_CARTRIDGE);
                for (Cartridge multiTenantCartridge : multiTenantCartridge400List) {

                    SubscribableInfo multiTenantSubscribableInfo = new SubscribableInfo();
                    CartridgeReferenceBean multiTenantCartridgeReference410 = new CartridgeReferenceBean();
                    List<CartridgeReferenceBean> multiTenantCartridges = new ArrayList<>();
                    ComponentBean multiTenantComponents = new ComponentBean();

                    for (ServiceDefinitionBean service : service400List) {
                        if (multiTenantCartridge.getCartridgeType().equalsIgnoreCase(service.getCartridgeType())) {
                            multiTenantSubscribableInfo.setAutoscalingPolicy(service.getAutoscalingPolicyName());
                            multiTenantSubscribableInfo.setDeploymentPolicy(service.getDeploymentPolicyName());
                            multiTenantSubscribableInfo.setAlias(
                                    multiTenantCartridge.getCartridgeType() + "-" + service.getServiceName()
                                            + Constants.APPLICATION_NAME);

                            multiTenantCartridgeReference410.setSubscribableInfo(multiTenantSubscribableInfo);
                            multiTenantCartridgeReference410.setType(multiTenantCartridge.getCartridgeType());
                            multiTenantCartridgeReference410.setCartridgeMax(Constants.CARTRIDGE_MAX_VALUE);
                            multiTenantCartridgeReference410.setCartridgeMin(Constants.CARTRIDGE_MIN_VALUE);
                           // multiTenantCartridges.add(0, multiTenantCartridgeReference410);
                            multiTenantCartridges.add(multiTenantCartridgeReference410);
                            multiTenantComponents.setCartridges(multiTenantCartridges);

                            multiTenantApplication410.setComponents(multiTenantComponents);
                            multiTenantApplication410.setAlias(multiTenantSubscribableInfo.getAlias());
                            multiTenantApplication410.setName(multiTenantSubscribableInfo.getAlias());
                            multiTenantApplication410.setApplicationId(multiTenantSubscribableInfo.getAlias());
                            multiTenantApplication410.setDescription(multiTenantCartridge.getDescription());

                            File outputDirectoryNameMultiTenantApp = new File(
                                    Constants.ROOT_DIRECTORY + Constants.DIRECTORY_APPLICATION + File.separator
                                            + multiTenantApplication410.getName() + File.separator
                                            + Constants.DIRECTORY_ARTIFACTS);
                            JsonWriter.writeFile(outputDirectoryNameMultiTenantApp,
                                    multiTenantApplication410.getName() + Constants.JSON_EXTENSION,
                                    getGson().toJson(multiTenantApplication410));
                            if (multiTenantApplication410.getApplicationId() != null)
                                log.info("Created Multi Tenant Application " + multiTenantApplication410
                                        .getApplicationId() + " 4.1.0 artifacts");

                            List<DomainMappingBean> domainMapping410List = new ArrayList<>();
                            List<ArtifactRepositoryBean> signup410List = new ArrayList<>();
                            int domainMappingIterator = 0;
                            int a = 0;

                            domainMappingAvailabilityMap.put(multiTenantApplication410.getName(), false);
                            for (CartridgeInfoBean subscription : subscription400List) {
                                if (multiTenantCartridge.getCartridgeType()
                                        .equalsIgnoreCase(subscription.getCartridgeType())) {

                                    ArtifactRepositoryBean artifactRepository = new ArtifactRepositoryBean();

                                    artifactRepository.setAlias(subscription.getAlias());
                                    artifactRepository.setPrivateRepo(subscription.isPrivateRepo());
                                    artifactRepository.setRepoUrl(subscription.getRepoURL());
                                    artifactRepository.setRepoUsername(subscription.getRepoUsername());
                                    artifactRepository.setRepoPassword(subscription.getRepoPassword());

                                    signup410List.add(a++, artifactRepository);

                                    List<SubscriptionDomainBean> domainMapping400List = ArtifactLoader400
                                            .fetchDomainMappingList(multiTenantCartridge.getCartridgeType(),
                                                    subscription.getAlias());

                                    if ((domainMapping400List != null) && (!domainMapping400List.isEmpty())) {
                                        domainMappingAvailabilityMap.put(multiTenantApplication410.getName(), true);
                                        for (SubscriptionDomainBean domainMapping : domainMapping400List) {
                                            DomainMappingBean domainMappingBean = new DomainMappingBean();
                                            domainMappingBean.setCartridgeAlias(subscription.getAlias());
                                            domainMappingBean.setDomainName(domainMapping.getDomainName());
                                            domainMappingBean.setContextPath(domainMapping.getApplicationContext());
                                            domainMapping410List.add(domainMappingIterator++, domainMappingBean);
                                        }
                                        //Converting domain mapping list string to the standard format
                                        String domainMappingJsonString =
                                                "{\"domainMappings\":" + getGson().toJson(domainMapping410List) + "}";
                                        JsonWriter.writeFile(outputDirectoryNameMultiTenantApp,
                                                Constants.FILENAME_DOMAIN_MAPPING, domainMappingJsonString);
                                    }
                                }
                            }

                            JsonWriter
                                    .writeFile(outputDirectoryNameMultiTenantApp, Constants.FILENAME_APPLICATION_SIGNUP,
                                            getGson().toJson(signup410List));

                            ConversionTool.addCommonDeployingScript(
                                    Constants.ROOT_DIRECTORY + Constants.DIRECTORY_OUTPUT_SCRIPT + File.separator
                                            + multiTenantApplication410.getName(), multiTenantSubscribableInfo,
                                    multiTenantCartridge.getDisplayName(), multiTenantApplication410.getName());
                            ConversionTool.addCommonUndeployingScript(
                                    Constants.ROOT_DIRECTORY + Constants.DIRECTORY_OUTPUT_SCRIPT + File.separator
                                            + multiTenantApplication410.getName(), multiTenantSubscribableInfo,
                                    multiTenantCartridge.getDisplayName(), multiTenantCartridge.getCartridgeType(),
                                    multiTenantApplication410.getName());
                        }
                    }

                    multiTenantCartridge410.setDisplayName(multiTenantCartridge.getDisplayName());
                    multiTenantCartridge410.setDescription(multiTenantCartridge.getDescription());
                    multiTenantCartridge410.setCategory(Constants.CARTRIDGE_CATEGORY);
                    multiTenantCartridge410.setType(multiTenantCartridge.getCartridgeType());
                    multiTenantCartridge410.setProvider(multiTenantCartridge.getProvider());
                    multiTenantCartridge410.setVersion(multiTenantCartridge.getVersion());
                    multiTenantCartridge410.setHost(multiTenantCartridge.getHostName());
                    multiTenantCartridge410.setMultiTenant(multiTenantCartridge.isMultiTenant());

                    //Setting the port mappings details
                    //Use of default values in port mappings
                    List<PortMappingBean> portMappingList = new ArrayList<>();
                    PortMappingBean portMappingBean = new PortMappingBean();
                    portMappingBean.setPort(Integer.parseInt(System.getProperty(Constants.PORT)));
                    portMappingBean.setProxyPort(Integer.parseInt(System.getProperty(Constants.PROXY_PORT)));
                    portMappingBean.setProtocol(System.getProperty(Constants.PROTOCOL));
                    portMappingList.add(0, portMappingBean);

                    multiTenantCartridge410.setPortMapping(portMappingList);

                    //Overwrite the default mappings if port mappings exist
                    if (multiTenantCartridge.getPortMappings() != null) {
                        PortMapping[] portMapping400List = multiTenantCartridge.getPortMappings();
                        List<PortMappingBean> portMapping410List = new ArrayList<>();

                        int b = 0;
                        for (PortMapping portMapping : portMapping400List) {

                            PortMappingBean portMappingBeanTemp = new PortMappingBean();
                            if (portMapping.getPort() != null)
                                portMappingBeanTemp.setPort(Integer.parseInt(portMapping.getPort()));
                            else
                                portMappingBeanTemp.setPort(Integer.parseInt(System.getProperty(Constants.PORT)));

                            if (portMapping.getProtocol() != null)
                                portMappingBeanTemp.setProtocol(portMapping.getProtocol());
                            else
                                portMappingBeanTemp.setProtocol(System.getProperty(Constants.PROTOCOL));

                            if (portMapping.getProxyPort() != null)
                                portMappingBeanTemp.setProxyPort(Integer.parseInt(portMapping.getProxyPort()));
                            else
                                portMappingBeanTemp
                                        .setProxyPort(Integer.parseInt(System.getProperty(Constants.PROXY_PORT)));

                            portMapping410List.add(b++, portMappingBeanTemp);
                        }
                        multiTenantCartridge410.setPortMapping(portMapping410List);
                    }

                    if (multiTenantCartridge.getPersistence() != null) {
                        Persistence persistence400 = multiTenantCartridge.getPersistence();
                        PersistenceBean persistenceBean410 = new PersistenceBean();
                        persistenceBean410.setRequired(persistence400.getPersistanceRequired());

                        Volume[] volume400Array = persistence400.getVolumes();
                        List<VolumeBean> volumeBean410List = new ArrayList<>();

                        int b = 0;
                        for (Volume volume : volume400Array) {
                            VolumeBean volumeBeanTemp = new VolumeBean();
                            volumeBeanTemp.setId(volume.getId());
                            volumeBeanTemp.setSize(String.valueOf(volume.getSize()));
                            volumeBeanTemp.setMappingPath(volume.getMappingPath());
                            volumeBeanTemp.setDevice(volume.getDevice());
                            volumeBeanTemp.setRemoveOnTermination(volume.isRemoveOnterminationSpecified());

                            volumeBean410List.add(b++, volumeBeanTemp);
                        }
                        persistenceBean410.setVolume(volumeBean410List);
                        multiTenantCartridge410.setPersistence(persistenceBean410);
                    }
                    //Setting IaaS provider details
                    List<NetworkInterfaceBean> networkInterfacesList = new ArrayList<>();
                    NetworkInterfaceBean networkInterface = new NetworkInterfaceBean();
                    networkInterface.setNetworkUuid(System.getProperty(Constants.NETWORK_UUID));
                    networkInterfacesList.add(networkInterface);

                    List<IaasProviderBean> iaasProviderList = new ArrayList<>();
                    IaasProviderBean iaasProvider = new IaasProviderBean();
                    iaasProvider.setType(System.getProperty(Constants.IAAS));
                    iaasProvider.setImageId(System.getProperty(Constants.IAAS_IMAGE_ID));
                    iaasProvider.setNetworkInterfaces(networkInterfacesList);
                    iaasProviderList.add(0, iaasProvider);
                    multiTenantCartridge410.setIaasProvider(iaasProviderList);

                    JsonWriter.writeFile(outputDirectoryNameCartridge,
                            multiTenantCartridge410.getDisplayName() + Constants.JSON_EXTENSION,
                            getGson().toJson(multiTenantCartridge410));
                    if (multiTenantCartridge410.getDisplayName() != null)
                        log.info("Created Multi Tenant Cartridge " + multiTenantCartridge410.getType()
                                + " 4.1.0 artifacts");
                }
            }
        } catch (JsonSyntaxException e) {
            String msg = "JSON syntax error in retrieving multi tenant cartridges";
            log.error(msg);
            throw new TransformationException(msg, e);
        } catch (ArtifactLoadingException e) {
            String msg = "Artifact loading error in fetching multi tenant cartridges";
            log.error(msg);
            throw new TransformationException(msg, e);
        }
    }

    /**
     * Method to add default application policies
     */
    public static void addDefaultApplicationPolicies() {

        List<String> deploymentPolicyIDList = memoryMap.get("deploymentPolicies");

        for (String deploymentPolicyID : deploymentPolicyIDList) {
            ApplicationPolicyBean applicationPolicyBean = new ApplicationPolicyBean();
            applicationPolicyBean.setId(Constants.APPLICATION_POLICY_ID + deploymentPolicyID);
            applicationPolicyBean.setAlgorithm(Constants.APPLICATION_POLICY_ALGO);

            //Adding network partitions specific to a deployment policy
            List<String> networkPartitionIDList = memoryMap.get(deploymentPolicyID);
            String[] networkPartitions = new String[networkPartitionIDList.size()];
            networkPartitions = networkPartitionIDList.toArray(networkPartitions);

            applicationPolicyBean.setNetworkPartitions(networkPartitions);

            deploymentPolicyToApplicationPolicyMap
                    .put(deploymentPolicyID, Constants.APPLICATION_POLICY_ID + deploymentPolicyID);
            File directoryName = new File(Constants.ROOT_DIRECTORY + Constants.DIRECTORY_POLICY_APPLICATION);
            JsonWriter.writeFile(directoryName,
                    Constants.APPLICATION_POLICY_ID + deploymentPolicyID + Constants.JSON_EXTENSION,
                    getGson().toJson(applicationPolicyBean));
        }
    }

    /**
     * Method to get Gson
     */
    private static Gson getGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        return gsonBuilder.setPrettyPrinting().create();
    }

    /**
     * Method to wait for the termination of the thread pool
     */
    public static void waitForThreadTermination() {
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            log.info("Interrupted while waiting to terminate the thread pool");
        }
    }
}
