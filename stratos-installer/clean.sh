#!/bin/bash
# ----------------------------------------------------------------------------
#
#  Licensed to the Apache Software Foundation (ASF) under one
#  or more contributor license agreements.  See the NOTICE file
#  distributed with this work for additional information
#  regarding copyright ownership.  The ASF licenses this file
#  to you under the Apache License, Version 2.0 (the
#  "License"); you may not use this file except in compliance
#  with the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing,
#  software distributed under the License is distributed on an
#  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
#  KIND, either express or implied.  See the License for the
#  specific language governing permissions and limitations
#  under the License.
#
# ----------------------------------------------------------------------------
#
#  This script is for cleaning the host machine where one or more of the Stratos servers are run.
# ----------------------------------------------------------------------------

current_dir=$(dirname $0)
source "$current_dir/conf/setup.conf"

if [ "$UID" -ne "0" ]; then
	echo ; echo "  You must be root to run $0.  (Try running 'sudo bash' first.)" ; echo 
	exit 69
fi

function help() {
    echo ""
    echo "This script will clean Apache Stratos installation."
    echo "usage:"
    echo "clean.sh -u <mysql username> -p <mysql password> -h <mysql hostname>"
    echo ""
}

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

# Set host to localhost if user has not specified a hostname
mysql_host=${mysql_host:-localhost}

function helpclean() {
    echo ""
    echo "Enter DB credentials if you need to clear Stratos DB"
    echo "usage:"
    echo "clean.sh -u <mysql username> -p <mysql password>"
    echo ""
}

function clean_validate() {
    if [[ -z $stratos_path ]]; then
        echo "stratos_path is not set"
        exit 1
    fi
    if [[ -z $log_path ]]; then
        echo "log_path is not set"
        exit 1
    fi
}

clean_validate
if [[ ( -n $mysql_user && -n $mysql_pass ) ]]; then
	read -p "Please confirm that you want to remove Apache Stratos databases, servers and logs [y/n] " answer
	if [[ $answer != y ]] ; then
    		exit 1
	fi
fi
echo 'Stopping all java processes'
killall java
echo 'Waiting for applications to exit'
sleep 5

if [[ ( -n $mysql_user && -n $mysql_pass ) ]]; then
   echo 'Removing userstore database'
   mysql -u $mysql_user -p$mysql_pass -h$mysql_host -e "DROP DATABASE IF EXISTS $userstore_db_schema;"
fi

if [[ -d $stratos_path/scripts ]]; then
   echo 'Removing scripts'
   rm -rf $stratos_path/scripts
fi


echo 'Removing logs'
rm -rf $log_path/*
