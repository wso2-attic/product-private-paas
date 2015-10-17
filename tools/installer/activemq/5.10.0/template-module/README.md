# Apache ActiveMQ 5.10.0 Template for the Configurator
---


###Creating ActiveMQ Template Module for Private PaaS

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
 

---

#### Set the path of product directory :

    CARBON_HOME = <ACTIVEMQ_HOME>

---------------------------------------------------------------------------------------

#### Modify Heap Size for ActiveMQ : 

    JAVA_HEAP_SIZE=2G

* Used in - < ACTIVEMQ_HOME >/bin/activemq

---

#### Set Message Broker Username :

    CONFIG_PARAM_MB_USERNAME=system

* Used in - < ACTIVEMQ_HOME >/conf/activemq.xml

---
        
#### Set Message Broker Password :

    CONFIG_PARAM_MB_PASSWORD=manager

* Used in - < ACTIVEMQ_HOME >/conf/activemq.xml
