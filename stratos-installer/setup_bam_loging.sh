# Die on any error:
set -e

source "./conf/setup.conf"
export LOG=$log_path/stratos-setup.log
stratos_extract_path=$stratos_extract_path"-default"

#setting the public IP for bam
export public_ip=$(curl --silent http://ipecho.net/plain; echo)


echo "Enabling log publishing in Stratos"
# Enable log viewer and log puplisher in stratos
cp -f ./config/all/repository/conf/etc/logging-config.xml $stratos_extract_path/repository/conf/etc/
cp -f ./config/all/repository/conf/log4j.properties $stratos_extract_path/repository/conf/

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

cp -f ./config/bam/repository/conf/etc/summarizer-config.xml $bam_path/repository/conf/etc/
cp -f ./config/bam/repository/conf/advanced/hive-site.xml $bam_path/repository/conf/advanced/
cp -f ./config/bam/repository/conf/datasources/master-datasources.xml $bam_path/repository/conf/datasources/
cp -f ./config/bam/Private_PaaS_Statistics_Monitoring.tbox $bam_path/repository/deployment/server/bam-toolbox/
cp -f ./config/bam/repository/components/dropins/org.wso2.carbon.logging.summarizer-4.2.0.jar $bam_path/repository/components/dropins/
cp -f ./config/bam/repository/components/dropins/org.wso2.carbon.databridge.agent.thrift-4.0.5.jar $bam_path/repository/components/dropins/
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

tar xzf $hadoop_pack_path -C $stratos_path

sudo apt-get -q -y install ssh --force-yes
sudo apt-get -q -y install rsync --force-yes

cp -f ./config/hadoop/core-site.xml $hadoop_path/conf/
cp -f ./config/hadoop/hdfs-site.xml $hadoop_path/conf/
cp -f ./config/hadoop/mapred-site.xml $hadoop_path/conf/

echo -e  'y\n' | ssh-keygen -t dsa -P '' -f ~/.ssh/id_dsa
cat ~/.ssh/id_dsa.pub >> ~/.ssh/authorized_keys

echo 'Y' | $hadoop_path/bin/hadoop namenode -format -a

# -----------------------------------------------

echo "Starting Hadoop server ..." >> $LOG
$hadoop_path/bin/start-all.sh
echo "Hadoop server started" >> $LOG

echo "Starting WSO2 BAM server ..." >> $LOG
nohup $bam_path/bin/wso2server.sh -DportOffset=1 &
echo "BAM server started" >> $LOG



