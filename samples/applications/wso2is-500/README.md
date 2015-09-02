WSO2IS-5.0.0 Application
=========================
A simple application with a wso2is-5.0.0 cartridge.

Application view
----------------
wso2is-500-application     <br />
-- wso2is-500-group        <br />
-- -- wso2is-500-manager   <br />


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
        └── wso2is-500
            └── wso2is-500-manager.json
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
  "value":"jdbc:mysql://`<MYSQL_HOST>:<MYSQL_PORT>/<IS_CONFIG_DB>`?autoReconnect=true"
},
{
  "name":"payload_parameter.CONFIG_PARAM_CONFIG_DB_USER_NAME",
  "value":"`<IS_CONFIG_DB_USERNAME>`"
},
{
  "name":"payload_parameter.CONFIG_PARAM_CONFIG_DB_PASSWORD",
  "value":"`<IS_CONFIG_DB_PASSWORD>`"
}
```
>Make sure you have configured 'IS_CONFIG_DB' using mysql.sql script inside `<IS_HOME>`/dbscripts before deploying the
IS Multitenant application.

How to run
----------
cd scripts/[iaas]/          <br />
./deploy.sh                 <br />

How to undeploy
---------------
cd scripts/[iaas]/          <br />
./undeploy.sh               <br />