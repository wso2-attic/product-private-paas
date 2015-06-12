WSO2-IS 5.0.0 Template for the Configurator
-------------------------------------------------------------------------------------

This template supports following configurations

1. Clustering IS
2. Fronting the IS cluster with WSO2 ELB

Following are the configuration parameters that is used by the template.
You can configure following in the module.ini file.

READ_FROM_ENVIRONMENT = false
* Read from environment variables 
-------------------------------------------------------------------------------------

REPOSITORY_CONF_DIRECTORY = <IS_HOME>/repository/conf
* Set the path of product directory
-------------------------------------------------------------------------------------

CONFIG_PARAM_CLUSTERING = true
* Enable clustering
* Used in - <IS_HOME>/repository/conf/axis2/axis2.xml
-------------------------------------------------------------------------------------

CONFIG_PARAM_MEMBERSHIP_SCHEME = wka
* Set Membership Schema
* Used in - <IS_HOME>/repository/conf/axis2/axis2.xml
-------------------------------------------------------------------------------------
            
CONFIG_PARAM_DOMAIN = wso2.is.domain
* Set Domain
* Used in - <IS_HOME>/repository/conf/axis2/axis2.xml
-------------------------------------------------------------------------------------
    
CONFIG_PARAM_WKA_MEMBERS = "127.0.0.1:4000,127.0.1.1:4001"
* Well known members declaration
* Format - "ip_address1:port1,ip_address2:port2"
* Used in - <IS_HOME>/repository/conf/axis2/axis2.xml
-------------------------------------------------------------------------------------

CONFIG_PARAM_LOCAL_MEMBER_HOST = 127.0.0.1
CONFIG_PARAM_LOCAL_MEMBER_PORT = 4000
* Set Local Member Hostname and port
* Used in - <IS_HOME>/repository/conf/axis2/axis2.xml
-------------------------------------------------------------------------------------

CONFIG_PARAM_PORT_OFFSET = 0
* Set Port offset
* Used in - <IS_HOME>/repository/conf/carbon.xml
-------------------------------------------------------------------------------------

CONFIG_PARAM_EMBEDDED_LDAP = true
* Enable Embedded LDAP
* Used in - <IS_HOME>/repository/conf/embedded-ldap.xml
-------------------------------------------------------------------------------------

CONFIG_PARAM_LDAP_CONNECTION_URL = ldap://localhost:10389
* Set LDAP Connection URL
* Used in - <IS_HOME>/repository/conf/user-mgt.xml
-------------------------------------------------------------------------------------

CONFIG_PARAM_HTTP_PROXY_PORT = 80
CONFIG_PARAM_HTTPS_PROXY_PORT = 443
* Set proxy ports when using a load balancer
* Used in - <IS_HOME>/repository/conf/tomcat/catalina-server.xml
-------------------------------------------------------------------------------------

## Following are the config parameters used for setting up external database 

CONFIG_PARAM_URL= jdbc:mysql://localhost:3306/
* Set URL

CONFIG_PARAM_USER_NAME=root
* Set Username

CONFIG_PARAM_PASSWORD=root
* Set Password

CONFIG_PARAM_DRIVER_CLASS_NAME=com.mysql.jdbc.Driver
* Set Driver class name

CONFIG_PARAM_MAX_ACTIVE=50
* Set Max Active

CONFIG_PARAM_MAX_WAIT=60000
* Set Max Wait

CONFIG_PARAM_TEST_ON_BORROW=true
* Set test on borrow

CONFIG_PARAM_VALIDATION_QUERY=SELECT 1
* Set validation query

CONFIG_PARAM_VALIDATION_INTERVAL=30000
* Set validation interval

CONFIG_PARAM_REGISTRY_LOCAL1="jdbc/WSO2CarbonDB:REGISTRY_LOCAL1"
* Set Local Registry database

CONFIG_PARAM_REGISTRY_DB="jdbc/WSO2RegistryDB:REGISTRY_DB"
* Set registry database

CONFIG_PARAM_USER_DB="jdbc/WSO2UMDB:WSO2_USER_DB"
* Set datasource and shared user database

* Used in - <IS_HOME>/repository/conf/user-mgt.xml
* Used in - <IS_HOME>/repository/conf/identity.xml
* Used in - <IS_HOME>/repository/conf/security/application-authentication.xml
* USed in - <IS_HOME>/repository/conf/datasources/master-datasources.xml
* Used in - <IS_HOME>/repository/conf/registry.xml
-------------------------------------------------------------------------------------

