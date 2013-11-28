#

class gitblit (
  $version            = undef,
  $members            = undef,
  $offset	      = '0',
  $maintenance_mode   = true,
  $cloud              = false,
  $owner              = 'root',
  $group              = 'root',
  $target             = '/mnt',
  $auto_scaler        = false,
  $auto_failover      = false,
) inherits params {

  $deployment_code    = 'gitblit'
  $carbon_version     = $version
  $service_code       = 'gitblit'
  $carbon_home        = "${target}/${service_code}-${carbon_version}"
  $service_templates  = [
				"gitblit.sh",
				"gitblit-stop.sh",
  			]

  $commons_templates  = [
  			
            ]

  tag ($service_code)

  clean {
    $deployment_code:
      mode   => $maintenance_mode,
      target => $carbon_home,
  }

  initialize {
    $deployment_code:
      repo      => $package_repo,
      version   => $carbon_version,
      mode      => $maintenance_mode,
      service   => $service_code,
      local_dir => $local_package_dir,
      owner     => $owner,
      target    => $target,
      require   => Clean[$deployment_code],
  }

  deploy { 
   $deployment_code:
     security => true,
     owner    => $owner,
     group    => $group,
     target   => $carbon_home,
     service  => $service_code,
     require  => Initialize[$deployment_code],
  }

  push_templates {
    $service_templates:
      target     => $carbon_home,
      directory  => $deployment_code,
      require    => Deploy[$deployment_code];

    $commons_templates:
      target     => $carbon_home,
      directory  => $deployment_code,
      require    => Deploy[$deployment_code];
  }
  
  start {
    $deployment_code:
      owner   => $owner,
      target  => $carbon_home,
      require => [  Initialize[$deployment_code],
                    Deploy[$deployment_code],
                    Push_templates[$service_templates],
		 ],
        }
}
