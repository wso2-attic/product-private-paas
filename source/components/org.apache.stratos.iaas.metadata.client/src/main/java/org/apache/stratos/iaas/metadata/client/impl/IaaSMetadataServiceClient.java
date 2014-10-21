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
package org.apache.stratos.iaas.metadata.client.impl;

import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.http.client.utils.URIBuilder;
import org.apache.stratos.iaas.metadata.client.constants.InstanceMetadataConstants;
import org.apache.stratos.iaas.metadata.client.exceptions.IaaSMetadataServiceClientException;
import org.apache.stratos.iaas.metadata.client.interfaces.IaaSMetadataServiceClientInterface;
import org.apache.stratos.iaas.metadata.client.model.InstanceMetadata;
import org.apache.stratos.iaas.metadata.client.rest.IaasMetadataResponse;
import org.apache.stratos.iaas.metadata.client.rest.RestClient;

public class IaaSMetadataServiceClient implements IaaSMetadataServiceClientInterface {

	private static final Log log = LogFactory.getLog(IaaSMetadataServiceClient.class);
	private RestClient restClient;
	private String baseURL;

	public IaaSMetadataServiceClient(String baseURL) {
		this.restClient = new RestClient();
		this.baseURL = baseURL;
	}
	
	public IaaSMetadataServiceClient() {
		this("http://169.254.169.254/latest/meta-data/");
	}

	@Override
	public InstanceMetadata getInstanceMetadata() throws IaaSMetadataServiceClientException {
		
		try {
			String instanceId = getInstanceId();
			String amiId = getAmiId();
			String hostName = getHostName();
			String instanceType = getInstanceType();
			String localHostName = getLocalHostname();
			String localIpv4 = getLocalIpv4();
			String publicHostName = getPublicHostname();
			String publicIpv4 = getPublicIpv4();
			
			InstanceMetadata metadata = new InstanceMetadata(instanceId);
			metadata.setAmiId(amiId);
			metadata.setHostName(hostName);
			metadata.setInstanceType(instanceType);
			metadata.setLocalHostname(localHostName);
			metadata.setLocalIpv4(localIpv4);
			metadata.setPublicHostname(publicHostName);
			metadata.setPublicIpv4(publicIpv4);

			return metadata;

		} catch (IaaSMetadataServiceClientException e) {
			throw e;
		} catch (Exception e) {
			String msg = "Error while retrieving metadata";
			log.error(msg, e);
			throw new IaaSMetadataServiceClientException(msg, e);
		}
	}
	
	@Override
	public String getInstanceId() throws IaaSMetadataServiceClientException {

		try {
			if (log.isDebugEnabled()) {
				String msg = String.format("Retrieving %s from Instance Metadata Service", InstanceMetadataConstants.INSTANCE_ID);
				log.debug(msg);
			}
			
			URI uri = new URIBuilder(baseURL + InstanceMetadataConstants.INSTANCE_ID).build();
			IaasMetadataResponse res = restClient.doGet(uri);
			
			String message = String.format("Metadata [%s] retrieval failed.", InstanceMetadataConstants.INSTANCE_ID);
			handleNullResponse(message, res);

			if (res.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
				String msg = String.format("Metadata [%s] doesn't exist.", InstanceMetadataConstants.INSTANCE_ID);
				log.error(msg);
				throw new IaaSMetadataServiceClientException(msg);
			}

			String content = res.getContent();
			if (log.isDebugEnabled()) {
				String msg = String.format("Succesfully retrievied %s from Instance Metadata Service. %s = %s", 
						InstanceMetadataConstants.INSTANCE_ID, InstanceMetadataConstants.INSTANCE_ID, content);
				log.debug(msg);
			}
			return content;

		} catch (IaaSMetadataServiceClientException e) {
			throw e;
		} catch (Exception e) {
			String msg = String.format("Error while retrieving metadata [%s]", InstanceMetadataConstants.INSTANCE_ID);
			log.error(msg, e);
			throw new IaaSMetadataServiceClientException(msg, e);
		}
	}

	@Override
	public String getAmiId() throws IaaSMetadataServiceClientException {
		
		try {
			if (log.isDebugEnabled()) {
				String msg = String.format("Retrieving %s from Instance Metadata Service", InstanceMetadataConstants.AMI_ID);
				log.debug(msg);
			}
			
			URI uri = new URIBuilder(baseURL + InstanceMetadataConstants.AMI_ID).build();
			IaasMetadataResponse res = restClient.doGet(uri);

			String message = String.format("Metadata [%s] retrieval failed.", InstanceMetadataConstants.AMI_ID);
			handleNullResponse(message, res);

			if (res.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
				String msg = String.format("Metadata [%s] doesn't exist.", InstanceMetadataConstants.AMI_ID);
				log.error(msg);
				throw new IaaSMetadataServiceClientException(msg);
			}

			String content = res.getContent();
			if (log.isDebugEnabled()) {
				String msg = String.format("Succesfully retrievied %s from Instance Metadata Service. %s = %s", 
						InstanceMetadataConstants.AMI_ID, InstanceMetadataConstants.AMI_ID, content);
				log.debug(msg);
			}
			return content;

		} catch (IaaSMetadataServiceClientException e) {
			throw e;
		} catch (Exception e) {
			String msg = String.format("Error while retrieving metadata [%s]", InstanceMetadataConstants.AMI_ID);
			log.error(msg, e);
			throw new IaaSMetadataServiceClientException(msg, e);
		}
	}

	@Override
	public String getHostName() throws IaaSMetadataServiceClientException {
	
		try {
			if (log.isDebugEnabled()) {
				String msg = String.format("Retrieving %s from Instance Metadata Service", InstanceMetadataConstants.HOST_NAME);
				log.debug(msg);
			}
			
			URI uri = new URIBuilder(baseURL + InstanceMetadataConstants.HOST_NAME).build();
			IaasMetadataResponse res = restClient.doGet(uri);
			
			String message = String.format("Metadata [%s] retrieval failed.", InstanceMetadataConstants.HOST_NAME);
			handleNullResponse(message, res);

			if (res.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
				String msg = String.format("Metadata [%s] doesn't exist.", InstanceMetadataConstants.HOST_NAME);
				log.error(msg);
				throw new IaaSMetadataServiceClientException(msg);
			}

			String content = res.getContent();
			if (log.isDebugEnabled()) {
				String msg = String.format("Succesfully retrievied %s from Instance Metadata Service. %s = %s", 
						InstanceMetadataConstants.HOST_NAME, InstanceMetadataConstants.HOST_NAME, content);
				log.debug(msg);
			}
			return content;

		} catch (IaaSMetadataServiceClientException e) {
			throw e;
		} catch (Exception e) {
			String msg = String.format("Error while retrieving metadata [%s]", InstanceMetadataConstants.HOST_NAME);
			log.error(msg, e);
			throw new IaaSMetadataServiceClientException(msg, e);
		}
	}

	@Override
	public String getInstanceType() throws IaaSMetadataServiceClientException {
		
		try {
			if (log.isDebugEnabled()) {
				String msg = String.format("Retrieving %s from Instance Metadata Service", InstanceMetadataConstants.INSTANCE_TYPE);
				log.debug(msg);
			}
			
			URI uri = new URIBuilder(baseURL + InstanceMetadataConstants.INSTANCE_TYPE).build();
			IaasMetadataResponse res = restClient.doGet(uri);
			
			String message = String.format("Metadata [%s] retrieval failed.", InstanceMetadataConstants.INSTANCE_TYPE);
			handleNullResponse(message, res);

			if (res.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
				String msg = String.format("Metadata [%s] doesn't exist.", InstanceMetadataConstants.INSTANCE_TYPE);
				log.error(msg);
				throw new IaaSMetadataServiceClientException(msg);
			}

			String content = res.getContent();
			if (log.isDebugEnabled()) {
				String msg = String.format("Succesfully retrievied %s from Instance Metadata Service. %s = %s", 
						InstanceMetadataConstants.INSTANCE_TYPE, InstanceMetadataConstants.INSTANCE_TYPE, content);
				log.debug(msg);
			}
			return content;

		} catch (IaaSMetadataServiceClientException e) {
			throw e;
		} catch (Exception e) {
			String msg = String.format("Error while retrieving metadata [%s]", InstanceMetadataConstants.INSTANCE_TYPE);
			log.error(msg, e);
			throw new IaaSMetadataServiceClientException(msg, e);
		}
	}

	@Override
	public String getLocalHostname() throws IaaSMetadataServiceClientException {

		try {
			if (log.isDebugEnabled()) {
				String msg = String.format("Retrieving %s from Instance Metadata Service", InstanceMetadataConstants.LOCAL_HOST_NAME);
				log.debug(msg);
			}
			
			URI uri = new URIBuilder(baseURL + InstanceMetadataConstants.LOCAL_HOST_NAME).build();
			IaasMetadataResponse res = restClient.doGet(uri);
			
			String message = String.format("Metadata [%s] retrieval failed.", InstanceMetadataConstants.LOCAL_HOST_NAME);
			handleNullResponse(message, res);

			if (res.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
				String msg = String.format("Metadata [%s] doesn't exist.", InstanceMetadataConstants.LOCAL_HOST_NAME);
				log.error(msg);
				throw new IaaSMetadataServiceClientException(msg);
			}

			String content = res.getContent();
			if (log.isDebugEnabled()) {
				String msg = String.format("Succesfully retrievied %s from Instance Metadata Service. %s = %s", 
						InstanceMetadataConstants.LOCAL_HOST_NAME, InstanceMetadataConstants.LOCAL_HOST_NAME, content);
				log.debug(msg);
			}
			return content;

		} catch (IaaSMetadataServiceClientException e) {
			throw e;
		} catch (Exception e) {
			String msg = String.format("Error while retrieving metadata [%s]", InstanceMetadataConstants.LOCAL_HOST_NAME);
			log.error(msg, e);
			throw new IaaSMetadataServiceClientException(msg, e);
		}
	}

	@Override
	public String getLocalIpv4() throws IaaSMetadataServiceClientException {

		try {
			if (log.isDebugEnabled()) {
				String msg = String.format("Retrieving %s from Instance Metadata Service", InstanceMetadataConstants.LOCAL_IPV4);
				log.debug(msg);
			}
			
			URI uri = new URIBuilder(baseURL + InstanceMetadataConstants.LOCAL_IPV4).build();
			IaasMetadataResponse res = restClient.doGet(uri);
			
			String message = String.format("Metadata [%s] retrieval failed.", InstanceMetadataConstants.LOCAL_IPV4);
			handleNullResponse(message, res);

			if (res.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
				String msg = String.format("Metadata [%s] doesn't exist.", InstanceMetadataConstants.LOCAL_IPV4);
				log.error(msg);
				throw new IaaSMetadataServiceClientException(msg);
			}

			String content = res.getContent();
			if (log.isDebugEnabled()) {
				String msg = String.format("Succesfully retrievied %s from Instance Metadata Service. %s = %s", 
						InstanceMetadataConstants.LOCAL_IPV4, InstanceMetadataConstants.LOCAL_IPV4, content);
				log.debug(msg);
			}
			return content;

		} catch (IaaSMetadataServiceClientException e) {
			throw e;
		} catch (Exception e) {
			String msg = String.format("Error while retrieving metadata [%s]", InstanceMetadataConstants.LOCAL_IPV4);
			log.error(msg, e);
			throw new IaaSMetadataServiceClientException(msg, e);
		}
	}

	@Override
	public String getPublicHostname() throws IaaSMetadataServiceClientException {

		try {
			if (log.isDebugEnabled()) {
				String msg = String.format("Retrieving %s from Instance Metadata Service", InstanceMetadataConstants.PUBLIC_HOST_NAME);
				log.debug(msg);
			}
			
			URI uri = new URIBuilder(baseURL + InstanceMetadataConstants.PUBLIC_HOST_NAME).build();
			IaasMetadataResponse res = restClient.doGet(uri);
			
			String message = String.format("Metadata [%s] retrieval failed.", InstanceMetadataConstants.PUBLIC_HOST_NAME);
			handleNullResponse(message, res);

			if (res.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
				String msg = String.format("Metadata [%s] doesn't exist.", InstanceMetadataConstants.PUBLIC_HOST_NAME);
				log.error(msg);
				throw new IaaSMetadataServiceClientException(msg);
			}

			String content = res.getContent();
			if (log.isDebugEnabled()) {
				String msg = String.format("Succesfully retrievied %s from Instance Metadata Service. %s = %s", 
						InstanceMetadataConstants.PUBLIC_HOST_NAME, InstanceMetadataConstants.PUBLIC_HOST_NAME, content);
				log.debug(msg);
			}
			return content;

		} catch (IaaSMetadataServiceClientException e) {
			throw e;
		} catch (Exception e) {
			String msg = String.format("Error while retrieving metadata [%s]", InstanceMetadataConstants.PUBLIC_HOST_NAME);
			log.error(msg, e);
			throw new IaaSMetadataServiceClientException(msg, e);
		}
	}

	@Override
	public String getPublicIpv4() throws IaaSMetadataServiceClientException {
		
		try {
			if (log.isDebugEnabled()) {
				String msg = String.format("Retrieving %s from Instance Metadata Service", InstanceMetadataConstants.PUBLIC_IPV4);
				log.debug(msg);
			}
			
			URI uri = new URIBuilder(baseURL + InstanceMetadataConstants.PUBLIC_IPV4).build();
			IaasMetadataResponse res = restClient.doGet(uri);
			
			String message = String.format("Metadata [%s] retrieval failed.", InstanceMetadataConstants.PUBLIC_IPV4);
			handleNullResponse(message, res);

			if (res.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
				String msg = String.format("Metadata [%s] doesn't exist.", InstanceMetadataConstants.PUBLIC_IPV4);
				log.error(msg);
				throw new IaaSMetadataServiceClientException(msg);
			}

			String content = res.getContent();
			if (log.isDebugEnabled()) {
				String msg = String.format("Succesfully retrievied %s from Instance Metadata Service. %s = %s", 
						InstanceMetadataConstants.PUBLIC_IPV4, InstanceMetadataConstants.PUBLIC_IPV4, content);
				log.debug(msg);
			}
			return content;

		} catch (IaaSMetadataServiceClientException e) {
			throw e;
		} catch (Exception e) {
			String msg = String.format("Error while retrieving metadata [%s]", InstanceMetadataConstants.PUBLIC_IPV4);
			log.error(msg, e);
			throw new IaaSMetadataServiceClientException(msg, e);
		}
	}	
	
    private void handleNullResponse(String message, IaasMetadataResponse res)
            throws IaaSMetadataServiceClientException {
        if (res == null) {
            log.error(message+ " Null response receieved.");
            throw new IaaSMetadataServiceClientException(message);
        }
    }

    public String getBaseURL() {
        return baseURL;
    }
}
