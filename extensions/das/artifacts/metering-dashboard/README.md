# WSO2 Private PaaS Metering Dashboard

This directory contains following artifacts: <br/>
(1) capps - Includes ppaas-metering-service car file which bundles all Event Stream, Event receiver, Even Store, Gadgets, SparkScripts and Dashboard artifacts. <br/>
(2) jaggery-files <br/>
(3) ues-patch <br/>

Follow the below steps to generate the metering dashboard: <br/>
1. Follow instruction given in <PPaaS-SOURCE-HOME>/extensions/das/ppaas-das-extension/README.md file to add ppaas-das-extension jar to DAS. <br/>
2. Add jaggery files which can be found in <PPaaS-DAS-Distribution>/metering-dashboard/jaggery-files/ to DAS server path <DAS_HOME/repository/deployment/server/jaggeryapps/portal/controllers/apis/ <br/>
3. Create MySQL database and tables using queries in <PPaaS-DAS-Distribution>/metering-dashboard/metering-mysqlscript.sql manually. <br/>
4. Apply ues-patch files in <PPaaS-DAS-Distribution>/metering-dashboard/ues-patch/ to DAS as mentioned in its README file. <br/>
5. Add ppaas-metering-service car file in <PPaaS-DAS-Distribution>/metering-dashboard/ to <DAS-HOME>/repository/deployment/server/carbonapps/ to generate the metering dashboard. <br/>