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
import org.apache.stratos.common.beans.application.ApplicationBean;
import org.apache.stratos.common.beans.application.ComponentBean;
import org.apache.stratos.common.beans.application.SubscribableInfo;
import org.apache.stratos.common.beans.cartridge.*;
import org.apache.stratos.common.beans.partition.NetworkPartitionBean;
import org.apache.stratos.common.beans.partition.NetworkPartitionReferenceBean;
import org.apache.stratos.common.beans.partition.PartitionReferenceBean;
import org.apache.stratos.common.beans.policy.autoscale.AutoscalePolicyBean;
import org.apache.stratos.common.beans.policy.deployment.DeploymentPolicyBean;
import org.apache.stratos.manager.dao.PortMapping;
import org.apache.stratos.manager.dto.Cartridge;
import org.apache.stratos.manager.dto.Persistence;
import org.apache.stratos.manager.dto.Volume;
import org.apache.stratos.rest.endpoint.bean.autoscaler.partition.Partition;
import org.apache.stratos.rest.endpoint.bean.autoscaler.partition.PartitionGroup;
import org.apache.stratos.rest.endpoint.bean.autoscaler.policy.autoscale.AutoscalePolicy;
import org.apache.stratos.rest.endpoint.bean.autoscaler.policy.deployment.DeploymentPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ppaas.tools.artifactmigration.loader.Constants;
import org.wso2.ppaas.tools.artifactmigration.loader.OldArtifactLoader;
import org.wso2.ppaas.tools.artifactmigration.loader.TemplateLoader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Transforms the artifacts from PPaaS 4.0.0 to 4.1.0
 */
public class Transformation {

    private static final Logger log = LoggerFactory.getLogger(Transformation.class);

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
    public void transformAutoscalePolicyList() {

        List<AutoscalePolicy> autoscalePolicy400List;
        AutoscalePolicyBean autoscalePolicy410Template;

        try {

            //Retrieving Json files of PPaaS 4.0.0
            autoscalePolicy400List = OldArtifactLoader.getInstance().fetchAutoscalePolicyList();
            log.info("Fetched Auto Scale Policy from PPaaS 4.0.0");

            //Retrieving the template
            autoscalePolicy410Template = TemplateLoader.getInstance()
                    .fetchTemplate(Constants.ROOT_TEMPLATE_DIRECTORY + Constants.DIRECTORY_TEMPLATE_POLICY_AUTOSCALE,
                            AutoscalePolicyBean.class);
            log.info("Fetched Auto Scale Policy Template");

            //Modifying the template according to the values of PPaaS 4.0.0
            File directoryName = new File(Constants.ROOT_DIRECTORY + Constants.DIRECTORY_POLICY_AUTOSCALE);

            for (AutoscalePolicy autoscalePolicy : autoscalePolicy400List) {

                autoscalePolicy410Template.setId(autoscalePolicy.getId());

                String json = new GsonBuilder().setPrettyPrinting().create().toJson(autoscalePolicy410Template);
                String fileName = autoscalePolicy410Template.getId();

                writeFile(directoryName, fileName, json);
            }
            log.info("Created Auto Scale Policy 4.1.0 artifacts");

        } catch (JsonSyntaxException e) {
            log.error("JSON syntax exception in fetching auto scale policies", e);
        } catch (IOException e) {
            log.error("IO exception in fetching auto scale policies", e);
        }
    }

    /**
     * Method to transform newtork partitions
     */
    public void transformNetworkPartitionList() {

        List<Partition> networkPartition400List;
        NetworkPartitionBean networkPartition410Template;

        try {
            //Retrieving Json files of PPaaS 4.0.0
            networkPartition400List = OldArtifactLoader.getInstance().fetchPartitionList();
            log.info("Fetched Newtork Partition List from PPaaS 4.0.0");

            //Retrieving the template
            networkPartition410Template = TemplateLoader.getInstance()
                    .fetchTemplate(Constants.ROOT_TEMPLATE_DIRECTORY + Constants.DIRECTORY_TEMPLATE_NETWORK_PARTITION,
                            NetworkPartitionBean.class);
            log.info("Fetched NetworkPartitionList Template");

            //Modifying the template according to the values of PPaaS 4.0.0
            for (Partition networkPartition400 : networkPartition400List) {

                networkPartition410Template.setId(networkPartition400.id);
                networkPartition410Template.setProvider(networkPartition400.provider);

                List<org.apache.stratos.rest.endpoint.bean.cartridge.definition.PropertyBean> property400List = networkPartition400.property;
                List<org.apache.stratos.common.beans.PropertyBean> property410List = new ArrayList<org.apache.stratos.common.beans.PropertyBean>();

                
                for (org.apache.stratos.rest.endpoint.bean.cartridge.definition.PropertyBean temp : property400List) {
                    org.apache.stratos.common.beans.PropertyBean property = new org.apache.stratos.common.beans.PropertyBean();
                    property.setName(temp.name);
                    property.setValue(temp.value);
                    property410List.add(property);
                }

                networkPartition410Template.setProperties(property410List);

                String json = getGson().toJson(networkPartition410Template);
                String fileName = networkPartition410Template.getId();
                File directoryName = new File(
                        Constants.ROOT_DIRECTORY + Constants.DIRECTORY_NETWORK_PARTITION + File.separator
                                + networkPartition400.provider);

                writeFile(directoryName, fileName, json);

            }

            log.info("Created Network Partition List 4.1.0 artifacts");

        } catch (JsonSyntaxException e) {
            log.error("JSON syntax exception in fetching network partition lists", e);
        } catch (IOException e) {
            log.error("IO exception in fetching network partition lists", e);
        }

    }

    /**
     * Method to transform DeploymentPolicy
     */
    public void transformDeploymentPolicyList() {

        List<DeploymentPolicy> deploymentPolicy400List;
        DeploymentPolicyBean deploymentPolicy410Template;

        try {

            //Retrieving Json files of PPaaS 4.0.0
            deploymentPolicy400List = OldArtifactLoader.getInstance().fetchDeploymentPolicyList();
            log.info("Fetched Deployment Policy from PPaaS 4.0.0");

            deploymentPolicy410Template = TemplateLoader.getInstance()
                    .fetchTemplate(Constants.ROOT_TEMPLATE_DIRECTORY + Constants.DIRECTORY_TEMPLATE_POLICY_DEPLOYMENT,
                            DeploymentPolicyBean.class);
            log.info("Fetched Deployment Policy Template");

            File directoryName = new File(Constants.ROOT_DIRECTORY + Constants.DIRECTORY_POLICY_DEPLOYMENT);
            for (DeploymentPolicy deploymentPolicy : deploymentPolicy400List) {

                deploymentPolicy410Template.setId(deploymentPolicy.id);

                List<PartitionGroup> partitionGroup400List = deploymentPolicy.partitionGroup;
                List<NetworkPartitionReferenceBean> networkPartitions410List = new ArrayList<NetworkPartitionReferenceBean>();

                int a = 0;
                for (PartitionGroup partitionGroup : partitionGroup400List) {

                    NetworkPartitionReferenceBean tempNetworkPartition = new NetworkPartitionReferenceBean();

                    tempNetworkPartition.setId(partitionGroup.id);
                    tempNetworkPartition.setPartitionAlgo(partitionGroup.partitionAlgo);
                    networkPartitions410List.add(a, tempNetworkPartition);

                    List<Partition> partition400List = partitionGroup.partition;
                    List<PartitionReferenceBean> partitions410List = new ArrayList<PartitionReferenceBean>();

                    int b = 0;
                    for (Partition partition : partition400List) {

                        PartitionReferenceBean tempPartition = new PartitionReferenceBean();
                        tempPartition.setId(partition.id);

                        partitions410List.add(b++, tempPartition);
                    }

                    networkPartitions410List.get(a).setPartitions(partitions410List);
                    a++;
                }

                deploymentPolicy410Template.setNetworkPartitions(networkPartitions410List);

                //writing to JSON file
                String json = new GsonBuilder().setPrettyPrinting().create().toJson(deploymentPolicy410Template);
                String fileName = deploymentPolicy410Template.getId();

                writeFile(directoryName, fileName, json);
            }
            log.info("Created Deployment Policy 4.1.0 artifacts");

        } catch (JsonSyntaxException e) {
            log.error("JSON syntax exception in fetching deployment policies", e);
        } catch (IOException e) {
            log.error("IO exception in fetching deployment policies", e);
        }

    }

    /**
     * Method to transform cartridge list
     */
    public void transformCartridgeList() {

        List<Cartridge> cartridge400List;
        ApplicationBean application410Template;
        CartridgeBean cartridge410Template;

        try {
            //Retrieving Json files of PPaaS 4.0.0
            cartridge400List = OldArtifactLoader.getInstance().fetchCartridgeList();
            log.info("Fetched Cartridge List from PPaaS 4.0.0");

            //Retrieving the template
            application410Template = TemplateLoader.getInstance()
                    .fetchTemplate(Constants.ROOT_TEMPLATE_DIRECTORY + Constants.DIRECTORY_TEMPLATE_APPLICATION,
                            ApplicationBean.class);
            log.info("Fetched Application Template");

            //Retrieving the template
            cartridge410Template = TemplateLoader.getInstance()
                    .fetchTemplate(Constants.ROOT_TEMPLATE_DIRECTORY + Constants.DIRECTORY_TEMPLATE_CARTRIDGE,
                            CartridgeBean.class);
            log.info("Fetched Cartridge List Template");

            //Creating Applications
            File outputDirectoryNameApp = new File(Constants.ROOT_DIRECTORY + Constants.DIRECTORY_APPLICATION);

            //Creating CartridgesAPPLICATION
            File outputDirectoryNameCartridge = new File(Constants.ROOT_DIRECTORY + Constants.DIRECTORY_CARTRIDGE);

            for (Cartridge cartridge : cartridge400List) {

                ComponentBean components = new ComponentBean();
                List<CartridgeReferenceBean> cartridges = new ArrayList<CartridgeReferenceBean>();
                CartridgeReferenceBean cartridge410 = new CartridgeReferenceBean();
                SubscribableInfo subscribableInfo = new SubscribableInfo();

                subscribableInfo.setAlias(cartridge.getCartridgeAlias());
                subscribableInfo.setAutoscalingPolicy(cartridge.getDefaultAutoscalingPolicy());

                cartridge410.setSubscribableInfo(subscribableInfo);
                cartridge410.setType(cartridge.getCartridgeType());

                cartridges.add(0, cartridge410);
                components.setCartridges(cartridges);
                application410Template.setComponents(components);
                application410Template.setName(cartridge.getDisplayName());
                application410Template.setDescription(cartridge.getDescription());

                //writing to JSON file
                String json = getGson().toJson(application410Template);
                String fileName = application410Template.getName();
                writeFile(outputDirectoryNameApp, fileName, json);

                cartridge410Template.setType(cartridge.getCartridgeType());
                cartridge410Template.setProvider(cartridge.getProvider());
                cartridge410Template.setHost(cartridge.getHostName());
                cartridge410Template.setDisplayName(cartridge.getDisplayName());
                cartridge410Template.setDescription(cartridge.getDescription());
                cartridge410Template.setVersion(cartridge.getVersion());
                cartridge410Template.setMultiTenant(cartridge.isMultiTenant());

                List<PortMapping> portMapping400List = new ArrayList<PortMapping>();
                List<PortMappingBean> portMapping410List = new ArrayList<PortMappingBean>();

                int a = 0;
                for (PortMapping portMapping : portMapping400List) {

                    PortMappingBean portMappingBeanTemp = new PortMappingBean();
                    portMappingBeanTemp.setPort(Integer.parseInt(portMapping.getPrimaryPort()));
                    portMappingBeanTemp.setProxyPort(Integer.parseInt(portMapping.getProxyPort()));

                    portMapping410List.add(a++, portMappingBeanTemp);

                }

                cartridge410Template.setPortMapping(portMapping410List);

                Persistence persistence400 = new Persistence();
                PersistenceBean persistenceBean410 = new PersistenceBean();
                persistenceBean410.setRequired(persistence400.isRequired());

                List<Volume> volume400List = new ArrayList<Volume>();
                List<VolumeBean> volumeBean410List = new ArrayList<VolumeBean>();

                int b = 0;
                for (Volume volume : volume400List) {

                    VolumeBean volumeBeanTemp = new VolumeBean();
                    volumeBeanTemp.setSize(String.valueOf(volume.getSize()));
                    volumeBeanTemp.setMappingPath(volume.getMappingPath());
                    volumeBeanTemp.setDevice(volume.getDevice());
                    volumeBeanTemp.setRemoveOnTermination(volume.isRemoveOnTermination());

                    volumeBean410List.add(b++, volumeBeanTemp);

                }

                persistenceBean410.setVolume(volumeBean410List);
                cartridge410Template.setPersistence(persistenceBean410);

                //writing to JSON file
                String jsonCart = getGson().toJson(application410Template);
                String fileNamejsonCart = cartridge410Template.getDisplayName();
                writeFile(outputDirectoryNameCartridge, fileNamejsonCart, jsonCart);

            }

            log.info("Created Cartridge List 4.1.0 artifacts");
            log.info("Created Application List 4.1.0 artifacts");

        } catch (JsonSyntaxException e) {
            log.error("JSON syntax exception in fetching cartridges", e);
        } catch (IOException e) {
            log.error("IO exception in fetching cartridges", e);
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
            log.error("IO exception in writing to JSON files", e);
        }

    }
}