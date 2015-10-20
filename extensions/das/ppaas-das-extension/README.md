# WSO2 Private PaaS DAS Extension

This directory contains DAS Extensions needed for Private PaaS. <br />
1. Add org.wso2.ppaas.das.extension-<ppaas-version>.jar file to '<DAS-HOME>/repository/components/lib/'. <br />
2. Add below UDF class path to 'spark-udf-config.xml' file in '<DAS-HOME>/repository/conf/analytics/spark/' folder. <br />
   <class-name>org.wso2.ppaas.das.extension.TimeUDF</class-name> <br />