# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
# 
#   http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

class mysql{

  if $stratos_mysql_password {
    $root_password = $stratos_mysql_password
  }
  else {
    $root_password = 'root'
  }

 Exec { path => [ '/bin', '/sbin', '/usr/bin', '/usr/sbin', ] }

 exec { 'system-update':
      command => 'sudo apt-get update',
 }

  Exec['system-update'] -> Package <| |>

  package { ['mysql-server','apache2']:
    ensure => installed,
  }

 package { 'phpmyadmin':
      ensure => present,
 }

  service { 'mysql':
    ensure  => running,
    pattern => 'mysql',
    require => Package['mysql-server'],
  }

  service { 'apache2':
    ensure  => running,
    pattern => 'apache2',
    require => Package['apache2'],
  }

  file { '/etc/apache2/conf.d/phpmyadmin.conf':
    ensure  => present,
    content => template('mysql/phpMyAdmin.conf.erb'),
    notify  => Service['apache2'],
    require => [
      Package['phpmyadmin'],
      Package['apache2'],
    ];
  }

  file { '/etc/mysql/my.cnf':
    ensure  => present,
    content => template('mysql/my.cnf.erb'),
    notify  => Service['mysql'],
    require => [
      Package['mysql-server']      
    ];
  }

  exec { 'clean sites':
      path    => ['/bin', '/usr/bin', '/usr/sbin/'],
      command => 'rm -rf /etc/apache2/sites-available/*; rm -rf /etc/apache2/sites-enabled/*',
      require => [
                   Package['phpmyadmin'],
                   Package['apache2'],
                 ];
  }

  file {
    '/etc/apache2/sites-available/default':
      owner   => 'root',
      group   => 'root',
      mode    => '0775',
      content => template('mysql/000-default.erb'),
      require => Exec['clean sites'];

    '/etc/apache2/sites-available/default-ssl':
      owner   => 'root',
      group   => 'root',
      mode    => '0775',
      content => template('mysql/default-ssl.erb'),
      require => Exec['clean sites'];

    '/etc/apache2/sites-enabled/default':
      ensure  => 'link',
      target  => '/etc/apache2/sites-available/default',
      require => File['/etc/apache2/sites-available/default'];

    '/etc/apache2/sites-enabled/default-ssl':
      ensure  => 'link',
      target  => '/etc/apache2/sites-available/default-ssl',
      require => File['/etc/apache2/sites-available/default-ssl'];	
  }

  mysql::importssl {'import ssl':
     ssl_certificate_file => $ssl_certificate_file,
     ssl_key_file         => $ssl_key_file,
     require               => Package['apache2'] 
  }

  exec {
    'enable ssl module':
      path    => ['/bin', '/usr/bin', '/usr/sbin/'],
      command => 'a2enmod ssl',
      require => [ Package['apache2'],
                   Mysql::Importssl['import ssl']
                 ];

    'apache2 restart':
      path    => ['/bin', '/usr/bin', '/usr/sbin/'],
      command => "/etc/init.d/apache2 restart",
      require => [ Exec["enable ssl module"],
                   File['/etc/apache2/sites-enabled/default'], 
                   File['/etc/apache2/sites-enabled/default-ssl']
                 ];
  }

}
