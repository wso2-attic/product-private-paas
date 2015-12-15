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
import com.google.gson.reflect.TypeToken;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.apache.stratos.manager.dto.Cartridge;
import org.apache.stratos.rest.endpoint.bean.CartridgeInfoBean;
import org.apache.stratos.rest.endpoint.bean.autoscaler.partition.Partition;
import org.apache.stratos.rest.endpoint.bean.autoscaler.policy.autoscale.AutoscalePolicy;
import org.apache.stratos.rest.endpoint.bean.autoscaler.policy.deployment.DeploymentPolicy;
import org.apache.stratos.rest.endpoint.bean.subscription.domain.SubscriptionDomainBean;
import org.wso2.ppaas.tools.artifactmigration.exception.ArtifactLoadingException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 * Fetches the JSON files from PPaaS 4.0.0
 */
public class OldArtifactLoader {

    private static final Logger log = Logger.getLogger(OldArtifactLoader.class);

    private static OldArtifactLoader instance = null;
    private Gson gson;

    private OldArtifactLoader() {
        gson = new Gson();
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
     * @throws ArtifactLoadingException
     */
    public List<Partition> fetchPartitionList() throws ArtifactLoadingException {
        try {
            String partitionString = readUrl(System.getProperty("baseUrl") + Constants.URL_PARTITION);
            String partitionListString = null;
            if (partitionString != null) {
                partitionListString = partitionString
                        .substring(partitionString.indexOf('['), (partitionString.lastIndexOf(']') + 1));
            }
            return gson.fromJson(partitionListString, new TypeToken<List<Partition>>() {
            }.getType());
        } catch (IOException e) {
            String msg = "IOException in fetching partition list";
            log.error(msg);
            throw new ArtifactLoadingException(msg, e);
        }
    }
    /**
     * Method to fetch Auto Scale Policy
     *
     * @return Auto Scale Policy List
     * @throws ArtifactLoadingException
     */
    public List<AutoscalePolicy> fetchAutoscalePolicyList() throws ArtifactLoadingException {
        try {
            String autoscalePolicyString = readUrl(System.getProperty("baseUrl") + Constants.URL_POLICY_AUTOSCALE);
            String autoscalePolicyListString;
            if (autoscalePolicyString != null) {
                autoscalePolicyListString = autoscalePolicyString
                        .substring(autoscalePolicyString.indexOf('['), (autoscalePolicyString.lastIndexOf(']') + 1));
            } else {
                String msg = "Error while fetching autoscaling policies";
                log.error(msg);
                throw new ArtifactLoadingException(msg);
            }
            return gson.fromJson(autoscalePolicyListString, new TypeToken<List<AutoscalePolicy>>() {
            }.getType());
        } catch (IOException e) {
            String msg = "Failed fetching autoscaling policy list due to IOException";
            log.error(msg);
            throw new ArtifactLoadingException(msg, e);
        }
    }
    /**
     * Method to fetch Deployment Policy
     *
     * @return Deployment Policy List
     * @throws ArtifactLoadingException
     */
    public List<DeploymentPolicy> fetchDeploymentPolicyList() throws ArtifactLoadingException {
        try {
            String deploymentPolicyString = readUrl(System.getProperty("baseUrl") + Constants.URL_POLICY_DEPLOYMENT);
            String deploymentPolicyListString;
            if (deploymentPolicyString != null) {
                deploymentPolicyListString = deploymentPolicyString
                        .substring(deploymentPolicyString.indexOf('['), (deploymentPolicyString.lastIndexOf(']') + 1));
            } else {
                String msg = "Error while fetching deployment policies";
                log.error(msg);
                throw new ArtifactLoadingException(msg);
            }
            return gson.fromJson(deploymentPolicyListString, new TypeToken<List<DeploymentPolicy>>() {
            }.getType());
        } catch (IOException e) {
            String msg = "Failed fetching deployment policy list due to IOException";
            log.error(msg);
            throw new ArtifactLoadingException(msg, e);
        }
    }
    /**
     * Method to fetch Cartridges
     *
     * @return Cartridges List
     * @throws ArtifactLoadingException
     */
    public List<Cartridge> fetchCartridgeList() throws ArtifactLoadingException {
        try {
            String cartridgeString = readUrl(System.getProperty("baseUrl") + Constants.URL_CARTRIDGE);
            String cartridgeListString;
            if (cartridgeString != null) {
                cartridgeListString = cartridgeString
                        .substring(cartridgeString.indexOf('['), (cartridgeString.lastIndexOf(']') + 1));
            } else {
                String msg = "Error while fetching cartridge lists";
                log.error(msg);
                throw new ArtifactLoadingException(msg);
            }
            return gson.fromJson(cartridgeListString, new TypeToken<List<Cartridge>>() {
            }.getType());
        } catch (IOException e) {
            String msg = "Failed fetching deployment policy list due to IOException";
            log.error(msg);
            throw new ArtifactLoadingException(msg, e);
        }
    }
    /**
     * Method to fetch Cartridges
     *
     * @return Cartridges List
     * @throws ArtifactLoadingException
     */
    public List<CartridgeInfoBean> fetchSubscriptionDataList() throws ArtifactLoadingException {
        try {
            String cartridgeString = readUrl(System.getProperty("baseUrl") + Constants.URL_SUBSCRIPTION);
            String cartridgeListString;
            if (cartridgeString != null) {
                cartridgeListString = cartridgeString
                        .substring(cartridgeString.indexOf('['), (cartridgeString.lastIndexOf(']') + 1));
            } else {
                String msg = "Error while fetching subscription data list";
                log.error(msg);
                throw new ArtifactLoadingException(msg);
            }
            return gson.fromJson(cartridgeListString, new TypeToken<List<CartridgeInfoBean>>() {
            }.getType());

        } catch (IOException e) {
            String msg = "Failed fetching deployment policy list due to IOException";
            log.error(msg);
            throw new ArtifactLoadingException(msg, e);
        }
    }

    public List<SubscriptionDomainBean> fetchDomainMappingList(String cartridgeType, String subscriptionAlias)
            throws ArtifactLoadingException {
        try {
            String domainString = readUrl(
                    System.getProperty("baseUrl") + Constants.STRATOS + "cartridge" + File.separator + cartridgeType
                            + File.separator + "subscription" + File.separator + subscriptionAlias + File.separator
                            + "domains");
            String domainListString;
            if (domainString != null) {
                domainListString = domainString
                        .substring(domainString.indexOf('['), (domainString.lastIndexOf(']') + 1));
            } else {
                String msg = "Error while fetching domain mapping lists";
                log.error(msg);
                throw new ArtifactLoadingException(msg);
            }
            return gson.fromJson(domainListString, new TypeToken<List<SubscriptionDomainBean>>() {
            }.getType());
        } catch (IOException e) {
            String msg = "Failed fetching deployment policy list due to IOException";
            log.error(msg);
            throw new ArtifactLoadingException(msg, e);
        }
    }

    /**
     * Method to connect to the REST endpoint without authorization
     *
     * @param serviceEndpoint the endpoint to connect with
     * @return JSON string
     * @throws IOException in connecting to REST endpoint
     */
    private String readUrl(String serviceEndpoint) throws IOException {
        try {
            String authString = System.getProperty("userName") + ":" + System.getProperty("password");
            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
            String authStringEnc = new String(authEncBytes);

            URL absoluteURL = new URL(serviceEndpoint);
            URLConnection urlConnection = absoluteURL.openConnection();
            if (urlConnection instanceof HttpURLConnection) {
                HttpURLConnection httpConnection = (HttpURLConnection) urlConnection;
                httpConnection.setRequestMethod("GET");
                httpConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
                httpConnection.setRequestProperty("Content-Type", "application/json");
                httpConnection.setRequestProperty("Accept", "*/*");

                if (httpConnection.getResponseCode() == 200 || httpConnection.getResponseCode() == 301) {
                    InputStream is = httpConnection.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    int numCharsRead;
                    char[] charArray = new char[1024];
                    StringBuilder sb = new StringBuilder();
                    while ((numCharsRead = isr.read(charArray)) > 0) {
                        sb.append(charArray, 0, numCharsRead);
                    }
                    return sb.toString();
                } else {
                    InputStream is = httpConnection.getErrorStream();
                    InputStreamReader isr = new InputStreamReader(is);

                    int numCharsRead;
                    char[] charArray = new char[1024];
                    StringBuilder sb = new StringBuilder();
                    while ((numCharsRead = isr.read(charArray)) > 0) {
                        sb.append(charArray, 0, numCharsRead);
                    }
                    System.out.println(sb.toString());
                    log.error(sb.toString());
                    throw new RuntimeException(sb.toString());
                }

            }
            return null;

        } catch (MalformedURLException e) {
            String msg = "Invalid URL in connecting to REST endpoint";
            log.error(msg);
            throw e;
        } catch (IOException e) {
            String msg = "IO error in connecting to REST endpoint";
            log.error(msg);
            throw e;
        }
    }
}
