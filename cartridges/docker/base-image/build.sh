#!/bin/bash
# --------------------------------------------------------------
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#
# --------------------------------------------------------------

set -e
prgdir=`dirname "$0"`
script_path=`cd "$prgdir"; pwd`

project_version="4.1.0-SNAPSHOT"
configurator_path=`cd ${script_path}/../../../components/org.wso2.ppaas.configurator/; pwd`
esb_template_module_path=`cd ${script_path}/../../../cartridges/templates-modules/wso2esb-4.8.1/; pwd`
clean=false
if [ "$1" = "clean" ]; then
   clean=true
fi

if ${clean} ; then
   echo "----------------------------------"
   echo "Building configurator"
   echo "----------------------------------"
   pushd ${configurator_path}
   mvn clean install                                                                                      
   cp -v target/ppaas-configurator-${project_version}.zip ${script_path}/packages/
   popd
fi

echo "----------------------------------"
echo "Building base docker image"
echo "----------------------------------"
docker build -t wso2/base-image:4.1.0 .
echo "Base docker image built successfully"
