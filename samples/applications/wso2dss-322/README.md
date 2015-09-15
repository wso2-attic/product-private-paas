WSO2DSS-3.2.2 Application
=========================
A simple application with a wso2dss-3.2.2 cartridge.

Application view
----------------
wso2dss-322-application     <br />
-- wso2dss-322-group        <br />
-- -- wso2dss-322-manager   <br />
-- -- wso2dss-322-worker    <br />

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
        └── wso2dss-322
            ├── wso2dss-322-manager.json
            └── wso2dss-322-worker.json
```
```
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
  "value":"jdbc:mysql://`<MYSQL_HOST>:<MYSQL_PORT>/<DSS_CONFIG_DB>`?autoReconnect=true"
},
{
  "name":"payload_parameter.CONFIG_PARAM_CONFIG_DB_USER_NAME",
  "value":"`<DSS_CONFIG_DB_USERNAME>`"
},
{
  "name":"payload_parameter.CONFIG_PARAM_CONFIG_DB_PASSWORD",
  "value":"`<DSS_CONFIG_DB_PASSWORD>`"
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
