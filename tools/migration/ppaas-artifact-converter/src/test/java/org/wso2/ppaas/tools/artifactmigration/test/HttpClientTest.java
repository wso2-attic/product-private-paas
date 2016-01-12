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
package org.wso2.ppaas.tools.artifactmigration.test;

import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.wso2.ppaas.tools.artifactmigration.Transformer;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class HttpClientTest {

    @BeforeClass public static void setUp() throws Exception {
        // Create Server
        Server server = new Server(Integer.getInteger("http.port"));
        ServletContextHandler context = new ServletContextHandler();
        server.setHandler(context);
        ServletHolder jerseyServlet = context
                .addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/stratos/*");
        jerseyServlet.setInitOrder(0);
        jerseyServlet
                .setInitParameter("jersey.config.server.provider.classnames", StratosV400Mock.class.getCanonicalName());
        ServletHolder jerseyServlet2 = context
                .addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/migration/*");
        jerseyServlet2.setInitOrder(0);
        jerseyServlet2
                .setInitParameter("jersey.config.server.provider.classnames", StratosV400Mock.class.getCanonicalName());
        server.setHandler(context);

        HttpConfiguration http_config = new HttpConfiguration();
        http_config.setSecureScheme("https");
        http_config.setSecurePort(Integer.getInteger("https.port"));
        http_config.setOutputBufferSize(TestConstants.BUFFER_SIZE);

        ServerConnector http = new ServerConnector(server, new HttpConnectionFactory(http_config));
        http.setPort(Integer.getInteger("http.port"));
        http.setIdleTimeout(TestConstants.IDLE_TIMEOUT);

        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStorePath(TestConstants.KEYSTORE_PATH);
        sslContextFactory.setKeyStorePassword("wso2carbon");
        sslContextFactory.setKeyManagerPassword("wso2carbon");

        HttpConfiguration https_config = new HttpConfiguration(http_config);
        https_config.addCustomizer(new SecureRequestCustomizer());

        ServerConnector https = new ServerConnector(server,
                new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
                new HttpConnectionFactory(https_config));
        https.setPort(Integer.getInteger("https.port"));
        https.setIdleTimeout(TestConstants.IDLE_TIMEOUT);

        // Set the connectors
        server.setConnectors(new Connector[] { http, https });
        // Start Server
        server.start();
    }

    @Test(timeout = 60000) public void transformNetworkPartitionListTest() throws Exception {
        Transformer.transformNetworkPartitionList();
        File partitionfile1 = new File(TestConstants.CREATED_PARTITION_TEST);
        File partitionfile2 = new File(TestConstants.PARTITION_TEST_WITH);
        assertTrue(FileUtils.contentEquals(partitionfile1, partitionfile2));
    }

    @Test(timeout = 60000) public void transformAutoscalePolicyListTest() throws Exception {
        Transformer.transformAutoscalePolicyList();
        File autoscalefile1 = new File(TestConstants.CREATED_AUTOSCALE_TEST);
        File autoscalefile2 = new File(TestConstants.AUTOSCALE_TEST_WITH);
        assertTrue(FileUtils.contentEquals(autoscalefile1, autoscalefile2));
    }

    @Test(timeout = 60000) public void transformDeploymentPolicyList() throws Exception {
        Transformer.transformDeploymentPolicyList();
        File deploymentfile1 = new File(TestConstants.CREATED_DEPLOYMENT_TEST);
        File deploymentfile2 = new File(TestConstants.DEPLOYMENT_TEST_WITH);
        assertTrue(FileUtils.contentEquals(deploymentfile1, deploymentfile2));
    }

    @Test(timeout = 60000) public void transformCartridgeList() throws Exception {
        Transformer.transformCartridgeList();
        File cartridgefile1 = new File(TestConstants.CREATED_CARTRIDGE_TEST);
        File cartridgefile2 = new File(TestConstants.CARTRIDGE_TEST_WITH);
        assertTrue(FileUtils.contentEquals(cartridgefile1, cartridgefile2));

    }
}
