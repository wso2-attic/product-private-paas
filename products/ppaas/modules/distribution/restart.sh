cd target
rm apache-stratos-4.1.0 -r
unzip -q apache-stratos-4.1.0.zip

cd apache-stratos-4.1.0/
#cp ~/Desktop/cloud-controller.xml ~/Desktop/log4j.properties ~/Desktop/mock-iaas.xml repository/conf/
cp ~/Desktop/web.xml repository/conf/tomcat/carbon/WEB-INF/web.xml

#read -p "Press any key to continue... " -n1 -s
#sh bin/stratos.sh -Dread.write.lock.monitor.enabled debug 5005
sh bin/stratos.sh 
