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

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.stratos.common.beans.application.SubscribableInfo;
import org.wso2.ppaas.tools.artifactmigration.loader.Constants;

import java.io.*;
import java.util.Properties;

public class ConversionTool {

    private static final Logger log = Logger.getLogger(Transformation.class);
    private static ConversionTool instance = null;

    private ConversionTool() {
    }

    public static ConversionTool getInstance() {
        if (instance == null) {
            synchronized (Transformation.class) {
                if (instance == null) {
                    instance = new ConversionTool();
                }
            }
        }
        return instance;
    }

    public void handleConsoleInputs() {
        if (log.isInfoEnabled()) {
            log.info("CLI started...");
        }
        Console console = System.console();

        System.out.println("Enter the Base URL: ");
        System.setProperty("baseUrl",console.readLine());

        System.out.println("Enter the User name: ");
        System.setProperty("userName",console.readLine());

        System.out.println("Enter the Password: ");
        char[] passwordChars = console.readPassword();
        System.setProperty("password",new String(passwordChars));
    }

    public void startTransformation() {

        if (log.isInfoEnabled()) {
            log.info("Artifact Migration started...");
        }
        boolean isSuccess = true;
        try {
            Transformation.getInstance().transformNetworkPartitionList();
            Transformation.getInstance().transformAutoscalePolicyList();
            Transformation.getInstance().transformDeploymentPolicyList();
            Transformation.getInstance().transformCartridgeList();
        } catch (Exception e) {
            isSuccess = false;
            log.error("Error while converting the artifacts ", e);
            System.out.println("Error while transforming NetworkPartition list. See log for more details.");
        }
        if (isSuccess)
            System.out.println("Conversion completed successfully");
    }

    public void addIaasScriptDirectories(String outputLocation) {
        File sourceLocationEc2 = new File(Constants.DIRECTORY_SOURCE_SCRIPT + File.separator + "ec2");
        File sourceLocationGce = new File(Constants.DIRECTORY_SOURCE_SCRIPT + File.separator + "gce");
        File sourceLocationKub = new File(Constants.DIRECTORY_SOURCE_SCRIPT + File.separator + "kubernetes");
        File sourceLocationMock = new File(Constants.DIRECTORY_SOURCE_SCRIPT + File.separator + "mock");
        File sourceLocationOS = new File(Constants.DIRECTORY_SOURCE_SCRIPT + File.separator + "openstack");
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

    public void addDeployingScript(String outputLocation, SubscribableInfo subscribableInfo, String cartridgeName) {
        try {
            File file = new File(Constants.DIRECTORY_SOURCE_SCRIPT + "/common/deploy.sh");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line, oldText = "";
            while ((line = reader.readLine()) != null) {
                oldText += line + "\r\n";
            }
            reader.close();

            String newText = oldText;
            if (subscribableInfo.getDeploymentPolicy() != null) {
                newText = newText.replaceAll("deployment-policy_name", subscribableInfo.getDeploymentPolicy());
            }
            if (subscribableInfo.getAutoscalingPolicy() != null) {
                newText = newText.replaceAll("autoscaling-policy_name", subscribableInfo.getAutoscalingPolicy());
            }
            if (cartridgeName != null) {
                newText = newText.replaceAll("cartridge_name", cartridgeName);
                newText = newText.replaceAll("application_name", cartridgeName);
            }
            newText = newText.replaceAll("uname", Constants.USER_NAME410);
            newText = newText.replaceAll("pword", Constants.PASSWORD410);

            File outputDirectory = new File(outputLocation + File.separator + "scripts" + File.separator + "common");

            if (!outputDirectory.exists()) {
                outputDirectory.mkdirs();
            }
            FileWriter writer = new FileWriter(new File(outputDirectory.getPath() + File.separator + "deploy.sh"),
                    false);
            writer.write(newText);
            writer.close();

        } catch (IOException e) {
            log.error("Error in copying scripts directory ", e);
        }

        addIaasScriptDirectories(outputLocation + File.separator + "scripts");
    }
    public void getPropValues() {

        InputStream inputStream = null;

        try {
            Properties prop = new Properties();
            String propFileName = System.getProperty("user.dir")+File.separator +".." + File.separator + "conf"+ File.separator +"config.properties";

            inputStream = new FileInputStream(propFileName);
            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }
            System.setProperty("baseUrl",prop.getProperty("base_url"));
            System.setProperty("userName",prop.getProperty("user_name"));
            System.setProperty("password",prop.getProperty("password"));

        } catch (Exception e) {
            log.error(e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                log.error(e);
            }
        }
    }
}
