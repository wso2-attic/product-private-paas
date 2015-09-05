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


class configurator (

  $target             = "/mnt",
  $configurator_home  = "/mnt/${configurator_name}-${configurator_version}"
)
{

  exec {
    "creating_local_package_repo_for_${configurator_name}":
      path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
      unless  => "test -d ${local_package_dir}",
      command => "mkdir -p ${local_package_dir}";
  }

  file { "${local_package_dir}/${configurator_name}-${configurator_version}.zip":
    ensure    => present,
    source    => "puppet:///modules/configurator/${configurator_name}-${configurator_version}.zip",
    owner     => "root",
    mode      => "0755",
    require   => Exec["creating_local_package_repo_for_${configurator_name}"];
  }

  file { "/etc/profile.d/configurator_home.sh":
    content => "export CONFIGURATOR_HOME=${configurator_home}",
    mode    => 755
  }

  exec {
    "extracting_${configurator_name}":
      path      => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin',
      cwd       => $target,
      command   => "unzip -o ${local_package_dir}/${configurator_name}-${configurator_version}.zip",
      logoutput => 'on_failure',
      creates   => "${target}/${configurator_name}-${configurator_version}",
      unless    => "test -d ${target}/${configurator}/configurator.py",
      require   => File["${local_package_dir}/${configurator_name}-${configurator_version}.zip"];
  }


}
