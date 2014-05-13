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
# Class: bps
#
# This class installs WSO2 BPS
#
# Parameters:
# version            => '3.2.0'
# offset             => 1,
# tribes_port        => 4100,
# config_db          => 'bps_config',
# maintenance_mode   => 'zero',
# depsync            => false,
# sub_cluster_domain => 'mgt',
# clustering         => true,
# owner              => 'root',
# group              => 'root',
# target             => '/mnt/',
# members            => {'elb2.wso2.com' => 4010, 'elb.wso2.com' => 4010 }
#
# Actions:
#   - Install WSO2 BPS
#
# Requires:
#
# Sample Usage:
#

class bps (
  $version            = undef,
  $sub_cluster_domain = undef,
  $members            = false,
  $offset             = 0,
  $tribes_port        = 4000,
  $config_db          = 'governance',
  $config_target_path = 'config/bps',	
  $maintenance_mode   = true,
  $depsync            = false,
  $clustering         = false,
  $cloud              = true,
  $owner              = 'root',
  $group              = 'root',
  $target             = '/mnt',
) inherits params {

  $deployment_code = 'bps'
  $carbon_version  = $version
  $service_code    = 'bps'
  $carbon_home     = "${target}/wso2${service_code}-${carbon_version}"

  $service_templates = $sub_cluster_domain ? {
    'mgt'    => [
      'conf/axis2/axis2.xml',
      'conf/carbon.xml',
#      'conf/datasources/master-datasources.xml',
#      'conf/registry.xml',
#      'conf/tomcat/catalina-server.xml',
#      'conf/user-mgt.xml',
      ],
    'worker' => [
      'conf/axis2/axis2.xml',
      'conf/carbon.xml',
#      'conf/datasources/master-datasources.xml',
#      'conf/registry.xml',
#      'conf/tomcat/catalina-server.xml',
#      'conf/user-mgt.xml',
      ],
    default => [
      'conf/axis2/axis2.xml',
      'conf/carbon.xml',
      'conf/datasources/master-datasources.xml',
      'conf/registry.xml',
#      'conf/tomcat/catalina-server.xml',
      'conf/user-mgt.xml',
      'conf/log4j.properties',
      ],
  }

  tag($service_code)

  bps::clean { $deployment_code:
    mode   => $maintenance_mode,
    target => $carbon_home,
  }

  bps::initialize { $deployment_code:
    repo      => $package_repo,
    version   => $carbon_version,
    service   => $service_code,
    local_dir => $local_package_dir,
    target    => $target,
    mode      => $maintenance_mode,
    owner     => $owner,
    require   => Bps::Clean[$deployment_code],
  }

  bps::deploy { $deployment_code:
    security => true,
    owner    => $owner,
    group    => $group,
    target   => $carbon_home,
    require  => Bps::Initialize[$deployment_code],
  }

  bps::push_templates {
    $service_templates:
      target    => $carbon_home,
      directory => $deployment_code,
      require   => Bps::Deploy[$deployment_code];
  }

#  bps::start { $deployment_code:
#    owner   => $owner,
#    target  => $carbon_home,
#    require => [
#      Bps::Initialize[$deployment_code],
#      Bps::Deploy[$deployment_code],
#      Push_templates[$service_templates],
#      ],
#  }

}
