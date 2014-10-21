/*
 * Licensed to the Apache Software Foundation (ASF) under one 
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY 
 * KIND, either express or implied.  See the License for the 
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.stratos.iaas.metadata.client.rest;

import java.net.URI;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;

public class RestClient {

    private DefaultHttpClient httpClient;
    
    public RestClient() {
        httpClient = new DefaultHttpClient();
    }

    public IaasMetadataResponse doGet(URI resourcePath) throws Exception{
        HttpGet getRequest = null;
        try {
            getRequest = new HttpGet(resourcePath);
            getRequest.addHeader("Content-Type", "application/json");

            IaasMetadataResponse response = httpClient.execute(getRequest, new IaasMetadataResponseHandler());
            return response;
        } finally {
            releaseConnection(getRequest);
        }
    }

    private void releaseConnection(HttpRequestBase request) {
        if (request != null) {
            request.releaseConnection();
        }
    }
}
