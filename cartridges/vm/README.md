# WSO2 Private PaaS Virtual Machine Cartridges

This folder contains WSO2 Private PaaS virtual machine cartridges.

### How to use Puppet Modules

---
(1) Copy and replace contents inside puppet folder to puppet master (i.e `/etc/puppet/` folder).

(2) Configure mandatory modules and respective server modules as mentioned below.

### **Mandatory Modules**
Following modules are mandatory to setup puppet-master.

##### **Python Cartridge Agent Module**
(1) Copy [apache-stratos-python-cartridge-agent-4.1.3.zip ](http://www.apache.org/dyn/closer.cgi/stratos)  to 
`/etc/puppet/modules/python_agent/files/packs` folder.

(2) Change file permission to 0755 for apache-stratos-python-cartridge-agent-4.1.3.zip.
```
chmod 755 apache-stratos-python-cartridge-agent-4.1.3.zip
```
(3) Update the following python agent related variables in `/etc/puppet/manifests/nodes/base.pp` file with respective values.
```
  $pca_name             = 'apache-stratos-python-cartridge-agent'
  $pca_version          = '4.1.3'
  $mb_ip                = 'MB-IP'
  $mb_port              = 'MB-PORT'
  $mb_type    			= 'activemq' #in wso2mb case, value should be 'wso2mb'
  $cep_urls 			= "CEP-IP:CEP-PORT" 
  $cep_username			= "admin"
  $cep_password			= "admin"
  $bam_ip               = '192.168.30.96'
  $bam_port             = '7611'
  $bam_secure_port      = '7711'
  $bam_username	      	= 'admin'
  $bam_password      	= 'admin'
  $metadata_service_url = 'METADATA-SERVICE-URL'
  $agent_log_level 		= 'INFO'
  $enable_log_publisher = 'false'
```
##### **Java Module**
(1) Copy [jdk-7u80-linux-x64.tar.gz](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html) to `/etc/puppet/modules/java/files` folder.

(2) Change file permission to 0755 for jdk-7u80-linux-x64.tar.gz
```
chmod 755 jdk-7u80-linux-x64.tar.gz
```
(3) Update the following JAVA related variables in `/etc/puppet/manifests/nodes/base.pp` file with respective values.
```
$java_distribution 	    = 'jdk-7u80-linux-x64.tar.gz'
$java_folder    		= 'jdk1.7.0_72'
```
##### **Configurator Module**
(1) Copy ppaas-configurator-4.1.0-SNAPSHOT.zip from `<PRIVATE_PaaS_HOME>/products/configurator/target/ppaas-configurator-4.1.0-SNAPSHOT.zip`  to `/etc/puppet/modules/configurator/files` folder.

(2) Change file permission to 0755 for ppaas-configurator-4.1.0-SNAPSHOT.zip .
```
chmod 755 ppaas-configurator-4.1.0-SNAPSHOT.zip 
```
(3) Update the following configurator related variables in `/etc/puppet/manifests/nodes/base.pp` file with respective values.
```
$configurator_name    = 'ppaas-configurator'
$configurator_version = '4.1.0-SNAPSHOT'
```
### **Optional Modules**
Following modules are mandatory to setup puppet-master. Only servers that are used by application needs to be configured. For example, If only esb cartridge is used in application only esb related module needs to be setup.

(1) Copy server pack from   to `/etc/puppet/modules/wso2installer/files/<server_name>/packs` folder. ( If configuring ESB, path is :`/etc/puppet/modules/wso2installer/files/wso2esb481/packs` )

(2) Copy respective [template module pack](https://github.com/wso2/product-private-paas/tree/master/cartridges/templates-modules) to `/etc/puppet/modules/wso2installer/files/<server_name>/packs/`

(3) Change file permission to 0755 for contents in side `/etc/puppet/modules/wso2installer/files/<server_name>/packs/` directory .
```
chmod 755 <server_pack>.zip 
chmod 755 <template_module>.zip 
```

(4) Copy respective [PCA plugins](https://github.com/wso2/product-private-paas/tree/master/cartridges/plugins) to `/etc/puppet/modules/wso2installer/files/<server_name>/plugins/`

(5) Change file permission to 0755 for contents in side `/etc/puppet/modules/wso2installer/files/<server_name>/plugins/` directory .
```
chmod 755 wso2esb-481-startup-handler.py 
chmod 755 wso2esb-481-startup-handler.yapsy-plugin 
```

(6) Update the following server related variables in `/etc/puppet/manifests/nodes/nodes.pp` file with respective values.
( If configuring ESB )
```
# ESB cartridge node
node /[0-9]{1,12}.*wso2esb-481/ inherits base {

  class { 'java': }
  class { 'python_agent':
    docroot => "/var/www"
  }
  class { 'configurator': }
  class { 'wso2installer':
    server_name       => 'wso2esb-4.8.1',
    module_name       => 'wso2esb481'
  }
}
```
---
#### ** Configured puppet master should look like below :**

```
/etc/puppet
|-- auth.conf
|-- autosign.conf
|-- environments
|   `-- example_env
|       |-- manifests
|       |-- modules
|       `-- README.environment
|-- fileserver.conf
|-- manifests
|   |-- nodes
|   |   |-- base.pp
|   |   `-- nodes.pp
|   `-- site.pp
|-- modules
|   |-- configurator
|   |   |-- files
|   |   |   `-- ppaas-configurator-4.1.0-SNAPSHOT.zip
|   |   `-- manifests
|   |       `-- init.pp
|   |-- java
|   |   |-- files
|   |   |   |-- jdk-7u80-linux-x64.tar.gz
|   |   |   `-- README
|   |   |-- manifests
|   |   |   `-- init.pp
|   |   `-- templates
|   |       `-- java_home.sh.erb
|   |-- ppaas_base
|   |   `-- manifests
|   |       `-- init.pp
|   |-- python_agent
|   |   |-- files
|   |   |   |-- apache-stratos-python-cartridge-agent-4.1.3-SNAPSHOT.zip
|   |   |   |-- README.txt
|   |   |   `-- start_agent.sh
|   |   |-- manifests
|   |   |   |-- copy_plugins.pp
|   |   |   |-- initialize.pp
|   |   |   |-- init.pp
|   |   |   |-- push_templates.pp
|   |   |   |-- remove_templates.pp
|   |   |   `-- start.pp
|   |   `-- templates
|   |       |-- agent.conf.erb
|   |       `-- logging.ini.erb
|   `-- wso2installer
|       |-- files
|       |   `-- wso2esb481
|       |       |-- packs
|       |       |   |-- README
|       |       |   |-- wso2esb-4.8.1-template-module-4.1.0-SNAPSHOT.zip
|       |       |   `-- wso2esb-4.8.1.zip
|       |       `-- plugins
|       |           |-- README
|       |           |-- wso2esb-481-startup-handler.py
|       |           `-- wso2esb-481-startup-handler.yapsy-plugin
|       `-- manifests
|           `-- init.pp
|-- puppet.conf
`-- templates
```
