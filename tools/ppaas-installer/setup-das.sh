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

source "$current_dir/conf/setup.conf"

#setting the public IP for bam
export public_ip=$(curl --silent http://ipecho.net/plain; echo)
export hadoop_hostname=$(hostname -f)

while getopts ":p:" opts
do
  case $opts in
    p)
        profile_list=${OPTARG}
        ;;
    \?)
        exit 1
        ;;
  esac
done

profile_list=`echo $profile_list | sed 's/^ *//g' | sed 's/ *$//g'`
if [[ !(-z $profile_list || $profile_list = "") ]]; then
    arr=$(echo $profile_list | tr " " "\n")

    for x in $arr
    do
    	if [[ $x = "default" ]]; then
            profile="default"
    	elif [[ $x = "stratos" ]]; then
            profile="stratos"
        else
            echo "Invalid profile."
            exit 1
    	fi
    done
else
    profile="default"
fi

stratos_extract_path=$stratos_extract_path"-"$profile

function start_das() {
       echo "Starting Hadoop server ..."
       $hadoop_path/bin/start-all.sh

       echo "Starting WSO2 das server ..."
       nohup $das_path/bin/wso2server.sh -DportOffset=2 &
}

function get_mysql_connector_jar() {
	IFS='/'
	arr=($mysql_connector_jar)
	for x in ${!arr[*]} ; do
	   connector_jar=${arr[x]}
	done
	IFS=$' \t\n'
}

# In silent mode, start das server and do not make any configurations
if [[ -z $silent_mode && $silent_mode = "true" ]]; then
       start_das
       exit 0
fi

echo "Enabling log publishing in Stratos"
# Enable log viewer and log puplisher in stratos
cp -f $current_dir/config/all/repository/conf/etc/logging-config.xml $stratos_extract_path/repository/conf/etc/
cp -f $current_dir/config/all/repository/conf/log4j.properties $stratos_extract_path/repository/conf/
cp -rf $current_dir/config/all/repository/components/patches/patch0900 $stratos_extract_path/repository/components/patches/

pushd $stratos_extract_path

#Setting the das location in stratos
sed -i "s@<!--<dasServerURL>https://dashost:dasport/services/</dasServerURL>-->@<dasServerURL>${host_ip}:${das_thrift_port}</dasServerURL>@g" repository/conf/carbon.xml

sed -i 's@<dataPublisher enable="false">@<dataPublisher enable="true">@g' repository/conf/cloud-controller.xml
sed -i "s@ENABLE@true@g" repository/conf/cartridge-config.properties
sed -i "s@das_IP@${public_ip}@g" repository/conf/cartridge-config.properties
sed -i "s@das_PORT@9444@g" repository/conf/cartridge-config.properties
sed -i "s@das_UNAME@admin@g" repository/conf/cartridge-config.properties
sed -i "s@das_PASS@admin@g" repository/conf/cartridge-config.properties

popd

unzip -q $das_pack_path -d $stratos_path

cp -f $current_dir/config/das/repository/conf/etc/summarizer-config.xml $das_path/repository/conf/etc/
cp -f $current_dir/config/das/repository/conf/advanced/hive-site.xml $das_path/repository/conf/advanced/
cp -f $current_dir/config/das/repository/conf/datasources/master-datasources.xml $das_path/repository/conf/datasources/
cp -f $current_dir/config/das/Private_PaaS_Statistics_Monitoring.tbox $das_path/repository/deployment/server/das-toolbox/
cp -f $current_dir/config/das/repository/components/dropins/org.wso2.carbon.logging.summarizer-4.2.0.jar $das_path/repository/components/dropins/
cp -f $current_dir/config/das/repository/components/dropins/org.wso2.carbon.databridge.agent.thrift-4.0.5.jar $das_path/repository/components/dropins/
cp -f $mysql_connector_jar $das_path/repository/components/lib/

pushd $das_path

get_mysql_connector_jar

echo "Setting up das"

sed -i "s@MYSQL_HOSTNAME@$dashboard_db_hostname@g" repository/conf/datasources/master-datasources.xml
sed -i "s@MYSQL_PORT@$dashboard_db_port@g" repository/conf/datasources/master-datasources.xml
sed -i "s@MYSQL_USERNAME@$dashboard_db_user@g" repository/conf/datasources/master-datasources.xml
sed -i "s@MYSQL_PASSWORD@$dashboard_db_pass@g" repository/conf/datasources/master-datasources.xml
sed -i "s@CASSANDRA_HOST@$dashboard_cassendra_host@g" repository/conf/datasources/master-datasources.xml
sed -i "s@CASSANDRA_PORT@$dashboard_cassendra_port@g" repository/conf/datasources/master-datasources.xml
sed -i "s@CASSANDRA_USER@$dashboard_cassendra_user@g" repository/conf/datasources/master-datasources.xml
sed -i "s@CASSANDRA_PASSWORD@$dashboard_cassendra_password@g" repository/conf/datasources/master-datasources.xml
sed -i "s@DATANODEHOST@$hadoop_hostname@g" repository/conf/advanced/hive-site.xml
sed -i "s@JOBTRACKERSHOST@$hadoop_hostname@g" repository/conf/advanced/hive-site.xml
sed -i "s@CONNECTOR_JAR@$connector_jar@g" repository/conf/advanced/hive-site.xml

popd

# Database Configuration
# -----------------------------------------------
echo "Create and configure MySql Databases for Dashboard"

echo "Creating dashboard database"

mysql -u$dashboard_db_user -p$dashboard_db_pass < $resource_path/dashboard.sql


# Hadoop setup
# -----------------------------------------------
echo "Setting up Hadoop"

if [[ -e $hadoop_pack_path ]]; then
   tar xzf $hadoop_pack_path -C $stratos_path

   sudo apt-get -q -y install ssh --force-yes
   sudo apt-get -q -y install rsync --force-yes

   cp -f $current_dir/config/hadoop/core-site.xml $hadoop_path/conf/
   sed -i "s@PATH@$stratos_path@g" $hadoop_path/conf/core-site.xml
   cp -f $current_dir/config/hadoop/hdfs-site.xml $hadoop_path/conf/
   sed -i "s@PATH@$stratos_path@g" $hadoop_path/conf/hdfs-site.xml
   cp -f $current_dir/config/hadoop/mapred-site.xml $hadoop_path/conf/
   sed -i "s@PATH@$stratos_path@g" $hadoop_path/conf/mapred-site.xml

   #setting java_home in hadoop-env.sh
   echo "export JAVA_HOME=$JAVA_HOME" >> $hadoop_path/conf/hadoop-env.sh

   echo -e  'y\n' | ssh-keygen -t dsa -P '' -f ~/.ssh/id_dsa
   cat ~/.ssh/id_dsa.pub >> ~/.ssh/authorized_keys

   echo 'Y' | $hadoop_path/bin/hadoop namenode -format -a

   # Start das server
   start_das
else
   echo "Hadoop pack [ $hadoop_pack_path ] not found!"
   exit 1
fi