#!/bin/bash
# ------------------------------------------------------------------------
#
# Copyright 2005-2015 WSO2, Inc. (http://wso2.com)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License
#
# ------------------------------------------------------------------------

set -e
prgdir=`dirname "$0"`
script_path=`cd "$prgdir"; pwd`

wso2_ppaas_version="4.1.0-SNAPSHOT"
wso2_product_type="esb"
wso2_product_version="4.8.1"
wso2_product_template_module_path=`cd ${script_path}/../../templates-modules/wso2${wso2_product_type}-${wso2_product_version}/; pwd`
wso2_product_plugin_path=`cd ${script_path}/../../plugins/wso2${wso2_product_type}-${wso2_product_version}/; pwd`
clean=false

if [ "$1" = "clean" ]; then
   clean=true
fi

if ${clean} ; then
   echo "-----------------------------------"
   echo "Building" ${wso2_product_type^^} - ${wso2_product_version} "template module"
   echo "-----------------------------------"
   pushd ${wso2_product_template_module_path}
   mvn clean install
   cp -v target/wso2${wso2_product_type}-${wso2_product_version}-template-module-${wso2_ppaas_version}.zip ${script_path}/packages/
   popd

   echo "----------------------------------"
   echo "Copying" ${wso2_product_type^^} - ${wso2_product_version} "python plugins"
   echo "----------------------------------"
   rm ${script_path}/plugins/*
   pushd ${wso2_product_plugin_path}
   cp * ${script_path}/plugins
   popd
fi

echo "----------------------------------"
echo "Building" ${wso2_product_type^^} - ${wso2_product_version} "docker image"
echo "----------------------------------"
docker build -t wso2/${wso2_product_type}:${wso2_product_version} .

echo "WSO2" ${wso2_product_type^^} - ${wso2_product_version} "docker image built successfully."
