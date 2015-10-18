# Template Module Based Installation for WSO2 Private PaaS

WSO2 Private PaaS can be installed as a Single Node installation or a Distributed installation using the Template Modules provided below. 

1. ppaas
2. wso2cep
3. wso2das
4. wso2cep-monitoring
5. activemq 

Run the Python Configurator on each template module by following the Configurator [README](https://github.com/wso2/private-paas-cartridges/blob/master/common/configurator/README.md). The modules can be applied on either a single node or a distributed setup. 

## Special Notes

1. The Template Module `wso2cep-monitoring` contains only the configurations related to setting up metering and monitoring with WSO2 DAS for WSO2 Private PaaS. It should be applied on top of an existing external WSO2 CEP installation configured work as the external CEP for WSO2 Private PaaS.
