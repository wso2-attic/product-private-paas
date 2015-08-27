##WSO2 Private PaaS Virtual Machine Cartridges

This folder contains WSO2 Private PaaS virtual machine cartridges.

###How to use Puppet Modules

---
(1) Copy and replace contents inside puppet folder to puppet master (i.e `/etc/puppet/` folder).
(2) Configure mandatory modules and respective server modules as mentioned below.

### **Mandatory Modules**
Following modules are mandatory to setup puppet-master.

##### **Python Cartridge Agent Module**
(1) Copy [apache-stratos-cartridge-agent-4.1.2.zip ](http://www.apache.org/dyn/closer.cgi/stratos)  to `/etc/puppet/modules/python_agent/files/packs` folder.

(2) Change file permission to 0755 for apache-stratos-cartridge-agent-4.1.2.zip.
```
chmod 755 apache-stratos-cartridge-agent-4.1.2.zip
```
(3) Update the following python agent related variables in `/etc/puppet/manifests/nodes/base.pp` file with respective values.
```
$pca_name             = 'apache-stratos-python-cartridge-agent'
  $pca_version          = '4.1.2'
  $mb_ip                = 'MB-IP'
  $mb_port              = 'MB-PORT'
  $mb_type    			= 'activemq' #in wso2mb case, value should be 'wso2mb'
  $cep_ip 				= "CEP-IP"
  $cep_port				= "7711"
  $cep_username			= "admin"
  $cep_password			= "admin"
  $bam_ip               = '192.168.30.96'
  $bam_port             = '7611'
  $bam_secure_port      = '7711'
  $bam_username	      	= 'admin'
  $bam_password      	= 'admin'
  $java_distribution  	= 'jdk-7u72-linux-x64.gz'
  $lb_private_ip  		= ''
  $lb_public_ip  		= ''
  $metadata_service_url = 'METADATA-SERVICE-URL'
  $agent_log_level 		= 'INFO'
  $enable_log_publisher = 'false'
```
##### **Java Module**
(1) Copy [jdk-7u72-linux-x64.gz](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html) to `/etc/puppet/modules/java/files` folder.

(2) Change file permission to 0755 for jdk-7u72-linux-x64.gz.
```
chmod 755 jdk-7u72-linux-x64.gz
```
(3) Update the following python agent related variables in `/etc/puppet/manifests/nodes/base.pp` file with respective values.
```
$java_distribution 	= 'jdk-7u72-linux-x64.gz'
$java_name    		= 'jdk1.7.0_72'
```
##### **Configurator Module**
(1) Copy ppaas-configurator-4.1.0-SNAPSHOT.zip from `<PRIVATE_PaaS_HOME>/products/configurator/target/ppaas-configurator-4.1.0-SNAPSHOT.zip`  to `/etc/puppet/modules/configurator/files` folder.

(2) Change file permission to 0755 for ppaas-configurator-4.1.0-SNAPSHOT.zip .
```
chmod 755 ppaas-configurator-4.1.0-SNAPSHOT.zip 
```
(3) Update the following python agent related variables in `/etc/puppet/manifests/nodes/base.pp` file with respective values.
```
$configurator_name    = 'ppaas-configurator'
$configurator_version = '4.1.0-SNAPSHOT'
```
### **Optional Modules**
Following modules are mandatory to setup puppet-master. Only servers that are used by application needs to be configured. For example, If only esb cartridge is used in application only esb related module needs to be setup.

(1) Copy server pack from   to `/etc/puppet/modules/<server_name>/files/packs` folder. ( If configuring ESB, path is :`/etc/puppet/modules/esb/files/packs` )

(2) Copy respective [template module pack](https://github.com/wso2/product-private-paas/tree/master/cartridges/templates-modules) to `/etc/puppet/modules/<server_name>/files/packs/`

(3) Change file permission to 0755 for contents in side `/etc/puppet/modules/<server_name>/files/packs/` directory .
```
chmod 755 <server_pack>.zip 
chmod 755 <template_module>.zip 
```

(4) Copy respective [PCA plugins](https://github.com/wso2/product-private-paas/tree/master/cartridges/plugins) to `/etc/puppet/modules/<server_name>/files/plugins/`

(5) Change file permission to 0755 for contents in side `/etc/puppet/modules/<server_name>/files/plugins/` directory .
```
chmod 755 wso2esb-481-startup-handler.py 
chmod 755 wso2esb-481-startup-handler.yapsy-plugin 
```

(6) Update the following python agent related variables in `/etc/puppet/manifests/nodes/<server>.pp` file with respective values.
( If configuring ESB, path is :`/etc/puppet/manifests/nodes/esb.pp` )
```
# ESB cartridge node
node /esb/ inherits base {

  class {'java':}
  class {'python_agent':
   docroot => "APPLICATION-PATH" #change this value
  }
  class {'configurator':}
  class {'esb':
     server_name     => 'wso2esb',#change this value
     version  	      => '4.8.1'  #change this value

  }

  Class['stratos_base'] -> Class['java'] -> Class['configurator']-> Class['python_agent'] -> Class['esb']
}
```
---
#### **Fully configured puppet master should look like below :**
```
/etc/puppet
|-- auth.conf
|-- autosign.conf
|-- manifests
|   |-- nodes
|   |   |-- apimanager.pp
|   |   |-- appserver.pp
|   |   |-- base.pp
|   |   |-- default.pp
|   |   |-- esb.pp
|   |   `-- is.pp
|   `-- site.pp
|-- modules
|   |-- apimanager
|   |   |-- files
|   |   |   |-- packs
|   |   |   |   `-- wso2am-1.9.0.zip
|   |   |   `-- plugins
|   |   |-- LICENSE
|   |   |-- manifests
|   |   |   `-- init.pp
|   |   `-- README
|   |-- appserver
|   |   |-- files
|   |   |   |-- packs
|   |   |   |   |-- wso2as-5.2.1-template-module-4.1.0-SNAPSHOT.zip
|   |   |   |   `-- wso2as-5.2.1.zip
|   |   |   `-- plugins
|   |   |       |-- wso2as-521-startup-handler.py
|   |   |       `-- wso2as-521-startup-handler.yapsy-plugin
|   |   |-- LICENSE
|   |   |-- manifests
|   |   |   |-- init.pp
|   |   | 
|   |   `-- README
|   |-- configurator
|   |   |-- files
|   |   |   `-- ppaas-configurator-4.1.0-SNAPSHOT.zip
|   |   `-- manifests
|   |       |-- init.pp
|   |       
|   |-- esb
|   |   |-- files
|   |   |   |-- packs
|   |   |   |   |-- wso2esb-4.8.1-template-module-4.1.0-SNAPSHOT.zip
|   |   |   |   `-- wso2esb-4.8.1.zip
|   |   |   `-- plugins
|   |   |       |-- wso2esb-481-startup-handler.py
|   |   |       `-- wso2esb-481-startup-handler.yapsy-plugin
|   |   |-- LICENSE
|   |   |-- manifests
|   |   |   `-- init.pp
|   |   `-- README
|   |-- is
|   |   |-- files
|   |   |   |-- packs
|   |   |   |   |-- wso2is-5.0.0-template-module-4.1.0-SNAPSHOT.zip
|   |   |   |   `-- wso2is-5.0.0.zip
|   |   |   `-- plugins
|   |   |       |-- wso2is-500-startup-handler.py
|   |   |       `-- wso2is-500-startup-handler.yapsy-plugin
|   |   |-- LICENSE
|   |   |-- manifests
|   |   |   `-- init.pp
|   |   `-- README
|   |-- java
|   |   |-- files
|   |   |   |-- jdk-7u72-linux-x64.gz
|   |   |   `-- README
|   |   |-- manifests
|   |   |   |-- init.pp
|   |-- python_agent
|   |   |-- files
|   |   |   |-- apache-stratos-python-cartridge-agent-4.1.2-SNAPSHOT.zip
|   |   |   |-- README.txt
|   |   |   `-- start_agent.sh
|   |   |-- manifests
|   |   |   |-- copy_plugins.pp
|   |   |   |-- initialize.pp
|   |   |   |-- init.pp
|   |   |   `-- push_templates.pp
|   |   `-- templates
|   |       |-- agent.conf.erb
|   |       |-- agent.conf.erb~
|   |       `-- logging.ini.erb
|   `-- stratos_base
|       |-- manifests
|       |   `-- init.pp
|-- puppet.conf
`-- templates
```