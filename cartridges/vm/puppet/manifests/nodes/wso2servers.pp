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

# WSO2 Server Nodes definition

# ESB cartridge node
node /[0-9]{1,12}.*wso2esb-481/ inherits base {

  class { 'java': }
  class { 'python_agent':
    docroot => "/var/www"
  }
  class { 'configurator': }
  class { 'wso2servers':
    server_name      => 'wso2esb-4.8.1',
    module_name   => 'wso2esb481'

  }

  Class['stratos_base'] -> Class['java'] -> Class['configurator']-> Class['python_agent'] -> Class['wso2servers']
}


# IS cartridge node
node /[0-9]{1,12}.*wso2is-500/ inherits base {

  class { 'java': }
  class { 'python_agent':
    docroot => "/var/www"
  }
  class { 'configurator': }
  class { 'wso2servers':
    server_name      => 'wso2is5.0.0',
    module_name   => 'wso2is-500'
  }

  Class['stratos_base'] -> Class['java'] -> Class['configurator']-> Class['python_agent'] -> Class['wso2servers']
}


# API Manager cartridge node
node /[0-9]{1,12}.*wso2am-190/ inherits base {

  class { 'java': }
  class { 'python_agent':
    docroot => "/var/www"
  }
  class { 'configurator': }
  class { 'wso2servers':
    server_name      => 'wso2am-1.9.0',
    module_name  => 'wso2am190'
  }

  Class['stratos_base'] -> Class['java'] -> Class['configurator']-> Class['python_agent'] -> Class['wso2servers']
}


# AppServer cartridge node
node /[0-9]{1,12}.*wso2as-521/ inherits base {

  class { 'java': }
  class { 'python_agent':
    docroot => "/var/www"
  }
  class { 'configurator': }
  class { 'wso2servers':
    server_name      => 'wso2as-5.2.1',
    module_name  => 'wso2as521'
  }

  Class['stratos_base'] -> Class['java'] -> Class['configurator']-> Class['python_agent'] -> Class['wso2servers']
}
