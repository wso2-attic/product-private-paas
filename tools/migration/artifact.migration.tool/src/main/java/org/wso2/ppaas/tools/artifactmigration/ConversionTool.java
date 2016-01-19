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

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.stratos.common.beans.application.SubscribableInfo;
import org.wso2.ppaas.tools.artifactmigration.exception.TransformationException;

import java.io.*;
import java.util.List;
import java.util.Map;

/*
Class for conversion
 */
class ConversionTool {

    private static final Logger log = Logger.getLogger(ConversionTool.class);
    private static Map<String, List<String>> memoryMap = Transformer.getMemoryMap();

    /**
     * Method to handle console inputs
     */
    public static void handleConsoleInputs() {

        log.info("CLI started...");

        validateConfigurationInputs(Constants.BASE_URL400, "Base URL of PPaaS 4.0.0:");
        validateConfigurationInputs(Constants.USERNAME400, "User name of PPaaS 4.0.0:");
        validateConfigurationInputs(Constants.PASSWORD400, "Password of PPaaS 4.0.0:");
        validateConfigurationInputs(Constants.BASE_URL410, "Base URL of PPaaS 4.1.0:");
        validateConfigurationInputs(Constants.USERNAME410, "User name of PPaaS 4.1.0:");
        validateConfigurationInputs(Constants.PASSWORD410, "Password of PPaaS 4.1.0:");
        validateConfigurationInputs(Constants.IAAS, "IaaS provider name:");
        validateConfigurationInputs(Constants.IAAS_IMAGE_ID, "IaaS image id:");
        validateConfigurationInputs(Constants.NETWORK_UUID, "Network UUID:");
    }

    private static void validateConfigurationInputs(String propertyConstant, String propertyName) {

        Console console = System.console();
        if (System.getProperty(propertyConstant) == null || System.getProperty(propertyConstant).isEmpty()) {
            System.out.print("Enter the " + propertyName);
            if (propertyName.contains("Password")) {
                char[] passwordChars = console.readPassword();
                System.setProperty(propertyConstant, new String(passwordChars));
            } else {
                System.setProperty(propertyConstant, console.readLine());
            }
            System.out.println();
        }
    }

    /**
     * Method to start transformation
     */
    public static void startTransformation() throws TransformationException {

        log.info("Artifact Migration started...");

        Transformer.transformNetworkPartitionList();
        Transformer.transformAutoscalePolicyList();
        Transformer.transformDeploymentPolicyList();
        Transformer.waitForThreadTermination();

        Transformer.addDefaultApplicationPolicies();
        Transformer.transformCartridgeList();
        log.info("Conversion completed successfully");
        System.out.println(
                "Default values have been used for the port mappings of the cartridges and can be updated in conf/config.properties file.");
    }

    /**
     * Method to add script directories specific to each IaaS
     *
     * @param outputLocation output location of the script directories
     */
    private static void addIaasScriptDirectories(String outputLocation) {

        File sourceLocationEc2 = new File(Constants.DIRECTORY_SOURCE_SCRIPT + Constants.DIRECTORY_SOURCE_SCRIPT_EC2);
        File sourceLocationGce = new File(Constants.DIRECTORY_SOURCE_SCRIPT + Constants.DIRECTORY_SOURCE_SCRIPT_GCE);
        File sourceLocationKub = new File(
                Constants.DIRECTORY_SOURCE_SCRIPT + Constants.DIRECTORY_SOURCE_SCRIPT_KUBERNETES);
        File sourceLocationMock = new File(Constants.DIRECTORY_SOURCE_SCRIPT + Constants.DIRECTORY_SOURCE_SCRIPT_MOCK);
        File sourceLocationOS = new File(
                Constants.DIRECTORY_SOURCE_SCRIPT + Constants.DIRECTORY_SOURCE_SCRIPT_OPENSTACK);
        File targetLocation = new File(outputLocation);
        try {
            FileUtils.copyDirectoryToDirectory(sourceLocationEc2, targetLocation);
            FileUtils.copyDirectoryToDirectory(sourceLocationGce, targetLocation);
            FileUtils.copyDirectoryToDirectory(sourceLocationKub, targetLocation);
            FileUtils.copyDirectoryToDirectory(sourceLocationMock, targetLocation);
            FileUtils.copyDirectoryToDirectory(sourceLocationOS, targetLocation);
        } catch (IOException e) {
            log.error("Error in copying scripts directory ", e);
        }
    }

    /**
     * Method to add common deploying script
     *
     * @param outputLocation   output location of the script
     * @param subscribableInfo subscribable information
     * @param cartridgeName    cartridge name
     */
    public static void addCommonDeployingScript(String outputLocation, SubscribableInfo subscribableInfo,
            String cartridgeName) {
        BufferedReader reader = null;
        FileWriter writer = null;
        try {
            File file = new File(Constants.DIRECTORY_SOURCE_SCRIPT + Constants.DIRECTORY_SOURCE_SCRIPT_DEPLOY);
            reader = new BufferedReader(new FileReader(file));
            String line, scriptText = "";
            while ((line = reader.readLine()) != null) {
                scriptText += line + System.getProperty("line.separator");
            }

            if (subscribableInfo.getDeploymentPolicy() != null) {
                scriptText = scriptText.replaceAll("deployment-policy_name", subscribableInfo.getDeploymentPolicy());
                String applicationPolicyName = Transformer.getDeploymentPolicyToApplicationPolicyMap()
                        .get(subscribableInfo.getDeploymentPolicy());
                scriptText = scriptText.replaceAll("application-policy_name", applicationPolicyName);
            }
            if (subscribableInfo.getAutoscalingPolicy() != null) {
                scriptText = scriptText.replaceAll("autoscaling-policy_name", subscribableInfo.getAutoscalingPolicy());
            }
            if (cartridgeName != null) {
                scriptText = scriptText.replaceAll("cartridge_name", cartridgeName);
                scriptText = scriptText.replaceAll("application_name", cartridgeName);
            }
            scriptText = scriptText.replaceAll("uname", System.getProperty(Constants.USERNAME410));
            scriptText = scriptText.replaceAll("pword", System.getProperty(Constants.PASSWORD410));
            scriptText = scriptText.replaceAll("base-url", System.getProperty(Constants.BASE_URL410));

            //Updating network partitions deploying commands
            String beginScriptText = scriptText.substring(0, scriptText.indexOf('*') + 2);
            String endScriptText = scriptText.substring(scriptText.indexOf('*') + 2);
            String modifiedScriptText = beginScriptText;

            List<String> networkPartitionIdList = memoryMap.get("networkPartitions");
            for (String networkPartitionId : networkPartitionIdList) {
                modifiedScriptText +=
                        System.getProperty("line.separator") + Constants.NETWORK_PARTITION_DEPLOYMENT_COMMAND_PART1
                                + networkPartitionId + Constants.NETWORK_PARTITION_DEPLOYMENT_COMMAND_PART2;
            }
            modifiedScriptText += endScriptText;
            File outputDirectory = new File(outputLocation + Constants.DIRECTORY_OUTPUT_SCRIPT_DEPLOY);

            boolean hasCreated = outputDirectory.mkdirs();
            if (!outputDirectory.exists() && !hasCreated) {
                throw new IOException("Error in creating the output directory");
            }
            writer = new FileWriter(new File(outputDirectory.getPath() + Constants.FILE_SOURCE_SCRIPT_DEPLOY), false);
            writer.write(modifiedScriptText);

        } catch (IOException e) {
            log.error("Error in copying scripts directory ", e);
        } finally {
            try {
                if (reader != null)
                    reader.close();
                if (writer != null)
                    writer.close();
            } catch (IOException ignore) {
            }
        }
        addIaasScriptDirectories(outputLocation + File.separator + "scripts");
    }

    /**
     * Method to add common undeploying script
     *
     * @param outputLocation   output location of the script
     * @param subscribableInfo subscribable information
     * @param cartridgeName    cartridge name
     */
    public static void addCommonUndeployingScript(String outputLocation, SubscribableInfo subscribableInfo,
            String cartridgeName, String cartridgeType) {
        BufferedReader reader = null;
        FileWriter writer = null;
        try {
            File file = new File(Constants.DIRECTORY_SOURCE_SCRIPT + Constants.DIRECTORY_SOURCE_SCRIPT_UNDEPLOY);
            reader = new BufferedReader(new FileReader(file));
            String line, scriptText = "";
            while ((line = reader.readLine()) != null) {
                scriptText += line + System.getProperty("line.separator");
            }

            if (subscribableInfo.getDeploymentPolicy() != null) {
                scriptText = scriptText.replaceAll("deployment-policy_name", subscribableInfo.getDeploymentPolicy());
                String applicationPolicyName = Transformer.getDeploymentPolicyToApplicationPolicyMap()
                        .get(subscribableInfo.getDeploymentPolicy());
                scriptText = scriptText.replaceAll("application-policy_name", applicationPolicyName);
            }
            if (subscribableInfo.getAutoscalingPolicy() != null) {
                scriptText = scriptText.replaceAll("autoscaling-policy_name", subscribableInfo.getAutoscalingPolicy());
            }
            if (cartridgeName != null) {
                scriptText = scriptText.replaceAll("cartridge_type", cartridgeType);
                scriptText = scriptText.replaceAll("application_name", cartridgeName);
            }
            scriptText = scriptText.replaceAll("uname", System.getProperty(Constants.USERNAME410));
            scriptText = scriptText.replaceAll("pword", System.getProperty(Constants.PASSWORD410));
            scriptText = scriptText.replaceAll("base-url", System.getProperty(Constants.BASE_URL410));

            List<String> networkPartitionIdList = memoryMap.get("networkPartitions");
            for (String networkPartitionId : networkPartitionIdList) {
                scriptText += Constants.NETWORK_PARTITION_UNDEPLOYMENT_COMMAND + networkPartitionId + System
                        .getProperty("line.separator");
            }
            File outputDirectory = new File(outputLocation + Constants.DIRECTORY_OUTPUT_SCRIPT_DEPLOY);

            boolean hasCreated = false;
            if (!outputDirectory.exists()) {
                hasCreated = outputDirectory.mkdirs();
            }
            if (!outputDirectory.exists() && !hasCreated) {
                throw new IOException("Error in creating the output directory");
            }
            writer = new FileWriter(new File(outputDirectory.getPath() + Constants.FILE_SOURCE_SCRIPT_UNDEPLOY), false);
            writer.write(scriptText);

        } catch (IOException e) {
            log.error("Error in copying scripts directory ", e);
        } finally {
            try {
                if (reader != null)
                    reader.close();
                if (writer != null)
                    writer.close();
            } catch (IOException ignore) {
            }
        }
    }

    /**
     * Method to get configuration details
     */
    public static void readInitialConfiguration() throws ConfigurationException {
        final PropertiesConfiguration propsConfig = new PropertiesConfiguration(
                System.getProperty(Constants.CONFIGURATION_FILE_NAME));
        SystemConfiguration.setSystemProperties(propsConfig);
    }
}
