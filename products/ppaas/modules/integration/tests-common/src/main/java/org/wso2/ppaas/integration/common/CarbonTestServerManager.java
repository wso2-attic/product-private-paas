/*
 * Copyright 2005-2015 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.ppaas.integration.common;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;
import org.wso2.carbon.automation.extensions.servers.carbonserver.TestServerManager;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class CarbonTestServerManager extends TestServerManager {
    private static final Log log = LogFactory.getLog(CarbonTestServerManager.class);
    public static final String PATH_SEP = File.separator;
    public static final String MOCK_IAAS_XML_FILENAME = "mock-iaas.xml";
    public static final String SCALING_DROOL_FILENAME = "scaling.drl";
    public static final String JNDI_PROPERTIES_FILENAME = "jndi.properties";
    public static final String JMS_OUTPUT_ADAPTER_FILENAME = "JMSOutputAdaptor.xml";
    public static final String CLOUD_CONTROLLER_FILENAME = "cloud-controller.xml";
    public static final String AUTOSCALER_FILENAME = "autoscaler.xml";
    public static final String CARTRIDGE_CONFIG_PROPERTIES_FILENAME = "cartridge-config.properties";
    public static final String IDENTITY_FILENAME = "identity.xml";
    private static final String LOG4J_PROPERTIES_FILENAME = "log4j.properties";


    public CarbonTestServerManager(AutomationContext context) {
        super(context);
    }

    public CarbonTestServerManager(AutomationContext context, String carbonZip, Map<String, String> commandMap) {
        super(context, carbonZip, commandMap);
    }

    public CarbonTestServerManager(AutomationContext context, int portOffset) {
        super(context, portOffset);
    }

    public CarbonTestServerManager(AutomationContext context, String carbonZip) {
        super(context, carbonZip);
    }

    public String startServer() throws IOException, AutomationFrameworkException, XPathExpressionException {
        String carbonHome = super.startServer();
        System.setProperty(Util.CARBON_HOME_KEY, carbonHome);
        return carbonHome;
    }

    public void configureServer() throws AutomationFrameworkException {
        try {
            copyArtifacts(carbonHome);
        }
        catch (IOException e) {
            log.error("Could not configure PPAAS server", e);
        }
    }

    public void stopServer() throws AutomationFrameworkException {
        super.stopServer();
    }

    protected void copyArtifacts(String carbonHome) throws IOException {
        String commonResourcesPath = Util.BASE_PATH + PATH_SEP + ".." + PATH_SEP + ".." + PATH_SEP + "src" + PATH_SEP +
                "test" + PATH_SEP + "resources" + PATH_SEP + "common";
        copyConfigFile(carbonHome, commonResourcesPath, MOCK_IAAS_XML_FILENAME, Util.CARBON_CONF_PATH);
        copyConfigFile(carbonHome, commonResourcesPath, JNDI_PROPERTIES_FILENAME, Util.CARBON_CONF_PATH);
        copyConfigFile(carbonHome, commonResourcesPath, LOG4J_PROPERTIES_FILENAME, Util.CARBON_CONF_PATH);
        copyConfigFile(carbonHome, commonResourcesPath, CLOUD_CONTROLLER_FILENAME, Util.CARBON_CONF_PATH);
        copyConfigFile(carbonHome, commonResourcesPath, AUTOSCALER_FILENAME, Util.CARBON_CONF_PATH);
        copyConfigFile(carbonHome, commonResourcesPath, CARTRIDGE_CONFIG_PROPERTIES_FILENAME, Util.CARBON_CONF_PATH);
        copyConfigFile(carbonHome, commonResourcesPath, IDENTITY_FILENAME, Util.CARBON_CONF_PATH);
        copyConfigFile(carbonHome, commonResourcesPath, SCALING_DROOL_FILENAME,
                Util.CARBON_CONF_PATH + PATH_SEP + "drools");
        copyConfigFile(carbonHome, commonResourcesPath, JMS_OUTPUT_ADAPTER_FILENAME,
                "repository" + PATH_SEP + "deployment" + PATH_SEP + "server" + PATH_SEP + "outputeventadaptors");
    }

    private void copyConfigFile(String carbonHome, String filePath, String fileName, String destinationFolder)
            throws IOException {
        assertNotNull(carbonHome, "CARBON_HOME is null");
        String fileAbsPath = filePath + PATH_SEP + fileName;
        log.info("Copying file: " + fileAbsPath);
        File srcFile = new File(fileAbsPath);
        assertTrue(srcFile.exists(), "File does not exist [file] " + srcFile.getAbsolutePath());
        File destFile = new File(carbonHome + PATH_SEP + destinationFolder + PATH_SEP + fileName);
        FileUtils.copyFile(srcFile, destFile);
        log.info("Copying file [source] " + srcFile.getAbsolutePath() + " to [dest] " + destFile.getAbsolutePath());
    }
}