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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.stratos.common.test.TestLogAppender;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;
import org.wso2.carbon.automation.extensions.servers.carbonserver.MultipleServersManager;
import org.wso2.ppaas.integration.tests.rest.IntegrationMockClient;
import org.wso2.ppaas.integration.tests.rest.RestClient;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertNotNull;

public class PPaaSTestServerManager {
    private static final Log log = LogFactory.getLog(PPaaSTestServerManager.class);
    public static final String PATH_SEP = File.separator;
    public final static String PORT_OFFSET_KEY = "carbon.port.offset";
    public final static String CARBON_ZIP_KEY = "carbon.zip";
    public static final String ACTIVEMQ_BIND_ADDRESS = "activemq.bind.address";
    public static final int GLOBAL_TEST_TIMEOUT = 5 * 60 * 1000;
    private static final String DEFAULT_PROFILE = "default";

    protected String adminUsername;
    protected String adminPassword;
    protected String ppaasBackendURL;
    protected String activemqBindAddress;
    protected RestClient restClient;
    private BrokerService broker = new BrokerService();
    private TestLogAppender testLogAppender = new TestLogAppender();
    protected AutomationContext ppaasAutomationCtx;
    protected IntegrationMockClient mockIaasApiClient;
    private MultipleServersManager manager;

    public PPaaSTestServerManager() {
        manager = new MultipleServersManager();
        activemqBindAddress = System.getProperty(ACTIVEMQ_BIND_ADDRESS);
    }

    public void init() {
        Logger.getRootLogger().addAppender(testLogAppender);
        Logger.getRootLogger().setLevel(Level.INFO);

        try {
            long time1 = System.currentTimeMillis();
            log.info("Starting ActiveMQ...");
            broker.setDataDirectory(PPaaSTestServerManager.class.getResource(PATH_SEP).getPath() +
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
            ppaasAutomationCtx = new AutomationContext("PPAAS", "ppaas-001", TestUserMode.SUPER_TENANT_ADMIN);
            adminUsername = ppaasAutomationCtx.getConfigurationValue
                    ("/automation/userManagement/superTenant/tenant/admin/user/userName");
            adminPassword = ppaasAutomationCtx.getConfigurationValue
                    ("/automation/userManagement/superTenant/tenant/admin/user/password");
            ppaasBackendURL = ppaasAutomationCtx.getContextUrls().getWebAppURL();
            restClient = new RestClient(ppaasBackendURL, adminUsername, adminPassword);
            mockIaasApiClient = new IntegrationMockClient(ppaasBackendURL + PATH_SEP + "mock-iaas" + PATH_SEP + "api");
            Map<String, String> startupParameterMap1 = new HashMap<String, String>();
            startupParameterMap1.put("-DportOffset", System.getProperty(PORT_OFFSET_KEY));
            startupParameterMap1.put("-Dprofile", DEFAULT_PROFILE);
            CarbonTestServerManager ppaasServer =
                    new CarbonTestServerManager(ppaasAutomationCtx, System.getProperty(CARBON_ZIP_KEY),
                            startupParameterMap1);
            setSystemproperties();
            log.info("PPaaS server port offset: " + System.getProperty(PORT_OFFSET_KEY));
            log.info("PPaaS backend URL: " + ppaasBackendURL);
            long time3 = System.currentTimeMillis();
            manager.startServers(ppaasServer);
            String carbonHome = System.getProperty(CarbonTestServerManager.CARBON_HOME_KEY);
            assertNotNull(carbonHome, "CARBON_HOME is null");
            System.setProperty("carbon.home", carbonHome);

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

        }
        catch (Exception e) {
            throw new RuntimeException("Could not start PPaaS server", e);
        }
    }

    public void cleanup() {
        try {
            manager.stopAllServers();
            log.info("Stopped PPaaS servers");
        }
        catch (AutomationFrameworkException e) {
            log.error("Could not stop PPaaS servers", e);
        }

        try {
            broker.stop();
            log.info("Stopped ActiveMQ server");
        }
        catch (Exception e) {
            log.error("Could not stop ActiveMQ server", e);
        }
    }

    private boolean serverStarted() {
        for (String message : testLogAppender.getMessages()) {
            if (message.contains("Topology initialized")) {
                return true;
            }
        }
        return false;
    }

    private boolean mockServiceStarted() {
        for (String message : testLogAppender.getMessages()) {
            if (message.contains("Mock IaaS service component activated")) {
                return true;
            }
        }
        return false;
    }

    public void setSystemproperties() {
        URL resourceUrl = getClass().getResource(File.separator + "keystores" + File.separator
                + "products" + File.separator + "wso2carbon.jks");
        System.setProperty("javax.net.ssl.trustStore", resourceUrl.getPath());
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
        log.info("trustStore set to " + resourceUrl.getPath());
    }
}