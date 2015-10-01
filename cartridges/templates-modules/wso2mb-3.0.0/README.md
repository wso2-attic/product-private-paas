WSO2-MB 3.0.0 Template for the Configurator
-------------------------------------------------------------------------------------

###Creating MB Template Module for Private PaaS

(1) Copy [mysql-connector-java-5.1.xx-bin.jar](http://dev.mysql.com/downloads/connector/j/) file to `<template_module_home>/files/repository/components/lib` folder. ( Folder structure needs to be created. )

(2) Copy `<private_paas_home>/extensions/carbon/ppaas-membership-scheme/target/ppaas-membership-scheme-4.1.0-SNAPSHOT.jar` files to `<template_module_home>/files/repository/components/dropins` folder.

(3) Copy following jar files to `<template_module_home>/files/repository/components/dropins` folder.

 * activemq_client_5.10.0_1.0.0.jar
 * geronimo_j2ee_management_1.1_spec_1.0.1_1.0.0.jar
 * hawtbuf_1.9_1.0.0.jar 
 * org.apache.commons.lang3_3.1.0.jar
 * org.apache.stratos.common-4.1.0.jar
 * org.apache.stratos.messaging-4.1.0.jar

(4) Final files folder should look like following.
```
files
└── repository
     └── components
        ├── dropins
        │   ├── activemq_client_5.10.0_1.0.0.jar
        │   ├── geronimo_j2ee_management_1.1_spec_1.0.1_1.0.0.jar
        │   ├── hawtbuf_1.9_1.0.0.jar
        │   ├── org.apache.commons.lang3_3.1.0.jar
        │   ├── org.apache.stratos.common-4.1.0.jar
        │   ├── org.apache.stratos.messaging-4.1.0.jar
        │   └── private-paas-membership-scheme-4.1.0-SNAPSHOT.jar
        └── lib
            └── mysql-connector-java-5.1.29-bin.jar

```
(5) Build the template module with above files.
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

    CARBON_HOME = <AS_HOME>

---

#### Enable clustering : 

    CONFIG_PARAM_CLUSTERING = true

* Used in - < MB_HOME >/repository/conf/axis2/axis2.xml

---

#### Set Membership Schema :

    CONFIG_PARAM_MEMBERSHIP_SCHEME = wka

* Used in - < MB_HOME >/repository/conf/axis2/axis2.xml

---
        
#### Set Domain :

    CONFIG_PARAM_DOMAIN = wso2.mb.domain

* Used in - < AS_HOME >/repository/conf/axis2/axis2.xml

---

#### Well known members declaration :

    CONFIG_PARAM_WKA_MEMBERS = "127.0.0.1:4000,127.0.1.1:4001"

* Format - "ip_address1:port1,ip_address2:port2"
* Used in - < MB_HOME >/repository/conf/axis2/axis2.xml

---

#### Set Local Member Hostname and port :

    CONFIG_PARAM_LOCAL_MEMBER_HOST = 127.0.0.1
    CONFIG_PARAM_LOCAL_MEMBER_PORT = 4000

* Used in - < MB_HOME >/repository/conf/axis2/axis2.xml

---

### Set Port offset :

    CONFIG_PARAM_PORT_OFFSET = 0

* Used in - < MB_HOME >/repository/conf/carbon.xml

---
#### Set proxy ports when using a load balancer :

    CONFIG_PARAM_HTTP_PROXY_PORT = 80
    CONFIG_PARAM_HTTPS_PROXY_PORT = 443

* Used in - < MB_HOME >/repository/conf/tomcat/catalina-server.xml


---
#### Set management console hostnames

    CONFIG_PARAM_MGT_HOST_NAME = mgt.am.cloud-test.wso2.com

* Used in - < MB_HOME >/repository/conf/carbon.xml

---

## Following are the config parameters used for setting up external database 
#### Set URL for MB store DB

    CONFIG_PARAM_MB_STORE_DB_URL= jdbc:mysql://localhost:3306/mb_store_db

#### Set Username for MB store DB

    CONFIG_PARAM_MB_STORE_DB_USER_NAME=root

#### Set Password for MB store DB
```
    CONFIG_PARAM_MB_STORE_DB_PASSWORD=root
```
#### Set Driver class name for MB store DB

    CONFIG_PARAM_MB_STORE_DB_DRIVER_CLASS_NAME=com.mysql.jdbc.Driver

#### Set URL for MB metrics DB

    CONFIG_PARAM_MB_METRICS_DB_URL= jdbc:mysql://localhost:3306/mb_metrics_db

#### Set Username for MB metrics DB

    CONFIG_PARAM_MB_METRICS_DB_USERNAME=root

#### Set Password for MB metrics DB
```
    CONFIG_PARAM_MB_METRICS_DB_PASSWORD=root
```
#### Set Driver class name for MB metrics DB

    CONFIG_PARAM_MB_METRICS_DB_DRIVER_CLASS_NAME=com.mysql.jdbc.Driver

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

* < MB_HOME >/repository/conf/user-mgt.xml
* < MB_HOME >/repository/conf/datasources/master-datasources.xml
* < MB_HOME >/repository/conf/datasources/metrics-datasources.xml
* < MB_HOME >/repository/conf/registry.xml

