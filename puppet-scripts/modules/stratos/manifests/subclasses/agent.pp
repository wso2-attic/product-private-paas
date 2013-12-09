# Class: agent
#
# This class installs Apache Stratos SC
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
#   members          => {'agent2.example.com' =>4010 };
#
# Actions:
#   - Install Apache Stratos SC
#
#

class stratos::agent (
  $version            = undef,
  $offset	      = '0',
  $members            = undef,
  $maintenance_mode   = true,
  $cloud              = false,
  $owner              = 'root',
  $group              = 'root',
  $target             = '/mnt',
  $auto_scaler        = false,
  $auto_failover      = false,
) inherits params {

  $deployment_code    = 'agent'
  $carbon_version     = $version
  $service_code       = 'agent'
  $carbon_home        = "${target}/apache-stratos-${service_code}-${carbon_version}"
  $service_templates  = [
			    'conf/carbon.xml',
			    'conf/agent.properties',
#			    'conf/cartridge-config.properties',
#			    'conf/log4j.properties',
#			    'conf/datasources/stratos-datasources.xml',
#			    'conf/datasources/master-datasources.xml',
#			    'conf/etc/logging-config.xml',
  			]

  $commons_templates  = [
  			]
  tag ('agent')

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

  push_agent_templates {
    $service_templates:
      target     => $carbon_home,
      directory  => "stratos",
      service  => $service_code,
      require    => Deploy[$deployment_code];

    $commons_templates:
      target     => $carbon_home,
      directory  => "stratos",
      service  => $service_code,
      require    => Deploy[$deployment_code];
  }
  
#  push_stratos_sh { 
#    'bin/stratos.sh':
#      target     => $carbon_home,
#      directory  => "stratos",
#      service   => $service_code,
#      require    => Deploy[$deployment_code];
#  }

  exec {
    'remove_registrants':
      path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
      command => "rm -rf ${carbon_home}/registrants",
      require => [
		Initialize[$deployment_code],
                Deploy[$deployment_code],
                Push_agent_templates[$service_templates],
#                Push_stratos_sh['bin/stratos.sh'],
		]; 
  }

  start {
    $deployment_code:
      owner   => $owner,
      target  => $carbon_home,
      require => [  Initialize[$deployment_code],
                    Deploy[$deployment_code],
                    Push_agent_templates[$service_templates],
		    Exec['remove_registrants']
#                    Push_stratos_sh['bin/stratos.sh'], 
		 ],
        }
}
