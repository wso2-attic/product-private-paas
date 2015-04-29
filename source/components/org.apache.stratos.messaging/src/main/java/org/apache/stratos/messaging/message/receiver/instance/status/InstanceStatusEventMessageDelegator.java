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

package org.apache.stratos.messaging.message.receiver.instance.status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.stratos.messaging.listener.EventListener;
import org.apache.stratos.messaging.message.processor.MessageProcessorChain;
import org.apache.stratos.messaging.message.processor.instance.status.InstanceStatusMessageProcessorChain;
import org.apache.stratos.messaging.util.Constants;

import javax.jms.TextMessage;


/**
 * Implements logic for processing instance status event messages based on a given topology process chain.
 */
class InstanceStatusEventMessageDelegator implements Runnable {

    private static final Log log = LogFactory.getLog(InstanceStatusEventMessageDelegator.class);
    private InstanceStatusEventMessageQueue messageQueue;
    private MessageProcessorChain processorChain;
    private boolean terminated;

    public InstanceStatusEventMessageDelegator(InstanceStatusEventMessageQueue messageQueue) {
        this.messageQueue = messageQueue;
        this.processorChain = new InstanceStatusMessageProcessorChain();
    }

    public void addEventListener(EventListener eventListener) {
        processorChain.addEventListener(eventListener);
    }

    @Override
    public void run() {
        log.info("Instance status event message delegator started!");

        while (!terminated) {
            try {
                TextMessage message = messageQueue.take();

                // Retrieve the header
                String type = message.getStringProperty(Constants.EVENT_CLASS_NAME);

                // Retrieve the actual message
                String json = message.getText();
                if (log.isDebugEnabled()) {
                    log.debug("Instance status event message received from queue: " + type);
                }

                // Delegate message to message processor chain
                if (log.isDebugEnabled()) {
                    log.debug("Delegating instance status event message: " + type);
                }
                processorChain.process(type, json, null);
            } catch (Exception e) {
                log.error("Failed to retrieve instance status event message!", e);
            }
        }
    }

    /**
     * Terminate topology event message delegator thread.
     */
    public void terminate() {
        terminated = true;
    }
}
