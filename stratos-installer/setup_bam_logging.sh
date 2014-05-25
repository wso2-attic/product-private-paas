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
stratos_extract_path=$stratos_extract_path"-default"

#setting the public IP for bam
export public_ip=$(curl --silent http://ipecho.net/plain; echo)

function start_bam() {
       echo "Starting Hadoop server ..."
       $hadoop_path/bin/start-all.sh

       echo "Starting WSO2 BAM server ..."
       nohup $bam_path/bin/wso2server.sh -DportOffset=1 &
}

# In silent mode, start BAM server and do not make any configurations
if [[ -z $silent_mode && $silent_mode = "true" ]]; then
       start_bam
       exit 0
fi

echo "Enabling log publishing in Stratos"
# Enable log viewer and log puplisher in stratos
cp -f $current_dir/config/all/repository/conf/etc/logging-config.xml $stratos_extract_path/repository/conf/etc/
cp -f $current_dir/config/all/repository/conf/log4j.properties $stratos_extract_path/repository/conf/

pushd $stratos_extract_path

#Setting the bam location in stratos
sed -i "s@<!--<BamServerURL>https://bamhost:bamport/services/</BamServerURL>-->@<BamServerURL>${host_ip}:${bam_thrift_port}</BamServerURL>@g" repository/conf/carbon.xml

sed -i 's@<dataPublisher enable="false">@<dataPublisher enable="true">@g' repository/conf/cloud-controller.xml
sed -i '$a bam.publisher.enabled=true' repository/conf/cartridge-config.properties
sed -i '$a bam.admin.username=admin' repository/conf/cartridge-config.properties
sed -i '$a bam.admin.password=admin' repository/conf/cartridge-config.properties

#Setting the BAM link in Stratos Console
sed -i "s@BAM_HOST@${public_ip}@g" repository/deployment/server/jaggeryapps/console/themes/theme1/partials/header.hbs
sed -i "s@BAM_PORT@$9444@g" repository/conf/carbon.xml

popd

unzip -q $bam_pack_path -d $stratos_path

cp -f $current_dir/config/bam/repository/conf/etc/summarizer-config.xml $bam_path/repository/conf/etc/
cp -f $current_dir/config/bam/repository/conf/advanced/hive-site.xml $bam_path/repository/conf/advanced/
cp -f $current_dir/config/bam/repository/conf/datasources/master-datasources.xml $bam_path/repository/conf/datasources/
cp -f $current_dir/config/bam/Private_PaaS_Statistics_Monitoring.tbox $bam_path/repository/deployment/server/bam-toolbox/
cp -f $current_dir/config/bam/repository/components/dropins/org.wso2.carbon.logging.summarizer-4.2.0.jar $bam_path/repository/components/dropins/
cp -f $current_dir/config/bam/repository/components/dropins/org.wso2.carbon.databridge.agent.thrift-4.0.5.jar $bam_path/repository/components/dropins/
cp -f $mysql_connector_jar $bam_path/repository/components/lib/

pushd $bam_path

echo "Setting up BAM"

cp -f repository/conf/datasources/master-datasources.xml repository/conf/datasources/master-datasources.xml.orig
cat repository/conf/datasources/master-datasources.xml.orig | sed -e "s@MYSQL_HOSTNAME@$dashboard_db_hostname@g" > repository/conf/datasources/master-datasources.xml

cp -f repository/conf/datasources/master-datasources.xml repository/conf/datasources/master-datasources.xml.orig
cat repository/conf/datasources/master-datasources.xml.orig | sed -e "s@MYSQL_PORT@$dashboard_db_port@g" > repository/conf/datasources/master-datasources.xml

cp -f repository/conf/datasources/master-datasources.xml repository/conf/datasources/master-datasources.xml.orig
cat repository/conf/datasources/master-datasources.xml.orig | sed -e "s@MYSQL_USERNAME@$dashboard_db_user@g" > repository/conf/datasources/master-datasources.xml

cp -f repository/conf/datasources/master-datasources.xml repository/conf/datasources/master-datasources.xml.orig
cat repository/conf/datasources/master-datasources.xml.orig | sed -e "s@MYSQL_PASSWORD@$dashboard_db_pass@g" > repository/conf/datasources/master-datasources.xml

cp -f repository/conf/datasources/master-datasources.xml repository/conf/datasources/master-datasources.xml.orig
cat repository/conf/datasources/master-datasources.xml.orig | sed -e "s@CASSANDRA_HOST@$dashboard_cassendra_host@g" > repository/conf/datasources/master-datasources.xml

cp -f repository/conf/datasources/master-datasources.xml repository/conf/datasources/master-datasources.xml.orig
cat repository/conf/datasources/master-datasources.xml.orig | sed -e "s@CASSANDRA_PORT@$dashboard_cassendra_port@g" > repository/conf/datasources/master-datasources.xml

cp -f repository/conf/datasources/master-datasources.xml repository/conf/datasources/master-datasources.xml.orig
cat repository/conf/datasources/master-datasources.xml.orig | sed -e "s@CASSANDRA_USER@$dashboard_cassendra_user@g" > repository/conf/datasources/master-datasources.xml

cp -f repository/conf/datasources/master-datasources.xml repository/conf/datasources/master-datasources.xml.orig
cat repository/conf/datasources/master-datasources.xml.orig | sed -e "s@CASSANDRA_PASSWORD@$dashboard_cassendra_password@g" > repository/conf/datasources/master-datasources.xml

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
   cp -f $current_dir/config/hadoop/hdfs-site.xml $hadoop_path/conf/
   cp -f $current_dir/config/hadoop/mapred-site.xml $hadoop_path/conf/
   #setting java_home in hadoop-env.sh
   echo "export JAVA_HOME=$JAVA_HOME" >> $hadoop_path/conf/hadoop-env.sh

   echo -e  'y\n' | ssh-keygen -t dsa -P '' -f ~/.ssh/id_dsa
   cat ~/.ssh/id_dsa.pub >> ~/.ssh/authorized_keys

   echo 'Y' | $hadoop_path/bin/hadoop namenode -format -a

   # Start BAM server
   start_bam
else
   echo "Gitblit pack [ $gitblit_pack_path ] not found!"
   exit 1
fi

# END
