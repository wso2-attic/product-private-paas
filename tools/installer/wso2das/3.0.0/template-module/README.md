WSO2-DAS 3.0.0 Template for the Configurator
-------------------------------------------------------------------------------------

###Creating DAS Template Module for Private PaaS

Build the template module using the following command.

```
mvn clean install
```

---
### Configuration parameters
Following are the configuration parameters that is used by the template.
You can configure following in the ***module.ini*** file.

#### Read from environment variables :


    READ_FROM_ENVIRONMENT = false
 

-------------------------------------------------------------------------------------

#### Set the path of product directory :

    CARBON_HOME = <DAS_HOME>

---

#### Enable clustering :

    CONFIG_PARAM_CLUSTERING = true

* Used in - < DAS_HOME >/repository/conf/axis2/axis2.xml

---

#### Set Membership Schema :

    CONFIG_PARAM_MEMBERSHIP_SCHEME = wka

* Used in - < DAS_HOME >/repository/conf/axis2/axis2.xml

---

#### Set Domain :

    CONFIG_PARAM_DOMAIN = wso2.das.domain

* Used in - < DAS_HOME >/repository/conf/axis2/axis2.xml

---

#### Well known members declaration :

    CONFIG_PARAM_WKA_MEMBERS = "127.0.0.1:4000,127.0.1.1:4001"

* Format - "ip_address1:port1,ip_address2:port2"
* Used in - < DAS_HOME >/repository/conf/axis2/axis2.xml

---

#### Set Local Member Hostname and port :

    CONFIG_PARAM_LOCAL_MEMBER_HOST = 127.0.0.1
    CONFIG_PARAM_LOCAL_MEMBER_PORT = 4000

* Used in - < DAS_HOME >/repository/conf/axis2/axis2.xml

---

### Set Port offset :

    CONFIG_PARAM_PORT_OFFSET = 0

* Used in - < DAS_HOME >/repository/conf/carbon.xml

---
#### Set proxy ports when using a load balancer :

    CONFIG_PARAM_HTTP_PROXY_PORT = 80
    CONFIG_PARAM_HTTPS_PROXY_PORT = 443

* Used in - < DAS_HOME >/repository/conf/tomcat/catalina-server.xml

---
#### Set spark master count  :

    CONFIG_PARAM_CARBON_SPARK_MASTER_COUNT= 2

 * Used in - < DAS_HOME >/repository/conf/analytics/spark/spark-defauls.conf

 #### Set spark symbolic link location :

     CONFIG_PARAM_SYMBOLIC_LINK= /mnt/wso2das

  * Used in - < DAS_HOME >/repository/conf/analytics/spark/spark-defauls.conf

---
#### Set hostname
    CONFIG_PARAM_HOST_NAME = das.cloud-test.wso2.com
    CONFIG_PARAM_MGT_HOST_NAME = mgt.das.cloud-test.wso2.com

* Used in - < DAS_HOME >/repository/conf/axis2/axis2.xml
* Used in - < DAS_HOME >/repository/conf/carbon.xml

---

## Following are the config parameters used for setting up external database

#### Set URL for Analytics FS DB

    CONFIG_PARAM_WSO2_ANALYTICS_WSO2_ANALYTICS_FS_DB_URL= jdbc:mysql://localhost:3306/analytics_fs_db

#### Set Username for Analytics FS DB

    CONFIG_PARAM_WSO2_ANALYTICS_WSO2_ANALYTICS_FS_DB_USER_NAME=root

#### Set Password for Analytics FS DB
```
    CONFIG_PARAM_WSO2_ANALYTICS_WSO2_ANALYTICS_FS_DB_PASSWORD=root
```
#### Set Driver class name for Analytics FS DB

    CONFIG_PARAM_WSO2_ANALYTICS_WSO2_ANALYTICS_FS_DB_DRIVER_CLASS_NAME=com.mysql.jdbc.Driver

#### Set URL for Event Store DB

    CONFIG_PARAM_WSO2_ANALYTICS_EVENT_STORE_DB_URL= jdbc:mysql://localhost:3306/event_store_db

#### Set Username for Event Store DB

    CONFIG_PARAM_WSO2_ANALYTICS_EVENT_STORE_DB_USER_NAME=root

#### Set Password for Event Store DB
```
    CONFIG_PARAM_WSO2_ANALYTICS_EVENT_STORE_DB_PASSWORD=root
```
#### Set Driver class name for Event Store DB

    CONFIG_PARAM_WSO2_ANALYTICS_EVENT_STORE_DB_DRIVER_CLASS_NAME=com.mysql.jdbc.Driver

#### Set URL for Processed Data Store DB

    CONFIG_PARAM_WSO2_ANALYTICS_PROCESSED_DATA_STORE_DB_URL= jdbc:mysql://localhost:3306/processed_data_db

#### Set Username for Processed Data Store DB

    CONFIG_PARAM_WSO2_ANALYTICS_PROCESSED_DATA_STORE_DB_USER_NAME=root

#### Set Password for Processed Data Store DB
```
    CONFIG_PARAM_WSO2_ANALYTICS_PROCESSED_DATA_STORE_DB_PASSWORD=root
```
#### Set Driver class name for Processed Data Store DB

    CONFIG_PARAM_WSO2_ANALYTICS_PROCESSED_DATA_STORE_DB_DRIVER_CLASS_NAME=com.mysql.jdbc.Driver

#### Set URL for registry DB

    CONFIG_PARAM_REGISTRY_DB_URL= jdbc:mysql://localhost:3306/registry_db

#### Set Username for registry DB

    CONFIG_PARAM_REGISTRY_DB_USER_NAME=root

#### Set Password for registry DB
```
    CONFIG_PARAM_REGISTRY_DB_PASSWORD=root
```
#### Set Driver class name for registry DB

    CONFIG_PARAM_REGISTRY_DB_DRIVER_CLASS_NAME=com.mysql.jdbc.Driver

#### Set URL for user DB

    CONFIG_PARAM_USER_MGT_DB_URL= jdbc:mysql://localhost:3306/user_db

#### Set Username for user DB

    CONFIG_PARAM_USER_MGT_DB_USER_NAME=root

#### Set Password for user DB
```
    CONFIG_PARAM_USER_MGT_DB_PASSWORD=root
```
#### Set Driver class name for user DB

    CONFIG_PARAM_USER_MGT_DB_DRIVER_CLASS_NAME=com.mysql.jdbc.Driver
##### Used in

* < DAS_HOME >/repository/conf/user-mgt.xml
* < DAS_HOME >/repository/conf/datasources/master-datasources.xml
* < DAS_HOME >/repository/conf/registry.xml