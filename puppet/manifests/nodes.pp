# ----------------------------------------------------------------------------
#  Copyright 2005-2013 WSO2, Inc. http://www.wso2.org
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
# ----------------------------------------------------------------------------
#

node 'base' {

  #essential variables
  $package_repo         = 'PACKAGE_REPO'
  $local_package_dir    = '/mnt/packs'
  $mb_ip                = 'MB_IP'
  $mb_port              = 'MB_PORT'
  $mb_type		= 'activemq' #in wso2 mb case, value should be 'wso2mb'
  $cep_ip               = 'CEP_IP'
  $cep_port             = 'CEP_PORT'
  $truststore_password  = 'wso2carbon'
  $java_distribution	= 'JAVA_FILE'
  $java_name		= 'JAVA_NAME'
  $member_type_ip       = 'private'
  $lb_httpPort 		= '80'
  $lb_httpsPort 	= '443'
  $tomcat_version 	= '7.0.52'
  $enable_log_publisher = 'false'

  #following variables required only if you want to install stratos using puppet.
  #not supported in alpha version
  # Service subdomains
  #$domain               = 'stratos.com'
  #$as_subdomain         = 'autoscaler'
  #$management_subdomain = 'management'

  #$admin_username       = 'admin'
  #$admin_password       = 'admin123'

  #$puppet_ip            = '10.4.128.7'
  


  #$cc_ip                = '10.4.128.9'
  #$cc_port              = '9443'

  #$sc_ip                = '10.4.128.13'
  #$sc_port              = '9443'

  #$as_ip                = '10.4.128.8'
  #$as_port              = '9443'

  #$git_hostname        = 'git.stratos.com'
  #$git_ip              = '10.4.128.13'

  $mysql_server         = 'DB_HOST'
  $mysql_port           = 'DB_PORT'
  $max_connections      = '100000'
  $max_active           = '150'
  $max_wait             = '360000'

  $bam_ip              = 'BAM_IP'
  $bam_port            = 'BAM_PORT'
  
  #$internal_repo_user     = 'admin'
  #$internal_repo_password = 'admin'

}

# php cartridge node
node /php/ inherits base {
  $docroot = "/var/www/"
  $syslog="/var/log/apache2/error.log"
  $samlalias="/var/www/"
  require java
  class {'agent':
    type => 'php',
  }
  class {'php':}
#install php before agent
  Class['php'] ~> Class['agent']
}

# loadbalancer cartridge node
node /lb/ inherits base {
  require java
  class {'agent':}
  class {'lb': maintenance_mode => 'norestart',}
}

# tomcat cartridge node
node /tomcat/ inherits base {
  $docroot = "/mnt/apache-tomcat-${tomcat_version}/webapps/"
  $samlalias="/mnt/apache-tomcat-${tomcat_version}/webapps/"

  require java
  class {'agent':}
  class {'tomcat':}

#install tomcat befor agent
#Class['tomcat'] ~> Class['agent']
}

# mysql cartridge node
node /mysql/ inherits base {
  require java
  class {'agent':
    type => 'mysql',
  }
  class {'mysql':}
}

# nodejs cartridge node
node /nodejs/ inherits base {
  require java
  class {'agent':
    type => 'nodejs',
  }
  class {'nodejs':}

#install agent before nodejs
  Class['nodejs'] ~> Class['agent']
}

# haproxy extension loadbalancer cartridge node
node /haproxy/ inherits base {
  require java
  class {'haproxy':}
  class {'agent':}
}

# ruby cartridge node
node /ruby/ inherits base {
  require java
  class {'agent':
  }
  class {'ruby':}
# Class['ruby'] ~> Class['agent']
}

#wordpress cartridge node
node /wordpress/ inherits base {
  class {'agent':}
  class {'wordpress':}
  class {'mysql':}

}

#appserver cartridge node
node /appserver/ inherits base {
  $docroot = "/mnt/wso2as-5.2.1"
  require java	
  class {'agent':}
  class {'appserver':

        version            => '5.2.1',
        sub_cluster_domain => 'test',
	members            => false,
	offset		   => 0,
        tribes_port        => 4100,
        config_db          => 'AS_CONFIG_DB',
	config_target_path => 'AS_CONFIG_PATH',
        maintenance_mode   => 'zero',
        depsync            => false,
        clustering         => false,
	cloud		   => true,
        owner              => 'root',
        group              => 'root',
        target             => '/mnt/'

  }

}

#is cartridge node
node /is/ inherits base {
  $docroot = "/mnt/wso2is-4.6.0"
  require java
  class {'agent':}
  class {'is':

        version            => '4.6.0',
        sub_cluster_domain => 'test',
        members            => false,
        offset             => 0,
        clustering_local_port        => 4100,
        config_db          => 'IS_CONFIG_DB',
        config_target_path => 'IS_CONFIG_PATH',
        maintenance_mode   => 'zero',
        depsync            => false,
        clustering         => false,
        cloud              => true,
        owner              => 'root',
        group              => 'root',
        target             => '/mnt/'

  }

}

#esb cartridge node
node /esb/ inherits base {
  $docroot = "/mnt/wso2esb-4.8.1"
  require java
  class {'agent':}
  class {'esb':

        version            => '4.8.1',
        sub_cluster_domain => 'test',
        members            => false,
	port_mapping	   => false,
        offset             => 0,
        hazelcast_port        => 4100,
        config_db          => 'ESB_CONFIG_DB',
	config_target_path => 'ESB_CONFIG_PATH',
        maintenance_mode   => 'zero',
        depsync            => false,
        clustering         => false,
        cloud              => true,
        owner              => 'root',
        group              => 'root',
        target             => '/mnt/'

  }

}



#bps cartridge node
node /bps/ inherits base {
  $docroot = "/mnt/wso2bps-3.2.0"
  require java
  class {'agent':}
  class {'bps':

        version            => '3.2.0',
        sub_cluster_domain => 'test',
        members            => false,
        offset             => 0,
        tribes_port        => 4100,
        config_db          => 'userstore',
        config_target_path => 'config/bps',
        maintenance_mode   => 'zero',
        depsync            => false,
        clustering         => false,
        cloud              => true,
        owner              => 'root',
        group              => 'root',
        target             => '/mnt/'

  }

}


node /gateway/ inherits base {
  $docroot = "/mnt/wso2am-1.6.0"
  require java
  class {'agent':}
  class {'apimanager':

	version            => "1.6.0",
  env                => undef,
  #sub_cluster_domain => 'mgt',
  sub_cluster_domain => undef,
  local_member_port  => '5000',
  members            => {'127.0.0.1' => '4000'},
  port_mapping       => false,
  amtype             => 'gateway',
  offset             => 0,
  config_database          => 'config',
  maintenance_mode   => 'refresh',
  depsync            => false,
  clustering         => true,
  cloud              => true,
  owner              => 'root',
  group              => 'root',
  target             => '/mnt'

  }

}

node /keymanager/ inherits base {
  $docroot = "/mnt/wso2am-1.6.0"
  require java
  class {'agent':}
  class {'apimanager':

        version            => "1.6.0",
  env                => undef,
  sub_cluster_domain => undef,
  local_member_port  => '5000',
  members            => {'127.0.0.1' => '4000'},
  port_mapping       => false,
  amtype             => 'keymanager',
  offset             => 0,
  config_database          => 'config',
  maintenance_mode   => 'refresh',
  depsync            => false,
  clustering         => true,
  cloud              => true,
  owner              => 'root',
  group              => 'root',
  target             => '/mnt'

  }

}


node /apistore/ inherits base {
  $docroot = "/mnt/wso2am-1.6.0"
  require java
  class {'agent':}
  class {'apimanager':

        version            => "1.6.0",
  env                => undef,
  sub_cluster_domain => undef,
  local_member_port  => '5000',
  members            => {'127.0.0.1' => '4000'},
  port_mapping       => false,
  amtype             => 'apistore',
  offset             => 0,
  config_database          => 'config',
  maintenance_mode   => 'refresh',
  depsync            => false,
  clustering         => true,
  cloud              => true,
  owner              => 'root',
  group              => 'root',
  target             => '/mnt'

  }

}


node /publisher/ inherits base {
  $docroot = "/mnt/wso2am-1.6.0"
  require java
  class {'agent':}
  class {'apimanager':

        version            => "1.6.0",
  env                => undef,
  sub_cluster_domain => undef,
  local_member_port  => '5000',
  members            => {'127.0.0.1' => '4000'},
  port_mapping       => false,
  amtype             => 'publisher',
  offset             => 0,
  config_database          => 'config',
  maintenance_mode   => 'refresh',
  depsync            => false,
  clustering         => true,
  cloud              => true,
  owner              => 'root',
  group              => 'root',
  target             => '/mnt'

  }

}


# stratos components related nodes
# not supported in alpha version.
node 'autoscaler.wso2.com' inherits base {
  require java
  class {'autoscaler': maintenance_mode => 'norestart',}
}

node 'cc.wso2.com' inherits base {
  require java
  class {'cc': maintenance_mode   => 'norestart',}
}

node 'cep.wso2.com' inherits base {
  require java
  class {'cep': maintenance_mode   => 'norestart',}
}


node 'mb.wso2.com' inherits base {
  require java
  class {'messagebroker': maintenance_mode   => 'norestart',}
}

node 'sc.wso2.com' inherits base {
  require java
  class {'manager': maintenance_mode   => 'norestart',}
}

# default (base) cartridge node
node /default/ inherits base {
  require java
  class {'agent':}
}

