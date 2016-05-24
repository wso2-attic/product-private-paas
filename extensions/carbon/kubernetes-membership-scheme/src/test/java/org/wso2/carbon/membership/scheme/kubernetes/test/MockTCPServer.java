/*
 * Copyright (c) 2016., WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.membership.scheme.kubernetes.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.membership.scheme.kubernetes.tcpforwarder.TCPForwardServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class accept text message and send the uppercase message back to the client
 */
class MockTCPServer extends Thread {
    private static final Log log = LogFactory.getLog(TCPForwardTestCase.class);
    private ExecutorService executorService = Executors.newFixedThreadPool(10);

    public void run() {
        try {
            ServerSocket welcomeSocket = new ServerSocket(TCPForwardTestCase.DESTINATION_PORT);
            log.info("Mock TCP Server starting at port: " + TCPForwardTestCase.DESTINATION_PORT);
            while (true) {
                final Socket connectionSocket = welcomeSocket.accept();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        handleClientRequest(connectionSocket);
                    }
                };
                executorService.execute(runnable);
            }
        } catch (IOException e) {
            log.error(e);
        }
    }


    private static void handleClientRequest(Socket socket) {
        try {
            BufferedReader socketReader = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            BufferedWriter socketWriter = new BufferedWriter(new OutputStreamWriter(
                    socket.getOutputStream()));

            String inMsg;
            while ((inMsg = socketReader.readLine()) != null) {
                log.info("Received message from client: " + inMsg);
                String outMsg = inMsg.toUpperCase();
                socketWriter.write(outMsg + "\n");
                socketWriter.flush();
            }
            socket.close();
        } catch (Exception e) {
            log.error("Error in handling client request", e);
        }
    }
}

