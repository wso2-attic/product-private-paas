# ----------------------------------------------------------------------------
#  Copyright 2005-2015 WSO2, Inc. http://www.wso2.org
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

class wso2installer (
  $owner              = 'root',
  $group              = 'root',
  $module_name        = undef,
  $server_name        = undef,
)  {

  $service_code         = "wso2installer/${module_name}"
  $target               = "/mnt/${server_ip}"
  $carbon_home          = "/mnt/${server_ip}/${server_name}"
  $configurator_home    = "/mnt/${configurator_name}-${configurator_version}"
  $template_module_pack = "${server_name}-template-module-${ppaas_version}.zip"
  $pca_home             = "/mnt/${pca_name}-${pca_version}"
  $java_home            = "/opt/${java_folder}"

# creating /mnt/{ip_address} folder
  exec {
    "creating_target_for_${server_name}":
      path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin',
      command => "mkdir -p ${target}";

    "creating_local_package_repo_for_${server_name}":
      path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
      unless  => "test -d ${local_package_dir}",
      command => "mkdir -p ${local_package_dir}";
  }

# copy {server}.zip file to /mnt/packs folder
  file {
    "$local_package_dir/${server_name}.zip":
      ensure    => present,
      source    => ["puppet:///modules/${service_code}/packs/${server_name}.zip"],
      require   => Exec["creating_local_package_repo_for_${server_name}", "creating_target_for_${server_name}"];
  }

# unzipping {server}.zip file to /mnt/{ip_address} folder 
  exec {
    "extracting_${server_name}.zip_for_${server_name}":
      path      => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin',
      cwd       => $target,
      command   => "unzip ${local_package_dir}/${server_name}.zip",
      logoutput => 'on_failure',
      creates   => "${target}/${server_name}/repository",
      timeout   => 0,
      require   => File["$local_package_dir/${server_name}.zip"];

    "setting_permission_for_${server_name}":
      path      => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin',
      cwd       => $target,
      command   => "chown -R ${owner}:${owner} ${target}/${server_name} ;
                    chmod -R 755 ${target}/${server_name}",
      logoutput => 'on_failure',
      timeout   => 0,
      require   => Exec["extracting_${server_name}.zip_for_${server_name}"];
  }

# Copying template module
  file {
    "$local_package_dir/${template_module_pack}":
      ensure    => present,
      source    => ["puppet:///modules/${service_code}/packs/${template_module_pack}"],
      require   => Exec["setting_permission_for_${server_name}"];
  }

  exec {
    "extracting_template_module_${template_module_pack}":
      path      => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin',
      cwd       => "/mnt/${configurator_name }-${configurator_version}/template-modules",
      command   => "unzip -o ${local_package_dir}/${template_module_pack}",
      logoutput => 'on_failure',
      timeout   => 0,
      require   => File["$local_package_dir/${template_module_pack}"];
  }


  file { "${pca_home}/plugins":
    source  => "puppet:///modules/${service_code}/plugins",
    recurse => true,
    ignore => "README",
    require => Exec["extracting_template_module_${template_module_pack}"]
  }

# starting python cartridge agent
  exec { "starting_${pca_home}":
    environment => [ "CARBON_HOME=${carbon_home}", "PCA_HOME=${pca_home}" ,"JAVA_HOME=${java_home}",
      "CONFIGURATOR_HOME=${configurator_home}" ],
    user        => $owner,
    path        => "/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:${pca_home}",
    cwd         => "${pca_home}",
    command     => "./start_agent.sh /tmp/start_agent.log 2>&1",
    require     =>  File["${pca_home}/plugins"]
  }

}
