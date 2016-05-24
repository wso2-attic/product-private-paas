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

/**
 * ForwardThread handles the TCP forwarding between a socket
 * input stream (source) and a socket output stream (dest).
 * It reads the input stream and forwards everything to the
 * output stream. If some of the streams fails, the forwarding
 * stops and the parent is notified to close all its sockets.
 */
class ForwardThread extends Thread {
    private static final int BUFFER_SIZE = 8192;
    private static final Log log = LogFactory.getLog(ForwardThread.class);
    private InputStream inputStream;
    private OutputStream outputStream;
    private ClientThread parentThread;

    /**
     * Creates a new traffic redirection thread specifying
     * its parent, input stream and output stream.
     */
    ForwardThread(ClientThread parentThread, InputStream
            inputStream, OutputStream outputStream) {
        this.parentThread = parentThread;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    /**
     * Runs the thread. Continuously reads the input stream and
     * writes the read data to the output stream. If reading or
     * writing fail, exits the thread and notifies the parent
     * about the failure.
     */
    public void run() {
        byte[] buffer = new byte[BUFFER_SIZE];
        try {
            while (TCPForwardServer.isRunning) {
                int bytesRead = inputStream.read(buffer);
                if (bytesRead == -1) {
                    // End of stream is reached --> exit
                    log.info("An endpoint has closed the connection");
                    break;
                }
                outputStream.write(buffer, 0, bytesRead);
                outputStream.flush();
            }
        } catch (IOException e) {
            // Read/write failed --> connection is broken
            if (parentThread.getForwardingActive()) {
                log.error("Read/Write failed. Connection broken", e);
            } else {
                log.info("Other endpoint has been closed");
            }
        }
        // Notify parent thread that the connection is broken
        finally {
            log.info("Closing forwarding sockets since one endpoint connection has been broken...");
            parentThread.breakConnection();
        }
    }
}
