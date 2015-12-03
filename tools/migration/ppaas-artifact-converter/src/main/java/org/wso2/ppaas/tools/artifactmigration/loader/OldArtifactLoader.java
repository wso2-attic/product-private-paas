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
import org.apache.commons.codec.binary.Base64;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
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
        String partitionString = readUrl(Constants.BASE_URL + Constants.URL_PARTITION);
        String partitionListString = partitionString.substring(partitionString.indexOf('['),(partitionString.lastIndexOf(']') + 1));
        return gson.fromJson(partitionListString, new TypeToken<List<Partition>>() {
        }.getType());

    }

    /**
     * Method to fetch Auto Scale Policy
     *
     * @return Auto Scale Policy List
     * @throws JsonSyntaxException
     */
    public List<AutoscalePolicy> fetchAutoscalePolicyList() throws JsonSyntaxException {
        String autoscalePolicyString = readUrl(Constants.BASE_URL + Constants.URL_POLICY_AUTOSCALE);
        String autoscalePolicyListString= autoscalePolicyString.substring(autoscalePolicyString.indexOf('['),(autoscalePolicyString.lastIndexOf(']') + 1));
        return gson.fromJson(autoscalePolicyListString, new TypeToken<List<AutoscalePolicy>>() {
        }.getType());


    }

    /**
     * Method to fetch Deployment Policy
     *
     * @return Deployment Policy List
     * @throws JsonSyntaxException
     */
    public List<DeploymentPolicy> fetchDeploymentPolicyList() throws JsonSyntaxException {
        String deploymentPolicyString = readUrl(Constants.BASE_URL + Constants.URL_POLICY_DEPLOYMENT);
        String deploymentPolicyListString= deploymentPolicyString.substring(deploymentPolicyString.indexOf('['),(deploymentPolicyString.lastIndexOf(']') + 1));
        return gson.fromJson(deploymentPolicyListString, new TypeToken<List<DeploymentPolicy>>() {
        }.getType());
    }

    /**
     * Method to fetch Cartridges
     *
     * @return Cartridges List
     * @throws JsonSyntaxException
     */
    public List<Cartridge> fetchCartridgeList() throws JsonSyntaxException {
        String cartridgeString = readUrl(Constants.BASE_URL + Constants.URL_CARTRIDGE);
        String cartridgeListString= cartridgeString.substring(cartridgeString.indexOf('['),(cartridgeString.lastIndexOf(']') + 1));
        return gson.fromJson(cartridgeListString, new TypeToken<List<Cartridge>>() {
        }.getType());
    }

    /**
     * Method to connect to the REST endpoint without authorization
     *
     * @param serviceEndpoint the endpoint to connect with
     * @return JSON string
     */
    private String readUrl(String serviceEndpoint) {

        String url = serviceEndpoint;
        String resultString = null;
        try {
            String webPage = url;
            String name = Constants.USER_NAME;
            String password = Constants.PASSWORD;

            String authString = name + ":" + password;
            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
            String authStringEnc = new String(authEncBytes);

            URL absoluteURL = new URL(webPage);
            URLConnection urlConnection = absoluteURL.openConnection();
            urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
            InputStream is = urlConnection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);

            int numCharsRead;
            char[] charArray = new char[1024];
            StringBuffer sb = new StringBuffer();
            while ((numCharsRead = isr.read(charArray)) > 0) {
                sb.append(charArray, 0, numCharsRead);
            }
            resultString = sb.toString();

        } catch (MalformedURLException e) {
            String msg = "malformed URL has occurred in connecting to rest end point";
            log.error(msg, e);
        } catch (IOException e) {
            String msg = "IO exception has occured in connecting to rest end point";
            log.error(msg, e);
        }

        return resultString;

    }

    public void setUsername(String username) {

        this.username = username;
    }

    public void setPassword(String password) {

        this.password = password;
    }
}
