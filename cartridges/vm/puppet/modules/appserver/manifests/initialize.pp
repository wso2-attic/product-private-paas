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
# Initializing the deployment

define appserver::initialize ($repo, $version, $service, $local_dir, $target, $mode, $owner,) {

  exec {
    "creating_target_for_${name}":
      path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin',
      command => "mkdir -p ${target}";

    "creating_local_package_repo_for_${name}":
      path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
      unless  => "test -d ${local_dir}",
      command => "mkdir -p ${local_dir}";
  }

  file {
    "/${local_dir}/wso2${service}-${version}.zip":
      ensure => present,
      source => ["puppet:///modules/appserver/wso2${service}-${version}.zip", "puppet:///packs/wso2${service}-${version}.zip"],
      require   => Exec["creating_local_package_repo_for_${name}", "creating_target_for_${name}"];
  }

  file {
    "/${local_dir}/ppaas-configurator-${stratos_version}-SNAPSHOT.zip":
      ensure => present,
      source => ["puppet:///modules/appserver/ppaas-configurator-${stratos_version}-SNAPSHOT.zip", "puppet:///packs/ppaas-configurator-${stratos_version}-SNAPSHOT.zip"],
      require   => Exec["creating_local_package_repo_for_${name}", "creating_target_for_${name}"];
  }

  file {
    "/${local_dir}/wso2${service}-${version}-template-module-${stratos_version}-SNAPSHOT.zip":
      ensure => present,
      source => ["puppet:///modules/appserver/wso2${service}-${version}-template-module-${stratos_version}.zip", "puppet:///packs/wso2${service}-${version}-template-module-${stratos_version}.zip"],
      require   => Exec["creating_local_package_repo_for_${name}", "creating_target_for_${name}"];
  }

  exec {
    "extracting_wso2${service}-${version}.zip_for_${name}":
      path      => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin',
      cwd       => $target,
      unless    => "test -d ${target}/wso2${service}-${version}/repository",
      command   => "unzip ${local_dir}/wso2${service}-${version}.zip",
      logoutput => 'on_failure',
      creates   => "${target}/wso2${service}-${version}/repository",
      timeout   => 0,
      require   => File["/${local_dir}/wso2${service}-${version}.zip"];

       "setting_permission_for_${name}":
      path      => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin',
      cwd       => $target,
      command   => "chown -R ${owner}:${owner} ${target}/wso2${service}-${version} ;
                    chmod -R 755 ${target}/wso2${service}-${version}",
      logoutput => 'on_failure',
      timeout   => 0,
      require   => Exec["extracting_wso2${service}-${version}.zip_for_${name}"];

    "extracting ppaas-configurator-${stratos_version}-SNAPSHOT.zip":
      path      => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin',
      cwd       => $target,
      unless    => "test -d ${target}/ppaas-configurator-${stratos_version}-SNAPSHOT/template-modules",
      command   => "unzip /${local_dir}/ppaas-configurator-${stratos_version}-SNAPSHOT.zip",
      logoutput => 'on_failure',
      creates   => "${target}/ppaas-configurator-${stratos_version}-SNAPSHOT/template-modules",
      timeout   => 0,
      require   => File["/${local_dir}/ppaas-configurator-${stratos_version}-SNAPSHOT.zip"];

    "extracting wso2${service}-${version}-template-module-${stratos_version}-SNAPSHOT.zip":
      path      => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin',
      cwd       => $target/ppaas-configurator-${stratos_version}-SNAPSHOT/template-modules,
      unless    => "test -d ${target}/ppaas-configurator-${stratos_version}-SNAPSHOT/template-modules/wso2${service}-${version}-template-module-${stratos_version}-SNAPSHOT/templates",
      command   => "unzip /${local_dir}/wso2${service}-${version}-template-module-${stratos_version}-SNAPSHOT.zip",
      logoutput => 'on_failure',
      creates   => "${target}/ppaas-configurator-${stratos_version}-SNAPSHOT/template-modules/wso2${service}-${version}-template-module-${stratos_version}-SNAPSHOT/templates",
      timeout   => 0,
      require   => [File["/${local_dir}/wso2${service}-${version}-template-module-${stratos_version}-SNAPSHOT.zip"],
                    Exec[extracting ppaas-configurator-${stratos_version}-SNAPSHOT.zip]];

      "setting_permission_for_ppaas-configurator-${stratos_version}-SNAPSHOT":
      path      => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin',
      cwd       => $target,
      command   => "chown -R ${owner}:${owner} ${target}/ppaas-configurator-${stratos_version}-SNAPSHOT ;
                    chmod -R 755 ${target}/ppaas-configurator-${stratos_version}-SNAPSHOT",
      logoutput => 'on_failure',
      timeout   => 0,
      require   => Exec["extracting wso2${service}-${version}-template-module-${stratos_version}-SNAPSHOT.zip"];

  }
}
