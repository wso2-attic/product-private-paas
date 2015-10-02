WSO2MB-3.0.0 Application
=========================
A simple application with a wso2mb-3.0.0 cartridge.

Application view
----------------
wso2mb-300-application     <br />
-- wso2mb-300-group        <br />
-- -- wso2mb-300           <br />

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
        └── wso2mb-300
            └── wso2mb-300.json
```
```
{
  "name":"payload_parameter.CONFIG_PARAM_MB_METRICS_DB_URL",
  "value":"jdbc:mysql://`<MYSQL_HOST>:<MYSQL_PORT>/<MB_METRICS_DB>`?autoReconnect=true"
},
{
  "name":"payload_parameter.CONFIG_PARAM_MB_METRICS_DB_USER_NAME",
  "value":"`<MB_METRICS_DB_USERNAME>`"
},
{
  "name":"payload_parameter.CONFIG_PARAM_MB_METRICS_DB_PASSWORD",
  "value":"`<MB_METRICS_DB_PASSWORD>`"
},
{
    "name": "payload_parameter.CONFIG_PARAM_MB_METRICS_DB_DRIVER_CLASS_NAME",
    "value": "com.mysql.jdbc.Driver"
},
{
  "name":"payload_parameter.CONFIG_PARAM_MB_STORE_DB_URL",
  "value":"jdbc:mysql://`<MYSQL_HOST>:<MYSQL_PORT>/<MB_STORE_MGT_DB>`?autoReconnect=true"
},
{
  "name":"payload_parameter.CONFIG_PARAM_MB_STORE_DB_USER_NAME",
  "value":"`<MB_STORE_DB_USER_NAME>`"
},
{
  "name":"payload_parameter.CONFIG_PARAM_MB_STORE_DB_PASSWORD",
  "value":"`<MB_STORE_DB_PASSWORD>`"
},
{
    "name": "payload_parameter.CONFIG_PARAM_MB_STORE_DB_DRIVER_CLASS_NAME",
    "value": "com.mysql.jdbc.Driver"
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