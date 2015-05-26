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
# Starts the service once the deployment is successful.

define esb::start ($target, $owner) {
  exec { "strating_${name}":
    user    => $owner,
    environment => "JAVA_HOME=/opt/java",
    path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
    unless  => "test -f ${target}/wso2carbon.lck",
    command => "touch ${target}/wso2carbon.lck; ${target}/bin/wso2server.sh > /dev/null 2>&1 &",
    creates => "${target}/repository/wso2carbon.log",
  }
}

