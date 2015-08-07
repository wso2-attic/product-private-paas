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

class python_agent(
  $version                = '4.1.1-SNAPSHOT',
  $owner                  = 'root',
  $group                  = 'root',
  $target                 = "/mnt",
  $type                   = 'default',
  $enable_artifact_update = true,
  $auto_commit            = false,
  $auto_checkout          = true,
  $module                 = 'undef'
){

  $service_code    = 'cartridge-agent'
  $agent_name = "apache-stratos-python-${service_code}-${version}"
  $agent_home= "${target}/${agent_name}"

  $split_mburls = split($mb_url, "//")
  $split_mburl = split($split_mburls[1], ":")
  $mb_ip = $split_mburl[0]
  $mb_port = $split_mburl[1]

  tag($service_code)


  python_agent::initialize { $service_code:
    repo       => $package_repo,
    version    => $version,
    agent_name => $agent_name,
    local_dir  => $local_package_dir,
    target     => $target,
    owner      => $owner,
  }

  exec { 'copy launch-params to agent_home':
    path    => '/bin/',
    command => "mkdir -p ${agent_home}/payload; cp /tmp/payload/launch-params ${agent_home}/payload/launch-params",
    require => Python_agent::Initialize[$service_code];
  }

  exec { 'make extension log folder':
    path    => '/bin/',
    command => "mkdir -p /var/log/apache-stratos",
  }

  exec { 'make extension log file':
    path    => '/bin/',
    command => "touch /var/log/apache-stratos/cartridge-agent-extensions.log",
    require => Exec['make extension log folder'];
  }


  file { "${agent_home}/agent.conf":
    ensure  => file,
    content => template("python_agent/agent.conf.erb"),
    require => Python_agent::Initialize[$service_code],
  }

  file { "${agent_home}/logging.ini":
    ensure  => file,
    content => template("python_agent/logging.ini.erb"),
    require => File["${agent_home}/agent.conf"],
  }


# python_agent::start { $service_code:
#   owner   => $owner,
#   target  => $agent_home,
#   require => Exec['copy launch-params to agent_home']
# }
}

