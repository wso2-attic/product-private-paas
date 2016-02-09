/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ppaas.python.cartridge.agent.integration.tests;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.stratos.messaging.domain.topology.ServiceType;
import org.apache.stratos.messaging.domain.topology.Topology;
import org.apache.stratos.messaging.event.topology.CompleteTopologyEvent;
import org.apache.stratos.messaging.event.topology.MemberInitializedEvent;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.ppaas.python.cartridge.agent.integration.common.JettyHttpServer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LogPublisherTestCase extends PythonAgentIntegrationTest {

    private static final Log log = LogFactory.getLog(LogPublisherTestCase.class);
    private static final int STARTUP_TIMEOUT = 5 * 60000;
    private static final String CLUSTER_ID = "tomcat.tomcat.domain";
    private static final String DEPLOYMENT_POLICY_NAME = "deployment-policy-1";
    private static final String AUTOSCALING_POLICY_NAME = "autoscaling-policy-1";
    private static final String APP_ID = "application-1";
    private static final String MEMBER_ID = "tomcat.member-1";
    private static final String INSTANCE_ID = "instance-1";
    private static final String CLUSTER_INSTANCE_ID = "cluster-1-instance-1";
    private static final String NETWORK_PARTITION_ID = "network-partition-1";
    private static final String PARTITION_ID = "partition-1";
    private static final String SERVICE_NAME = "tomcat";
    private static final Topology topology = PythonAgentIntegrationTest.createTestTopology(
            SERVICE_NAME,
            CLUSTER_ID,
            DEPLOYMENT_POLICY_NAME,
            AUTOSCALING_POLICY_NAME,
            APP_ID,
            MEMBER_ID,
            CLUSTER_INSTANCE_ID,
            NETWORK_PARTITION_ID,
            PARTITION_ID,
            ServiceType.SingleTenant);

    private static final int ADC_TIMEOUT = 300000;
    private int jettyServerPort;
    private static final String JETTY_SERVER_PORT = "jetty.server.port";
    private boolean logPublishedToServer;

    public LogPublisherTestCase() throws IOException {

        integrationProperties
                .load(PythonAgentIntegrationTest.class.getResourceAsStream(PATH_SEP + "integration-test.properties"));
        jettyServerPort = Integer.parseInt(integrationProperties.getProperty(JETTY_SERVER_PORT));

    }

    @Override
    protected String getClassName() {
        return this.getClass().getSimpleName();
    }


    @BeforeMethod(alwaysRun = true)
    public void setupJettyServer() throws Exception {

        log.info("Setting up Jetty Server");
        // Set jndi.properties.dir system property for initializing event publishers and receivers
        System.setProperty("jndi.properties.dir", getCommonResourcesPath());

        // start Python agent with configurations provided in resource path
        super.setup(ADC_TIMEOUT);

        JettyHttpServer.startServer(jettyServerPort);

        log.info("Started Jetty server");

        File source =
                new File(getResourcesPath() + PATH_SEP + getClassName() + PATH_SEP + "log_file.log");
        File destination = new File(PATH_SEP + "tmp" + PATH_SEP + "log_file.log");

        try {
            FileUtils.copyFile(source, destination);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Simulate server socket
        startServerSocket(8080);
    }


    /**
     * TearDown method for test method testPythonCartridgeAgent
     */
    @AfterMethod(alwaysRun = true)
    public void tearDownAgentStartupTest() {
        tearDown();

        try {
            if (JettyHttpServer.getJettyServer() != null) {
                JettyHttpServer.stopServer();
            }
        } catch (Exception ignore) {
        }


        try {
            File logFile = new File(PATH_SEP + "tmp" + PATH_SEP + "log_file.log");
            if (logFile != null)
                logFile.delete();
        } catch (Exception ignore) {
        }

    }

    @Test(timeOut = STARTUP_TIMEOUT, description = "Test PCA initialization, activation, log publishing", groups = {"smoke"})
    public void testPythonCartridgeAgent() {
        startCommunicatorThread();
        Thread startupTestThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!eventReceiverInitialized) {
                    sleep(2000);
                }
                List<String> outputLines = new ArrayList<String>();
                while (!outputStream.isClosed()) {
                    List<String> newLines = getNewLines(outputLines, outputStream.toString());
                    if (newLines.size() > 0) {
                        for (String line : newLines) {
                            if (line.contains("Subscribed to 'topology/#'")) {
                                sleep(2000);
                                // Send complete topology event
                                log.info("Publishing complete topology event...");
                                CompleteTopologyEvent completeTopologyEvent = new CompleteTopologyEvent(topology);
                                publishEvent(completeTopologyEvent);
                                log.info("Complete topology event published");

                                // Publish member initialized event
                                log.info("Publishing member initialized event...");
                                MemberInitializedEvent memberInitializedEvent = new MemberInitializedEvent(
                                        SERVICE_NAME, CLUSTER_ID, CLUSTER_INSTANCE_ID, MEMBER_ID, NETWORK_PARTITION_ID,
                                        PARTITION_ID, INSTANCE_ID
                                );
                                publishEvent(memberInitializedEvent);
                                log.info("Member initialized event published");
                            }

                            if (line.contains("Published to Analyzing Server")) {
                                logPublishedToServer = true;
                            }
                        }
                    }
                    sleep(1000);
                }
            }
        });

        startupTestThread.start();

        while (!logPublishedToServer) {
            sleep(2000);
        }

    }


}
