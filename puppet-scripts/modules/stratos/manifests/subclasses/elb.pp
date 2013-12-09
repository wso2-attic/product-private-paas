# Class: cc
#
# This class installs Apache Stratos ELB
#
# Parameters:
#   version          => '2.1.0',
#   maintenance_mode => 'zero',
#   auto_scaler      => 'false',
#   auto_failover    => false,
#   cloud            => true,
#   owner            => 'root',
#   group            => 'root',
#   target           => '/mnt',
#   stage            => deploy,
#   members          => {'ELB2.example.com' =>4010 };
#
# Actions:
#   - Install Apache Stratos ELB
#
#

class stratos::elb (
  $version            = undef,
  $offset	      = '0',
  $members            = undef,
  $maintenance_mode   = true,
  $cloud              = false,
  $target             = '/mnt',
  $auto_scaler        = false,
  $auto_failover      = false,
) inherits params {

  $owner              = 'root'
  $group              = 'root'

  $deployment_code    = 'elb'
  $carbon_version     = $version
  $service_code       = 'elb'
  $carbon_home        = "${target}/apache-stratos-${service_code}-${carbon_version}"
  $service_templates  = [
			    'conf/axis2/axis2.xml',
#			    'conf/carbon.xml',
			    'conf/etc/jmx.xml',
			    'conf/loadbalancer.conf',
			    'conf/datasources/master-datasources.xml',
#			    'conf/cartridge-config.properties',
#			    'conf/log4j.properties',
#			    'conf/etc/logging-config.xml',
  			]

  $commons_templates  = [
  			]
  tag ('elb')

  clean {
    $deployment_code:
      service  => $service_code,
      version   => $carbon_version,
      local_dir => $local_package_dir,
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

  deploy { $deployment_code:
    service  => $service_code,
    security => true,
    owner    => $owner,
    group    => $group,
    target   => $carbon_home,
    require  => Initialize[$deployment_code],
  }

  push_elb_templates {
    $service_templates:
      target     => $carbon_home,
      directory  => "stratos",
      service   => $service_code,
      require    => Deploy[$deployment_code];

    $commons_templates:
      target     => $carbon_home,
      directory  => "stratos",
      service   => $service_code,
      require    => Deploy[$deployment_code];
  }
  
#  push_stratos_sh { 
#    'bin/stratos.sh':
#      target     => $carbon_home,
#      directory  => "stratos",
#      service   => $service_code,
#      require    => Deploy[$deployment_code];
#  }

  start {
    $deployment_code:
      owner   => $owner,
      target  => $carbon_home,
      require => [  Initialize[$deployment_code],
                    Deploy[$deployment_code],
                    Push_elb_templates[$service_templates],
        #            Push_stratos_sh['bin/stratos.sh'], 
		 ],
        }
}
