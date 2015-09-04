# WSO2 Configurator

Configurator is a python module which provides features for configuring a server with set of name value pairs.
A template module needs to be created with a set of jinja template files and a configuration settings ini file for using
configurator for configuring a server.

### **How to use configurator**

Please follow below steps to proceed with the installation:

(1) Install python pip.
   ```
   sudo apt-get install python-pip
   ```

(2) Install jinja2 module.
   ```
   pip install Jinja2
   ```
(3) Build required [template module](https://github.com/wso2/product-private-paas/tree/master/cartridges/templates-modules).

(4) Unzip and copy template module to `<configurator_home>/template-modules` folder. 

Final folder structure should look like below :
```
ppaas-configurator-4.1.0-SNAPSHOT
|-- conf
|   `-- logging_config.ini
|-- configparserutil.py
|-- configurator.log
|-- configurator.py
|-- constants.py
|-- __init__.py
`-- template-modules
    `-- wso2as-5.2.1-template-module-4.1.0-SNAPSHOT
        |-- files
        |   |-- bin
        |   |   `-- ciphertool.bat
        |   `-- repository
        |       |-- components
        |       |   |-- dropins
        |       |   |   |-- activemq_client_5.10.0_1.0.0.jar
        |       |   |   |-- geronimo_j2ee_management_1.1_spec_1.0.1_1.0.0.jar
        |       |   |   |-- hawtbuf_1.9_1.0.0.jar
        |       |   |   |-- org.apache.commons.lang3_3.1.0.jar
        |       |   |   |-- org.apache.stratos.common-4.1.0.jar
        |       |   |   |-- org.apache.stratos.messaging-4.1.0.jar
        |       |   |   `-- private-paas-membership-scheme-4.1.0-SNAPSHOT.jar
        |       |   |-- lib
        |       |   |   |-- mysql-connector-java-5.1.29-bin.jar
        |       |   |   |-- org.wso2.carbon.server-4.2.0.jar
        |       |   |   `-- org.wso2.ciphertool-1.0.0-wso2v2.jar
        |       |   `-- patches
        |       |       |-- patch0004
        |       |       |   |-- org.wso2.carbon.application.deployer_4.2.0.jar
        |       |       |   |-- org.wso2.carbon.core_4.2.0.jar
        |       |       |   |-- org.wso2.carbon.core.services_4.2.0.jar
        |       |       |   |-- org.wso2.carbon.ui_4.2.0.jar
        |       |       |   |-- org.wso2.carbon.user.core_4.2.0.jar
        |       |       |   `-- org.wso2.carbon.utils_4.2.0.jar
        |       |       |-- patch0005
        |       |       |   |-- axis2_1.6.1.wso2v10.jar
        |       |       |   |-- axis2-transport-jms_1.1.0.wso2v9.jar
        |       |       |   |-- org.wso2.carbon.application.deployer_4.2.0.jar
        |       |       |   |-- org.wso2.carbon.core_4.2.0.jar
        |       |       |   |-- org.wso2.carbon.user.core_4.2.0.jar
        |       |       |   `-- XmlSchema_1.4.7.wso2v2.jar
        |       |       |-- patch0006
        |       |       |   |-- axis2_1.6.1.wso2v10.jar
        |       |       |   |-- axis2-json_1.6.1.wso2v10.jar
        |       |       |   |-- axis2-transport-rabbitmq-amqp_1.1.0.wso2v9.jar
        |       |       |   |-- javax.cache.wso2_4.2.0.jar
        |       |       |   |-- org.wso2.carbon.application.deployer_4.2.0.jar
        |       |       |   |-- org.wso2.carbon.core_4.2.0.jar
        |       |       |   |-- org.wso2.carbon.user.api_4.2.0.jar
        |       |       |   |-- org.wso2.carbon.user.core_4.2.0.jar
        |       |       |   `-- wsdl4j_1.6.2.wso2v4.jar
        |       |       |-- patch0007
        |       |       |   |-- axis2_1.6.1.wso2v10.jar
        |       |       |   |-- axis2-json_1.6.1.wso2v10.jar
        |       |       |   |-- org.wso2.carbon.application.deployer_4.2.0.jar
        |       |       |   |-- org.wso2.carbon.core_4.2.0.jar
        |       |       |   |-- org.wso2.carbon.registry.server_4.2.0.jar
        |       |       |   |-- org.wso2.carbon.server.admin_4.2.0.jar
        |       |       |   |-- org.wso2.carbon.ui_4.2.0.jar
        |       |       |   |-- org.wso2.carbon.user.core_4.2.0.jar
        |       |       |   `-- org.wso2.carbon.utils_4.2.0.jar
        |       |       |-- patch0008
        |       |       |   |-- axis2_1.6.1.wso2v10.jar
        |       |       |   |-- javax.cache.wso2_4.2.0.jar
        |       |       |   |-- org.wso2.carbon.registry.core_4.2.0.jar
        |       |       |   |-- org.wso2.carbon.securevault_4.2.0.jar
        |       |       |   `-- org.wso2.carbon.user.core_4.2.0.jar
        |       |       |-- patch0009
        |       |       |   |-- axiom_1.2.11.wso2v4.jar
        |       |       |   |-- axis2_1.6.1.wso2v10.jar
        |       |       |   |-- axis2-json_1.6.1.wso2v10.jar
        |       |       |   |-- axis2-transport-jms_1.1.0.wso2v9.jar
        |       |       |   |-- axis2-transport-sms_1.1.0.wso2v9.jar
        |       |       |   |-- hazelcast_3.0.1.wso2v1.jar
        |       |       |   |-- javax.cache.wso2_4.2.0.jar
        |       |       |   |-- org.wso2.carbon.application.deployer_4.2.0.jar
        |       |       |   |-- org.wso2.carbon.base_4.2.0.jar
        |       |       |   |-- org.wso2.carbon.core_4.2.0.jar
        |       |       |   |-- org.wso2.carbon.jasper.patch_4.2.0.jar
        |       |       |   |-- org.wso2.carbon.registry.core_4.2.0.jar
        |       |       |   |-- org.wso2.carbon.registry.server_4.2.0.jar
        |       |       |   |-- org.wso2.carbon.server.admin_4.2.0.jar
        |       |       |   |-- org.wso2.carbon.ui_4.2.0.jar
        |       |       |   |-- org.wso2.carbon.user.core_4.2.0.jar
        |       |       |   |-- org.wso2.carbon.utils_4.2.0.jar
        |       |       |   |-- poi-ooxml_3.9.0.wso2v1.jar
        |       |       |   |-- spring.framework_3.1.0.wso2v1.jar
        |       |       |   `-- woden_1.0.0.M8-wso2v1.jar
        |       |       |-- patch0010
        |       |       |   |-- axiom_1.2.11.wso2v4.jar
        |       |       |   |-- axis2_1.6.1.wso2v10.jar
        |       |       |   |-- axis2-json_1.6.1.wso2v10.jar
        |       |       |   |-- axis2-transport-jms_1.1.0.wso2v9.jar
        |       |       |   |-- hazelcast_3.0.1.wso2v1.jar
        |       |       |   |-- javax.cache.wso2_4.2.0.jar
        |       |       |   |-- org.wso2.carbon.core_4.2.0.jar
        |       |       |   |-- org.wso2.carbon.core.services_4.2.0.jar
        |       |       |   |-- org.wso2.carbon.feature.mgt.services_4.2.0.jar
        |       |       |   |-- org.wso2.carbon.ndatasource.rdbms_4.2.0.jar
        |       |       |   |-- org.wso2.carbon.registry.core_4.2.0.jar
        |       |       |   |-- org.wso2.carbon.tomcat.ext_4.2.0.jar
        |       |       |   |-- org.wso2.carbon.tomcat.patch_4.2.0.jar
        |       |       |   |-- org.wso2.carbon.ui_4.2.0.jar
        |       |       |   |-- org.wso2.carbon.user.core_4.2.0.jar
        |       |       |   |-- tomcat_7.0.34.wso2v1.jar
        |       |       |   |-- tomcat-ha_7.0.34.wso2v1.jar
        |       |       |   `-- wss4j_1.5.11.wso2v6.jar
        |       |       |-- patch0011
        |       |       |   |-- axis2-json_1.6.1.wso2v10.jar
        |       |       |   |-- axis2-transport-jms_1.1.0.wso2v9.jar
        |       |       |   |-- axis2-transport-rabbitmq-amqp_1.1.0.wso2v9.jar
        |       |       |   |-- org.wso2.carbon.core_4.2.0.jar
        |       |       |   |-- org.wso2.carbon.registry.core_4.2.0.jar
        |       |       |   |-- org.wso2.carbon.ui-4.2.0.jar
        |       |       |   |-- org.wso2.carbon.user.core_4.2.0.jar
        |       |       |   `-- org.wso2.carbon.utils_4.2.0.jar
        |       |       |-- patch0012
        |       |       |   `-- org.wso2.carbon.core-4.2.0.jar
        |       |       |-- patch0955
        |       |       |   |-- org.wso2.carbon.webapp.list.ui_4.2.1.jar
        |       |       |   |-- org.wso2.carbon.webapp.mgt_4.2.2.jar
        |       |       |   `-- org.wso2.carbon.webapp.mgt.stub_4.2.0.jar
        |       |       |-- patch1095
        |       |       |   `-- wss4j_1.5.11.wso2v6.jar
        |       |       |-- patch1261
        |       |       |   |-- org.wso2.carbon.tomcat.ext_4.2.0.jar
        |       |       |   |-- org.wso2.carbon.tomcat.patch_4.2.0.jar
        |       |       |   |-- tomcat_7.0.34.wso2v1.jar
        |       |       |   `-- tomcat-ha_7.0.34.wso2v1.jar
        |       |       `-- patch1262
        |       |           `-- org.wso2.carbon.webapp.mgt_4.2.2.jar
        |       `-- resources
        |           `-- security
        |               `-- client-truststore.jks
        |-- module.ini
        `-- templates
            `-- repository
                `-- conf
                    |-- axis2
                    |   `-- axis2.xml.template
                    |-- carbon.xml.template
                    |-- datasources
                    |   `-- master-datasources.xml.template
                    |-- jndi.properties.template
                    |-- registry.xml.template
                    |-- tomcat
                    |   `-- catalina-server.xml.template
                    `-- user-mgt.xml.template

```

  
(5) Run Configurator `<confiugrator_home>/configurator.py`.
   ```
   ./configurator.py
   ```

