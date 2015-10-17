# WSO2 Private PaaS Metering Dashboard Spark UDFs (User Defined Functions)

This directory contains Spark UDFs (user Defined Function) required for executing the spark queries with UDFs.
Follow the below steps to use UDF in spark environment:
1. Add the org.wso2.ppaas.das.extension-<ppaas-version>.jar file to '<DAS-HOME>/repository/components/lib'.
2. Add each UDF class path to 'spark-udf-config.xml' file in '<DAS-HOME>/repository/conf/analytics/spark/' folder.
   Example: <class-name>org.wso2.ppaas.das.extension.TimeUDF</class-name>