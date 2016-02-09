package org.wso2.ppaas.python.cartridge.agent.integration.common;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;


public class JettyHttpServer extends AbstractHandler {

    private static final Log log = LogFactory.getLog(JettyHttpServer.class);
    private static Server server = null;

    public static void startServer(int webServerPort) throws Exception {

        server = new Server(webServerPort);
        server.setHandler(new JettyHttpServer());
        server.start();
        log.info("Jetty server started");

    }

    public static void stopServer() throws Exception {
        try {
            server.stop();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new Exception("Could not stop the server", ex);
        }

    }

    public static Server getJettyServer()  {
        return server;
    }

    public void handle(String pathInContext, Request request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {

        boolean validRequest;
        boolean validLogMessage = false;
        BufferedReader reader = httpRequest.getReader();
        StringBuilder logMessage = new StringBuilder();
        String line;

        if (httpRequest.getMethod().equals("POST")) {

            validRequest = isValidRequest(request);

            do {
                line = reader.readLine();

                if (line != null)
                    logMessage.append(line);
            }
            while (line != null);

            log.info("Log Message: ");
            log.info(logMessage);

            if (logMessage.toString().contains("@logstream"))
                validLogMessage = true;


            httpResponse.setContentType("application/json");
            if (validRequest && validLogMessage)
                httpResponse.setStatus(HttpServletResponse.SC_ACCEPTED);
            else {
                httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }

            request.setHandled(true);
        }
    }

    private boolean isValidRequest(Request request) {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null)
            return (request.getContentType().contains("application/json") && request.getHeader("Authorization").
                    contains("Basic"));
        else {
            return false;
        }

    }
}

