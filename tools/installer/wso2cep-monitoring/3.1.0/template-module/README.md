WSO2-CEP 3.1.0 Monitoring Template for the Configurator
-------------------------------------------------------------------------------------

This template supports following configurations

1. Monitoring with WSO2 Data Analytics Server

Following are the configuration parameters that is used by the template.
You can configure following in the ***module.ini*** file.

#### Read from environment variables :


    READ_FROM_ENVIRONMENT = false
 

-------------------------------------------------------------------------------------

#### Set the path of product directory :

    CARBON_HOME = <CEP_HOME>

---

#### Username for Monitoring Server : 

    CONFIG_PARAM_MONITORING_USERNAME = admin

* Used in - < CEP_HOME >/repository/deployment/server/outputeventadaptors/DefaultWSO2EventOutputAdaptor.xml

---

#### Password for Monitoring Server :

    CONFIG_PARAM_MONITORING_PASSWORD = admin

* Used in - < CEP_HOME >/repository/deployment/server/outputeventadaptors/DefaultWSO2EventOutputAdaptor.xml

---

#### Set Hostname and port for Monitoring Server:

    CONFIG_PARAM_MONITORING_HOST = 127.0.0.1
    CONFIG_PARAM_MONITORING_PORT = 7661
    CONFIG_PARAM_MONITORING_SECURE_PORT = 7761

* Used in - < CEP_HOME >/repository/deployment/server/outputeventadaptors/DefaultWSO2EventOutputAdaptor.xml

