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

import org.apache.commons.logging.*;
import org.apache.stratos.messaging.listener.*;
import org.apache.stratos.messaging.listener.instance.status.*;
import org.apache.stratos.messaging.message.processor.*;

/**
 * Defines default instance status message processor chain.
 */
public class InstanceStatusMessageProcessorChain extends MessageProcessorChain {
    private static final Log log = LogFactory.getLog(InstanceStatusMessageProcessorChain.class);

    private InstanceStatusArtifactDeploymentStartedMessageProcessor instanceStatusArtifactDeploymentStartedMessageProcessor;;
    private InstanceStatusArtifactDeploymentCompletedMessageProcessor instanceStatusArtifactDeploymentCompletedMessageProcessor;;

    public void initialize() {
        // Add instance status event processors
        instanceStatusArtifactDeploymentStartedMessageProcessor = new InstanceStatusArtifactDeploymentStartedMessageProcessor();
        add(instanceStatusArtifactDeploymentStartedMessageProcessor);
        instanceStatusArtifactDeploymentCompletedMessageProcessor = new InstanceStatusArtifactDeploymentCompletedMessageProcessor();
        add(instanceStatusArtifactDeploymentCompletedMessageProcessor);

        if (log.isDebugEnabled()) {
            log.debug("Instance notifier message processor chain initialized");
        }
    }

    public void addEventListener(EventListener eventListener) {
        if (eventListener instanceof ArtifactDeploymentStartedEventListener) {
            instanceStatusArtifactDeploymentStartedMessageProcessor.addEventListener(eventListener);
        } else if (eventListener instanceof ArtifactDeploymentCompletedEventListener) {
            instanceStatusArtifactDeploymentCompletedMessageProcessor.addEventListener(eventListener);
        } else {
            throw new RuntimeException("Unknown event listener");
        }
    }

}
