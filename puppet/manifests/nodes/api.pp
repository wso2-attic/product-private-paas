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

# API - gateway cartridge node
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
        config_database    => 'config',
        maintenance_mode   => 'refresh',
        depsync            => false,
        clustering         => true,
        cloud              => true,
        owner              => 'root',
        group              => 'root',
        target             => '/mnt'
  }

  Class['stratos_base'] -> Class['java'] -> Class['apimanager'] ~> Class['agent']
}


# API - keymanager cartridge node
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
        config_database    => 'config',
        maintenance_mode   => 'refresh',
        depsync            => false,
        clustering         => true,
        cloud              => true,
        owner              => 'root',
        group              => 'root',
        target             => '/mnt'

  }

  Class['stratos_base'] -> Class['java'] -> Class['apimanager'] ~> Class['agent']
}

# API - apistore cartridge node
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
        config_database    => 'config',
        maintenance_mode   => 'refresh',
        depsync            => false,
        clustering         => true,
        cloud              => true,
        owner              => 'root',
        group              => 'root',
        target             => '/mnt'

  }

  Class['stratos_base'] -> Class['java'] -> Class['apimanager'] ~> Class['agent']
}

# API - publisher cartridge node
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
        config_database    => 'config',
        maintenance_mode   => 'refresh',
        depsync            => false,
        clustering         => true,
        cloud              => true,
        owner              => 'root',
        group              => 'root',
        target             => '/mnt'
  }

  Class['stratos_base'] -> Class['java'] -> Class['apimanager'] ~> Class['agent']
}
