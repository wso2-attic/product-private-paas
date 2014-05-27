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

# API Manager - gateway (worker) cartridge node
node /[0-9]{1,12}.(default|manager|worker).gateway/ inherits base {
  $docroot = "/mnt/${server_ip}/wso2am-1.7.0"
  require java
  class {'agent':}
  class {'apimanager':

        version            => "1.7.0",
        env                => undef,
        #sub_cluster_domain => 'mgt',
        sub_cluster_domain => undef,
        hazelcast_port     => 4000,
        members            => undef,
        port_mapping       => false,
        amtype             => 'gateway',
        offset             => 0,
        config_database          => 'GATEWAY_CONFIG_DB',
        config_target_path => 'GATEWAY_CONFIG_PATH',
        maintenance_mode   => 'refresh',
        depsync            => false,
        clustering         => true,
        cloud              => true,
        owner              => 'root',
        group              => 'root',
        target             => "/mnt/${server_ip}"
  }

  Class['stratos_base'] -> Class['java'] -> Class['apimanager'] ~> Class['agent']
}

# API Manager - gateway (manager) cartridge node
node /[0-9]{1,12}.(default|manager|worker).gatewaymgt/ inherits base {
  $docroot = "/mnt/${server_ip}/wso2am-1.7.0"
  require java
  class {'agent':}
  class {'apimanager':

        version            => "1.7.0",
        env                => undef,
        #sub_cluster_domain => 'mgt',
        sub_cluster_domain => undef,
        hazelcast_port     => 4000,
        members            => undef,
        port_mapping       => false,
        amtype             => 'gateway',
        offset             => 0,
        config_database         => 'GATEWAY_CONFIG_DB',
        config_target_path => 'GATEWAY_CONFIG_PATH',
        maintenance_mode   => 'refresh',
        depsync            => false,
        clustering         => true,
        cloud              => true,
        owner              => 'root',
        group              => 'root',
        target             => "/mnt/${server_ip}"
  }

  Class['stratos_base'] -> Class['java'] -> Class['apimanager'] ~> Class['agent']
}


# API Manager - keymanager cartridge node
node /[0-9]{1,12}.(default|manager|worker).keymanager/ inherits base {
  $docroot = "/mnt/${server_ip}/wso2am-1.7.0"
  require java
  class {'agent':}
  class {'apimanager':

        version            => "1.7.0",
        env                => undef,
        sub_cluster_domain => undef,
        hazelcast_port     => 4000,
        members            => undef,
        port_mapping       => false,
        amtype             => 'keymanager',
        offset             => 0,
        config_database          => 'KEYMANAGER_CONFIG_DB',
        config_target_path => 'KEYMANAGER_CONFIG_PATH',
        maintenance_mode   => 'refresh',
        depsync            => false,
        clustering         => 'KEYMANGER_CLUSTERING',
        cloud              => 'KEYMANGER_CLOUD',
        owner              => 'root',
        group              => 'root',
        target             => "/mnt/${server_ip}"

  }

  Class['stratos_base'] -> Class['java'] -> Class['apimanager'] ~> Class['agent']
}

# API Manager - apistore cartridge node
node /[0-9]{1,12}.(default|manager|worker).apistore/ inherits base {
  $docroot = "/mnt/${server_ip}/wso2am-1.7.0"
  require java
  class {'agent':}
  class {'apimanager':

        version            => "1.7.0",
        env                => undef,
        sub_cluster_domain => undef,
        hazelcast_port     => 4000,
        members            => undef,
        port_mapping       => false,
        amtype             => 'apistore',
        offset             => 0,
        config_database          => 'STORE_CONFIG_DB',
        config_target_path => 'STORE_CONFIG_PATH',
        maintenance_mode   => 'refresh',
        depsync            => false,
        clustering         => true,
        cloud              => true,
        owner              => 'root',
        group              => 'root',
        target             => "/mnt/${server_ip}"

  }

  Class['stratos_base'] -> Class['java'] -> Class['apimanager'] ~> Class['agent']
}

# API Manager - publisher cartridge node
node /[0-9]{1,12}.(default|manager|worker).publisher/ inherits base {
  $docroot = "/mnt/${server_ip}/wso2am-1.7.0"
  require java
  class {'agent':}
  class {'apimanager':

        version            => "1.7.0",
        env                => undef,
        sub_cluster_domain => undef,
        hazelcast_port     => 4000,
        members            => undef,
        port_mapping       => false,
        amtype             => 'publisher',
        offset             => 0,
        config_database          => 'STORE_CONFIG_DB',
        config_target_path => 'STORE_CONFIG_PATH',
        maintenance_mode   => 'refresh',
        depsync            => false,
        clustering         => true,
        cloud              => true,
        owner              => 'root',
        group              => 'root',
        target             => "/mnt/${server_ip}"
  }

  Class['stratos_base'] -> Class['java'] -> Class['apimanager'] ~> Class['agent']
}
