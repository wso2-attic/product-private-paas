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
package org.apache.stratos.iaas.metadata.client.model;

import org.apache.stratos.iaas.metadata.client.constants.InstanceMetadataConstants;

/**
 * Holding metadata retrived from Instance Metadata Service
 * @author rajkumar
 *
 */
public class InstanceMetadata {

	private String instanceId;
	private String amiId;
	private String hostName;
	private String instanceType;
	private String localHostname;
	private String localIpv4;
	private String publicHostname;
	private String publicIpv4;
	
	public InstanceMetadata(String instanceId) {
		this.instanceId = instanceId;
	}
	
	public String getInstanceId() {
		return instanceId;
	}
	
	public String getAmiId() {
		return amiId;
	}
	
	public void setAmiId(String amiId) {
		this.amiId = amiId;
	}
	
	public String getHostName() {
		return hostName;
	}
	
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	
	public String getInstanceType() {
		return instanceType;
	}
	
	public void setInstanceType(String instanceType) {
		this.instanceType = instanceType;
	}
	
	public String getLocalHostname() {
		return localHostname;
	}
	
	public void setLocalHostname(String localHostname) {
		this.localHostname = localHostname;
	}
	
	public String getLocalIpv4() {
		return localIpv4;
	}
	
	public void setLocalIpv4(String localIpv4) {
		this.localIpv4 = localIpv4;
	}
	
	public String getPublicHostname() {
		return publicHostname;
	}
	
	public void setPublicHostname(String publicHostname) {
		this.publicHostname = publicHostname;
	}
	
	public String getPublicIpv4() {
		return publicIpv4;
	}

	public void setPublicIpv4(String publicIpv4) {
		this.publicIpv4 = publicIpv4;
	}
	
	@Override
	public String toString() {
		return String.format("InstanceMetadata : [%s = %s, %s = %s, %s = %s, %s = %s, %s = %s, %s = %s, %s = %s,]", 
				InstanceMetadataConstants.INSTANCE_ID, this.instanceId, 
				InstanceMetadataConstants.PUBLIC_HOST_NAME, this.publicHostname,
				InstanceMetadataConstants.PUBLIC_IPV4, this.publicIpv4,
				InstanceMetadataConstants.LOCAL_HOST_NAME, this.localHostname,
				InstanceMetadataConstants.LOCAL_IPV4, this.localIpv4,
				InstanceMetadataConstants.AMI_ID, this.amiId,
				InstanceMetadataConstants.INSTANCE_TYPE, this.instanceType);
	}
}
