# WSO2 Private PaaS DAS Extension

This directory contains DAS Extensions needed for Private PaaS.
1. Add the org.wso2.ppaas.das.extension-<ppaas-version>.jar file to '<DAS-HOME>/repository/components/lib'.
2. Add each UDF class path to 'spark-udf-config.xml' file in '<DAS-HOME>/repository/conf/analytics/spark/' folder.
   Example: <class-name>org.wso2.ppaas.das.extension.TimeUDF</class-name>