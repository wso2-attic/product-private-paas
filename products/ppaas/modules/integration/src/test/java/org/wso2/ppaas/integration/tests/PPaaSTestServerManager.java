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

package org.wso2.ppaas.integration.tests;

import org.apache.activemq.broker.BrokerService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.stratos.common.test.TestLogAppender;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.wso2.carbon.integration.framework.TestServerManager;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.integration.framework.utils.ServerUtils;
import org.wso2.carbon.integration.framework.utils.TestUtil;
import org.wso2.ppaas.integration.tests.rest.IntegrationMockClient;
import org.wso2.ppaas.integration.tests.rest.RestClient;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import static org.testng.Assert.assertNotNull;

/**
 * Prepare activemq, WSO2 Private PaaS server for tests, enables mock iaas, starts servers and stop them after the
 * tests.
 */
public class PPaaSTestServerManager extends TestServerManager {
    private static final Log log = LogFactory.getLog(PPaaSTestServerManager.class);
    private static Properties integrationProperties;
    public static final String BASE_PATH = PPaaSTestServerManager.class.getResource("/").getPath();
    public static final String PPAAS_DISTRIBUTION_NAME = "distribution.path";
    public final static String PORT_OFFSET = "carbon.port.offset";
    public static final String ACTIVEMQ_BIND_ADDRESS = "activemq.bind.address";
    public static final String PPAAS_ENDPOINT = "ppaas.endpoint";
    public static final String ADMIN_USERNAME = "ppaas.admin.username";
    public static final String ADMIN_PASSWORD = "ppaas.admin.password";
    public static final String MOCK_IAAS_XML_FILE = "mock-iaas.xml";
    public static final String SCALING_DROOL_FILE = "scaling.drl";
    public static final String JNDI_PROPERTIES_FILE = "jndi.properties";
    public static final String JMS_OUTPUT_ADAPTER_FILE = "JMSOutputAdaptor.xml";

    protected String distributionName;
    protected int portOffset;
    protected String adminUsername;
    protected String adminPassword;
    protected String ppaasEndpoint;
    protected String activemqBindAddress;
    protected RestClient restClient;
    private BrokerService broker = new BrokerService();
    private TestLogAppender testLogAppender = new TestLogAppender();
    private ServerUtils serverUtils;
    private String carbonHome;
    protected IntegrationMockClient mockIaasApiClient;

    public PPaaSTestServerManager() {
        super(BASE_PATH + getIntegrationTestProperty(PPAAS_DISTRIBUTION_NAME),
                Integer.parseInt(getIntegrationTestProperty(PORT_OFFSET)));

        distributionName = integrationProperties.getProperty(PPAAS_DISTRIBUTION_NAME);
        portOffset = Integer.parseInt(integrationProperties.getProperty(PORT_OFFSET));
        adminUsername = integrationProperties.getProperty(ADMIN_USERNAME);
        adminPassword = integrationProperties.getProperty(ADMIN_PASSWORD);
        ppaasEndpoint = integrationProperties.getProperty(PPAAS_ENDPOINT);
        activemqBindAddress = integrationProperties.getProperty(ACTIVEMQ_BIND_ADDRESS);
        serverUtils = new ServerUtils();
        restClient = new RestClient(ppaasEndpoint, adminUsername, adminPassword);
        mockIaasApiClient = new IntegrationMockClient(ppaasEndpoint + "/mock-iaas/api");
    }

    private static String getIntegrationTestProperty(String key) {
        if (integrationProperties == null) {
            integrationProperties = new Properties();
            try {
                integrationProperties
                        .load(PPaaSTestServerManager.class.getResourceAsStream("/integration-test.properties"));
                log.info("PPaaS integration properties: " + integrationProperties.toString());
            }
            catch (IOException e) {
                log.error("Error loading integration-test.properties file from classpath. Please make sure that file " +
                        "exists in classpath.", e);
            }
        }
        return integrationProperties.getProperty(key);
    }

    @Override
    @BeforeSuite(timeOut = 600000)
    public String startServer() throws IOException {
        Logger.getRootLogger().addAppender(testLogAppender);
        Logger.getRootLogger().setLevel(Level.INFO);

        try {
            // Start ActiveMQ
            long time1 = System.currentTimeMillis();
            log.info("Starting ActiveMQ...");
            broker.setDataDirectory(PPaaSTestServerManager.class.getResource("/").getPath() +
                    File.separator + ".." + File.separator + "activemq-data");
            broker.setBrokerName("testBroker");
            broker.addConnector(activemqBindAddress);
            broker.start();
            long time2 = System.currentTimeMillis();
            log.info(String.format("ActiveMQ started in %d sec", (time2 - time1) / 1000));
        }
        catch (Exception e) {
            throw new RuntimeException("Could not start ActiveMQ", e);
        }

        try {
            log.info("Setting up PPaaS server...");
            long time3 = System.currentTimeMillis();
            String carbonZip = getCarbonZip();
            if (carbonZip == null) {
                carbonZip = System.getProperty("carbon.zip");
            }

            if (carbonZip == null) {
                throw new IllegalArgumentException("carbon zip file is null");
            } else {
                carbonHome = this.serverUtils.setUpCarbonHome(carbonZip);
                TestUtil.copySecurityVerificationService(carbonHome);
                this.copyArtifacts(carbonHome);
                log.info("PPaaS server setup completed");

                log.info("Starting PPaaS server...");
                this.serverUtils.startServerUsingCarbonHome(carbonHome, carbonHome, "wso2server", portOffset, null);
                FrameworkSettings.init();

                while (!serverStarted()) {
                    log.info("Waiting for topology to be initialized...");
                    Thread.sleep(5000);
                }

                while (!mockServiceStarted()) {
                    log.info("Waiting for mock service to be initialized...");
                    Thread.sleep(1000);
                }

                long time4 = System.currentTimeMillis();
                log.info(String.format("PPaaS server started in %d sec", (time4 - time3) / 1000));
                return carbonHome;
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Could not start PPaaS server", e);
        }
    }

    private boolean mockServiceStarted() {
        for (String message : testLogAppender.getMessages()) {
            if (message.contains("Mock IaaS service component activated")) {
                return true;
            }
        }
        return false;
    }

    @Override
    @AfterSuite(timeOut = 600000)
    public void stopServer() throws Exception {
        super.stopServer();
        broker.stop();
        log.info("Stopped ActiveMQ server.");
    }

    protected void copyArtifacts(String carbonHome) throws IOException {
        copyConfigFile(carbonHome, MOCK_IAAS_XML_FILE);
        copyConfigFile(carbonHome, JNDI_PROPERTIES_FILE);
        copyConfigFile(carbonHome, SCALING_DROOL_FILE, "repository/conf/drools");
        copyConfigFile(carbonHome, JMS_OUTPUT_ADAPTER_FILE, "repository/deployment/server/outputeventadaptors");
    }

    private void copyConfigFile(String carbonHome, String sourceFilePath) throws IOException {
        copyConfigFile(carbonHome, sourceFilePath, "repository/conf");
    }

    private void copyConfigFile(String carbonHome, String sourceFilePath, String destinationFolder) throws IOException {
        log.info("Copying file: " + sourceFilePath);
        URL fileURL = getClass().getResource("/" + sourceFilePath);
        assertNotNull(fileURL);
        File srcFile = new File(fileURL.getFile());
        File destFile = new File(carbonHome + "/" + destinationFolder + "/" + sourceFilePath);
        FileUtils.copyFile(srcFile, destFile);
        log.info(sourceFilePath + " file copied");
    }

    private boolean serverStopped() {
        for (String message : testLogAppender.getMessages()) {
            if (message.contains("Halting JVM")) {
                return true;
            }
        }
        return false;
    }


    private boolean serverStarted() {
        for (String message : testLogAppender.getMessages()) {
            if (message.contains("Topology initialized")) {
                return true;
            }
        }
        return false;
    }
}