package org.wso2.ppaas.tools.artifactmigration.loader;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;

public class RestClient {
    private static final Logger log = LoggerFactory.getLogger(RestClient.class);

    private String baseURL;
    private String username;
    private String password;

    private final int TIME_OUT_PARAM = 6000000;

    public RestClient(String baseURL, String username, String password) {
        this.baseURL = baseURL;
        this.username = username;
        this.password = password;
    }

    public String getBaseURL() {
        return baseURL;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Handle http get request. Return String
     *
     * @param httpClient   This should be httpClient which used to connect to rest endpoint
     * @param resourcePath This should be REST endpoint
     * @return The HttpResponse
     * @throws org.apache.http.client.ClientProtocolException and IOException
     *                                                        if any errors occur when executing the request
     */
    public HttpResponse doGet(DefaultHttpClient httpClient, String resourcePath) throws IOException {
        HttpGet getRequest = new HttpGet(resourcePath);
        getRequest.addHeader("Content-Type", "application/json");

        String userPass = username + ":" + password;
        String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userPass.getBytes("UTF-8"));
        getRequest.addHeader("Authorization", basicAuth);

        httpClient = (DefaultHttpClient) WebClientWrapper.wrapClient(httpClient);

        HttpParams params = httpClient.getParams();
        HttpConnectionParams.setConnectionTimeout(params, TIME_OUT_PARAM);
        HttpConnectionParams.setSoTimeout(params, TIME_OUT_PARAM);

        HttpResponse response = httpClient.execute(getRequest);
        return response;
    }

    public String executeGet(String serviceEndpoint) throws Exception {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = null;

        try {
            response = doGet(httpClient, getBaseURL() + serviceEndpoint);
            int responseCode = response.getStatusLine().getStatusCode();

            if ((responseCode >= 400) && (responseCode < 500)) {
                // Entity not found
                return null;
            } else if (responseCode < 200 || responseCode >= 300) {
                //CliUtils.printError(response);
                return null;
            } else {
                return getHttpResponseString(response);
            }
        } finally {
            httpClient.getConnectionManager().shutdown();
        }
    }

    public static String getHttpResponseString(HttpResponse response) {
        try {
            String output;
            String result = "";

            if ((response != null) && (response.getEntity() != null) && (response.getEntity().getContent() != null)) {
                BufferedReader reader = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
                while ((output = reader.readLine()) != null) {
                    result += output;
                }
            }
            return result;
        } catch (SocketException e) {
            String message = "A connection error occurred while reading response message: " + e.getMessage();
            log.error(message, e);
            return null;
        } catch (IOException e) {
            String message = "An IO error occurred while reading response message: " + e.getMessage();
            log.error(message, e);
            return null;
        } catch (Exception e) {
            String message = "An unknown error occurred while reading response message: " + e.getMessage();
            log.error(message, e);
            return null;
        }
    }

}
