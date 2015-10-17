# WSO2-PPaaS 4.1.0 Template for the Configurator
---


###Creating PPaaS Template Module for Private PaaS

(1) Build the template module using the following command.

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

    CARBON_HOME = <PPAAS_HOME>

---

#### Enable clustering : 

    CONFIG_PARAM_CLUSTERING = true

* Used in - < PPAAS_HOME >/repository/conf/axis2/axis2.xml

---

#### Set Membership Schema :

    CONFIG_PARAM_MEMBERSHIP_SCHEME = private-paas

* Used in - < PPAAS_HOME >/repository/conf/axis2/axis2.xml

---
        
#### Set Domain :

    CONFIG_PARAM_DOMAIN = wso2.am.domain

* Used in - < PPAAS_HOME >/repository/conf/axis2/axis2.xml

---


#### Set Local Member Hostname and port :

    CONFIG_PARAM_LOCAL_MEMBER_HOST = 127.0.0.1
    CONFIG_PARAM_LOCAL_MEMBER_PORT = 4000

* Used in - < PPAAS_HOME >/repository/conf/axis2/axis2.xml

---

### Set Port offset :

    CONFIG_PARAM_PORT_OFFSET = 0

* Used in - < PPAAS_HOME >/repository/conf/carbon.xml



