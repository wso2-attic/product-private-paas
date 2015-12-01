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
import org.apache.stratos.manager.dto.Cartridge;
import org.apache.stratos.rest.endpoint.bean.autoscaler.partition.Partition;
import org.apache.stratos.rest.endpoint.bean.autoscaler.policy.autoscale.AutoscalePolicy;
import org.apache.stratos.rest.endpoint.bean.autoscaler.policy.deployment.DeploymentPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;

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
    private String username = Constants.USER_NAME;
    private String password = Constants.PASSWORD;
    private Gson gson = new Gson();

    private OldArtifactLoader() {
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
     * @return
     * @throws JsonSyntaxException
     */
    public List<Partition> fetchPartitionList() throws JsonSyntaxException {

        return gson.fromJson(readUrl(Constants.BASE_URL + Constants.URL_PARTITION), new TypeToken<List<Partition>>() {
        }.getType());

    }

    /**
     * Method to fetch Auto Scale Policy
     * @return
     * @throws JsonSyntaxException
     */
    public List<AutoscalePolicy> fetchAutoscalePolicyList() throws JsonSyntaxException {
        return gson.fromJson(readUrl(Constants.BASE_URL + Constants.URL_POLICY_AUTOSCALE), new TypeToken<List<AutoscalePolicy>>() {
        }.getType());

    }

    /**
     * Method to fetch Deployment Policy
     * @return
     * @throws JsonSyntaxException
     */
    public List<DeploymentPolicy> fetchDeploymentPolicyList() throws JsonSyntaxException {
        return gson.fromJson(readUrl(Constants.BASE_URL + Constants.URL_POLICY_DEPLOYMENT), new TypeToken<List<DeploymentPolicy>>() {
        }.getType());

    }

    /**
     * Method to fetch Cartridges
     * @return
     * @throws JsonSyntaxException
     */
    public List<Cartridge> fetchCartridgeList() throws JsonSyntaxException {
        return gson.fromJson(readUrl(Constants.BASE_URL + Constants.URL_CARTRIDGE), new TypeToken<List<Cartridge>>() {
        }.getType());

    }


    public <T> List<T> fetchJSON(String url, Class<T> typeofClass) throws JsonSyntaxException{
        return gson.fromJson(readUrl(url), new TypeToken<List<T>>() {}.getType());
    }

    /**
     * Method to connect to the REST endpoint
     * @param serviceEndpoint the endpoint to connect with
     * @return
     */
    private String readUrl(String serviceEndpoint) {


        String resultString = null;
        try {

            String authString = Constants.USER_NAME + ":" + Constants.PASSWORD;
            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
            String authStringEnc = new String(authEncBytes);

            URL url = new URL(serviceEndpoint);
            URLConnection urlConnection = url.openConnection();
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
            log.error(e.getMessage(),e);
        } catch (IOException e) {
            log.error(e.getMessage(),e);
        }
        return resultString;

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
}
