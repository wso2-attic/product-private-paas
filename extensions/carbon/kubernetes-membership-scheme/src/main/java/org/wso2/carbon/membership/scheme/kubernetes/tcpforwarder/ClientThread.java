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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ClientThread is responsible for starting forwarding between
 * the client and the server. It keeps track of the client and
 * servers sockets that are both closed on input/output error
 * during the forwarding. The forwarding is bidirectional and
 * is performed by two ForwardThread instances.
 */
class ClientThread extends Thread {

    private static final Log log = LogFactory.getLog(ClientThread.class);
    private static final String DESTINATION_HOST = "127.0.0.1";
    private volatile boolean forwardingActive = false;

    private Socket clientSocket;
    private Socket serverSocket;
    private int destinationPort;
    private ExecutorService clientExecutorService;

    ClientThread(Socket clientSocket, int destinationPort, ExecutorService clientExecutorService) {
        this.clientSocket = clientSocket;
        this.destinationPort = destinationPort;
        this.clientExecutorService = clientExecutorService;
    }

    /**
     * Establishes connection to the destination server and
     * starts bidirectional forwarding ot data between the
     * client and the server.
     */
    public void run() {
        InputStream clientIn;
        OutputStream clientOut;
        InputStream serverIn;
        OutputStream serverOut;

        try {
            // Connect to the destination server
            serverSocket = new Socket(
                    DESTINATION_HOST,
                    this.destinationPort);
            // Turn on keep-alive for both the sockets
            serverSocket.setKeepAlive(true);
            clientSocket.setKeepAlive(true);

            // Obtain client & server input & output streams
            clientIn = clientSocket.getInputStream();
            clientOut = clientSocket.getOutputStream();
            serverIn = serverSocket.getInputStream();
            serverOut = serverSocket.getOutputStream();
        } catch (IOException e) {
            log.error("Cannot connect to " + DESTINATION_HOST + ": " +
                    this.destinationPort, e);
            breakConnection();
            return;
        }
        // Start forwarding data between server and client
        forwardingActive = true;

        ForwardThread clientForward =
                new ForwardThread(this, clientIn, serverOut);
        clientExecutorService.execute(clientForward);

        ForwardThread serverForward =
                new ForwardThread(this, serverIn, clientOut);
        clientExecutorService.execute(serverForward);
        log.info("TCP Forwarding " +
                clientSocket.getInetAddress().getHostAddress() +
                ":" + clientSocket.getPort() + " <--> " +
                serverSocket.getInetAddress().getHostAddress() +
                ":" + serverSocket.getPort() + " started.");
    }

    /**
     * Called by some of the forwarding threads to indicate
     * that its socket connection is broken and both client
     * and server sockets should be closed. Closing the client
     * and server sockets causes all threads blocked on reading
     * or writing to these sockets to get an exception and to
     * finish their execution.
     */
    void breakConnection() {
        synchronized (this) {
            if (forwardingActive) {
                log.info("Shutting down TCP Forwarding " +
                        clientSocket.getInetAddress().getHostAddress()
                        + ": " + clientSocket.getPort() + " <--> " +
                        serverSocket.getInetAddress().getHostAddress()
                        + ": " + serverSocket.getPort());
                forwardingActive = false;
            } else {
                return;
            }
        }

        try {
            log.info("Closing server socket: " + serverSocket.toString());
            serverSocket.close();
        } catch (Exception e) {
            log.error("Error in closing server socket.", e);
        }
        try {
            log.info("Closing client socket: " + clientSocket.toString());
            clientSocket.close();
        } catch (Exception e) {
            log.error("Error in closing client socket.", e);
        }
    }

    public synchronized boolean getForwardingActive() {
        if (log.isDebugEnabled()) {
            log.debug("ForwardingActive: " + forwardingActive);
        }
        return forwardingActive;
    }
}