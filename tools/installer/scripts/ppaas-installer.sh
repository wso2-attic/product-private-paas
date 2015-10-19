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
setup_path="$current_dir"
stratos_pack_path="$current_dir/packs"
stratos_install_path="$current_dir/install"
resource_path="$current_dir/resources"
cep_artifact_path="$resource_path/cep/artifacts/"
puppet_env="stratos"
puppet_installed="false"
cep_port=7611
cep_port_offset=0
activemq_client_libs=(activemq-broker-5.9.1.jar  activemq-client-5.9.1.jar  geronimo-j2ee-management_1.1_spec-1.0.1.jar  geronimo-jms_1.1_spec-1.1.1.jar  hawtbuf-1.9.jar)
export LOG=$log_path/stratos-setup.log

# Usage modes
silent_mode="false"
puppet_only="false"
deploy_services="true"

#/etc/hosts mapping
using_dns="false"

# Registry databases
registry_db="registry"

# Config databases
sm_config_db="sm_config"
as_config_db="as_config"
apim_db="apim_db"
apim_stats_db="amstats"
esb_config_db="esb_config"
greg_config_db="greg_config"
is_config_db="is_config"
bps_config_db="bps_config"


function help() {
    echo ""
    echo "This script will install WSO2 Private PaaS 4.1.0"
    echo "usage:"
    echo "boot.sh [-s ]"
    echo "   -s : Silent mode. This will start core services without performing any configuration."
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

function replace_configurator_module() {
    #echo "Setting value $2 for property $1 as $2"
    setp_conf_file=wso2ppaas/4.1.0/template-module/module.ini
    sed -i s$'\001'"$1"$'\001'"$2"$'\001''g' $setp_conf_file
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
function setup_apache_stratos_with_configurator() {
    cd install
    wget $CONFIGURATOR_DOWNLOAD_LOCATION
    unzip wso2ppaas-configurator-4.1.0.zip
    cd ..
    pwd
    cp -avr packs/wso2ppaas/4.1.0/template-module install/ppaas-configurator-4.1.0/template-modules/template-module
    cd install/ppaas-configurator-4.1.0
    python configurator.py


}

function setup_apache_stratos() {
    # Configure IaaS properties
    iaas=$(read_user_input "Enter your IaaS. vCloud, EC2 and OpenStack are the currently supported IaaSs. Enter \"vcloud\" for vCloud, \"ec2\" for EC2 , \"os\" for OpenStack and  \"kub\" for Kubernetes: " "" $iaas )

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
    separate_cep=$(read_user_input "Do you need to setup WSO2 CEP as a separate service? [y/n] " "" $separate_cep )
    if [[ $separate_cep =~ ^[Yy]$ ]]; then
       	separate_cep="true"
        cep_port=7614
        cep_port_offset=3
        /bin/bash $setup_path/setup.sh -p "stratos"
        su - $host_user -c "/bin/bash $setup_path/setup-cep.sh"
    else
    	/bin/bash $setup_path/setup.sh -p "default"
    fi
}


function get_core_services_confirmations() {
    bam_needed=$(read_user_input "Do you need to setup WSO2 DAS as a core service? [y/n] " "" $das_enabled )
    if [[ $bam_needed =~ ^[Yy]$ ]]; then
       das_enabled="true"
    fi

    wso2_ppaas_needed=$(read_user_input "Do you need to start WSO2 Private PaaS? [y/n] " "" $wso2_ppaas_enabled )
    if [[ $wso2_ppaas_needed =~ ^[Yy]$ ]]; then
       wso2_ppaas_enabled="true"
    fi
}

function configure_products() {
   # Create databases for the governence registry
   create_registry_database "$registry_db"
   create_config_database "$sm_config_db"

   # Configure Apache Stratos
   setup_apache_stratos
}

function get_host_user(){
    host_user=$(read_user_input "Enter host user :" "" $host_user )
}

function init() {
    # Create a backup of setup.conf file, we are going to change it.
    backup_file $setup_path/conf/setup.conf

    # backup mysql.sql, we are going to write stuff into it
    #backup_file $setup_path/resources/mysql.sql


    list_ip_addreses
    machine_ip=$(read_user_input "Above are the IP addresses assigned to your machine. Please select the preferred IP address : " "" $machine_ip )
    get_host_user

    if [ "$machine_ip" == "" ];then
        echo -e "Machine IP is not specified, so proceeding with the default 127.0.0.1"
        machine_ip="127.0.0.1"
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
    profile="default"
    if [[ $separate_cep = "true" ]]; then
	profile="stratos"
    fi

    if [[ $das_enabled = "true" ]]; then
       # Setup BAM server
       echo "Starting BAM core service..."
      # nohup su - $host_user -c "/bin/bash $setup_path/setup-das.sh -p $profile" >> wso2bam.log
      #  while ! echo exit | nc localhost $BAM_PORT; do sleep $SLEEPTIME; done
       sleep $SLEEPTIME
    fi

    if [[ $wso2_ppaas_enabled = "true" ]]; then
       # Start Apache Stratos with default profile
       echo -e "Starting WSO2 Private PaaS server as $host_user user... "

       if [[ $separate_cep = "true" ]]; then
           su - $host_user -c "source $setup_path/conf/setup.conf;$setup_path/start-servers.sh -p stratos >> $LOG"
           while ! echo exit | nc localhost $PPAAS_PORT; do sleep $SLEEPTIME; done
           sleep $SLEEPTIME

       	   echo -e "Starting WSO2 CEP service..."
           nohup su - $host_user -c "source $setup_path/conf/setup.conf;/bin/bash $stratos_install_path/wso2cep-3.0.0/bin/wso2server.sh &" >> wso2cep.log
           while ! echo exit | nc localhost $CEP_PORT; do sleep $SLEEPTIME; done
           sleep $SLEEPTIME
       else
           su - $host_user -c "source $setup_path/conf/setup.conf;$setup_path/start-servers.sh -p default >> $LOG"
           while ! echo exit | nc localhost $PPAAS_PORT; do sleep $SLEEPTIME; done
           sleep $SLEEPTIME
       fi
    else
        echo -e "Skipping WSO2 Private PaaS startup."
    fi


}


function update_hosts_file() {
    # call the python script to get LB ip
    lb_ip=$(python -c 'import agent; print agent.getLBIp()')

    # update the /etc/hosts file
    echo " " >> /etc/hosts

    echo $machine_ip  $stratos_domain >> /etc/hosts

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
else
     # Run main configuration
     echo -e "Starting WSO2 Private PaaS installer"

     init
     run_setup_sh
     # Do the product specific configurations
     configurator_needed=$(read_user_input "Do you need to setup use configurator to set up private paas? [y/n] " "" $configurator_needed )
     if [[ $configurator_needed =~ ^[Yy]$ ]]; then
        setup_apache_stratos_with_configurator
     else
        setup_apache_stratos
     fi


     # Start core servers
     start_servers

     # Update hosts file
     update_hosts_file

fi

echo ""
echo "**************************************************************"
echo "Management Console : https://$stratos_domain:9443/console"
echo "**************************************************************"
echo -e "\nboot.sh: WSO2 Private PaaS installation completed successfully!\n"
# END