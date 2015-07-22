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

# appserver cartridge node
node /[0-9]{1,12}.(default|manager|worker).appserver/ inherits base {
  $docroot = "/mnt/${server_ip}/wso2as-5.2.1"
  require java	
  class {'python_agent':}
  class {'appserver':

        version            => '5.2.1',
        sub_cluster_domain => 'test',
	    members            => undef,
	    offset		   => 0,
        hazelcast_port     => 4000,
	    config_db          => 'AS_CONFIG_DB',
        config_target_path => 'AS_CONFIG_PATH',
        maintenance_mode   => 'zero',
        agent_home         =>
        depsync            => false,
        clustering         => CLUSTERING,
        membership_scheme   => MEMBERSHIP_SCHEME,
	    cloud		   => true,
        owner              => 'root',
        group              => 'root',
        target             => "/mnt/${server_ip}",
        agent_home          => "/mnt/${server_ip}/apache-stratos-python-cartridge-agent-4.1.0-SNAPSHOT"

  }

  Class['stratos_base'] -> Class['java'] -> Class['appserver'] ~> Class['agent']
}
