
#
# Executes the deployment by pushing all necessary configurations and patches

define stratos::deploy ( $security, $target, $owner, $group, $service ) {

  file {
    "/tmp/${::deployment_code}":
      ensure          => present,
      owner           => $owner,
      group           => $group,
      sourceselect    => all,
      ignore          => '.svn',
      recurse         => true,
      source          => [
                          "puppet:///modules/stratos/${service}/configs/",
                          "puppet:///modules/stratos/${service}/patches/"]
  }

  exec {
    "Copy_${name}_modules_to_carbon_home":
      path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
      command => "cp -r /tmp/${::deployment_code}/* ${target}/; chown -R ${owner}:${owner} ${target}/; chmod -R 755 ${target}/",
      require => File["/tmp/${::deployment_code}"];

    "Remove_${name}_temporory_modules_directory":
      path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
      command => "rm -rf /tmp/${::deployment_code}",
      require => Exec["Copy_${name}_modules_to_carbon_home"];
  }
}
