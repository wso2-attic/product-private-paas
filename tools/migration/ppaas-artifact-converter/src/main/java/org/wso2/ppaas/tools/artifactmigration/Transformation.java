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
import org.apache.stratos.cloud.controller.pojo.Persistence;
import org.apache.stratos.cloud.controller.pojo.PortMapping;
import org.apache.stratos.cloud.controller.pojo.Volume;
import org.apache.stratos.common.beans.application.ApplicationBean;
import org.apache.stratos.common.beans.application.ComponentBean;
import org.apache.stratos.common.beans.application.SubscribableInfo;
import org.apache.stratos.common.beans.application.domain.mapping.DomainMappingBean;
import org.apache.stratos.common.beans.artifact.repository.ArtifactRepositoryBean;
import org.apache.stratos.common.beans.cartridge.*;
import org.apache.stratos.common.beans.partition.NetworkPartitionBean;
import org.apache.stratos.common.beans.partition.NetworkPartitionReferenceBean;
import org.apache.stratos.common.beans.partition.PartitionReferenceBean;
import org.apache.stratos.common.beans.policy.autoscale.AutoscalePolicyBean;
import org.apache.stratos.common.beans.policy.deployment.DeploymentPolicyBean;
import org.apache.stratos.manager.dto.Cartridge;
import org.apache.stratos.rest.endpoint.bean.CartridgeInfoBean;
import org.apache.stratos.rest.endpoint.bean.autoscaler.partition.Partition;
import org.apache.stratos.rest.endpoint.bean.autoscaler.partition.PartitionGroup;
import org.apache.stratos.rest.endpoint.bean.autoscaler.policy.autoscale.AutoscalePolicy;
import org.apache.stratos.rest.endpoint.bean.autoscaler.policy.deployment.DeploymentPolicy;
import org.apache.stratos.rest.endpoint.bean.cartridge.definition.PropertyBean;
import org.apache.stratos.rest.endpoint.bean.subscription.domain.SubscriptionDomainBean;
import org.wso2.ppaas.tools.artifactmigration.exception.ArtifactLoadingException;
import org.wso2.ppaas.tools.artifactmigration.exception.TransformationException;
import org.wso2.ppaas.tools.artifactmigration.loader.Constants;
import org.wso2.ppaas.tools.artifactmigration.loader.OldArtifactLoader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Transforms the artifacts from PPaaS 4.0.0 to 4.1.0
 */
public class Transformation {

    private static final Logger log = Logger.getLogger(Transformation.class);
    private static Transformation instance = null;

    private Transformation() {
    }

    public static Transformation getInstance() {
        if (instance == null) {
            synchronized (Transformation.class) {
                if (instance == null) {
                    instance = new Transformation();
                }
            }
        }
        return instance;
    }

    /**
     * Method to transform Auto Scale Policies
     */
    public void transformAutoscalePolicyList() throws TransformationException {

        List<AutoscalePolicy> autoscalePolicy400List;
        AutoscalePolicyBean autoscalePolicy410 = new AutoscalePolicyBean();
        try {
            autoscalePolicy400List = OldArtifactLoader.getInstance().fetchAutoscalePolicyList();
            log.info("Fetched Auto Scale Policy from PPaaS 4.0.0");

            File directoryName = new File(Constants.ROOT_DIRECTORY + Constants.DIRECTORY_POLICY_AUTOSCALE);

            for (AutoscalePolicy autoscalePolicy : autoscalePolicy400List) {
                autoscalePolicy410.setId(autoscalePolicy.getId());
                writeFile(directoryName, autoscalePolicy410.getId() + ".json", getGson().toJson(autoscalePolicy410));
            }
            log.info("Created Auto Scale Policy 4.1.0 artifacts");

        } catch (JsonSyntaxException e) {
            String msg = "JSON syntax error in retrieving auto scale policies";
            log.error(msg);
            throw new TransformationException(msg, e);
        } catch (ArtifactLoadingException e) {
            String msg = "Artifact Loading error in fetching auto scale policies";
            log.error(msg);
            throw new TransformationException(msg, e);
        }
    }

    /**
     * Method to transform network partitions
     */
    public void transformNetworkPartitionList() throws TransformationException {

        List<Partition> networkPartition400List;
        NetworkPartitionBean networkPartition410 = new NetworkPartitionBean();
        File directoryName;
        try {
            networkPartition400List = OldArtifactLoader.getInstance().fetchPartitionList();
            log.info("Fetched Network Partition List from PPaaS 4.0.0");

            for (Partition networkPartition400 : networkPartition400List) {

                networkPartition410.setId(networkPartition400.getId());
                networkPartition410.setProvider(networkPartition400.getProvider());

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
                    networkPartition410.setProperties(property410List);
                }
                directoryName = new File(
                        Constants.ROOT_DIRECTORY + Constants.DIRECTORY_NETWORK_PARTITION + File.separator
                                + networkPartition400.getProvider());
                writeFile(directoryName, networkPartition410.getId() + ".json", getGson().toJson(networkPartition410));
            }
            log.info("Created Network Partition List 4.1.0 artifacts");
        } catch (JsonSyntaxException e) {
            String msg = "JSON syntax error in retrieving network partition lists";
            log.error(msg);
            throw new TransformationException(msg, e);
        } catch (ArtifactLoadingException e) {
            String msg = "Artifact loading error in fetching network partition lists";
            log.error(msg);
            throw new TransformationException(msg, e);
        }
    }

    /**
     * Method to transform DeploymentPolicy
     */
    public void transformDeploymentPolicyList() throws TransformationException {

        List<DeploymentPolicy> deploymentPolicy400List;
        DeploymentPolicyBean deploymentPolicy410 = new DeploymentPolicyBean();
        try {
            deploymentPolicy400List = OldArtifactLoader.getInstance().fetchDeploymentPolicyList();
            log.info("Fetched Deployment Policy from PPaaS 4.0.0");

            File directoryName = new File(Constants.ROOT_DIRECTORY + Constants.DIRECTORY_POLICY_DEPLOYMENT);

            for (DeploymentPolicy deploymentPolicy : deploymentPolicy400List) {

                deploymentPolicy410.setId(deploymentPolicy.getId());
                List<PartitionGroup> partitionGroup400List = deploymentPolicy.getPartitionGroup();
                List<NetworkPartitionReferenceBean> networkPartitions410List = new ArrayList<>();
                int a = 0;
                for (PartitionGroup partitionGroup : partitionGroup400List) {

                    NetworkPartitionReferenceBean tempNetworkPartition = new NetworkPartitionReferenceBean();

                    tempNetworkPartition.setId(partitionGroup.getId());
                    tempNetworkPartition.setPartitionAlgo(partitionGroup.getPartitionAlgo());

                    List<Partition> partition400List = partitionGroup.getPartition();
                    List<PartitionReferenceBean> partitions410List = new ArrayList<>();

                    int b = 0;
                    for (Partition partition : partition400List) {
                        PartitionReferenceBean tempPartition = new PartitionReferenceBean();
                        tempPartition.setId(partition.getId());
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
                        partitions410List.add(b++, tempPartition);
                    }
                    tempNetworkPartition.setPartitions(partitions410List);
                    networkPartitions410List.add(a, tempNetworkPartition);
                }
                deploymentPolicy410.setNetworkPartitions(networkPartitions410List);

                writeFile(directoryName, deploymentPolicy410.getId() + ".json", getGson().toJson(deploymentPolicy410));
            }
            log.info("Created Deployment Policy 4.1.0 artifacts");
        } catch (JsonSyntaxException e) {
            String msg = "JSON syntax error in retrieving deployment policies";
            log.error(msg);
            throw new TransformationException(msg, e);

        } catch (ArtifactLoadingException e) {
            String msg = "Artifact Loading error in fetching deployment policies";
            log.error(msg);
            throw new TransformationException(msg, e);
        }
    }

    /**
     * Method to transform cartridge list
     */
    public void transformCartridgeList() throws TransformationException {
        List<Cartridge> cartridge400List;
        List<CartridgeInfoBean> subscription400List;
        ApplicationBean application410 = new ApplicationBean();
        CartridgeBean cartridge410 = new CartridgeBean();

        try {
            cartridge400List = OldArtifactLoader.getInstance().fetchCartridgeList();
            log.info("Fetched Cartridge List from PPaaS 4.0.0");

            subscription400List = OldArtifactLoader.getInstance().fetchSubscriptionDataList();
            log.info("Fetched Subscription List");

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

                        ArtifactRepositoryBean artifactRepository = new ArtifactRepositoryBean();

                        artifactRepository.setAlias(subscription.getAlias());
                        artifactRepository.setPrivateRepo(subscription.isPrivateRepo());
                        artifactRepository.setRepoUrl(subscription.getRepoURL());
                        artifactRepository.setRepoUsername(subscription.getRepoUsername());
                        artifactRepository.setRepoPassword(subscription.getRepoPassword());

                        signup410List.add(a++, artifactRepository);

                        List<SubscriptionDomainBean> domainMapping400 = OldArtifactLoader.getInstance()
                                .fetchDomainMappingList(cartridge.getCartridgeType(), subscription.getAlias());

                        for (SubscriptionDomainBean domainMapping : domainMapping400) {
                            DomainMappingBean domainMappingBean = new DomainMappingBean();
                            domainMappingBean.setCartridgeAlias(cartridge.getCartridgeAlias());
                            domainMappingBean.setDomainName(domainMapping.getDomainName());
                            domainMappingBean.setContextPath(domainMapping.getApplicationContext());
                            domainMapping410List.add(domainMappingIterator++, domainMappingBean);
                        }
                    }
                }
                cartridgeReference410.setSubscribableInfo(subscribableInfo);
                cartridgeReference410.setType(cartridge.getCartridgeType());

                cartridges.add(0, cartridgeReference410);
                components.setCartridges(cartridges);
                application410.setComponents(components);
                application410.setName(cartridge.getDisplayName());
                application410.setDescription(cartridge.getDescription());

                File outputDirectoryNameApp = new File(
                        Constants.ROOT_DIRECTORY + Constants.DIRECTORY_APPLICATION + File.separator + application410
                                .getName() + File.separator + "artifacts");
                writeFile(outputDirectoryNameApp, application410.getName() + ".json", getGson().toJson(application410));
                ConversionTool.getInstance().addScriptDirectory(
                        Constants.ROOT_DIRECTORY + Constants.DIRECTORY_OUTPUT_SCRIPT + File.separator + application410
                                .getName());
                writeFile(outputDirectoryNameApp, "application-signup.json", getGson().toJson(signup410List));
                writeFile(outputDirectoryNameApp, "domain-mapping.json", getGson().toJson(domainMapping410List));

                cartridge410.setDisplayName(cartridge.getDisplayName());
                cartridge410.setDescription(cartridge.getDescription());
                cartridge410.setType(cartridge.getCartridgeType());
                cartridge410.setProvider(cartridge.getProvider());
                cartridge410.setVersion(cartridge.getVersion());
                cartridge410.setHost(cartridge.getHostName());
                cartridge410.setMultiTenant(cartridge.isMultiTenant());

                if (cartridge.getPortMappings() != null) {

                    PortMapping[] portMapping400List = cartridge.getPortMappings();
                    List<PortMappingBean> portMapping410List = new ArrayList<>();

                    int b = 0;
                    for (PortMapping portMapping : portMapping400List) {

                        PortMappingBean portMappingBeanTemp = new PortMappingBean();
                        portMappingBeanTemp.setPort(Integer.parseInt(portMapping.getPort()));
                        portMappingBeanTemp.setProtocol(portMapping.getProtocol());
                        portMappingBeanTemp.setProxyPort(Integer.parseInt(portMapping.getProxyPort()));

                        portMapping410List.add(b++, portMappingBeanTemp);
                    }
                    cartridge410.setPortMapping(portMapping410List);
                }
                if (cartridge.getPersistence() != null) {
                    Persistence persistence400 = cartridge.getPersistence();
                    PersistenceBean persistenceBean410 = new PersistenceBean();

                    persistenceBean410.setRequired(persistence400.isPersistanceRequired());

                    Volume[] volume400Array = persistence400.getVolumes();
                    List<VolumeBean> volumeBean410List = new ArrayList<>();

                    int b = 0;
                    for (Volume volume : volume400Array) {

                        VolumeBean volumeBeanTemp = new VolumeBean();
                        volumeBeanTemp.setId(volume.getId());
                        volumeBeanTemp.setSize(String.valueOf(volume.getSize()));
                        volumeBeanTemp.setMappingPath(volume.getMappingPath());
                        volumeBeanTemp.setDevice(volume.getDevice());
                        volumeBeanTemp.setRemoveOnTermination(volume.isRemoveOntermination());

                        volumeBean410List.add(b++, volumeBeanTemp);
                    }
                    persistenceBean410.setVolume(volumeBean410List);
                    cartridge410.setPersistence(persistenceBean410);
                }
                writeFile(outputDirectoryNameCartridge, cartridge410.getDisplayName() + ".json",
                        getGson().toJson(application410));
            }
            log.info("Created Cartridge List 4.1.0 artifacts");
            log.info("Created Application List 4.1.0 artifacts");

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
     * Method to get Gson
     */
    public Gson getGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        return gsonBuilder.setPrettyPrinting().create();
    }

    /**
     * Method to write to a file
     *
     * @param directoryName Output directory name
     * @param fileName      file name
     * @param json          json string
     */
    public void writeFile(File directoryName, String fileName, String json) {
        try {
            if (!directoryName.exists()) {
                directoryName.mkdirs();
            }
            FileWriter writer = new FileWriter(new File(directoryName.getPath() + File.separator + fileName), false);
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            log.error("Error in writing to JSON files", e);
        }
    }
}
