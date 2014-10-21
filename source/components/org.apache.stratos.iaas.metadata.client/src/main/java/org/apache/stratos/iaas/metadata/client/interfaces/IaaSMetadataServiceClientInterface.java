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

package org.apache.stratos.iaas.metadata.client.interfaces;

import org.apache.stratos.iaas.metadata.client.exceptions.IaaSMetadataServiceClientException;
import org.apache.stratos.iaas.metadata.client.model.InstanceMetadata;

public interface IaaSMetadataServiceClientInterface {
	
	/**
	 * Get all metadata
	 * @return {@link InstanceMetadata}
	 * @throws IaaSMetadataServiceClientException
	 */
	public InstanceMetadata getInstanceMetadata() throws IaaSMetadataServiceClientException;
	
	/**
	 * Get instance-id
	 * @return instance-id
	 * @throws IaaSMetadataServiceClientException
	 */
	public String getInstanceId() throws IaaSMetadataServiceClientException;
	
	/**
	 * Get ami-id
	 * @return ami-id
	 * @throws IaaSMetadataServiceClientException
	 */
	public String getAmiId() throws IaaSMetadataServiceClientException;
	
	/**
	 * Get hostname
	 * @return hostname
	 * @throws IaaSMetadataServiceClientException
	 */
	public String getHostName() throws IaaSMetadataServiceClientException;
	
	/**
	 * Get instance-type
	 * @return instance-type
	 * @throws IaaSMetadataServiceClientException
	 */
	public String getInstanceType() throws IaaSMetadataServiceClientException;
	
	/**
	 * Get local hostname
	 * @return local-hostname
	 * @throws IaaSMetadataServiceClientException
	 */
	public String getLocalHostname() throws IaaSMetadataServiceClientException;
	
	/**
	 * Get local ipv4
	 * @return local-ipv4
	 * @throws IaaSMetadataServiceClientException
	 */
	public String getLocalIpv4() throws IaaSMetadataServiceClientException;
	
	/**
	 * Get public hostname
	 * @return public-hostname
	 * @throws IaaSMetadataServiceClientException
	 */
	public String getPublicHostname() throws IaaSMetadataServiceClientException;
	
	/**
	 * Get public ipv4
	 * @return public-ipv4
	 * @throws IaaSMetadataServiceClientException
	 */
	public String getPublicIpv4() throws IaaSMetadataServiceClientException;
}
