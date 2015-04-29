/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.stratos.messaging.message.receiver.instance.status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 * Implements functionality for receiving text based event messages from the instance status
 * message broker topic and add them to the event queue.
 */
class InstanceStatusEventMessageListener implements MessageListener {

    private static final Log log = LogFactory.getLog(InstanceStatusEventMessageListener.class);

    private final InstanceStatusEventMessageQueue messageQueue;

    public InstanceStatusEventMessageListener(InstanceStatusEventMessageQueue messageQueue) {
        this.messageQueue = messageQueue;
    }

    @Override
    public void onMessage(Message message) {
        if (message instanceof TextMessage) {
            TextMessage receivedMessage = (TextMessage) message;
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Instance status message received: " + ((TextMessage) message).getText());
                }
                // Add received message to the queue
                messageQueue.add(receivedMessage);
            } catch (JMSException e) {
                log.error("Error while adding the message to the queue!", e);
            }
        } else {
            log.warn("Getting a non-text message which can't be processed!");
        }
    }

}
