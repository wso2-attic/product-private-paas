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
package org.wso2.ppaas.tools.artifactmigration.loader;

import com.google.gson.Gson;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.stratos.common.beans.application.ApplicationBean;
import org.apache.stratos.common.beans.partition.NetworkPartitionBean;
import org.apache.stratos.common.beans.policy.autoscale.AutoscalePolicyBean;
import org.apache.stratos.common.beans.policy.deployment.DeploymentPolicyBean;
import org.wso2.ppaas.tools.artifactmigration.config.Configuration;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Loads the default artifacts from the template
 */
public class TemplateLoader {

    private static final Logger logger = Logger.getLogger(TemplateLoader.class);
    private static TemplateLoader instance = null;

    private TemplateLoader() {
    }

    public static TemplateLoader getInstance() {
        if (instance == null){
            synchronized (TemplateLoader.class){
                if (instance == null){
                    instance = new TemplateLoader();
                }
            }
        }

        return instance;
    }


    public NetworkPartitionBean fetchNetworkPartitionTemplate() {

        NetworkPartitionBean networkPartition = null;
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(
                    Configuration.ROOT_TEMPLATE_DIRECTORY + Configuration.DIRECTORY_TEMPLATE_NETWORK_PARTITION));
            networkPartition = new Gson().fromJson(br, NetworkPartitionBean.class);
            br.close();
        } catch (FileNotFoundException e) {
            logger.log(Level.ERROR, e.getMessage());
        } catch (IOException e) {
            logger.log(Level.ERROR, e.getMessage());
        }
        return networkPartition;
    }

    public AutoscalePolicyBean fetchAutoscalePolicy() {

        AutoscalePolicyBean autoscalePolicy = null;
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(
                    Configuration.ROOT_TEMPLATE_DIRECTORY + Configuration.DIRECTORY_TEMPLATE_POLICY_AUTOSCALE));
            autoscalePolicy = new Gson().fromJson(br, AutoscalePolicyBean.class);
            br.close();
        } catch (FileNotFoundException e) {
            logger.log(Level.ERROR, e.getMessage());
        } catch (IOException e) {
            logger.log(Level.ERROR, e.getMessage());
        }
        return autoscalePolicy;

    }

    //Method to fetch DeploymentPolicy
    public DeploymentPolicyBean fetchDeploymentPolicy() {

        DeploymentPolicyBean deploymentPolicy = null;
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(
                    Configuration.ROOT_TEMPLATE_DIRECTORY + Configuration.DIRECTORY_TEMPLATE_POLICY_DEPLOYMENT));
            deploymentPolicy = new Gson().fromJson(br, DeploymentPolicyBean.class);
            br.close();
        } catch (FileNotFoundException e) {
            logger.log(Level.ERROR, e.getMessage());
        } catch (IOException e) {
            logger.log(Level.ERROR, e.getMessage());
        }
        return deploymentPolicy;

    }

    public ApplicationBean fetchApplication() {

        ApplicationBean application = null;
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(
                    Configuration.ROOT_TEMPLATE_DIRECTORY + Configuration.DIRECTORY_TEMPLATE_APPLICATION));
            application = new Gson().fromJson(br, ApplicationBean.class);
            br.close();
        } catch (FileNotFoundException e) {
            logger.log(Level.ERROR, e.getMessage());
        } catch (IOException e) {
            logger.log(Level.ERROR, e.getMessage());
        }
        return application;
    }

}
