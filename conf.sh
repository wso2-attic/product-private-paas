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
export auto_start_servers="true"

# External Puppetmaster
export puppet_external=""
export puppet_external_ip=""
export puppet_external_host=""

# IaaS configuration
export iaas=""
export os_identity=""
export os_credentials=""
export os_jclouds_endpoint=""
export cartridge_base_img_id=""
export region=""
export os_keypair_name=""
export os_security_groups=""
export ec2_identity=""
export ec2_credentials=""
export ec2_owner_id=""
export ec2_keypair_name=""
export ec2_security_groups=""
export ec2_availability_zone=""
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
export as_needed=""
export bps_needed=""
export esb_needed=""
export is_needed=""
export apim_needed=""

# Clustering of services
export as_clustering=""
export is_clustering=""
export esb_clustering=""
export bps_clustering=""

# WSO2 PPaaS core services
export bam_needed=""
export config_sso=""
export wso2_ppaas_confirm=""
