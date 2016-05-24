/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.membership.scheme.kubernetes.tcpforwarder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.membership.scheme.kubernetes.KubernetesMembershipSchemeConstants;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This code is extracted from
 * "http://www.nakov.com/books/inetjava/source-code-html/Chapter-1-Sockets/1.4-TCP-Sockets/TCPForwardServer.java.html"
 * TCPForwardServer is a simple TCP bridging code that
 * allows a TCP port on some host to be transparently forwarded
 * to some other TCP port on some other host. TCPForwardServer
 * continuously accepts client connections on the listening TCP
 * port (source port) and starts a thread (ClientThread) that
 * connects to the destination host and starts forwarding the
 * data between the client socket and destination socket.
 */
public class TCPForwardServer extends Thread {

    private static final Log log = LogFactory.getLog(TCPForwardServer.class);
    private int sourcePort;
    private int destinationPort;
    public static boolean isRunning = true;
    private final ExecutorService executorService = Executors.newFixedThreadPool(this.getThreadPoolSize());

    public TCPForwardServer(int sourcePort, int destinationPort) {
        this.sourcePort = sourcePort;
        this.destinationPort = destinationPort;
    }

    public void run() {
        ServerSocket listenerSocket;
        try {
            listenerSocket = new ServerSocket(this.sourcePort);
        } catch (IOException e) {
            log.error("Error in starting listener socket in [source-port]: " + this.sourcePort, e);
            return;
        }
        while (isRunning) {
            try {
                Socket clientSocket = listenerSocket.accept();
                ClientThread clientThread =
                        new ClientThread(clientSocket, this.destinationPort, executorService);
                executorService.execute(clientThread);
            } catch (IOException e) {
                log.error("Error in creating client socket", e);
            }
        }
    }

    private int getThreadPoolSize() {
        String threadPoolSize = System.getProperty(KubernetesMembershipSchemeConstants.TCP_FORWARDER_THREAD_POOL_SIZE,
                KubernetesMembershipSchemeConstants.DEFAULT_THREAD_POOL_SIZE);
        return Integer.parseInt(threadPoolSize);
    }


}
