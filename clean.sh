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

dir=`dirname $0`
current_dir=`cd $dir;pwd`
stratos_install_path="$current_dir/install"

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

while getopts :Pku:p:h: opts
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
    P)
        clean_puppet="true"
        ;;
    k)
        kill_proc="true"
        ;;
    *)
        help
        exit 1
        ;;
  esac
done

if [[ $kill_proc = "true" ]]; then
   echo 'Stopping all Java processes'
   killall java
   exit 0
fi

if [[ -z $mysql_user ]]; then
   read -p "Please enter MySQL username : " mysql_user  
fi

if [[ -z $mysql_pass ]]; then
   read -s -p "Please enter MySQL password : " mysql_pass
   echo ""  
fi

if [[ -z $clean_puppet ]]; then
   read -p "Do you want to clean Puppet scripts in /etc/puppet? [y/n] " input_clean_puppet
   if [[ $input_clean_puppet =~ ^[Yy]$ ]]; then
      clean_puppet="true"
   else
      clean_puppet="false"
   fi
fi


read -p "Do you want to clean conf.sh? [y/n] " input_clean_conf
if [[ $input_clean_conf =~ ^[Yy]$ ]]; then
    if [[  -f "conf.sh.orig" ]]; then
         cp -f "conf.sh.orig" "conf.sh"

    fi
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
mysql -u $mysql_user -p$mysql_pass -h$mysql_host -e "DROP DATABASE IF EXISTS sm_config;"

if [[ $clean_puppet = "true" ]]; then
   echo "Cleaning Puppet scripts in /etc/puppet/"
   sudo rm -rf /etc/puppet/modules/*
   sudo rm -rf /etc/puppet/manifests/*
   sudo rm -rf /var/lib/puppet/ssl/*

   echo "Restarting Puppet master"
   sudo /etc/init.d/puppetmaster restart
fi


if [[ -z $clean_install ]]; then
   read -p "Do you want to clean Private PaaS Install Directory in $stratos_install_path? [y/n] " input_clean_install
   if [[ $input_clean_install =~ ^[Yy]$ ]]; then
      rm -rf $stratos_install_path/*
      echo "Private PaaS install directory cleaned up !"
   else
      echo "Private PaaS install directory didn't clear"
   fi
fi

# END
