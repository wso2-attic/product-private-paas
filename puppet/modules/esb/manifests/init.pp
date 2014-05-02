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
# Class: esb
#
# This class installs WSO2 ESB
#
# Parameters:
#
# version            => '4.8.0',
# offset             => 0,
# hazelcast_port     => 4000,
# config_db          => 'esb_config',
# maintenance_mode   => 'zero',
# depsync            => false,
# sub_cluster_domain => 'worker',
# clustering         => true,
# cloud              => true,
# owner              => 'wso2',
# group              => 'wso2',
# target             => '/mnt/',
# members            => {'192.168.18.122' => 4010 },
# port_mapping       => { 80 => 9763, 443 => 9443, 8280 => 8280, 8243 => 8243};
#
# Actions:
#   - Install WSO2 ESB
#
# Requires:
#
# Sample Usage:
#

class esb (
  $version            = undef,
  $sub_cluster_domain = undef,
  $members            = undef,
  $port_mapping       = undef,
  $offset             = 0,
  $hazelcast_port     = 4000,
  $config_db          = 'governance',
  $config_target_path = 'config/esb',
  $maintenance_mode   = true,
  $depsync            = false,
  $clustering         = false,
  $cloud              = true,
  $owner              = 'root',
  $group              = 'root',
  $target             = '/mnt',
) inherits params {

  $deployment_code = 'esb'
  $carbon_version  = $version
  $service_code    = 'esb'
  $carbon_home     = "${target}/wso2${service_code}-${carbon_version}"

  $service_templates = $sub_cluster_domain ? {
    'mgt'    => [
      'conf/axis2/axis2.xml',
      'conf/carbon.xml',
#      'conf/datasources/master-datasources.xml',
#      'conf/registry.xml',
      'conf/tomcat/catalina-server.xml',
#      'conf/user-mgt.xml',
      ],
    'worker' => [
      'conf/axis2/axis2.xml',
      'conf/carbon.xml',
#      'conf/datasources/master-datasources.xml',
#      'conf/registry.xml',
      'conf/tomcat/catalina-server.xml',
#      'conf/user-mgt.xml',
      ],
    default => [
      'conf/axis2/axis2.xml',
      'conf/carbon.xml',
      'conf/datasources/master-datasources.xml',
      'conf/registry.xml',
      'conf/tomcat/catalina-server.xml',
      'conf/user-mgt.xml',
      'conf/log4j.properties',
      ],
  }

  $common_templates = []

  tag($service_code)

  esb::clean { $deployment_code:
    mode   => $maintenance_mode,
    target => $carbon_home,
  }

  esb::initialize { $deployment_code:
    repo      => $package_repo,
    version   => $carbon_version,
    service   => $service_code,
    local_dir => $local_package_dir,
    target    => $target,
    mode      => $maintenance_mode,
    owner     => $owner,
    require   => Esb::Clean[$deployment_code],
  }

  esb::deploy { $deployment_code:
    security => true,
    owner    => $owner,
    group    => $group,
    target   => $carbon_home,
    require  => Esb::Initialize[$deployment_code],
  }

  esb::push_templates {
    $service_templates:
      target    => $carbon_home,
      directory => $deployment_code,
      require   => Esb::Deploy[$deployment_code];

    $common_templates:
      target    => $carbon_home,
      directory => 'commons',
      require   => Esb::Deploy[$deployment_code],
  }

  esb::start { $deployment_code:
    owner   => $owner,
    target  => $carbon_home,
    require => [
      Esb::Initialize[$deployment_code],
      Esb::Deploy[$deployment_code],
      Push_templates[$service_templates],
      Push_templates[$common_templates],
      ],
  }
}
