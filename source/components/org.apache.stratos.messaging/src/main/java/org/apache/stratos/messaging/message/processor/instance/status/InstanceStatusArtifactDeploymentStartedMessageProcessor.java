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
import org.apache.stratos.messaging.event.instance.status.ArtifactDeploymentStartedEvent;
import org.apache.stratos.messaging.message.processor.MessageProcessor;
import org.apache.stratos.messaging.util.Util;

/**
 * Artifact deployment started message processor.
 */
public class InstanceStatusArtifactDeploymentStartedMessageProcessor extends MessageProcessor {

    private static final Log log = LogFactory.getLog(InstanceStatusArtifactDeploymentStartedMessageProcessor.class);

    private MessageProcessor nextProcessor;

    @Override
    public void setNext(MessageProcessor nextProcessor) {
        this.nextProcessor = nextProcessor;
    }

    @Override
    public boolean process(String type, String message, Object object) {
        if (ArtifactDeploymentStartedEvent.class.getName().equals(type)) {
            // Parse complete message and build event
            ArtifactDeploymentStartedEvent event = (ArtifactDeploymentStartedEvent)
                    Util.jsonToObject(message, ArtifactDeploymentStartedEvent.class);

            // Notify event listeners
            notifyEventListeners(event);
            return true;
        } else {
            if (nextProcessor != null) {
                return nextProcessor.process(type, message, object);
            } else {
                String msg = "Failed to process artifact deployment started message using available message processors: " +
                        type + "," + message;
                log.error(msg);
            }
        }
        return false;
    }

}
