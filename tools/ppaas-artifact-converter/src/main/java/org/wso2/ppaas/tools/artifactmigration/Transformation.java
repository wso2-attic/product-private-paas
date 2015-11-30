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

import com.google.gson.GsonBuilder;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.stratos.common.Properties;
import org.apache.stratos.common.beans.application.ApplicationBean;
import org.apache.stratos.common.beans.application.ComponentBean;
import org.apache.stratos.common.beans.application.SubscribableInfo;
import org.apache.stratos.common.beans.cartridge.CartridgeReferenceBean;
import org.apache.stratos.common.beans.partition.NetworkPartitionBean;
import org.apache.stratos.common.beans.partition.NetworkPartitionReferenceBean;
import org.apache.stratos.common.beans.partition.PartitionReferenceBean;
import org.apache.stratos.common.beans.policy.autoscale.AutoscalePolicyBean;
import org.apache.stratos.common.beans.policy.deployment.DeploymentPolicyBean;
import org.apache.stratos.manager.dto.Cartridge;
import org.apache.stratos.rest.endpoint.bean.autoscaler.partition.Partition;
import org.apache.stratos.rest.endpoint.bean.autoscaler.partition.PartitionGroup;
import org.apache.stratos.rest.endpoint.bean.autoscaler.policy.autoscale.AutoscalePolicy;
import org.apache.stratos.rest.endpoint.bean.autoscaler.policy.deployment.DeploymentPolicy;
import org.wso2.ppaas.tools.artifactmigration.config.Configuration;
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

    private static final Logger logger = Logger.getLogger(Transformation.class);
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

    public void transformAutoscalePolicyList() {

        List<AutoscalePolicy> autoscalePolicy400 = null;
        AutoscalePolicyBean autoscalePolicy410 = null;

        //Retrieving Json files of PPaaS 4.0.0
        try {
            autoscalePolicy400 = OldArtifactLoader.getInstance().fetchAutoscalePolicyList();
        } catch (Exception e) {
            //TODO:logger.error();
            logger.log(Level.ERROR, e.getMessage());
        }

        //Retrieving the template
        try {
            autoscalePolicy410 = TemplateLoader.getInstance().fetchAutoscalePolicy();
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage());
        }

        //Modifying the template according to the values of PPaaS 4.0.0
        for (int j = 0; j < autoscalePolicy400.size(); j++) {

            autoscalePolicy410.setId(autoscalePolicy400.get(j).getId());

            String json = new GsonBuilder().setPrettyPrinting().create().toJson(autoscalePolicy410);
            String fileName = "autoscaling-policy-" + (j + 1);
            File directoryName = new File(Configuration.ROOT_DIRECTORY + Configuration.DIRECTORY_POLICY_AUTOSCALE);

            writeFile(directoryName, fileName, json);

        }

    }

    //Method to transform newtork partitions
    public void transformNetworkPartitionList() {

        List<Partition> networkPartition400 = null;
        NetworkPartitionBean networkPartition410 = null;

        //Retrieving Json files of PPaaS 4.0.0
        try {
            networkPartition400 = OldArtifactLoader.getInstance().fetchPartitionList();
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage());
        }

        //Retrieving the template
        try {
            networkPartition410 = TemplateLoader.getInstance().fetchNetworkPartitionTemplate();
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage());
        }

        //Modifying the template according to the values of PPaaS 4.0.0
        for (int j = 0; j < networkPartition400.size(); j++) {

            networkPartition410.setId(networkPartition400.get(j).id);
            networkPartition410.setProvider(networkPartition400.get(j).provider);

            List<org.apache.stratos.rest.endpoint.bean.cartridge.definition.PropertyBean> property400 = networkPartition400
                    .get(j).property;
            List<org.apache.stratos.common.beans.PropertyBean> property410 = new ArrayList<org.apache.stratos.common.beans.PropertyBean>();

            Properties properties = new Properties();
            for (int i = 0; i < property400.size(); i++) {
                org.apache.stratos.common.beans.PropertyBean property = new org.apache.stratos.common.beans.PropertyBean();
                property.setName(property400.get(i).name);
                property.setValue(property400.get(i).value);
                property410.add(property);
            }

            networkPartition410.setProperties(property410);

            String json = new GsonBuilder().setPrettyPrinting().create().toJson(networkPartition410);
            String fileName = "network-partition-" + (j + 1);
            File directoryName = new File(
                    Configuration.ROOT_DIRECTORY + Configuration.DIRECTORY_NETWORK_PARTITION + "/" + networkPartition400
                            .get(j).provider);

            writeFile(directoryName, fileName, json);

        }

    }

    //Method to transform DeploymentPolicy
    public void transformDeploymentPolicyList() {

        List<DeploymentPolicy> deploymentPolicy400 = null;
        DeploymentPolicyBean deploymentPolicy410 = null;

        //Retrieving Json files of PPaaS 4.0.0
        try {
            deploymentPolicy400 = OldArtifactLoader.getInstance().fetchDeploymentPolicyList();
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage());
        }

        //Retrieving the template
        try {
            deploymentPolicy410 = TemplateLoader.getInstance().fetchDeploymentPolicy();
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage());
        }

        for (int j = 0; j < deploymentPolicy400.size(); j++) {

            deploymentPolicy410.setId(deploymentPolicy400.get(j).id);

            List<PartitionGroup> partitionGroup400 = deploymentPolicy400.get(j).partitionGroup;
            List<NetworkPartitionReferenceBean> networkPartitions410 = new ArrayList<NetworkPartitionReferenceBean>();

            for (int a = 0; a < partitionGroup400.size(); a++) {

                NetworkPartitionReferenceBean tempNetworkPartition = new NetworkPartitionReferenceBean();

                tempNetworkPartition.setId(partitionGroup400.get(a).id);
                tempNetworkPartition.setPartitionAlgo(partitionGroup400.get(a).partitionAlgo);
                networkPartitions410.add(a, tempNetworkPartition);

                List<Partition> partition400 = partitionGroup400.get(a).partition;
                List<PartitionReferenceBean> partitions410 = new ArrayList<PartitionReferenceBean>();

                for (int b = 0; b < partition400.size(); b++) {

                    PartitionReferenceBean tempPartition = new PartitionReferenceBean();
                    tempPartition.setId(partition400.get(b).id);

                    partitions410.add(b, tempPartition);

                }

                networkPartitions410.get(a).setPartitions(partitions410);

            }
            deploymentPolicy410.setNetworkPartitions(networkPartitions410);

            //writing to JSON file
            String json = new GsonBuilder().setPrettyPrinting().create().toJson(deploymentPolicy410);
            String fileName = "deployment-policy-" + (j + 1);
            File directoryName = new File(Configuration.ROOT_DIRECTORY + Configuration.DIRECTORY_POLICY_DEPLOYMENT);

            writeFile(directoryName, fileName, json);

        }

    }

    public void transformCartridgeList() {

        List<Cartridge> cartridge400 = null;
        ApplicationBean application410 = null;

        //Retrieving Json files of PPaaS 4.0.0
        try {
            cartridge400 = OldArtifactLoader.getInstance().fetchCartridgeList();
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage());
        }

        //Retrieving the template
        try {
            application410 = TemplateLoader.getInstance().fetchApplication();
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage());
        }

        for (int j = 0; j < cartridge400.size(); j++) {

            ApplicationBean application = new ApplicationBean();
            ComponentBean components = new ComponentBean();
            List<CartridgeReferenceBean> cartridges = new ArrayList<CartridgeReferenceBean>();
            CartridgeReferenceBean cartridge410 = new CartridgeReferenceBean();
            SubscribableInfo subscribableInfo = new SubscribableInfo();

            subscribableInfo.setAlias(cartridge400.get(j).getCartridgeAlias());
            subscribableInfo.setAutoscalingPolicy(cartridge400.get(j).getDefaultAutoscalingPolicy());

            cartridge410.setSubscribableInfo(subscribableInfo);
            cartridge410.setType(cartridge400.get(j).getCartridgeType());
            cartridges.add(0, cartridge410);
            components.setCartridges(cartridges);
            application.setComponents(components);
            application.setName(cartridge400.get(j).getDisplayName());
            application.setDescription(cartridge400.get(j).getDescription());

            //writing to JSON file
            String json = new GsonBuilder().setPrettyPrinting().create().toJson(application);
            String fileName = "application-" + (j + 1);

            File directoryName = new File(Configuration.ROOT_DIRECTORY + Configuration.DIRECTORY_APPLICATION);
            writeFile(directoryName, fileName, json);

        }

    }

    public void writeFile(File directoryName, String fileName, String json) {
        try {

            if (!directoryName.exists()) {
                directoryName.mkdirs();
            }

            FileWriter writer = new FileWriter(new File(directoryName.getPath() + "/" + fileName), false);

            writer.write(json);
            writer.close();

        } catch (IOException e) {
            logger.log(Level.ERROR, e.getMessage());
        }

    }
}
