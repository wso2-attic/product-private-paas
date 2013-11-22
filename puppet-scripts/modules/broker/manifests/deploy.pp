
#
# Executes the deployment by pushing all necessary configurations and patches

define broker::deploy ( $security, $target, $owner, $group, $service ) {

  file {
    "/tmp/${broker::deployment_code}":
      ensure          => present,
      owner           => $owner,
      group           => $group,
      sourceselect    => all,
      ignore          => '.svn',
      recurse         => true,
      source          => [
                          "puppet:///modules/broker/configs/",
                          "puppet:///modules/broker/patches/"
			]
  }

  exec {
    "Copy_${name}_modules_to_carbon_home":
      path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
      command => "cp -r /tmp/${broker::deployment_code}/* ${target}/; chown -R ${owner}:${owner} ${target}/; chmod -R 755 ${target}/",
      require => File["/tmp/${broker::deployment_code}"];

    "Remove_${name}_temporory_modules_directory":
      path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
      command => "rm -rf /tmp/${broker::deployment_code}",
      require => Exec["Copy_${name}_modules_to_carbon_home"];
  }

  notify { 'deploy_${name}':
	message => "temp is : /tmp/${broker::deployment_code}",
	
  }
}
