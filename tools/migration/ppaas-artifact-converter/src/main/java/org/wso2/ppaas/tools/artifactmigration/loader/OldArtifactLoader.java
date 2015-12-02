/*
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ppaas.tools.artifactmigration.loader;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.stratos.manager.dto.Cartridge;
import org.apache.stratos.rest.endpoint.bean.autoscaler.partition.Partition;
import org.apache.stratos.rest.endpoint.bean.autoscaler.policy.autoscale.AutoscalePolicy;
import org.apache.stratos.rest.endpoint.bean.autoscaler.policy.deployment.DeploymentPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Fetches the JSON files from PPaaS 4.0.0
 */
public class OldArtifactLoader {

    private static final Logger log = LoggerFactory.getLogger(OldArtifactLoader.class);

    private static OldArtifactLoader instance = null;
    private String username;
    private String password;
    private Gson gson;

    private OldArtifactLoader() {

        gson = new Gson();
        this.setUsername(Constants.USER_NAME);
        this.setPassword(Constants.PASSWORD);
    }

    public static synchronized OldArtifactLoader getInstance() {

        if (instance == null) {
            synchronized (OldArtifactLoader.class) {
                if (instance == null) {
                    instance = new OldArtifactLoader();
                }
            }
        }
        return instance;

    }

    /**
     * Method to fetch Partition Lists
     *
     * @return Partition List
     * @throws JsonSyntaxException
     */
    public List<Partition> fetchPartitionList() throws JsonSyntaxException {

        return gson.fromJson(readUrl(Constants.BASE_URL + Constants.URL_PARTITION), new TypeToken<List<Partition>>() {
        }.getType());

    }

    /**
     * Method to fetch Auto Scale Policy
     *
     * @return Auto Scale Policy List
     * @throws JsonSyntaxException
     */
    public List<AutoscalePolicy> fetchAutoscalePolicyList() throws JsonSyntaxException {
        return gson.fromJson(readUrl(Constants.BASE_URL + Constants.URL_POLICY_AUTOSCALE),
                new TypeToken<List<AutoscalePolicy>>() {
                }.getType());

    }

    /**
     * Method to fetch Deployment Policy
     *
     * @return Deployment Policy List
     * @throws JsonSyntaxException
     */
    public List<DeploymentPolicy> fetchDeploymentPolicyList() throws JsonSyntaxException {
        return gson.fromJson(readUrl(Constants.BASE_URL + Constants.URL_POLICY_DEPLOYMENT),
                new TypeToken<List<DeploymentPolicy>>() {
                }.getType());

    }

    /**
     * Method to fetch Cartridges
     *
     * @return Cartridges List
     * @throws JsonSyntaxException
     */
    public List<Cartridge> fetchCartridgeList() throws JsonSyntaxException {
        return gson.fromJson(readUrl(Constants.BASE_URL + Constants.URL_CARTRIDGE), new TypeToken<List<Cartridge>>() {
        }.getType());

    }

    /**
     * Method to connect to the REST endpoint without authorization
     *
     * @param serviceEndpoint the endpoint to connect with
     * @return JSON string
     */
    private String readUrl(String serviceEndpoint) {

        String result = "";

        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod(serviceEndpoint);

        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

        try {

            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                System.err.println("Method failed: " + method.getStatusLine());
            }

            byte[] responseBody = method.getResponseBody();

            result = new String(responseBody);

        } catch (HttpException e) {
            log.error("HTTP exception in connecting to the endpoints" + e);
        } catch (IOException e) {
            log.error("IO exception in connecting to the endpoints " + e);
        } finally {
            method.releaseConnection();
        }

        return result;

    }

    public void setUsername(String username) {

        this.username = username;
    }

    public void setPassword(String password) {

        this.password = password;
    }
}
