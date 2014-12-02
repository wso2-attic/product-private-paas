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

package org.apache.stratos.cartridge.agent.data.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.stratos.cartridge.agent.config.CartridgeAgentConfiguration;
import org.wso2.carbon.databridge.agent.thrift.Agent;
import org.wso2.carbon.databridge.agent.thrift.AsyncDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.conf.AgentConfiguration;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.securevault.SecretResolver;
import java.util.Date;

public abstract class DataPublisher implements GenericDataPublisher {

    private static final Log log = LogFactory.getLog(DataPublisher.class);

    private StreamDefinition streamDefinition;
    private DataPublisherConfiguration dataPublisherConfig;
    private AsyncDataPublisher dataPublisher;
    private boolean isDataPublisherInitialized;

    public DataPublisher (DataPublisherConfiguration dataPublisherConfig, StreamDefinition streamDefinition) {

        this.dataPublisherConfig = dataPublisherConfig;
        this.streamDefinition = streamDefinition;
        this.setDataPublisherInitialized(false);
    }

    public void initialize () {

        String trustStorePasswordProperty = "truststore.password";
        String trustStorePasswordValue = System.getProperty(trustStorePasswordProperty);
        SecretResolver secretResolver = CartridgeAgentConfiguration.getInstance().getSecretResolver();
        String alias = trustStorePasswordProperty;

        // Resolve the secret password.
        if (log.isDebugEnabled()) {
            log.debug(String.format("Trying to decrypt property: %s", trustStorePasswordProperty));
        }
        if (trustStorePasswordValue.equalsIgnoreCase("secretAlias:" + alias)) {
            if (secretResolver != null && secretResolver.isInitialized()) {
                if (log.isDebugEnabled()) {
                    log.debug("SecretResolver is initialized.");
                }
                if (secretResolver.isTokenProtected(alias)) {
                    if (log.isDebugEnabled()) {
                        log.debug("SecretResolver [" + alias + "] is token protected");
                    }
                    trustStorePasswordValue = secretResolver.resolve(alias);
                    if (log.isDebugEnabled()) {
                        log.debug("SecretResolver [" + alias + "] is decrypted properly");
                    }
                }
            }
        }
        System.setProperty("javax.net.ssl.trustStorePassword", trustStorePasswordValue);
    	
        AgentConfiguration agentConfiguration = new AgentConfiguration();
        
        Agent agent = new Agent(agentConfiguration);

        dataPublisher = new AsyncDataPublisher(dataPublisherConfig.getMonitoringServerUrl(), dataPublisherConfig.getAdminUsername(),
                dataPublisherConfig.getAdminPassword(), agent);

        if (!dataPublisher.isStreamDefinitionAdded(streamDefinition.getName(), streamDefinition.getVersion())) {
            dataPublisher.addStreamDefinition(streamDefinition);
        }

        setDataPublisherInitialized(true);

        log.info("DataPublisher initialized");
    }

    public void publish (DataContext dataContext) {

        Event event = new Event();
        event.setTimeStamp(new Date().getTime());
        event.setMetaData(dataContext.getMetaData());
        event.setPayloadData(dataContext.getPayloadData());

        try {
            dataPublisher.publish(streamDefinition.getName(), streamDefinition.getVersion(), event);

        } catch (AgentException e) {
            String errorMsg = "Error in publishing event";
            log.error(errorMsg, e);
            // no need to throw here
        }
    }

    public void terminate () {

        dataPublisher.stop();
    }

    public boolean isDataPublisherInitialized() {
        return isDataPublisherInitialized;
    }

    public void setDataPublisherInitialized(boolean dataPublisherInitialized) {
        isDataPublisherInitialized = dataPublisherInitialized;
    }
}
