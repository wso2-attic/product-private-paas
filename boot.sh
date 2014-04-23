#!/bin/bash

# Die on any error:
set -e

current_dir=`pwd`
setup_path="$current_dir/stratos-installer"
stratos_pack_path="$current_dir/packs"
stratos_install_path="$current_dir/install"
resource_path="$current_dir/resources"
cep_artifact_path="$resource_path/cep/artifacts/"
puppet_env="stratos"

backup_file(){
    if [[  -f "$1.orig" ]];
    then
        echo "Restoring from the Original template file $1"
        cp -f "$1.orig" "$1"
    else
        echo -e "Creating a backof of the file $1"
        cp -f "$1" "$1.orig"
    fi
}

backup_file $setup_path/conf/setup.conf

replace_setup_conf(){
    #echo "Setting value $2 for property $1 as $2"
    setp_conf_file=$setup_path/conf/setup.conf
    sed -i s$'\001'"$1"$'\001'"$2"$'\001''g' $setp_conf_file
}

replace_in_file(){
    #echo "Setting value $2 for property $1 as $2 in file $3"
    sed -i "s@$1@$2@g"  $3
}

install_mysql(){
    export DEBIAN_FRONTEND=noninteractive
    sudo apt-get -q -y install mysql-server  --force-yes

    echo -e "Setting MySQL password $mysql_password for $mysql_uname
    mysqladmin -u $mysql_uname password $mysql_password  ";
}

#mysql_installed(){
#    status=`service mysql status`
#    pattern="start/running"

#    if [[ $status ==  *$pattern* ]]
#    then
#        return 0
#    else
#        return 1
#    fi
#}

create_registry_database(){
    echo -e "Creating the database $1"

    cp -f "$resource_path/registry.sql" "$resource_path/registry.sql.orig"
    sed -i "s@REGISTRY_DB_SCHEMA@$1@g"  "$resource_path/registry.sql.orig"
    mysql -u$mysql_uname -p$mysql_password < "$resource_path/registry.sql.orig"
    rm "$resource_path/registry.sql.orig"
}

list_ip_addreses(){
 # echo -e "Below are IP addresses assigned to your machine. Please enter prefered IP "
 /sbin/ifconfig | grep "inet addr" | awk -F: '{print $2}' | awk '{print $1}'
}


read -p "Please enter a prefered domain name for the private PAAS environment " stratos_domain
list_ip_addreses
read -p "Above are the IP addreses assigned to your machine. Please select the prefered IP address " machine_ip
read -p "Enter host user " host_user

# Puppet
read -p "Enter Puppet  IP " puppet_ip
read -p "Enter Puppet  hostname  " puppet_host
read -p "Enter package repository URL  " package_repo

# MySQL
read -p "Do you need to install MySQL? " setup_mysql

read -p "Please provide MySQL host? " mysql_host
read -p "Please provide MySQL port. Default port is 3306 " mysql_port
read -p "Please provide MySQL username. Default username is root " mysql_uname
read -s -p "Please provide MySQL password? " mysql_password
echo ""

if [[ $setup_mysql =~ ^[Yy]$ ]]
then
    echo -e "Starting to install MySQL. \n"
    install_mysql

else
    echo -e "Setup did not install MySQL. \n";
fi


list_ec2_regions(){
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

read -p "Enter your IAAS. vCloud, EC2 and Openstack are the currently supported IAASs. Enter vcloud for vCloud ec2 for EC2 and os for OpenStack." iaas
if [[ "$iaas" == "os" ]];then
    echo -e "You selected OpenStack "
    read -p "Enter OpensStack  identity  " os_identity
    read -p "Enter OpensStack  credentials  " os_credentials
    read -p "Enter OpensStack  jclouds_endpoint " os_jclouds_endpoint
    read -p "Enter the region of the IAAS you want to spin up instances " $region
    read -p "Enter OpensStack  keypair name " os_keypair_name
    read -p "Enter OpensStack security groups  " os_security_groups
else  if [[ "$iaas" == "ec2" ]];then
    echo -e "You selected Amazon EC2 "
    read -p "Enter EC2  identity  " ec2_identity
    read -p "Enter EC2  credentials  " ec2_credentials
    read -p "Enter EC2  owner id  " ec2_owner_id
    read -p "Enter EC2  keypair name  " ec2_keypair_name
    read -p "Enter EC2  security groups  " ec2_security_groups
    list_ec2_regions
    read -p "Enter the region of the IAAS you want to spin up instances " $region
    read -p "Enter EC2 availability zone  " ec2_availability_zone
else  if [[ "$iaas" == "vcloud" ]];then
    read -p "Enter vCloud  identity  " vcloud_identity
    read -p "Enter vCloud  credentials  " vcloud_credentials
    read -p "Enter vCloud  jclouds_endpoint " vcloud_jclouds_endpoint
fi
fi
fi

if [ "$machine_ip" == "" ];then
    echo -e "IP is not specified, so proceeding with the default 127.0.0.1"
    machine_ip="127.0.0.1"
fi

if [ "$JAVA_HOME" == "" ];then
    read -p "JAVA_HOME is not set as a environment variable. Please set it specify it here " java_home
    JAVA_HOME=$java_home
fi

read -p "Do you need to deploy AS (Application Server) service ? y/n " -n 1 -r as_needed
echo
read -p "Do you need to deploy ESB (Enterprise Service Bus) service ? y/n " -n 1 -r  esb_needed
echo
read -p "Do you need to deploy BPS (Business Process Server) service ? y/n " -n 1 -r  bps_needed
echo

read -p "Are you sure you want to start WSO2 Private PAAS ? y/n " -r  confirm

if [[ $confirm =~ ^[nN]$ ]]
then
    echo -e "Exiting the instalation."
    exit
fi

replace_setup_conf "STRATOS_SETUP_PATH" "$setup_path"
replace_setup_conf "PACK_PATH" "$stratos_pack_path"
replace_setup_conf "INSTALLER_PATH" "$stratos_install_path"

replace_setup_conf "JAVAHOME" "$JAVA_HOME"
replace_setup_conf "HOST_USER" "$host_user"

replace_setup_conf "STRATOS_DOMAIN" "$stratos_domain"

replace_setup_conf "machine_ip" "$machine_ip"


replace_setup_conf "puppet-ip" "$puppet_ip"
replace_setup_conf "puppet-host" "$puppet_host"
replace_setup_conf "puppet-environment" "$puppet_env"
replace_setup_conf "CEP_ARTIFACTS_PATH" "$cep_artifact_path"

if [ "$mysql_port" == "" ];then
    mysql_port=3306
fi

if [ "$mysql_uname" == "" ];then
    mysql_uname="root"
fi

replace_setup_conf "DB_HOST" "$mysql_host"
replace_setup_conf "DB_PORT" "$mysql_port"
replace_setup_conf "DB_USER" "$mysql_uname"
replace_setup_conf "DB_PASSWORD" "$mysql_password"

if [[ "$iaas" == "os" ]];then
    replace_setup_conf "OS_ENABLED" "true"
    replace_setup_conf "OS_IDENTITY" "$os_identity"
    replace_setup_conf "OS_CREDENTIAL" "$os_credentials"
    replace_setup_conf "OS_JCLOUDS_ENDPOINT" "$os_jclouds_endpoint"
    replace_setup_conf "OS_KEYPAIR_NAME" "$os_keypair_name"
    replace_setup_conf "OS_SECURITY_GROUPS" "$os_security_groups"
else if [[ "$iaas" == "ec2" ]];then
    replace_setup_conf "EC2_ENABLED" "true"
    replace_setup_conf "EC2_IDENTITY" "$ec2_identity"
    replace_setup_conf "EC2_CREDENTIAL" "$ec2_credentials"
    replace_setup_conf "EC2_KEYPAIR_NAME" "$ec2_keypair_name"
    replace_setup_conf "EC2_OWNER_ID" "$ec2_owner_id"
    replace_setup_conf "EC2_AVAILABILITY_ZONE" "$ec2_availability_zone"
    replace_setup_conf "EC2_SECURITY_GROUPS" "$ec2_security_groups"
else if [[ "$iaas" == "vcloud" ]];then
    replace_setup_conf "VCLOUD_ENABLED" "true"
    replace_setup_conf "VCLOUD_IDENTITY" "$vcloud_identity"
    replace_setup_conf "VCLOUD_CREDENTIAL" "$vcloud_credentials"
    replace_setup_conf "VCLOUD_JCLOUDS_ENDPOINT" "$vcloud_jclouds_endpoint"
fi
fi
fi

# replace the region of partition file
sed  "s/REGION/$region/g" resources/json/p1.json > tmp/p1.json

# Create databases for the governence registry
# Using the same userstore to the registry
registry_db="userstore"
#create_registry_database "$registry_db"


as_config_path="config/as"
esb_config_path="config/esb"


backup_file "/etc/puppet/manifests/nodes.pp"

replace_in_file "PACKAGE_REPO" "$package_repo" "/etc/puppet/manifests/nodes.pp"
replace_in_file "MB_IP" "$machine_ip" "/etc/puppet/manifests/nodes.pp"
replace_in_file "MB_PORT" "5677" "/etc/puppet/manifests/nodes.pp"
replace_in_file "CEP_IP" "$machine_ip" "/etc/puppet/manifests/nodes.pp"
replace_in_file "CEP_PORT" "7615" "/etc/puppet/manifests/nodes.pp"
replace_in_file "DB_HOST" "$mysql_host" "/etc/puppet/manifests/nodes.pp"
replace_in_file "DB_PORT" "$mysql_port" "/etc/puppet/manifests/nodes.pp"
replace_in_file "BAM_IP" "$machine_ip" "/etc/puppet/manifests/nodes.pp"
replace_in_file "BAM_PORT" "7611" "/etc/puppet/manifests/nodes.pp"
replace_in_file "AS_CONFIG_DB" "$registry_db" "/etc/puppet/manifests/nodes.pp"
replace_in_file "AS_CONFIG_PATH" "$as_config_path" "/etc/puppet/manifests/nodes.pp"
replace_in_file "ESB_CONFIG_DB" "$registry_db" "/etc/puppet/manifests/nodes.pp"
replace_in_file "ESB_CONFIG_PATH" "$esb_config_path" "/etc/puppet/manifests/nodes.pp"
replace_in_file "BPS_CONFIG_DB" "$registry_db" "/etc/puppet/manifests/nodes.pp"
replace_in_file "BPS_CONFIG_PATH" "$esb_config_path" "/etc/puppet/manifests/nodes.pp"

backup_file "/etc/puppet/modules/appserver/manifests/params.pp"
replace_in_file "ADMIN_USER" "admin" "/etc/puppet/modules/appserver/manifests/params.pp"
replace_in_file "ADMIN_PASSWORD" "admin" "/etc/puppet/modules/appserver/manifests/params.pp"
replace_in_file "DB_USER" "$mysql_uname" "/etc/puppet/modules/appserver/manifests/params.pp"
replace_in_file "DB_PASSWORD" "$mysql_password" "/etc/puppet/modules/appserver/manifests/params.pp"
replace_in_file "REGISTRY_DB" "$registry_db" "/etc/puppet/modules/appserver/manifests/params.pp"
replace_in_file "USERSTORE_DB" "userstore" "/etc/puppet/modules/appserver/manifests/params.pp"

backup_file "/etc/puppet/modules/esb/manifests/params.pp"
replace_in_file "ADMIN_USER" "admin" "/etc/puppet/modules/esb/manifests/params.pp"
replace_in_file "ADMIN_PASSWORD" "admin" "/etc/puppet/modules/esb/manifests/params.pp"
replace_in_file "DB_USER" "$mysql_uname" "/etc/puppet/modules/esb/manifests/params.pp"
replace_in_file "DB_PASSWORD" "$mysql_password" "/etc/puppet/modules/esb/manifests/params.pp"
replace_in_file "REGISTRY_DB" "$registry_db" "/etc/puppet/modules/esb/manifests/params.pp"
replace_in_file "USERSTORE_DB" "userstore" "/etc/puppet/modules/esb/manifests/params.pp"

backup_file "/etc/puppet/modules/bps/manifests/params.pp"
replace_in_file "ADMIN_USER" "admin" "/etc/puppet/modules/bps/manifests/params.pp"
replace_in_file "ADMIN_PASSWORD" "admin" "/etc/puppet/modules/bps/manifests/params.pp"
replace_in_file "DB_USER" "$mysql_uname" "/etc/puppet/modules/bps/manifests/params.pp"
replace_in_file "DB_PASSWORD" "$mysql_password" "/etc/puppet/modules/bps/manifests/params.pp"
replace_in_file "REGISTRY_DB" "$registry_db" "/etc/puppet/modules/bps/manifests/params.pp"
replace_in_file "USERSTORE_DB" "userstore" "/etc/puppet/modules/bps/manifests/params.pp"

cwd=$(pwd)
cd stratos-installer
/bin/bash setup.sh -p "default" -s
cd $cwd

export JAVA_HOME=$JAVA_HOME
echo -e "Unzipping and starting WSO2 BAM "
unzip -o -q $stratos_pack_path/wso2bam-2.4.0.zip -d $stratos_install_path
nohup $stratos_install_path/wso2bam-2.4.0/bin/wso2server.sh -DportOffset=1 &

if [ -e $stratos_pack_path/wso2is-4.6.0-private-paas.zip ]
then
   unzip -o -q $stratos_pack_path/wso2is-4.6.0-private-paas.zip -d $stratos_install_path
   nohup $stratos_install_path/wso2is-4.6.0/bin/wso2server.sh -DportOffset=2 &
else
   echo "IS pack [ $stratos_pack_path/wso2is-4.6.0-private-paas.zip ] not found!"
fi


# waiting a bit since products become up and running
sleep 3m 
echo -e "Deploying a partition at $resource_path/json/partition.json"
curl -X POST -H "Content-Type: application/json" -d @"$resource_path/json/partition.json" -k  -u admin:admin "https://$machine_ip:9443/stratos/admin/policy/deployment/partition"

echo -e ""
echo -e "Deploying a autoscale policy  at $resource_path/json/autoscale-policy.json"
curl -X POST -H "Content-Type: application/json" -d @"$resource_path/json/autoscale-policy.json" -k  -u admin:admin "https://$machine_ip:9443/stratos/admin/policy/autoscale"

echo -e ""
echo -e "Deploying a deployment policy at $resource_path/json/deployment-policy.json"
curl -X POST -H "Content-Type: application/json" -d @"$resource_path/json/deployment-policy.json" -k  -u admin:admin "https://$machine_ip:9443/stratos/admin/policy/deployment"

echo -e ""
echo -e "Deploying a LB cartridge at $resource_path/json/lb-cart.json"
curl -X POST -H "Content-Type: application/json" -d @"$resource_path/json/lb-cart.json" -k  -u admin:admin "https://$machine_ip:9443/stratos/admin/cartridge/definition"

if [[ $as_needed =~ ^[Yy]$ ]]
then
    echo -e "Deploying a Aplication Server (AS) cartridge at $resource_path/json/appserver-cart.json"
    curl -X POST -H "Content-Type: application/json" -d @"$resource_path/json/appserver-cart.json" -k  -u admin:admin "https://$machine_ip:9443/stratos/admin/cartridge/definition"

    echo -e "Deploying a Application Service service"
    curl -X POST -H "Content-Type: application/json" -d @"$resource_path/json/appserver-service-deployment.json" -k -u admin:admin https://$machine_ip:9443/stratos/admin/service/definition
fi

if [[ $esb_needed =~ ^[Yy]$ ]]
then
echo -e ""
    echo -e "Enterprise Service Bus (ESB) cartridge at $resource_path/json/esb-cart.json"
    curl -X POST -H "Content-Type: application/json" -d @"$resource_path/json/esb-cart.json" -k  -u admin:admin "https://$machine_ip:9443/stratos/admin/cartridge/definition"

echo -e ""
    echo -e "Enterprise Service Bus (ESB) service at esb-service-deployment.json"
    curl -X POST -H "Content-Type: application/json" -d @"$resource_path/json/esb-service-deployment.json" -k -u admin:admin https://$machine_ip:9443/stratos/admin/service/definition
fi

echo -e ""
if [[ $bps_needed =~ ^[Yy]$ ]]
then
    echo -e "Deploying a Business Process Server (BPS) cartridge at $resource_path/json/bps-cart.json"
    curl -X POST -H "Content-Type: application/json" -d @"$resource_path/json/bps-cart.json" -k  -u admin:admin "https://$machine_ip:9443/stratos/admin/cartridge/definition"

    echo -e "Deploying a Business Process Server service"
    curl -X POST -H "Content-Type: application/json" -d @"$resource_path/json/bps-service-deployment.json" -k -u admin:admin https://$machine_ip:9443/stratos/admin/service/definition
fi


echo -e "Completed.."
