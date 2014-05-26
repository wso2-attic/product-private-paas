#!/bin/bash
# ----------------------------------------------------------------------------
#  Copyright 2005-2013 WSO2, Inc. http://www.wso2.org
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
# ----------------------------------------------------------------------------

# Pack files
export ACTIVE_MQ_DISTRIBUTION=apache-activemq-5.9.1-bin.tar.gz # Relavent activemq distribution
export ACTIVE_MQ_EXTRACTED=apache-activemq-5.9.1 # Extracted activemq distribution folder name

export JAVA_FILE_DISTRUBUTION=jdk-7u51-linux-x64.tar.gz # Relevant JDK distribution
export JAVA_NAME_EXTRACTED=jdk1.7.0_51 # Extracted JDK folder name

export MYSQL_CONNECTOR=mysql-connector-java-5.1.29-bin.jar # Relevant MySQL connector

# General configuration
export JAVA_HOME=${JAVA_HOME:-}
export log_path=/var/log/apache-stratos
export stratos_domain=""
export machine_ip=""
export host_user=""

# Puppet master configuration
export skip_puppet=""
export puppet_external=""
export puppet_external_ip=""
export puppet_external_host=""

# IaaS configuration
export iaas=""
# Region Name
export region=""
# Cartridge base image
export cartridge_base_img_id=""

# OpenStack
export os_identity=""
export os_credentials=""
export os_jclouds_endpoint=""
export os_keypair_name=""
export os_security_groups=""

# EC2
export ec2_identity=""
export ec2_credentials=""
export ec2_owner_id=""
export ec2_keypair_name=""
export ec2_security_groups=""
export ec2_availability_zone=""

# vCloud
export vcloud_identity=""
export vcloud_credentials=""
export vcloud_jclouds_endpoint=""

# MySQL configuration
export setup_mysql=""
export mysql_host=""
export mysql_port=""
export mysql_uname=""
export mysql_password=""

# WSO2 PPaaS services
export as_enabled=""
export bps_enabled=""
export esb_enabled=""
export is_enabled=""
export apim_enabled=""

# Worker Manager deployment
export as_worker_mgt_enabled=""
export bps_worker_mgt_enabled=""
export esb_worker_mgt_enabled=""
export is_worker_mgt_enabled=""

# Clustering of services
export as_clustering_enabled=""
export is_clustering_enabled=""
export esb_clustering_enabled=""
export bps_clustering_enabled=""

# WSO2 PPaaS core services
export bam_enabled=""
export config_sso_enabled=""
export wso2_ppaas_enabled=""
