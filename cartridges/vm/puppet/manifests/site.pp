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
# import private paas nodes definitions
import 'nodes/api.pp'
import 'nodes/appserver.pp'
import 'nodes/base.pp'
import 'nodes/bps.pp'
import 'nodes/esb.pp'
import 'nodes/haproxy.pp'
import 'nodes/is.pp'
import 'nodes/lb.pp'
import 'nodes/mysql.pp'
import 'nodes/nodejs.pp'
import 'nodes/php.pp'
import 'nodes/ruby.pp'
import 'nodes/tomcat.pp'
import 'nodes/wordpress.pp'
import 'nodes/cep.pp'

# import a single manifest file with node definitions
import 'nodes.pp'

import 'nodes/default.pp'
