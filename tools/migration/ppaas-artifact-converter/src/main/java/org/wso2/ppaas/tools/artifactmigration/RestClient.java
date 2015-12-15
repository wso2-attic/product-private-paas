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

package org.wso2.ppaas.tools.artifactmigration;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.wso2.ppaas.tools.artifactmigration.exception.RestClientException;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import java.io.*;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class RestClient {
    private static final Logger log = Logger.getLogger(RestClient.class);
    private SSLSocketFactory sslSocketFactory;
    private String authStringEnc;

    public RestClient(String username,String password) throws RestClientException{
        try {
            InputStream fs = new FileInputStream(new File(getResourcesFolderPath() + "/wso2carbon.jks"));
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(fs, new String("wso2carbon").toCharArray());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, tmf.getTrustManagers(), null);
            sslSocketFactory = ctx.getSocketFactory();
            String authString = username + ":" + password;
            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
            authStringEnc = new String(authEncBytes);
        } catch (FileNotFoundException e) {
            String msg = "File not found in getting the JKS certificate";
            log.error(msg);
            throw new RestClientException(msg, e);
        } catch (KeyStoreException e) {
            String msg = "Error in creating a Key Store instance";
            log.error(msg);
            throw new RestClientException(msg, e);
        } catch (IOException e) {
            String msg = "Error in converting the key store password to a a character array";
            log.error(msg);
            throw new RestClientException(msg, e);
        } catch (NoSuchAlgorithmException e) {
            String msg = "Error in getting the Trust Manager Factory default Algorithm";
            log.error(msg);
            throw new RestClientException(msg, e);
        } catch (CertificateException e) {
            String msg = "Certificate error in loading the KeyStore from the given input stream";
            log.error(msg);
            throw new RestClientException(msg, e);
        } catch (KeyManagementException e) {
            String msg = "Error in Key Management when initializing the context";
            log.error(msg);
            throw new RestClientException(msg, e);
        }

    }

    private static String getResourcesFolderPath() {
        String path = System.getProperty("user.dir") +File.separator + ".." +File.separator + "resources";
        return StringUtils.removeEnd(path, File.separator);
    }

    public String doGet(URL resourcePath) throws RestClientException{
        try {
            HttpsURLConnection con = (HttpsURLConnection) resourcePath.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Authorization", "Basic " + authStringEnc);
            con.setSSLSocketFactory(sslSocketFactory);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "*/*");
            InputStream is = con.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);

            int numCharsRead;
            char[] charArray = new char[1024];
            StringBuffer sb = new StringBuffer();
            while ((numCharsRead = isr.read(charArray)) > 0) {
                sb.append(charArray, 0, numCharsRead);
            }
            return sb.toString();
        } catch (IOException e) {
            String msg = "IO error in getting the JSONs from the given service point";
            log.error(msg);
            throw new RestClientException(msg, e);
        }



    }

    static {
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override public boolean verify(String s, SSLSession sslSession) {
                System.out.println();
                return true;
            }
        });
    }

}