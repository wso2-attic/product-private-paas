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

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;

class HttpClientSetUp {
    private static final Server server = new Server(Integer.getInteger("https.port"));

    public HttpClientSetUp() {
        // Create Server
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

        HttpConfiguration https_config = new HttpConfiguration();
        https_config.setSecureScheme("https");
        https_config.setSecurePort(Integer.getInteger("https.port"));
        https_config.setOutputBufferSize(TestConstants.BUFFER_SIZE);
        https_config.addCustomizer(new SecureRequestCustomizer());

        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStorePath(TestConstants.KEYSTORE_PATH);
        sslContextFactory.setKeyStorePassword("wso2carbon");
        sslContextFactory.setKeyManagerPassword("wso2carbon");

        ServerConnector https = new ServerConnector(server,
                new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
                new HttpConnectionFactory(https_config));
        https.setPort(Integer.getInteger("https.port"));
        https.setIdleTimeout(TestConstants.IDLE_TIMEOUT);

        // Set the connectors
        server.setConnectors(new Connector[] { https });

    }

    public static void startServer() throws Exception {
        // Start Server
        server.start();
    }

}
