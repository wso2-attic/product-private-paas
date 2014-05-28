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

# Define error handling function
function error_handler() {
        MYSELF="$0"               # equals to script name
        LASTLINE="$1"            # argument 1: last line of error occurence
        LASTERR="$2"             # argument 2: error code of last command
        echo "ERROR in ${MYSELF}: line ${LASTLINE}: exit status of last command: ${LASTERR}"
	exit 1       
}

# Execute error_handler function on script error
trap 'error_handler ${LINENO} $?' ERR

dir=`dirname $0`
current_dir=`cd $dir;pwd`
source "$current_dir/conf.sh"

# Global configuration variables
export public_ip=$(curl -s http://checkip.dyndns.org | awk '{print $6}' | awk -F'<' '{print $1}')
setup_path="$current_dir/stratos-installer"
stratos_pack_path="$current_dir/packs"
stratos_install_path="$current_dir/install"
resource_path="$current_dir/resources"
cep_artifact_path="$resource_path/cep/artifacts/"
puppet_env="stratos"
puppet_installed="false"
cep_port=7611
activemq_client_libs=(activemq-broker-5.9.1.jar  activemq-client-5.9.1.jar  geronimo-j2ee-management_1.1_spec-1.0.1.jar  geronimo-jms_1.1_spec-1.1.1.jar  hawtbuf-1.9.jar)
export LOG=$log_path/stratos-setup.log

# Usage modes
silent_mode="false"
puppet_only="false"
deploy_services="true"

# Registry databases
registry_db="registry"

# Config databases
sm_config_db="sm_config"
as_config_db="as_config"
apim_db="apim_db"
apim_stats_db="amstats"
esb_config_db="esb_config"
is_config_db="is_config"
bps_config_db="bps_config"

# GReg Mount path
# Using the same target mount path for now
as_config_path="config"
esb_config_path="config"
bps_config_path="config"
is_config_path="config"

# Using the same config DB for store and publisher
apim_store_config_db="apim_store_config"
#apim_publisher_config_db="apim_publisher_config"
apim_gateway_config_db="apim_gateway_config"
apim_keymanager_config_db="apim_keymanager_config"

function help() {
    echo ""
    echo "This script will install WSO2 Private PaaS 4.0"
    echo "usage:"
    echo "boot.sh [ -p | -s | -t ]"
    echo "   -p : Puppet only mode. This will only deploy and configure Puppet scripts."
    echo "   -s : Silent mode. This will start core services without performing any configuration."   
    echo "   -t : Configuring and start PaaS mode. This will configure and start the core services, but will not deploy any Cartridge service."   
    echo ""
}

function backup_file() {
    if [[  -f "$1.orig" ]]; then
        echo "Restoring from the Original template file $1"
	cp -f "$1" "$1.last"
        cp -f "$1.orig" "$1"
    else
        echo -e "Creating a back-up of the file $1"
        cp -f "$1" "$1.orig"
    fi
}

function replace_setup_conf() {
    #echo "Setting value $2 for property $1 as $2"
    setp_conf_file=$setup_path/conf/setup.conf
    sed -i s$'\001'"$1"$'\001'"$2"$'\001''g' $setp_conf_file
}

function replace_in_file() {
    #echo "Setting value $2 for property $1 as $2 in file $3"
    sed -i "s%$1%$2%g"  $3
}

function install_mysql() {
    export DEBIAN_FRONTEND=noninteractive
    sudo apt-get -q -y install mysql-server  --force-yes

    echo -e "Setting MySQL password $mysql_password for $mysql_uname
    mysqladmin -u $mysql_uname password $mysql_password  ";
}

function create_registry_database(){
    echo -e "Creating registry database: $1"
    backup_file "$resource_path/dbscripts/registry.sql"
    sed -i "s@REGISTRY_DB_SCHEMA@$1@g"  "$resource_path/dbscripts/registry.sql"
    mysql -u$mysql_uname -p$mysql_password -h$mysql_host < "$resource_path/dbscripts/registry.sql"   
}

function create_config_database() {
    echo -e "Creating config database: $1"
    backup_file "$resource_path/dbscripts/config.sql"    
    sed -i "s@CONFIG_DB_SCHEMA@$1@g"  "$resource_path/dbscripts/config.sql"
    mysql -u$mysql_uname -p$mysql_password -h$mysql_host < "$resource_path/dbscripts/config.sql"   
}

function create_apim_database() {
    echo -e "Creating APIM database: $1"    
    backup_file "$resource_path/dbscripts/apim.sql"
    sed -i "s@APIM_DB_SCHEMA@$1@g"  "$resource_path/dbscripts/apim.sql"
    mysql -u$mysql_uname -p$mysql_password -h$mysql_host < "$resource_path/dbscripts/apim.sql"
}

function install_puppet() {
    args=("-m" "-d${stratos_domain}")
    echo -e "\nRunning puppet installation with arguments: ${args[@]}" 
	
    cwd=$(pwd)
    cd puppetinstall
    /bin/bash puppetinstall "${args[@]}"
    cd $cwd
}

function list_ec2_regions() {
    echo -e "   Below are the available regions in Amazon EC2"
    echo -e "   ap-northeast-1 - Asia Pacific (Tokyo) Region"
    echo -e "   ap-southeast-1 - Asia Pacific (Singapore) Region"
    echo -e "   ap-southeast-2 - Asia Pacific (Sydney) Region"
    echo -e "   eu-west-1 - EU (Ireland) Region"
    echo -e "   sa-east-1 - South America (Sao Paulo) Region"
    echo -e "   us-east-1 - US East (Northern Virginia) Region"
    echo -e "   us-west-1 - US West (Northern California) Region"
    echo -e "   us-west-2 - US West (Oregon) Region"
}

function list_ip_addreses() { 
 /sbin/ifconfig | grep "inet addr" | awk -F: '{print $2}' | awk '{print $1}'
}

function check_for_puppet() {
# Checking for puppet
    if which puppet > /dev/null 2>&1; then
	puppet_installed="true"
    fi
}

function read_user_input() {    
    if [[ "$3" = "" ]] ; then        
        read $2 -p "$1" user_input        
        echo -n $user_input
    else
        echo $3
    fi
}

function setup_apache_stratos() {
    # Configure IaaS properties
    iaas=$(read_user_input "Enter your IaaS. vCloud, EC2 and OpenStack are the currently supported IaaSs. Enter \"vcloud\" for vCloud, \"ec2\" for EC2 and \"os\" for OpenStack: " "" $iaas )

    if [[ "$iaas" == "os" ]]; then
        echo -e "You selected OpenStack. "
        os_identity=$(read_user_input "Enter OpenStack identity : " "" $os_identity )
        os_credentials=$(read_user_input "Enter OpenStack credentials : " "-s" $os_credentials )
        echo ""
        os_jclouds_endpoint=$(read_user_input "Enter OpenStack jclouds_endpoint : " "" $os_jclouds_endpoint )
        region=$(read_user_input "Enter the region of the IaaS you want to spin up instances : " "" $region )
        os_keypair_name=$(read_user_input "Enter OpenStack keypair name : " "" $os_keypair_name )
        os_security_groups=$(read_user_input "Enter OpenStack security groups : " "" $os_security_groups )
        cartridge_base_img_id=$(read_user_input "Enter OpenStack cartridge base image id : " "" $cartridge_base_img_id )

    elif [[ "$iaas" == "ec2" ]]; then
        echo -e "You selected Amazon EC2. "
        ec2_vpc=$(read_user_input "Are you in a EC2 VPC Environment? [y/n] : " "" $ec2_vpc )
        ec2_identity=$(read_user_input "Enter EC2 identity : " "" $ec2_identity )
        ec2_credentials=$(read_user_input "Enter EC2 credentials : " "-s" $ec2_credentials )
        echo ""
        ec2_owner_id=$(read_user_input "Enter EC2 owner id : " "" $ec2_owner_id )
        ec2_keypair_name=$(read_user_input "Enter EC2 keypair name : " "" $ec2_keypair_name )
        ec2_availability_zone=$(read_user_input "Enter EC2 availability zone : " "" $ec2_availability_zone )

        if [[ "$ec2_vpc" == "y" ]]; then
        	ec2_security_group_ids=$(read_user_input "Enter EC2 security group IDs : " "" $ec2_security_group_ids )
        	ec2_subnet_id=$(read_user_input "Enter EC2 VPC Subnet ID : " "" $ec2_subnet_id )
        else
        	ec2_security_groups=$(read_user_input "Enter EC2 security groups : " "" $ec2_security_groups )
	fi
	if [[ "$deploy_services" == "true" ]]; then
        	list_ec2_regions
        	region=$(read_user_input "Enter the region of the IaaS you want to spin up instances : " "" $region )
        	cartridge_base_img_id=$(read_user_input "Enter EC2 cartridge base image id : " "" $cartridge_base_img_id )
        fi

    elif [[ "$iaas" == "vcloud" ]];then
        vcloud_identity=$(read_user_input "Enter vCloud identity : " "" $vcloud_identity )
        vcloud_credentials=$(read_user_input "Enter vCloud credentials : " "-s" $vcloud_credentials )
        echo ""
        vcloud_jclouds_endpoint=$(read_user_input "Enter vCloud jclouds_endpoint : " "" $vcloud_jclouds_endpoint )
        cartridge_base_img_id=$(read_user_input "Enter vCloud cartridge base image id : " "" $cartridge_base_img_id )
    fi

    # Update Apache Stratos setup.conf 
    if [[ "$iaas" == "os" ]]; then
        replace_setup_conf "OS_ENABLED" "true"
        replace_setup_conf "OS_IDENTITY" "$os_identity"
        replace_setup_conf "OS_CREDENTIAL" "$os_credentials"
        replace_setup_conf "OS_JCLOUDS_ENDPOINT" "$os_jclouds_endpoint"
        replace_setup_conf "OS_KEYPAIR_NAME" "$os_keypair_name"
        replace_setup_conf "OS_SECURITY_GROUPS" "$os_security_groups"
    elif [[ "$iaas" == "ec2" ]]; then
        replace_setup_conf "EC2_ENABLED" "true"
        replace_setup_conf "EC2_VPC" "$ec2_vpc"
        replace_setup_conf "EC2_IDENTITY" "$ec2_identity"
        replace_setup_conf "EC2_CREDENTIAL" "$ec2_credentials"
        replace_setup_conf "EC2_KEYPAIR_NAME" "$ec2_keypair_name"
        replace_setup_conf "EC2_OWNER_ID" "$ec2_owner_id"
        replace_setup_conf "EC2_AVAILABILITY_ZONE" "$ec2_availability_zone"
        replace_setup_conf "EC2_SECURITY_GROUPS" "$ec2_security_groups"
        replace_setup_conf "EC2_SECURITY_GROUP_IDS" "$ec2_security_group_ids"
        replace_setup_conf "EC2_SUBNET_ID" "$ec2_subnet_id"
        replace_setup_conf "EC2_ASSOCIATE_PUBLIC_IP" "$ec2_associate_public_ip_address"
    elif [[ "$iaas" == "vcloud" ]]; then
        replace_setup_conf "VCLOUD_ENABLED" "true"
        replace_setup_conf "VCLOUD_IDENTITY" "$vcloud_identity"
        replace_setup_conf "VCLOUD_CREDENTIAL" "$vcloud_credentials"
        replace_setup_conf "VCLOUD_JCLOUDS_ENDPOINT" "$vcloud_jclouds_endpoint"
    fi
    
    replace_setup_conf "LOG_PATH" "$log_path"
    replace_setup_conf "STRATOS_SETUP_PATH" "$setup_path"
    replace_setup_conf "PACK_PATH" "$stratos_pack_path"
    replace_setup_conf "INSTALLER_PATH" "$stratos_install_path"
    replace_setup_conf "JAVAHOME" "$JAVA_HOME"
    replace_setup_conf "HOST_USER" "$host_user"
    replace_setup_conf "STRATOS_DOMAIN" "$stratos_domain"
    replace_setup_conf "machine_ip" "$machine_ip"
    replace_setup_conf "CONFIG_MB" "$CONFIG_MB"
    replace_setup_conf "puppet-ip" "$puppet_ip"
    replace_setup_conf "puppet-host" "$puppet_host"
    replace_setup_conf "puppet-environment" "$puppet_env"
    replace_setup_conf "CEP_ARTIFACTS_PATH" "$cep_artifact_path"
    replace_setup_conf "DB_HOST" "$mysql_host"
    replace_setup_conf "DB_HOST" "$mysql_host"
    replace_setup_conf "DB_PORT" "$mysql_port"
    replace_setup_conf "DB_USER" "$mysql_uname"
    replace_setup_conf "DB_PASSWORD" "$mysql_password"
    replace_setup_conf "REGISTRY_DB" "$registry_db"
    replace_setup_conf "CONFIG_DB" "$sm_config_db"   

    run_setup_sh

}

function  run_setup_sh() {
	/bin/bash $setup_path/setup.sh -p "default"
}

function get_service_deployment_confirmations() {
    as_needed=$(read_user_input "Do you need to deploy AS (Application Server) service? [y/n] " "" $as_enabled )
    if [[ $as_needed =~ ^[Yy]$ ]]; then
       as_enabled="true"

       as_worker_mgt_needed=$(read_user_input "Do you need to deploy AS (Application Server) in worker manager setup? [y/n] " "" $as_worker_mgt_enabled )
       if [[ $as_worker_mgt_needed =~ ^[Yy]$ ]]; then
          as_worker_mgt_enabled="true"
          as_clustering_enabled="true"
       else
          clustering_appserver=$(read_user_input "Do you need to enable clustering for AS? [y/n] " "" $as_clustering_enabled )
          if [[ $clustering_appserver =~ ^[Yy]$ ]]; then 
             as_clustering_enabled="true"
          else
             as_clustering_enabled="false"
          fi
       fi
    fi

    bps_needed=$(read_user_input "Do you need to deploy BPS (Business Process Server) service? [y/n] " "" $bps_enabled )
    if [[ $bps_needed =~ ^[Yy]$ ]]; then
       bps_enabled="true"
     
       bps_worker_mgt_needed=$(read_user_input "Do you need to deploy BPS (Business Process Server) in worker manager setup? [y/n] " "" $bps_worker_mgt_enabled )
       if [[ $bps_worker_mgt_needed =~ ^[Yy]$ ]]; then
          bps_worker_mgt_enabled="true"
          bps_clustering_enabled="true"
       else
          clustering_bps=$(read_user_input "Do you need to enable clustering for BPS? [y/n] " "" $bps_clustering_enabled )
          if [[ $clustering_bps =~ ^[Yy]$ ]]; then
             bps_clustering_enabled="true"
          else
             bps_clustering_enabled="false"
          fi
       fi      
    fi

    esb_needed=$(read_user_input "Do you need to deploy ESB (Enterprise Service Bus) service? [y/n] " "" $esb_enabled )
    if [[ $esb_needed =~ ^[Yy]$ ]]; then
       esb_enabled="true"
     
       esb_worker_mgt_needed=$(read_user_input "Do you need to deploy ESB (Enterprise Service Bus) in worker manager setup? [y/n] " "" $esb_worker_mgt_enabled )
       if [[ $esb_worker_mgt_needed =~ ^[Yy]$ ]]; then
          esb_worker_mgt_enabled="true"
          esb_clustering_enabled="true"
       else
          clustering_esb=$(read_user_input "Do you need to enable clustering for ESB? [y/n] " "" $esb_clustering_enabled )
          if [[ $clustering_esb =~ ^[Yy]$ ]]; then
             esb_clustering_enabled="true"
          else
             esb_clustering_enabled="false"
          fi
       fi      
    fi   

    apim_needed=$(read_user_input "Do you need to deploy APIM (API Manager) service? [y/n] " "" $apim_enabled )
    if [[ $apim_needed =~ ^[Yy]$ ]]; then
       apim_enabled="true"
       clustering_keymanager=$(read_user_input "Do you need to enable clustering for Keymanager? [y/n] " "" $keymanager_clustering_enabled )
       if [[ $clustering_keymanager =~ ^[Yy]$ ]]; then
          keymanager_clustering_enabled="true"
       else
          keymanager_clustering_enabled="false"
       fi
    fi
}

function get_core_services_confirmations() {
    bam_needed=$(read_user_input "Do you need to setup WSO2 BAM (Business Activity Monitor) as a core service? [y/n] " "" $bam_enabled )
    if [[ $bam_needed =~ ^[Yy]$ ]]; then
       bam_enabled="true"
    fi

    wso2_ppaas_needed=$(read_user_input "Do you need to start WSO2 Private PaaS? [y/n] " "" $wso2_ppaas_enabled )
    if [[ $wso2_ppaas_needed =~ ^[Yy]$ ]]; then
       wso2_ppaas_enabled="true"
    fi
}

function setup_as() {
    echo "Configuring Application Server..."
    if [[ $puppet_external = "false" && $skip_puppet = "false" ]]; then
        # Copy AS patches to Puppet
        cp -rf $current_dir/patches/patch0009/ /etc/puppet/modules/appserver/files/patches/repository/components/patches

        # Copy packs files to Puppet
        cp -f $stratos_pack_path/wso2as-5.2.1.zip /etc/puppet/modules/appserver/files
        cp -f $stratos_pack_path/$MYSQL_CONNECTOR /etc/puppet/modules/appserver/files/configs/repository/components/lib

        # appserver node parameters
        replace_in_file "AS_CONFIG_DB" "$as_config_db" "/etc/puppet/manifests/nodes/appserver.pp"
        replace_in_file "AS_CONFIG_PATH" "$as_config_path" "/etc/puppet/manifests/nodes/appserver.pp"
        replace_in_file "CLUSTERING" "$as_clustering_enabled" "/etc/puppet/manifests/nodes/appserver.pp"
        replace_in_file "ADMIN_USER" "admin" "/etc/puppet/modules/appserver/manifests/params.pp"
        replace_in_file "ADMIN_PASSWORD" "admin" "/etc/puppet/modules/appserver/manifests/params.pp"
        replace_in_file "DB_USER" "$mysql_uname" "/etc/puppet/modules/appserver/manifests/params.pp"
        replace_in_file "DB_PASSWORD" "$mysql_password" "/etc/puppet/modules/appserver/manifests/params.pp"
        replace_in_file "REGISTRY_DB" "$registry_db" "/etc/puppet/modules/appserver/manifests/params.pp"
        replace_in_file "USERSTORE_DB" "userstore" "/etc/puppet/modules/appserver/manifests/params.pp"

        # Configure SSO in appserver
        if [[ "$config_sso_enabled" == "true" ]] ; then           
           replace_in_file "SSO_DISABLED" "false" "/etc/puppet/modules/appserver/templates/conf/security/authenticators.xml.erb"
           replace_in_file "IDP_URL" "$public_ip" "/etc/puppet/modules/appserver/templates/conf/security/authenticators.xml.erb"
        else       
           backup_file "/etc/puppet/modules/appserver/templates/conf/security/authenticators.xml.erb"
           replace_in_file "SSO_DISABLED" "true" "/etc/puppet/modules/appserver/templates/conf/security/authenticators.xml.erb"
        fi
    fi

    if [[ "$puppet_only" = "true" ]]; then
       return
    fi

    #appserver config db changes 
    create_config_database "$as_config_db"

    # Configure cartridge definition json
    backup_file $current_dir/resources/json/$iaas/appserver-cart.json
    replace_in_file "REGION" "$region" "$current_dir/resources/json/$iaas/appserver-cart.json"
    replace_in_file "BASE_IMAGE_ID" "$cartridge_base_img_id" "$current_dir/resources/json/$iaas/appserver-cart.json"

    backup_file $current_dir/resources/json/$iaas/appserver-cart-mgt.json
    replace_in_file "REGION" "$region" "$current_dir/resources/json/$iaas/appserver-cart-mgt.json"
    replace_in_file "BASE_IMAGE_ID" "$cartridge_base_img_id" "$current_dir/resources/json/$iaas/appserver-cart-mgt.json"

    backup_file $current_dir/resources/json/$iaas/appserver-cart-worker.json
    replace_in_file "REGION" "$region" "$current_dir/resources/json/$iaas/appserver-cart-worker.json"
    replace_in_file "BASE_IMAGE_ID" "$cartridge_base_img_id" "$current_dir/resources/json/$iaas/appserver-cart-worker.json"

    if [[ $as_clustering_enabled = "true" ]]; then
       replace_in_file "@PRIMARY" "true" "$current_dir/resources/json/$iaas/appserver-cart.json"
       replace_in_file "@CLUSTERING" "true" "$current_dir/resources/json/$iaas/appserver-cart.json"
    else
       replace_in_file "@PRIMARY" "false" "$current_dir/resources/json/$iaas/appserver-cart.json"
       replace_in_file "@CLUSTERING" "false" "$current_dir/resources/json/$iaas/appserver-cart.json"
    fi
}


function setup_bps() {
    echo "Configuring Business Process Server..."
    if [[ $puppet_external = "false" && $skip_puppet = "false" ]]; then
        # Copy BPS patches to Puppet
        cp -rf $current_dir/patches/patch0008/ /etc/puppet/modules/bps/files/patches/repository/components/patches

        # Copy packs files to Puppet
        cp -f $stratos_pack_path/wso2bps-3.2.0.zip /etc/puppet/modules/bps/files
        cp -f $stratos_pack_path/$MYSQL_CONNECTOR /etc/puppet/modules/bps/files/configs/repository/components/lib

        # bps node parameters        
        replace_in_file "BPS_CONFIG_DB" "$bps_db" "/etc/puppet/manifests/nodes/bps.pp"
        replace_in_file "BPS_CONFIG_PATH" "$bps_config_path" "/etc/puppet/manifests/nodes/bps.pp"
        replace_in_file "CLUSTERING" "$bps_clustering_enabled" "/etc/puppet/manifests/nodes/bps.pp"
        backup_file "/etc/puppet/modules/bps/manifests/params.pp"
        replace_in_file "ADMIN_USER" "admin" "/etc/puppet/modules/bps/manifests/params.pp"
        replace_in_file "ADMIN_PASSWORD" "admin" "/etc/puppet/modules/bps/manifests/params.pp"
        replace_in_file "DB_USER" "$mysql_uname" "/etc/puppet/modules/bps/manifests/params.pp"
        replace_in_file "DB_PASSWORD" "$mysql_password" "/etc/puppet/modules/bps/manifests/params.pp"
        replace_in_file "REGISTRY_DB" "$registry_db" "/etc/puppet/modules/bps/manifests/params.pp"
        replace_in_file "USERSTORE_DB" "userstore" "/etc/puppet/modules/bps/manifests/params.pp"        
    fi

    if [[ "$puppet_only" = "true" ]]; then
           return
    fi

    #bps config db changes 
    create_config_database "$bps_config_db"

    # Configure cartridge definition json
    backup_file $current_dir/resources/json/$iaas/bps-cart.json
    replace_in_file "REGION" "$region" "$current_dir/resources/json/$iaas/bps-cart.json"
    replace_in_file "BASE_IMAGE_ID" "$cartridge_base_img_id" "$current_dir/resources/json/$iaas/bps-cart.json"

    backup_file $current_dir/resources/json/$iaas/bps-cart-mgt.json
    replace_in_file "REGION" "$region" "$current_dir/resources/json/$iaas/bps-cart-mgt.json"
    replace_in_file "BASE_IMAGE_ID" "$cartridge_base_img_id" "$current_dir/resources/json/$iaas/bps-cart-mgt.json"

    backup_file $current_dir/resources/json/$iaas/bps-cart-worker.json
    replace_in_file "REGION" "$region" "$current_dir/resources/json/$iaas/bps-cart-worker.json"
    replace_in_file "BASE_IMAGE_ID" "$cartridge_base_img_id" "$current_dir/resources/json/$iaas/bps-cart-worker.json"

    if [[ $bps_clustering_enabled = "true" ]]; then
       replace_in_file "@PRIMARY" "true" "$current_dir/resources/json/$iaas/bps-cart.json"
       replace_in_file "@CLUSTERING" "true" "$current_dir/resources/json/$iaas/bps-cart.json"
    else
       replace_in_file "@PRIMARY" "false" "$current_dir/resources/json/$iaas/bps-cart.json"
       replace_in_file "@CLUSTERING" "false" "$current_dir/resources/json/$iaas/bps-cart.json"
    fi
}


function setup_esb() {
    echo "Configuring Enterprise Service Bus..."
    if [[ $puppet_external = "false" && $skip_puppet = "false" ]]; then
       # Copy ESB patches to Puppet
       cp -rf $current_dir/patches/patch0008/ /etc/puppet/modules/esb/files/patches/repository/components/patches

       # Copy packs files to Puppet
       cp -f $stratos_pack_path/wso2esb-4.8.1.zip /etc/puppet/modules/esb/files
       cp -f $stratos_pack_path/$MYSQL_CONNECTOR /etc/puppet/modules/esb/files/configs/repository/components/lib

       # esb node parameters       
       replace_in_file "ESB_CONFIG_DB" "$esb_config_db" "/etc/puppet/manifests/nodes/esb.pp"
       replace_in_file "ESB_CONFIG_PATH" "$esb_config_path" "/etc/puppet/manifests/nodes/esb.pp"
       replace_in_file "CLUSTERING" "$esb_clustering_enabled" "/etc/puppet/manifests/nodes/esb.pp"

       backup_file "/etc/puppet/modules/esb/manifests/params.pp"
       replace_in_file "ADMIN_USER" "admin" "/etc/puppet/modules/esb/manifests/params.pp"
       replace_in_file "ADMIN_PASSWORD" "admin" "/etc/puppet/modules/esb/manifests/params.pp"
       replace_in_file "DB_USER" "$mysql_uname" "/etc/puppet/modules/esb/manifests/params.pp"
       replace_in_file "DB_PASSWORD" "$mysql_password" "/etc/puppet/modules/esb/manifests/params.pp"
       replace_in_file "REGISTRY_DB" "$registry_db" "/etc/puppet/modules/esb/manifests/params.pp"
       replace_in_file "USERSTORE_DB" "userstore" "/etc/puppet/modules/esb/manifests/params.pp"       
    fi

    if [[ "$puppet_only" = "true" ]]; then
          return
    fi

    #esb config db changes 
    create_config_database "$esb_config_db"

    # Configure cartridge definition json
    backup_file $current_dir/resources/json/$iaas/esb-cart.json
    replace_in_file "REGION" "$region" "$current_dir/resources/json/$iaas/esb-cart.json"
    replace_in_file "BASE_IMAGE_ID" "$cartridge_base_img_id" "$current_dir/resources/json/$iaas/esb-cart.json"

    backup_file $current_dir/resources/json/$iaas/esb-cart-mgt.json
    replace_in_file "REGION" "$region" "$current_dir/resources/json/$iaas/esb-cart-mgt.json"
    replace_in_file "BASE_IMAGE_ID" "$cartridge_base_img_id" "$current_dir/resources/json/$iaas/esb-cart-mgt.json"

    backup_file $current_dir/resources/json/$iaas/esb-cart-worker.json
    replace_in_file "REGION" "$region" "$current_dir/resources/json/$iaas/esb-cart-worker.json"
    replace_in_file "BASE_IMAGE_ID" "$cartridge_base_img_id" "$current_dir/resources/json/$iaas/esb-cart-worker.json"

    if [[ $esb_clustering_enabled = "true" ]]; then
       replace_in_file "@PRIMARY" "true" "$current_dir/resources/json/$iaas/esb-cart.json"
       replace_in_file "@CLUSTERING" "true" "$current_dir/resources/json/$iaas/esb-cart.json"
    else
       replace_in_file "@PRIMARY" "false" "$current_dir/resources/json/$iaas/esb-cart.json"
       replace_in_file "@CLUSTERING" "false" "$current_dir/resources/json/$iaas/esb-cart.json"
    fi
}

function setup_is() {
    echo "Configuring Identity Server..."
    if [[ $puppet_external = "false" && $skip_puppet = "false" ]]; then
       cp -f $stratos_pack_path/wso2is-5.0.0.zip /etc/puppet/modules/is/files
       cp -f $stratos_pack_path/$MYSQL_CONNECTOR /etc/puppet/modules/is/files/configs/repository/components/lib    
    fi

    # is node parameters    
    replace_in_file "CLUSTERING" "$is_clustering_enabled" "/etc/puppet/manifests/nodes/is.pp"
    replace_in_file "IS_CONFIG_DB" "$is_config_db" "/etc/puppet/manifests/nodes/is.pp"
    replace_in_file "IS_CONFIG_PATH" "$is_config_path" "/etc/puppet/manifests/nodes/is.pp"

    backup_file "/etc/puppet/modules/is/manifests/params.pp"
    replace_in_file "ADMIN_USER" "admin" "/etc/puppet/modules/is/manifests/params.pp"
    replace_in_file "ADMIN_PASSWORD" "admin" "/etc/puppet/modules/is/manifests/params.pp"
    replace_in_file "DB_USER" "$mysql_uname" "/etc/puppet/modules/is/manifests/params.pp"
    replace_in_file "DB_PASSWORD" "$mysql_password" "/etc/puppet/modules/is/manifests/params.pp"
    replace_in_file "REGISTRY_DB" "$registry_db" "/etc/puppet/modules/is/manifests/params.pp"
    replace_in_file "USERSTORE_DB" "userstore" "/etc/puppet/modules/is/manifests/params.pp"

    if [[ "$puppet_only" = "true" ]]; then
          return
    fi

    #esb config db changes 
    create_config_database "$is_config_db"
}

function setup_is_core_service() {
    
    if [[ -e $stratos_pack_path/wso2is-5.0.0.zip ]]; then
        su - $host_user -c "unzip -o -q $stratos_pack_path/wso2is-5.0.0.zip -d $stratos_install_path"

        #copy mysql connector jar	
        cp -f $stratos_pack_path/$MYSQL_CONNECTOR $stratos_install_path/wso2is-5.0.0/repository/components/lib

	#is config db changes 
	create_config_database "$is_config_db"

        #populating IS specific databases
        mysql -u$mysql_uname -p$mysql_password -h$mysql_host -D$is_config_db < $stratos_install_path/wso2is-5.0.0/dbscripts/identity/mysql.sql
        mysql -u$mysql_uname -p$mysql_password -h$mysql_host -D$is_config_db < $stratos_install_path/wso2is-5.0.0/dbscripts/identity/application-mgt/mysql.sql
   
	   
        # copy the templated master-datasource.xml and replace the relevant parameters
        cp $current_dir/resources/datasource-template/master-datasource.xml.template $stratos_install_path/wso2is-5.0.0/repository/conf/datasources/master-datasources.xml
        replace_in_file 'MYSQL_HOST' $mysql_host $stratos_install_path/wso2is-5.0.0/repository/conf/datasources/master-datasources.xml
        replace_in_file 'MYSQL_USER' $mysql_uname $stratos_install_path/wso2is-5.0.0/repository/conf/datasources/master-datasources.xml
        replace_in_file 'MYSQL_PASSWORD' $mysql_password $stratos_install_path/wso2is-5.0.0/repository/conf/datasources/master-datasources.xml

        cp $current_dir/resources/is_config/registry.xml $stratos_install_path/wso2is-5.0.0/repository/conf/registry.xml
        replace_in_file 'MYSQL_HOST' $mysql_host $stratos_install_path/wso2is-5.0.0/repository/conf/registry.xml
        replace_in_file 'MYSQL_USER' $mysql_uname $stratos_install_path/wso2is-5.0.0/repository/conf/registry.xml
        replace_in_file 'MYSQL_PASSWORD' $mysql_password $stratos_install_path/wso2is-5.0.0/repository/conf/registry.xml

        cp $current_dir/resources/is_config/identity.xml $stratos_install_path/wso2is-5.0.0/repository/conf/identity.xml
 	replace_in_file 'IDP_URL' "$public_ip" $stratos_install_path/wso2is-5.0.0/repository/conf/identity.xml

        cp $current_dir/resources/is_config/user-mgt.xml $stratos_install_path/wso2is-5.0.0/repository/conf/user-mgt.xml

        # copy the templated sso-idp-config.xml file and repalce relevant parameters
        cp $current_dir/resources/sso-idp-config-template/sso-idp-config.xml-template $stratos_install_path/wso2is-5.0.0/repository/conf/security/sso-idp-config.xml        

        # replace the sso-idp-config.xml file
        replace_in_file 'AS_ASSERTION_CONSUMER_HOST' appserver.wso2.com $stratos_install_path/wso2is-5.0.0/repository/conf/security/sso-idp-config.xml
        replace_in_file 'IS_ASSERTION_CONSUMER_HOST' is.wso2.com $stratos_install_path/wso2is-5.0.0/repository/conf/security/sso-idp-config.xml
        replace_in_file 'ESB_ASSERTION_CONSUMER_HOST' esb.wso2.com $stratos_install_path/wso2is-5.0.0/repository/conf/security/sso-idp-config.xml
        replace_in_file 'BPS_ASSERTION_CONSUMER_HOST' bps.wso2.com $stratos_install_path/wso2is-5.0.0/repository/conf/security/sso-idp-config.xml
        replace_in_file 'IDP_URL' "$public_ip" $stratos_install_path/wso2is-5.0.0/repository/conf/security/sso-idp-config.xml
        replace_in_file '<HostName>.*</HostName>' "<HostName>$public_ip</HostName>" $stratos_install_path/wso2is-5.0.0/repository/conf/carbon.xml
        replace_in_file '<MgtHostName>.*</MgtHostName>' "<MgtHostName>$public_ip</MgtHostName>" $stratos_install_path/wso2is-5.0.0/repository/conf/carbon.xml
	   
    else
        echo "IS pack [ $stratos_pack_path/wso2is-5.0.0.zip ] not found!"
        exit 1
    fi
    
}

function setup_apim() {
    if [[ $puppet_external = "false" && $skip_puppet = "false" ]]; then
       # Copy ESB patches to Puppet
       cp -rf $current_dir/patches/patch0009/ /etc/puppet/modules/apimanager/files/patches/repository/components/patches
       cp -f $stratos_pack_path/wso2am-1.7.0.zip /etc/puppet/modules/apimanager/files
       cp -f $stratos_pack_path/$MYSQL_CONNECTOR /etc/puppet/modules/apimanager/files/configs/repository/components/lib

       # apim node parameters       
       replace_in_file "GATEWAY_CONFIG_DB" "$apim_gateway_config_db" "/etc/puppet/manifests/nodes/api.pp"
       replace_in_file "STORE_CONFIG_DB" "$apim_store_config_db" "/etc/puppet/manifests/nodes/api.pp"
       replace_in_file "KEYMANAGER_CONFIG_DB" "$apim_keymanager_config_db" "/etc/puppet/manifests/nodes/api.pp"
       replace_in_file "GATEWAY_CONFIG_PATH" "$gateway_config_path" "/etc/puppet/manifests/nodes/api.pp"
       replace_in_file "STORE_CONFIG_PATH" "$store_config_path" "/etc/puppet/manifests/nodes/api.pp"
       replace_in_file "KEYMANAGER_CONFIG_PATH" "$keymanager_config_path" "/etc/puppet/manifests/nodes/api.pp"
       replace_in_file "KEYMANGER_CLUSTERING" "$keymanager_clustering_enabled" "/etc/puppet/manifests/nodes/api.pp"
      
       replace_in_file "ADMIN_USER" "admin" "/etc/puppet/modules/apimanager/manifests/params.pp"
       replace_in_file "ADMIN_PASSWORD" "admin" "/etc/puppet/modules/apimanager/manifests/params.pp"
       replace_in_file "DB_USER" "$mysql_uname" "/etc/puppet/modules/apimanager/manifests/params.pp"
       replace_in_file "DB_PASSWORD" "$mysql_password" "/etc/puppet/modules/apimanager/manifests/params.pp"
       replace_in_file "REGISTRY_DB" "$registry_db" "/etc/puppet/modules/apimanager/manifests/params.pp"
       replace_in_file "USERSTORE_DB" "userstore" "/etc/puppet/modules/apimanager/manifests/params.pp"
       replace_in_file "STATS_DB" "$apim_stats_db" "/etc/puppet/modules/apimanager/manifests/params.pp"
       replace_in_file "APIM_DB" "$apim_db" "/etc/puppet/modules/apimanager/manifests/params.pp"
    fi

    # In puppet only mode, do not change other configurations
    if [[ $puppet_only = "true" ]]; then
       return
    fi

    create_apim_database "$apim_db"
    # Using the same config DB for store and publisher
    create_config_database "$apim_store_config_db"
    create_config_database "$apim_gateway_config_db" 
    create_config_database "$apim_keymanager_config_db"

    # Configure cartridge definition json
    backup_file $current_dir/resources/json/$iaas/gateway.json
    replace_in_file "REGION" "$region" "$current_dir/resources/json/$iaas/gateway.json"
    replace_in_file "BASE_IMAGE_ID" "$cartridge_base_img_id" "$current_dir/resources/json/$iaas/gateway.json"   

    backup_file $current_dir/resources/json/$iaas/gatewaymgt.json
    replace_in_file "REGION" "$region" "$current_dir/resources/json/$iaas/gatewaymgt.json"
    replace_in_file "BASE_IMAGE_ID" "$cartridge_base_img_id" "$current_dir/resources/json/$iaas/gatewaymgt.json"

    backup_file $current_dir/resources/json/$iaas/keymanager.json
    replace_in_file "REGION" "$region" "$current_dir/resources/json/$iaas/keymanager.json"
    replace_in_file "BASE_IMAGE_ID" "$cartridge_base_img_id" "$current_dir/resources/json/$iaas/keymanager.json"
    replace_in_file "@PRIMARY" "$keymanager_clustering_enabled" "$current_dir/resources/json/$iaas/keymanager.json"
    replace_in_file "@CLUSTERING" "$keymanager_clustering_enabled" "$current_dir/resources/json/$iaas/keymanager.json"

    backup_file $current_dir/resources/json/$iaas/publisher.json
    replace_in_file "REGION" "$region" "$current_dir/resources/json/$iaas/publisher.json"
    replace_in_file "BASE_IMAGE_ID" "$cartridge_base_img_id" "$current_dir/resources/json/$iaas/publisher.json"

    backup_file $current_dir/resources/json/$iaas/store.json
    replace_in_file "REGION" "$region" "$current_dir/resources/json/$iaas/store.json"
    replace_in_file "BASE_IMAGE_ID" "$cartridge_base_img_id" "$current_dir/resources/json/$iaas/store.json"
}

function deploy_puppet() {
    # Copy Puppet scripts from private-paas repo
    echo "Deploying Puppet files to /etc/puppet. Please wait..."
    cp -rf puppet/* /etc/puppet/
    echo "Puppet files successfully deployed."

    echo "Configuring Puppet scripts..."
    # Set Puppet base node parameters
    replace_in_file "PACKAGE_REPO" "$package_repo" "/etc/puppet/manifests/nodes/base.pp"
    replace_in_file "MB_IP" "$machine_ip" "/etc/puppet/manifests/nodes/base.pp"
    replace_in_file "MB_PORT" "61616" "/etc/puppet/manifests/nodes/base.pp"
    replace_in_file "CEP_IP" "$machine_ip" "/etc/puppet/manifests/nodes/base.pp"
    replace_in_file "CEP_PORT" "$cep_port" "/etc/puppet/manifests/nodes/base.pp"
    replace_in_file "DB_HOST" "$mysql_host" "/etc/puppet/manifests/nodes/base.pp"
    replace_in_file "DB_PORT" "$mysql_port" "/etc/puppet/manifests/nodes/base.pp"
    
    # Enable BAM via Puppet if BAM is enabled
    if [[ "$bam_enabled" = "true" ]]; then
       replace_in_file "BAM_IP" "$machine_ip" "/etc/puppet/manifests/nodes/base.pp"
       replace_in_file "BAM_PORT" "7612" "/etc/puppet/manifests/nodes/base.pp"
       replace_in_file "ENABLE_LOG_PUBLISHER" "true" "/etc/puppet/manifests/nodes/base.pp"
    fi
    
    replace_in_file "JAVA_FILE" "$JAVA_FILE_DISTRUBUTION" "/etc/puppet/manifests/nodes/base.pp"
    replace_in_file "JAVA_NAME" "$JAVA_NAME_EXTRACTED" "/etc/puppet/manifests/nodes/base.pp"
    # JAVA_NAME should match extracted directory name of Java tar.gz archive, eg. jdk-7u45-linux-x64.tar.gz -> jdk1.7.0_45

    # Copy packs files to Puppet modules
    cp -f $stratos_pack_path/apache-stratos-cartridge-agent-4.0.0-wso2v1-bin.zip /etc/puppet/modules/agent/files
    cp -f $stratos_pack_path/apache-stratos-load-balancer-4.0.0-wso2v1.zip /etc/puppet/modules/lb/files
    cp -f $stratos_pack_path/$JAVA_FILE_DISTRUBUTION /etc/puppet/modules/java/files

    # modify autosign.conf
    echo "*."$stratos_domain > /etc/puppet/autosign.conf

    # Restart Puppet master after configurations
    echo "Restarting Puppet master"
    /etc/init.d/puppetmaster restart
    echo "Puppet scripts configuration completed."
}

function configure_products() {

    if [[ $puppet_external = "false" && $skip_puppet = "false" ]]; then
       deploy_puppet
    fi 
    # Get confirmation on configuring SSO with WSO2 IS
    config_sso_needed=$(read_user_input "Do you need to setup WSO2 IS (Identity Server) as a core service and configure SSO feature? [y/n] " "" $config_sso_enabled )
    if [[ $config_sso_needed =~ ^[Yy]$ ]]; then
       config_sso_enabled="true"
    fi

    if [[ $puppet_only = "false" ]]; then

       if [[ $deploy_services = "true" ]]; then
	       # Get user confirmations to deploy WSO2 PPaaS services
	       get_service_deployment_confirmations
       fi
 
       # Create databases for the governence registry       
       create_registry_database "$registry_db"
       create_config_database "$sm_config_db"

       # Configure Apache Stratos
       setup_apache_stratos

       if [[ $deploy_services = "true" ]]; then
	       # Configure LB cartridge definition json
	       backup_file $current_dir/resources/json/$iaas/lb-cart.json
	       replace_in_file "REGION" "$region" "$current_dir/resources/json/$iaas/lb-cart.json"
	       replace_in_file "BASE_IMAGE_ID" "$cartridge_base_img_id" "$current_dir/resources/json/$iaas/lb-cart.json"
       fi
    fi    

    if [[ $puppet_external = "false" && $skip_puppet = "false" ]]; then
       # Copy activemq client jars to puppet
          for activemq_client_lib in "${activemq_client_libs[@]}" 
              do
	          cp -f $stratos_install_path/$ACTIVE_MQ_EXTRACTED/lib/$activemq_client_lib /etc/puppet/modules/agent/files/activemq/
                  cp -f $stratos_install_path/$ACTIVE_MQ_EXTRACTED/lib/$activemq_client_lib /etc/puppet/modules/lb/files/activemq/
              done
    fi    

    if [[ $as_enabled = "true" ]]; then
       setup_as
    fi

    if [[ $bps_enabled = "true" ]]; then
       setup_bps
    fi

    if [[ $esb_enabled = "true" ]]; then
       setup_esb
    fi
    
    if [[ $config_sso_enabled = "true" ]]; then
       setup_is_core_service
    fi

    if [[ $apim_enabled = "true" ]]; then
       setup_apim
    fi

    echo "WSO2 Private PaaS product configuration completed."
}

function get_host_user(){
    host_user=$(read_user_input "Enter host user :" "" $host_user )
}

function init() {
    # Create a backup of setup.conf file, we are going to change it.
    backup_file $setup_path/conf/setup.conf

    # backup mysql.sql, we are going to write stuff into it
    #backup_file $setup_path/resources/mysql.sql
    if [[ -z $skip_puppet || "$skip_puppet" = "" ]]; then
       skip_puppet="false"
    fi

    # Check whether Puppet master is installed and configure it
    check_for_puppet
    if [[ $puppet_installed = "true" ]]; then
        stratos_domain=$(dnsdomainname)
        echo "Domain name for the WSO2 Private PaaS environment: $stratos_domain"
    else   
        stratos_domain=$(read_user_input "Please enter a prefered domain name for the WSO2 Private PaaS environment : " "" $stratos_domain )
        echo "Using Stratos domain: $stratos_domain"
    fi

    list_ip_addreses
    machine_ip=$(read_user_input "Above are the IP addresses assigned to your machine. Please select the preferred IP address : " "" $machine_ip )
    get_host_user

    if [ "$machine_ip" == "" ];then
        echo -e "Machine IP is not specified, so proceeding with the default 127.0.0.1"
        machine_ip="127.0.0.1"
    fi    

    # Configure an external Puppet master
    puppet_external=$(read_user_input "Do you need to configure an external Puppet master? [y/n] : " "" $puppet_external )

    if [[ $puppet_external =~ ^[Yy]$ ]]; then
       puppet_external="true"
       echo -e "Configuring an external Puppet master...\n"
       puppet_external_ip=$(read_user_input "Please enter external Puppet master IP : " "" $puppet_external_ip )
       puppet_external_host=$(read_user_input "Please enter external Puppet master hostname : " "" $puppet_external_host )
 
       puppet_ip=$puppet_external_ip
       puppet_host=$puppet_external_host
    else
       puppet_external="false"
       # Install Puppet master if it is not already installed
       if [[ $puppet_installed = "false" ]]; then        
          install_puppet
       fi

       puppet_ip=$machine_ip
       echo -e "Puppet master IP address is $puppet_ip"
       puppet_host="puppet."$stratos_domain
       echo -e "Puppet master hostname is $puppet_host"
    fi    

    # Configure MySQL 
    setup_mysql=$(read_user_input "Do you need to install MySQL? [y/n] : " "" $setup_mysql )
    mysql_host=$(read_user_input "Please provide MySQL host : " "" $mysql_host )
    mysql_port=$(read_user_input "Please provide MySQL port. Default port is 3306 : " "" $mysql_port )
    mysql_port=${mysql_port:-3306}
    mysql_uname=$(read_user_input "Please provide MySQL username. Default username is root : " "" $mysql_uname )
    mysql_uname=${mysql_uname:-root}
    mysql_password=$(read_user_input "Please provide MySQL password : " "-s" $mysql_password )

    if [[ $setup_mysql =~ ^[Yy]$ ]]; then
        echo -e "\nStarting MySQL installation... \n"
        install_mysql

        # Make MySQL bind to IP address 0.0.0.0
            if [[ -e '/etc/mysql/my.cnf' ]]; then
                replace_in_file "bind-address.*" "bind-address=0.0.0.0" /etc/mysql/my.cnf
                # Restart MySQL service
                service mysql restart
                # Update privileges        
               mysql -u$mysql_uname -p$mysql_password -e "GRANT ALL PRIVILEGES ON *.* TO $mysql_uname@'%' IDENTIFIED BY '$mysql_password'; FLUSH PRIVILEGES;"
            else
                echo 'my.cnf not found. Unable to set listen address to 0.0.0.0'
            fi
    else
        echo -e "\nSkipping MySQL installation... \n";
    fi

    # Set JAVA_HOME environment variable
    if [[ "$JAVA_HOME" = "" ]]; then
        java_home_input=$(read_user_input "JAVA_HOME is not set as an environment variable. Please enter JAVA_HOME path : " "" $java_home_input )
        export JAVA_HOME=$java_home_input
    fi
    
    # Make everything accessible in packs directory
    chmod -R 777 $stratos_pack_path
}

# Start core services
function start_servers() {

    # Get user confirmations to start WSO2 PPaaS core services
    get_core_services_confirmations

    if [[ $bam_enabled = "true" ]]; then
       # Setup BAM server
       echo "Starting BAM core service..."
       /bin/bash $setup_path/setup_bam_logging.sh >> wso2bam.log
       while ! echo exit | nc localhost $BAM_PORT; do sleep $SLEEPTIME; done
       sleep $SLEEPTIME
    fi

    if [[ $apim_enabled = "true" ]]; then
       # Setup Gitblit Server
       echo "Starting Gitblit core service..."
       nohup su - $host_user -c "/bin/bash $setup_path/gitblit.sh" >> gitblit.log
       while ! echo exit | nc localhost $GITBLIT_PORT; do sleep $SLEEPTIME; done
       sleep $SLEEPTIME
    fi

    if [[ $wso2_ppaas_enabled = "true" ]]; then
       # Start Apache Stratos with default profile
       echo -e "Starting WSO2 Private PaaS server as $host_user user... "
       su - $host_user -c "source $setup_path/conf/setup.conf;$setup_path/start-servers.sh -p default >> $LOG"
       while ! echo exit | nc localhost $PPAAS_PORT; do sleep $SLEEPTIME; done
       sleep $SLEEPTIME
    else
        echo -e "Skipping WSO2 Private PaaS startup."
    fi 

    if [[ $config_sso_enabled = "true" ]]; then
       echo -e "Starting WSO2 IS core service..."
       nohup su - $host_user -c "source $setup_path/conf/setup.conf;/bin/bash $stratos_install_path/wso2is-5.0.0/bin/wso2server.sh -DportOffset=2 &" >> wso2is.log
       while ! echo exit | nc localhost $IS_PORT; do sleep $SLEEPTIME; done
       sleep $SLEEPTIME
    fi
}

function deploy_wso2_ppaas_services() {
    # Replace the region and zone of partition file
    backup_file $current_dir/resources/json/$iaas/partition.json
    replace_in_file "REGION" "$region" "$current_dir/resources/json/$iaas/partition.json"
    replace_in_file "AVAILABILITY_ZONE" "$ec2_availability_zone" "$current_dir/resources/json/$iaas/partition.json"

    echo -e "Deploying partition at $resource_path/json/$iaas/partition.json"
    curl -X POST -H "Content-Type: application/json" -d @"$resource_path/json/$iaas/partition.json" -k  -u admin:admin "https://$machine_ip:9443/stratos/admin/policy/deployment/partition"

    echo -e "Deploying autoscale policy at $resource_path/json/$iaas/autoscale-policy.json"
    curl -X POST -H "Content-Type: application/json" -d @"$resource_path/json/$iaas/autoscale-policy.json" -k  -u admin:admin "https://$machine_ip:9443/stratos/admin/policy/autoscale"

    echo -e "Deploying deployment policy at $resource_path/json/$iaas/deployment-policy.json"
    curl -X POST -H "Content-Type: application/json" -d @"$resource_path/json/$iaas/deployment-policy.json" -k  -u admin:admin "https://$machine_ip:9443/stratos/admin/policy/deployment"

    echo -e "Deploying deployment policy at $resource_path/json/$iaas/deployment-flat.json"
    curl -X POST -H "Content-Type: application/json" -d @"$resource_path/json/$iaas/deployment-flat.json" -k  -u admin:admin "https://$machine_ip:9443/stratos/admin/policy/deployment"

    echo -e "Deploying LB cartridge at $resource_path/json/$iaas/lb-cart.json"
    curl -X POST -H "Content-Type: application/json" -d @"$resource_path/json/$iaas/lb-cart.json" -k  -u admin:admin "https://$machine_ip:9443/stratos/admin/cartridge/definition"

    if [[ "$as_enabled" = "true" ]]; then
       if [[ "$as_worker_mgt_enabled" = "true" ]]; then
          echo -e "Deploying Aplication Server (AS) Manager cartridge at $resource_path/json/$iaas/appserver-cart-mgt.json"
          curl -X POST -H "Content-Type: application/json" -d @"$resource_path/json/$iaas/appserver-cart-mgt.json" -k  -u admin:admin "https://$machine_ip:9443/stratos/admin/cartridge/definition"

          echo -e "Deploying Application Service Manager service"
          curl -X POST -H "Content-Type: application/json" -d @"$resource_path/json/$iaas/appserver-mgt-service-deployment.json" -k -u admin:admin https://$machine_ip:9443/stratos/admin/service/definition

          echo -e "Deploying Aplication Server (AS) Worker cartridge at $resource_path/json/$iaas/appserver-cart-worker.json"
          curl -X POST -H "Content-Type: application/json" -d @"$resource_path/json/$iaas/appserver-cart-worker.json" -k  -u admin:admin "https://$machine_ip:9443/stratos/admin/cartridge/definition"

          echo -e "Deploying Application Service Worker service"
          curl -X POST -H "Content-Type: application/json" -d @"$resource_path/json/$iaas/appserver-worker-service-deployment.json" -k -u admin:admin https://$machine_ip:9443/stratos/admin/service/definition
       else
           echo -e "Deploying Aplication Server (AS) cartridge at $resource_path/json/$iaas/appserver-cart.json"
           curl -X POST -H "Content-Type: application/json" -d @"$resource_path/json/$iaas/appserver-cart.json" -k  -u admin:admin "https://$machine_ip:9443/stratos/admin/cartridge/definition"

           echo -e "Deploying Application Service service"
           curl -X POST -H "Content-Type: application/json" -d @"$resource_path/json/$iaas/appserver-service-deployment.json" -k -u admin:admin https://$machine_ip:9443/stratos/admin/service/definition
       fi
    fi

    if [[ "$apim_enabled" = "true" ]]; then
        echo -e "Deploying API Manager (AM) - Gateway cartridge at $resource_path/json/$iaas/gateway.json"
        curl -X POST -H "Content-Type: application/json" -d @"$resource_path/json/$iaas/gateway.json" -k  -u admin:admin "https://$machine_ip:9443/stratos/admin/cartridge/definition"

        echo -e "Deploying API Manager - Gateway service"
        curl -X POST -H "Content-Type: application/json" -d @"$resource_path/json/$iaas/gateway-dep.json" -k -u admin:admin https://$machine_ip:9443/stratos/admin/service/definition

        echo -e "Deploying API Manager (AM) - Gateway manager cartridge at $resource_path/json/$iaas/gatewaymgt.json"
        curl -X POST -H "Content-Type: application/json" -d @"$resource_path/json/$iaas/gatewaymgt.json" -k  -u admin:admin "https://$machine_ip:9443/stratos/admin/cartridge/definition"

        echo -e "Deploying API Manager - Gateway manager service"
        curl -X POST -H "Content-Type: application/json" -d @"$resource_path/json/$iaas/gatewaymgt-dep.json" -k -u admin:admin https://$machine_ip:9443/stratos/admin/service/definition

        echo -e "Deploying API Manager (AM) - Keymanager cartridge at $resource_path/json/$iaas/keymanager.json"
        curl -X POST -H "Content-Type: application/json" -d @"$resource_path/json/$iaas/keymanager.json" -k  -u admin:admin "https://$machine_ip:9443/stratos/admin/cartridge/definition"

        echo -e "Deploying API Manager (AM) - Keymanager service"
        curl -X POST -H "Content-Type: application/json" -d @"$resource_path/json/$iaas/keymanager-dep.json" -k -u admin:admin https://$machine_ip:9443/stratos/admin/service/definition

        echo -e "Deploying API Manager (AM) - Publisher cartridge at $resource_path/json/$iaas/publisher.json"
        curl -X POST -H "Content-Type: application/json" -d @"$resource_path/json/$iaas/publisher.json" -k  -u admin:admin "https://$machine_ip:9443/stratos/admin/cartridge/definition"

        echo -e "Deploying API Manager (AM) - Publisher service"
        curl -X POST -H "Content-Type: application/json" -d @"$resource_path/json/$iaas/publisher-dep.json" -k -u admin:admin https://$machine_ip:9443/stratos/admin/service/definition

        echo -e "Deploying API Manager (AM) - Store cartridge at $resource_path/json/$iaas/store.json"
        curl -X POST -H "Content-Type: application/json" -d @"$resource_path/json/$iaas/store.json" -k  -u admin:admin "https://$machine_ip:9443/stratos/admin/cartridge/definition"

        echo -e "Deploying API Manager (AM) - Store service"
        curl -X POST -H "Content-Type: application/json" -d @"$resource_path/json/$iaas/store-dep.json" -k -u admin:admin https://$machine_ip:9443/stratos/admin/service/definition
    fi

    if [[ "$esb_enabled" = "true" ]]; then
       if [[ "$esb_worker_mgt_enabled" = "true" ]]; then
          echo -e "Deploying Enterprise Service Bus (ESB) Manager cartridge at $resource_path/json/$iaas/esb-cart-mgt.json"
          curl -X POST -H "Content-Type: application/json" -d @"$resource_path/json/$iaas/esb-cart-mgt.json" -k  -u admin:admin "https://$machine_ip:9443/stratos/admin/cartridge/definition"

          echo -e "Deploying Enterprise Service Bus (ESB) Manager service"
          curl -X POST -H "Content-Type: application/json" -d @"$resource_path/json/$iaas/esb-mgt-service-deployment.json" -k -u admin:admin https://$machine_ip:9443/stratos/admin/service/definition

          echo -e "Deploying Enterprise Service Bus (ESB) Worker cartridge at $resource_path/json/$iaas/esb-cart-worker.json"
          curl -X POST -H "Content-Type: application/json" -d @"$resource_path/json/$iaas/esb-cart-worker.json" -k  -u admin:admin "https://$machine_ip:9443/stratos/admin/cartridge/definition"

          echo -e "Deploying Enterprise Service Bus (ESB) Worker service"
          curl -X POST -H "Content-Type: application/json" -d @"$resource_path/json/$iaas/esb-worker-service-deployment.json" -k -u admin:admin https://$machine_ip:9443/stratos/admin/service/definition
       else
          echo -e "Deploying Enterprise Service Bus (ESB) cartridge at $resource_path/json/$iaas/esb-cart.json"
          curl -X POST -H "Content-Type: application/json" -d @"$resource_path/json/$iaas/esb-cart.json" -k  -u admin:admin "https://$machine_ip:9443/stratos/admin/cartridge/definition"
    
          echo -e "Deploying Enterprise Service Bus (ESB) service at esb-service-deployment.json"
          curl -X POST -H "Content-Type: application/json" -d @"$resource_path/json/$iaas/esb-service-deployment.json" -k -u admin:admin https://$machine_ip:9443/stratos/admin/service/definition
       fi
    fi

    if [[ "$bps_enabled" = "true" ]]; then
       if [[ "$bps_worker_mgt_enabled" = "true" ]]; then
          echo -e "Deploying Business Process Server (BPS) Manager cartridge at $resource_path/json/$iaas/bps-cart-mgt.json"
          curl -X POST -H "Content-Type: application/json" -d @"$resource_path/json/$iaas/bps-cart-mgt.json" -k  -u admin:admin "https://$machine_ip:9443/stratos/admin/cartridge/definition"

          echo -e "Deploying Business Process Server (BPS) Manager service"
          curl -X POST -H "Content-Type: application/json" -d @"$resource_path/json/$iaas/bps-mgt-service-deployment.json" -k -u admin:admin https://$machine_ip:9443/stratos/admin/service/definition

          echo -e "Deploying Business Process Server (BPS) Worker cartridge at $resource_path/json/$iaas/bps-cart-worker.json"
          curl -X POST -H "Content-Type: application/json" -d @"$resource_path/json/$iaas/bps-cart-worker.json" -k  -u admin:admin "https://$machine_ip:9443/stratos/admin/cartridge/definition"

          echo -e "Deploying Business Process Server (BPS) Worker service"
          curl -X POST -H "Content-Type: application/json" -d @"$resource_path/json/$iaas/bps-worker-service-deployment.json" -k -u admin:admin https://$machine_ip:9443/stratos/admin/service/definition
       else
          echo -e "Deploying Business Process Server (BPS) cartridge at $resource_path/json/$iaas/bps-cart.json"
          curl -X POST -H "Content-Type: application/json" -d @"$resource_path/json/$iaas/bps-cart.json" -k  -u admin:admin "https://$machine_ip:9443/stratos/admin/cartridge/definition"

          echo -e "Deploying Business Process Server service"
          curl -X POST -H "Content-Type: application/json" -d @"$resource_path/json/$iaas/bps-service-deployment.json" -k -u admin:admin https://$machine_ip:9443/stratos/admin/service/definition
       fi
    fi

    # wait till services are active
    echo -e "\nWaiting till all the services are active...\n"
    sleep 5m
}

function update_hosts_file() {
    # call the python script to get LB ip
    lb_ip=$(python -c 'import agent; print agent.getLBIp()')

    # update the /etc/hosts file
    echo $lb_ip  appserver.wso2.com >> /etc/hosts
    echo $lb_ip  esb.wso2.com >> /etc/hosts
    echo $lb_ip  bps.wso2.com >> /etc/hosts
}

# -----------------------
# Execution Start Point
# -----------------------

# Make sure the user is running as root.
if [ "$UID" -ne "0" ]; then
        echo -e "\n You must be root to run $0.  (Try running 'sudo bash' first.) \n" 
        exit 69
fi


while getopts ":sphtca: --autostart" opts
do
  case $opts in
    s)
        export silent_mode="true"
        ;;
    p)
        export puppet_only="true"
        ;;  
    t)
        export deploy_services="false"
        ;; 
    h)
        help
        exit 0
        ;;
    *)
        ${ECHO} -e "boot.sh: Invalid option: -${OPTARG}" 	
        exit 1
    ;;
  esac
done

# On silent mode, start the servers without prompting anything from the user
if [[ "$silent_mode" = "true" ]]; then
     echo -e "\nboot.sh: Running in silent mode\n"
     get_host_user
     run_setup_sh
     # Start WSO2 Private PaaS core services
     start_servers

elif [[ "$puppet_only" = "true" ]]; then
     # Run main configuration
     init
     echo -e "\nboot.sh: Running in puppet only mode\n"
     configure_products
     echo -e "boot.sh: Puppet configuration completed"

elif [[ "$deploy_services" = "false" ]]; then
     # Run main configuration
     init
     echo -e "\nboot.sh: Running in configuring and start PaaS mode.\n"
     # Do the product specific configurations
     configure_products         

     # Start core servers    
     start_servers

else
     # Run main configuration
     init
     # Do the product specific configurations
     configure_products         

     # Start core servers    
     start_servers     

     if [[ "$wso2_ppaas_enabled" = "true" ]]; then
        # Deploy cartridges to Apache Stratos
        deploy_wso2_ppaas_services

        # Update hosts file
        update_hosts_file
     fi
fi

echo ""
echo "**************************************************************"
echo "Management Console : https://$stratos_domain:9443/console"
echo "**************************************************************"
echo -e "\nboot.sh: WSO2 Private PaaS installation completed successfully!\n"
# END
