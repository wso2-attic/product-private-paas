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

current_dir=$(dirname $0)

function help() {
    echo ""
    echo "This script will clean WSO2 Private PaaS installation."
    echo "usage:"
    echo "clean.sh -u <mysql username> -p <mysql password> -h <mysql hostname>"
    echo ""
}

if [ "$UID" -ne "0" ]; then
	echo ; echo "  You must be root to run $0.  (Try running 'sudo bash' first.)" ; echo 
	exit 69
fi

while getopts u:p:h: opts
do
  case $opts in
    u)
        mysql_user=${OPTARG}
        ;;
    p)
        mysql_pass=${OPTARG}
        ;;
    h)
        mysql_host=${OPTARG}
        ;;
    *)
        help
        exit 1
        ;;
  esac
done

if [[ -z $mysql_user || -z $mysql_pass ]]; then
   echo "Please provide MySQL username and password"
   help
   exit 1
fi

# Set host to localhost if user has not specified a hostname
mysql_host=${mysql_host:-localhost}
/bin/bash $current_dir/stratos-installer/clean.sh -u$mysql_user -p$mysql_pass -h$mysql_host

mysql -u $mysql_user -p$mysql_pass -h$mysql_host -e "DROP DATABASE IF EXISTS config;"
mysql -u $mysql_user -p$mysql_pass -h$mysql_host -e "DROP DATABASE IF EXISTS StratosStats;"
mysql -u $mysql_user -p$mysql_pass -h$mysql_host -e "DROP DATABASE IF EXISTS esb_config;"
mysql -u $mysql_user -p$mysql_pass -h$mysql_host -e "DROP DATABASE IF EXISTS apim_db;"
mysql -u $mysql_user -p$mysql_pass -h$mysql_host -e "DROP DATABASE IF EXISTS as_config;"
mysql -u $mysql_user -p$mysql_pass -h$mysql_host -e "DROP DATABASE IF EXISTS bps_config;"
mysql -u $mysql_user -p$mysql_pass -h$mysql_host -e "DROP DATABASE IF EXISTS is_config;"
mysql -u $mysql_user -p$mysql_pass -h$mysql_host -e "DROP DATABASE IF EXISTS amstats;"
mysql -u $mysql_user -p$mysql_pass -h$mysql_host -e "DROP DATABASE IF EXISTS apim_gateway_config;"
mysql -u $mysql_user -p$mysql_pass -h$mysql_host -e "DROP DATABASE IF EXISTS apim_keymanager_config;"
mysql -u $mysql_user -p$mysql_pass -h$mysql_host -e "DROP DATABASE IF EXISTS apim_store_config;"
mysql -u $mysql_user -p$mysql_pass -h$mysql_host -e "DROP DATABASE IF EXISTS apim_publisher_config;"
mysql -u $mysql_user -p$mysql_pass -h$mysql_host -e "DROP DATABASE IF EXISTS registry;"


sudo rm -rf /etc/puppet/modules/*
sudo rm -rf /etc/puppet/manifests/*
sudo rm -rf /var/lib/puppet/ssl/*

sudo /etc/init.d/puppetmaster restart
echo -e "\nSuccessfully cleaned up everything!"
# END
