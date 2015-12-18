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

/*
Class for conversion
 */
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

    /**
     * Method to handle console inputs
     */
    public void handleConsoleInputs() {

        if (log.isInfoEnabled()) {
            log.info("CLI started...");
        }
        Console console = System.console();
        if (System.getProperty(Constants.BASE_URL400) == null || System.getProperty(Constants.BASE_URL400).isEmpty()) {
            System.out.println("Enter the Base URL of PPaaS 4.0.0:");
            System.setProperty(Constants.BASE_URL400, console.readLine());
        }

        if (System.getProperty(Constants.USERNAME400) == null || System.getProperty(Constants.USERNAME400).isEmpty()) {
            System.out.println("Enter the User name of PPaaS 4.0.0:");
            System.setProperty(Constants.USERNAME400, console.readLine());
        }

        if (System.getProperty(Constants.PASSWORD400) == null || System.getProperty(Constants.PASSWORD400).isEmpty()) {
            System.out.println("Enter the Password of PPaaS 4.0.0:");
            char[] passwordChars = console.readPassword();
            System.setProperty(Constants.PASSWORD400, new String(passwordChars));
        }

        if (System.getProperty(Constants.USERNAME410) == null || System.getProperty(Constants.USERNAME410).isEmpty()) {
            System.out.println("Enter the User name of PPaaS 4.1.0:");
            System.setProperty(Constants.USERNAME410, console.readLine());
        }

        if (System.getProperty(Constants.PASSWORD410) == null || System.getProperty(Constants.PASSWORD410).isEmpty()) {
            System.out.println("Enter the Password of PPaaS 4.1.0:");
            char[] passwordChars = console.readPassword();
            System.setProperty(Constants.PASSWORD410, new String(passwordChars));
        }
    }

    /**
     * Method to start transformation
     */
    public void startTransformation() {

        log.info("Artifact Migration started...");
        try {
            Transformation.transformNetworkPartitionList();
            Transformation.transformAutoscalePolicyList();
            Transformation.transformDeploymentPolicyList();
            Transformation.transformCartridgeList();
            System.out.println("Conversion completed successfully");
        } catch (Exception e) {
            log.error("Error while converting the artifacts ", e);
            System.out.println("Error while transforming NetworkPartition list. See log for more details.");
        }
    }

    /**
     * Method to add script directories specific to each IaaS
     *
     * @param outputLocation output location of the script directories
     */
    private void addIaasScriptDirectories(String outputLocation) {

        File sourceLocationEc2 = new File(Constants.DIRECTORY_SOURCE_SCRIPT_EC2);
        File sourceLocationGce = new File(Constants.DIRECTORY_SOURCE_SCRIPT_GCE);
        File sourceLocationKub = new File(Constants.DIRECTORY_SOURCE_SCRIPT_KUBERNETES);
        File sourceLocationMock = new File(Constants.DIRECTORY_SOURCE_SCRIPT_MOCK);
        File sourceLocationOS = new File(Constants.DIRECTORY_SOURCE_SCRIPT_OPENSTACK);
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
    public void addDeployingScript(String outputLocation, SubscribableInfo subscribableInfo, String cartridgeName) {
        BufferedReader reader = null;
        FileWriter writer = null;
        try {
            File file = new File(Constants.DIRECTORY_SOURCE_SCRIPT_DEPLOY);
            reader = new BufferedReader(new FileReader(file));
            String line,oldText = "";
            while ((line = reader.readLine()) != null) {
                oldText += line + "\n";
            }
            //String newText = oldText;
            if (subscribableInfo.getDeploymentPolicy() != null) {
                oldText = oldText.replaceAll("deployment-policy_name", subscribableInfo.getDeploymentPolicy());
            }
            if (subscribableInfo.getAutoscalingPolicy() != null) {
                oldText = oldText.replaceAll("autoscaling-policy_name", subscribableInfo.getAutoscalingPolicy());
            }
            if (cartridgeName != null) {
                oldText = oldText.replaceAll("cartridge_name", cartridgeName);
                oldText = oldText.replaceAll("application_name", cartridgeName);
            }
            oldText = oldText.replaceAll("uname", System.getProperty(Constants.USERNAME410));
            oldText = oldText.replaceAll("pword", System.getProperty(Constants.PASSWORD410));

            File outputDirectory = new File(outputLocation + Constants.DIRECTORY_OUTPUT_SCRIPT_DEPLOY);

            if (!outputDirectory.exists()) {
                outputDirectory.mkdirs();
            }
            writer = new FileWriter(new File(outputDirectory.getPath() + Constants.FILE_SOURCE_SCRIPT_DEPLOY),
                    false);
            writer.write(oldText);


        } catch (IOException e) {
            log.error("Error in copying scripts directory ", e);
        } finally {
            try {
                assert reader != null;
                    reader.close();
                assert writer != null;
                    writer.close();
            } catch (IOException ignore) {
            }
        }
        addIaasScriptDirectories(outputLocation + File.separator + "scripts");
    }

    /**
     * Method to get configuration details
     */
    public void readInitialConfiguration() {

        //        String propFileName = System.getProperty("user.dir") + File.separator + ".." + File.separator + "conf" + File.separator
        //                + "config.properties";
        //        PropertiesConfiguration propsConfig;
        //        try {
        //           propsConfig = new PropertiesConfiguration(
        //                    ArtifactLoaderConfiguration.class.getResource(propFileName));
        //
        //        } catch (Exception e) {
        //            throw new RuntimeException("Failed to load config file: " + propFileName, e);
        //        }
        //        SystemConfiguration.setSystemProperties(propsConfig);
        InputStream inputStream = null;
        try {
            Properties properties = new Properties();
            String propFileName =
                    System.getProperty("user.dir") + File.separator + ".." + File.separator + "conf" + File.separator
                            + "config.properties";

            inputStream = new FileInputStream(propFileName);
            properties.load(inputStream);

            if (properties.getProperty(Constants.BASE_URL400) != null)
                System.setProperty(Constants.BASE_URL400, properties.getProperty(Constants.BASE_URL400));
            if (properties.getProperty(Constants.USERNAME400) != null)
                System.setProperty(Constants.USERNAME400, properties.getProperty(Constants.USERNAME400));
            if (properties.getProperty(Constants.PASSWORD400) != null)
                System.setProperty(Constants.PASSWORD400, properties.getProperty(Constants.PASSWORD400));
            if (properties.getProperty(Constants.USERNAME410) != null)
                System.setProperty(Constants.USERNAME410, properties.getProperty(Constants.USERNAME410));
            if (properties.getProperty(Constants.PASSWORD410) != null)
                System.setProperty(Constants.PASSWORD410, properties.getProperty(Constants.PASSWORD410));

        } catch (IOException e) {
            String msg = "IO error in getting configuration details";
            log.error(msg, e);
        } finally {
            try {
                assert inputStream != null;
                    inputStream.close();
            } catch (IOException ignore) {
            }
        }
    }
}
