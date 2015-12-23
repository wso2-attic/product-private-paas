#!/bin/bash
# Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
#
# WSO2 Inc. licenses this file to you under the Apache License,
# Version 2.0 (the "License"); you may not use this file except
# in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
echo "-------------------------------------------------------"
echo "Starting PPaaS Artifact Migration Tool (4.0.0 to 4.0.1)"
echo "-------------------------------------------------------"
script_path="$( cd -P "$( dirname "$SOURCE" )" && pwd )/`dirname $0`"
lib_path=${script_path}/../lib/
class_path=`echo ${lib_path}/*.jar | tr ' ' ':'`

properties="-Dlog4j.configuration=file://${script_path}/../conf/log4j.properties
            -Dlog4jfile=${script_path}/../log/ppaas-artifact-converter-log.log
            -Dconfig=${script_path}/../conf/config.properties"

java -cp "${class_path}" ${properties} org.wso2.ppaas.tools.artifactmigration.Main$*