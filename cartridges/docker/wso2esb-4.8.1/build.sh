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
esb_template_module_path=`cd ${script_path}/../../../cartridges/templates-modules/wso2esb-4.8.1/; pwd`
clean=false
if [ "$1" = "clean" ]; then
   clean=true
fi

if ${clean} ; then
   echo "----------------------------------"
   echo "Building ESB template module"
   echo "----------------------------------"
   pushd ${esb_template_module_path}
   mvn clean install
   cp -v target/wso2esb-4.8.1-template-module-${project_version}.zip ${script_path}/packages/
   popd
fi

echo "----------------------------------"
echo "Building ESB docker image"
echo "----------------------------------"
docker build -t wso2/esb:4.8.1 .
echo "ESB docker image built successfully"
