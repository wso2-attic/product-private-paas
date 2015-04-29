/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.stratos.messaging.message.processor.instance.status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.stratos.messaging.listener.EventListener;
import org.apache.stratos.messaging.listener.instance.status.ArtifactDeploymentCompletedEventListener;
import org.apache.stratos.messaging.listener.instance.status.ArtifactDeploymentStartedEventListener;
import org.apache.stratos.messaging.message.processor.MessageProcessorChain;

/**
 * Defines default instance status message processor chain.
 */
public class InstanceStatusMessageProcessorChain extends MessageProcessorChain {
    private static final Log log = LogFactory.getLog(InstanceStatusMessageProcessorChain.class);

    private InstanceStatusArtifactDeploymentStartedMessageProcessor artifactDeploymentStartedMessageProcessor;
    private InstanceStatusArtifactDeploymentCompletedMessageProcessor artifactDeploymentCompletedMessageProcessor;

    /**
     * Initialize the processor chain with relevant message processors
     */
    public void initialize() {
        artifactDeploymentStartedMessageProcessor = new InstanceStatusArtifactDeploymentStartedMessageProcessor();
        add(artifactDeploymentStartedMessageProcessor);
        artifactDeploymentCompletedMessageProcessor = new InstanceStatusArtifactDeploymentCompletedMessageProcessor();
        add(artifactDeploymentCompletedMessageProcessor);

        if (log.isDebugEnabled()) {
            log.debug("Instance notifier message processor chain initialized!");
        }
    }

    /**
     * Add event listeners to the processors
     * @param eventListener {@link EventListener}
     */
    public void addEventListener(EventListener eventListener) {
        if (eventListener instanceof ArtifactDeploymentStartedEventListener) {
            artifactDeploymentStartedMessageProcessor.addEventListener(eventListener);
        } else if (eventListener instanceof ArtifactDeploymentCompletedEventListener) {
            artifactDeploymentCompletedMessageProcessor.addEventListener(eventListener);
        } else {
            throw new IllegalArgumentException("Unknown event listener!");
        }
    }

}
