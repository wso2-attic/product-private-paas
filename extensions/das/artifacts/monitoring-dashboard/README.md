# WSO2 Private PaaS Monitoring Dashboard

This directory contains following artifacts:
(1) capps - Includes ppaas-monitoring-service car file which bundles all Event Stream, Event receiver, Even Store, Gadgets and Dashboard artifacts.
(2) jaggery-files

Follow the below steps to generate the monitoring dashboard:
1. Add the jaggery files which can be found inside directory 'jaggery-files' to DAS server path '/jaggeryapps/portal/controllers/apis'
2. Create MySQL database and tables using queries in 'mysqlscript.sql' manually.
3. Add ppaas-monitoring-service car file to DAS server to generate the monitoring dashboard.