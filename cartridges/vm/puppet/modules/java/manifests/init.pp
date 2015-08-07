class java {

  $java_home  = "/opt/${java_name}"
  $package  = $java_distribution
  $local_dir = $local_package_dir

  file {
    "/opt/${package}":
      ensure => present,
      source => "puppet:///modules/java/${package}",
      mode   => '0755',
      ignore => '.svn';

    '/opt/java':
      ensure  => link,
      target  => "${java_home}",
      require => Exec['Install java'];
  }

  exec {
    'Install java':
      path    => "/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
      cwd     => '/opt',
      command => "/bin/tar xzf ${package}",
      unless  => "/usr/bin/test -d ${java_home}",
      creates => "${java_home}/COPYRIGHT",
      require => File["/opt/${package}"];
  }

  file { "/etc/environment":
    content => inline_template("JAVA_HOME=${java_home}"),
    require => Exec['Install java'];
  }
}
