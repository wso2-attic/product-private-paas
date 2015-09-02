# WSO2-AM 1.9.0 Template for the Configurator
---


###Creating AM Template Module for Private PaaS

(1) Copy [mysql-connector-java-5.1.xx-bin.jar](http://dev.mysql.com/downloads/connector/j/) file to `<template_module_home>/files/repository/components/lib` folder. ( Folder structure needs to be created. )

(2) Copy `<private_paas_home>/extensions/carbon/ppaas-membership-scheme/target/ppaas-membership-scheme-4.1.0-SNAPSHOT.jar` files to `<template_module_home>/files/repository/components/dropins` folder.

(3) Copy following jar files to `<template_module_home>/files/repository/components/dropins` folder.

 * activemq_client_5.10.0_1.0.0.jar
 * geronimo_j2ee_management_1.1_spec_1.0.1_1.0.0.jar
 * hawtbuf_1.9_1.0.0.jar 
 * org.apache.commons.lang3_3.1.0.jar
 * org.apache.stratos.common-4.1.0.jar
 * org.apache.stratos.messaging-4.1.0.jar
 * ppaas-membership-scheme-4.1.0-SNAPSHOT.jar

(4) Copy following [kernel patch](http://dist.wso2.org/maven2/org/wso2/carbon/WSO2-CARBON-PATCH-4.2.0/) to  relevant folder structure accordingly.

**Kernel patches**
* patch0012


(5) Final files folder should look like following.
```
files
 └── repository
       └── components
           ├── dropins
           │   ├── activemq_client_5.10.0_1.0.0.jar
           │   ├── geronimo_j2ee_management_1.1_spec_1.0.1_1.0.0.jar
           │   ├── hawtbuf_1.9_1.0.0.jar
           │   ├── org.apache.commons.lang3_3.1.0.jar
           │   ├── org.apache.stratos.common-4.1.0.jar
           │   ├── org.apache.stratos.messaging-4.1.0.jar
           │   └── private-paas-membership-scheme-4.1.0-SNAPSHOT.jar
           ├── lib
           │   ├── mysql-connector-java-5.1.35-bin.jar
           │   └── org.wso2.carbon.server-4.2.0.jar
           └── patches
               └── patch0012
                   ├── axis2_1.6.1.wso2v10.jar
                   ├── axis2-json_1.6.1.wso2v10.jar
                   ├── axis2-transport-jms_1.1.0.wso2v9.jar
                   ├── axis2-transport-tcp_1.1.0.wso2v9.jar
                   ├── hazelcast_3.0.1.wso2v1.jar
                   ├── javax.cache.wso2_4.2.0.jar
                   ├── org.wso2.carbon.core_4.2.0.jar
                   ├── org.wso2.carbon.registry.core_4.2.0.jar
                   ├── org.wso2.carbon.tomcat_4.2.0.jar
                   ├── org.wso2.carbon.ui_4.2.0.jar
                   ├── org.wso2.carbon.user.core_4.2.0.jar
                   └── org.wso2.carbon.utils_4.2.0.jar
          
```
(6) Build the template module with above files.
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

    CARBON_HOME = <AM_HOME>

---

#### Enable clustering : 

    CONFIG_PARAM_CLUSTERING = true

* Used in - < AM_HOME >/repository/conf/axis2/axis2.xml

---

#### Set Membership Schema :

    CONFIG_PARAM_MEMBERSHIP_SCHEME = private-paas

* Used in - < AM_HOME >/repository/conf/axis2/axis2.xml

---
        
#### Set Domain :

    CONFIG_PARAM_DOMAIN = wso2.am.domain

* Used in - < AM_HOME >/repository/conf/axis2/axis2.xml

---

#### Well known members declaration :

    CONFIG_PARAM_WKA_MEMBERS = "127.0.0.1:4000,127.0.1.1:4001"

* Format - "ip_address1:port1,ip_address2:port2"
* Used in - < AM_HOME >/repository/conf/axis2/axis2.xml

---

#### Set Local Member Hostname and port :

    CONFIG_PARAM_LOCAL_MEMBER_HOST = 127.0.0.1
    CONFIG_PARAM_LOCAL_MEMBER_PORT = 4000

* Used in - < AM_HOME >/repository/conf/axis2/axis2.xml

---

### Set Port offset :

    CONFIG_PARAM_PORT_OFFSET = 0

* Used in - < AM_HOME >/repository/conf/carbon.xml

---
#### Set proxy ports when using a load balancer :

    CONFIG_PARAM_HTTP_PROXY_PORT = 80
    CONFIG_PARAM_HTTPS_PROXY_PORT = 443

* Used in - < AM_HOME >/repository/conf/tomcat/catalina-server.xml

---
#### Set port mapping in manager Nodes :

    CONFIG_PARAM_PT_HTTP_PROXY_PORT  = 80
    CONFIG_PARAM_PT_HTTPS_PROXY_PORT  = 443

* Used in - < AM_HOME >/repository/conf/axis2/axis2.xml

---
#### Set worker/manger sub-domain in nodes  :

    CONFIG_PARAM_SUB_DOMAIN= worker

 * Used in - < AM_HOME >/repository/conf/axis2/axis2.xml
 * Used in - < AM_HOME >/repository/conf/carbon.xml
 * Used in - < AM_HOME >/repository/conf/registry.xml

---
#### Set worker and manager hostnames

    CONFIG_PARAM_WORKER_HOST_NAME = am.cloud-test.wso2.com
    CONFIG_PARAM_MGT_HOST_NAME = mgt.am.cloud-test.wso2.com

* Used in - < AM_HOME >/repository/conf/axis2/axis2.xml
* Used in - < AM_HOME >/repository/conf/carbon.xml

---

## Following are the config parameters used for setting up external database 
#### Set URL

    CONFIG_PARAM_APIMGT_DB_URL = jdbc:mysql://localhost:3306/apimgt
    CONFIG_PARAM_UM_DB_URL = jdbc:mysql://172.17.42.1:3306/stratos_user_db
    CONFIG_PARAM_REG_DB_URL = jdbc:mysql://172.17.42.1:3306/stratos_reg_db

#### Set Username

    CONFIG_PARAM_DB_USER_NAME=root

#### Set Password

    CONFIG_PARAM_DB_PAMSWORD=root

##### Used in 

* < AM_HOME >/repository/conf/user-mgt.xml
* < AM_HOME >/repository/conf/datasources/master-datasources.xml
* < AM_HOME >/repository/conf/registry.xml

