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
import org.junit.*;
import org.wso2.carbon.membership.scheme.kubernetes.tcpforwarder.TCPForwardServer;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static junit.framework.TestCase.assertEquals;

public class TCPForwardTestCase {

    private static final Log log = LogFactory.getLog(TCPForwardTestCase.class);
    public static final int DESTINATION_PORT = 46789;
    private static final int SOURCE_PORT = 4001;
    private static final String IP = "127.0.0.1";
    private static final String MESSAGE_1 = "Test Message 1";
    private static final String MESSAGE_2 = "Test Message 2";
    private final static ExecutorService executorService = Executors.newFixedThreadPool(2);

    @BeforeClass
    public static void init() throws IOException {
        //Start Mock TCP server and TCP Forwarding threads

        executorService.execute(new MockTCPServer());
        TCPForwardServer tcpForwardServer = new TCPForwardServer(SOURCE_PORT, DESTINATION_PORT);
        executorService.execute(tcpForwardServer);
    }

    @Test
    public void testSendReceiveMessage() throws IOException {

        log.info("Starting send and receive message test case.");
        Socket clientSocket = new Socket(IP, SOURCE_PORT);
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());

        outToServer.writeBytes(MESSAGE_1 + '\n');
        outToServer.flush();
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(
                clientSocket.getInputStream()));
        String responseMessage1 = inFromServer.readLine();
        log.info("Received message from Server: " + responseMessage1);
        assertEquals("Received response for message-01", responseMessage1, MESSAGE_1.toUpperCase());

        outToServer.writeBytes(MESSAGE_2 + '\n');
        outToServer.flush();
        String responseMessage2 = inFromServer.readLine();
        log.info("Received message from Server: " + responseMessage2);
        assertEquals("Received response for message-02", responseMessage2, MESSAGE_2.toUpperCase());
        log.info("Closing client connection.");
        clientSocket.close();

        log.info("End of test case.");
    }

    @Test
    public void testCloseConnection() throws IOException {

        log.info("Starting closed client test case.");
        Socket clientSocket = new Socket(IP, SOURCE_PORT);
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        outToServer.writeBytes(MESSAGE_1 + '\n');
        outToServer.flush();
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(
                clientSocket.getInputStream()));
        String responseMessage1 = inFromServer.readLine();
        log.info("Received message from Server :" + responseMessage1);
        assertEquals("Received response for message-01 ", responseMessage1, MESSAGE_1.toUpperCase());
        log.info("Closing client connection.");
        clientSocket.close();

        log.info("Creating new client connection.");
        Socket clientSocket2 = new Socket(IP, SOURCE_PORT);
        log.info("Created new client connection");
        DataOutputStream outToServer2 = new DataOutputStream(clientSocket2.getOutputStream());
        outToServer2.writeBytes(MESSAGE_2 + '\n');
        outToServer2.flush();
        BufferedReader inFromServer2 = new BufferedReader(new InputStreamReader(
                clientSocket2.getInputStream()));
        String responseMessage2 = inFromServer2.readLine();
        log.info("Received message from Server: " + responseMessage2);
        assertEquals("Received response for message-02", responseMessage2, MESSAGE_2.toUpperCase());
        log.info("Closing client connection.");
        clientSocket2.close();

        assertEquals("Received response for message-01", MESSAGE_1.toUpperCase(), responseMessage1);
        assertEquals("Received response for message-02", MESSAGE_2.toUpperCase(), responseMessage2);
        log.info("End of test case.");
    }

    @Test
    public void testMultipleClientConnection() throws IOException {

        log.info("Starting multiple client test case.");
        Socket clientSocket1 = new Socket(IP, SOURCE_PORT);
        DataOutputStream outToServer1 = new DataOutputStream(clientSocket1.getOutputStream());
        outToServer1.writeBytes(MESSAGE_1 + '\n');
        outToServer1.flush();
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(
                clientSocket1.getInputStream()));
        String responseMessage1 = inFromServer.readLine();
        log.info("Received message from Server :" + responseMessage1);
        assertEquals("Received response for message-01 ", responseMessage1, MESSAGE_1.toUpperCase());

        Socket clientSocket2 = new Socket(IP, SOURCE_PORT);
        DataOutputStream outToServer2 = new DataOutputStream(clientSocket1.getOutputStream());
        outToServer2.writeBytes(MESSAGE_1 + '\n');
        outToServer2.flush();
        BufferedReader inFromServer2 = new BufferedReader(new InputStreamReader(
                clientSocket1.getInputStream()));
        outToServer2.writeBytes(MESSAGE_1 + '\n');
        outToServer2.flush();
        String responseMessage2 = inFromServer2.readLine();
        log.info("Received message from Server: " + responseMessage2);
        assertEquals("Received response for message-01.", responseMessage2, MESSAGE_1.toUpperCase());
        log.info("Closing client connection");
        clientSocket1.close();
        clientSocket2.close();
        log.info("End of test case.");
    }

    @AfterClass
    public static void  cleanUp() {
        executorService.shutdownNow();
    }
}
