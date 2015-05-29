rm apche-stratos-4.1.0/ -rf
mysql -u root -proot -e "DROP DATABASE userstore"
mysql -u root -proot -e "CREATE DATABASE userstore"
mysql -u root -proot -e "USE userstore"

unzip -q -o apache-stratos-4.1.0.zip
mysql  -u "root" "-proot" "userstore" < "dbscripts/registry/mysql.sql"
mysql  -u "root" "-proot" "userstore" < "dbscripts/identity/application-mgt/mysql.sql"
mysql  -u "root" "-proot" "userstore" < "resources/dbscripts/identity/mysql.sql"

cd apache-stratos-4.1.0/
#cp /home/ubuntu/cloud-controller.xml /home/ubuntu/autoscaler.xml /home/ubuntu/cartridge-config.properties /home/ubuntu/apache-stratos-4.1.0/repository/conf
#cp /home/ubuntu/org.apache.stratos.autoscaler-4.1.0.jar /home/ubuntu/apache-stratos-4.1.0/repository/components/plugins
cp ../../master-datasources.xml repository/conf/datasources/master-datasources.xml
cp ../../mysql-connector-java-5.1.30-bin.jar repository/components/lib
#cp /home/ubuntu/metadata.war /home/ubuntu/apache-stratos-4.1.0/repository/deployment/server/webapps/


#read -p "Press any key to continue... " -n1 -s
#sh bin/stratos.sh -Dread.write.lock.monitor.enabled debug 5005
nohup sh bin/stratos.sh &
