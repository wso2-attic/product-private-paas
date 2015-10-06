WSO2DAS-3.0.0 Application
=========================
A simple application with a wso2das-3.0.0 cartridge.

Application view
----------------
wso2das-300-application     <br />
-- wso2das-300-group        <br />
-- -- wso2das-300   <br />

Application folder structure
----------------------------
-- artifacts/[iaas]/ IaaS specific artifacts        <br />
-- scripts/common/ Common scripts for all iaass     <br />
-- scripts/[iaas] IaaS specific scripts             <br />

Before Run
----------
For IaaSs except mock, add datasource configurations in following cartridge definitions under properties section.
```
samples
└── cartridges
    └── [iaas]
        └── wso2das-300
            └── wso2das-300.json
```
```
{
  "name": "payload_parameter.CONFIG_PARAM_WSO2_ANALYTICS_WSO2_ANALYTICS_FS_DB_URL",
  "value": "jdbc:mysql://'<MYSQL_HOST>'/'<DAS_ANALYTICS_FS_DB>'"
},
{
  "name": "payload_parameter.CONFIG_PARAM_WSO2_ANALYTICS_WSO2_ANALYTICS_FS_DB_USER_NAME",
  "value": "'<DAS_CONFIG_DB_USERNAME>`"
},
{
  "name": "payload_parameter.CONFIG_PARAM_WSO2_ANALYTICS_WSO2_ANALYTICS_FS_DB_PASSWORD",
  "value": "`<DAS_CONFIG_DB_PASSWORD>`"
},
{
  "name": "payload_parameter.CONFIG_PARAM_WSO2_ANALYTICS_WSO2_ANALYTICS_FS_DB_DRIVER_CLASS_NAME",
  "value": "com.mysql.jdbc.Driver"
},
{
  "name": "payload_parameter.CONFIG_PARAM_WSO2_WSO2_ANALYTICS_EVENT_STORE_DB_URL",
  "value": "jdbc:mysql://'<MYSQL_HOST>'/'<DAS_EVENT_STORE_DB>'"
},
{
  "name": "payload_parameter.CONFIG_PARAM_WSO2_WSO2_ANALYTICS_EVENT_STORE_DB_USER_NAME",
  "value": "'<DAS_CONFIG_DB_USERNAME>`"
},
{
  "name": "payload_parameter.CONFIG_PARAM_WSO2_WSO2_ANALYTICS_EVENT_STORE_DB_PASSWORD",
  "value": "`<DAS_CONFIG_DB_PASSWORD>`"
},
{
  "name": "payload_parameter.CONFIG_PARAM_WSO2_WSO2_ANALYTICS_EVENT_STORE_DB_DRIVER_CLASS_NAME",
  "value": "com.mysql.jdbc.Driver"
},
{
  "name": "payload_parameter.CONFIG_PARAM_WSO2_ANALYTICS_PROCESSED_DATA_STORE_DB_URL",
  "value": "jdbc:mysql://'<MYSQL_HOST>'/'<DAS_PROCESSED_DATA_STORE_DB>'"
},
{
  "name": "payload_parameter.CONFIG_PARAM_WSO2_ANALYTICS_PROCESSED_DATA_STORE_DB_USER_NAME",
  "value": "'<DAS_CONFIG_DB_USERNAME>`"
},
{
  "name": "payload_parameter.CONFIG_PARAM_WSO2_ANALYTICS_PROCESSED_DATA_STORE_DB_PASSWORD",
  "value": "`<DAS_CONFIG_DB_PASSWORD>`"
},
{
  "name": "payload_parameter.CONFIG_PARAM_WSO2_ANALYTICS_PROCESSED_DATA_STORE_DB_DRIVER_CLASS_NAME",
  "value": "com.mysql.jdbc.Driver"
},
{
  "name":"payload_parameter.CONFIG_PARAM_REGISTRY_DB_URL",
  "value":"jdbc:mysql://`<MYSQL_HOST>:<MYSQL_PORT>/<PPAAS_REGISTRY_DB>`?autoReconnect=true"
},
{
  "name":"payload_parameter.CONFIG_PARAM_REGISTRY_DB_USER_NAME",
  "value":"`<PPAAS_REGISTRY_DB_USERNAME>`"
},
{
  "name":"payload_parameter.CONFIG_PARAM_REGISTRY_DB_PASSWORD",
  "value":"`<PPAAS_REGISTRY_DB_PASSWORD>`"
},
{
  "name":"payload_parameter.CONFIG_PARAM_USER_MGT_DB_URL",
  "value":"jdbc:mysql://`<MYSQL_HOST>:<MYSQL_PORT>/<PPAAS_USER_MGT_DB>`?autoReconnect=true"
},
{
  "name":"payload_parameter.CONFIG_PARAM_USER_MGT_DB_USER_NAME",
  "value":"`<PPAAS_USER_MGT_DB_USER_NAME>`"
},
{
  "name":"payload_parameter.CONFIG_PARAM_USER_MGT_DB_PASSWORD",
  "value":"`<PPAAS_USER_MGT_DB_PASSWORD>`"
},
{
  "name":"payload_parameter.CONFIG_PARAM_CONFIG_DB_URL",
  "value":"jdbc:mysql://`<MYSQL_HOST>:<MYSQL_PORT>/<AS_CONFIG_DB>`?autoReconnect=true"
},
{
  "name":"payload_parameter.CONFIG_PARAM_CONFIG_DB_USER_NAME",
  "value":"`<DAS_CONFIG_DB_USERNAME>`"
},
{
  "name":"payload_parameter.CONFIG_PARAM_CONFIG_DB_PASSWORD",
  "value":"`<DAS_CONFIG_DB_PASSWORD>`"
}
```

How to run
----------
cd scripts/[iaas]/          <br />
./deploy.sh                 <br />

How to undeploy
---------------
cd scripts/[iaas]/          <br />
./undeploy.sh               <br />