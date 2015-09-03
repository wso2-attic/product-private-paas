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

# init script will export CARBON_HOME as an envrionment variable
# it will execute start-agent.sh file if it's started from private-paas
# and will start the configurator/carbon-server otherwise

local_ip=`awk 'NR==1 {print $1}' /etc/hosts`
server_path=/mnt/${local_ip}
mkdir -p $server_path
unzip /opt/wso2${WSO2_SERVER_TYPE}-${WSO2_SERVER_VERSION}.zip -d $server_path
rm /opt/wso2${WSO2_SERVER_TYPE}-${WSO2_SERVER_VERSION}.zip
export CARBON_HOME="$server_path/wso2${WSO2_SERVER_TYPE}-${WSO2_SERVER_VERSION}"
echo "CARBON_HOME=${CARBON_HOME}" >> /etc/environment
echo "CARBON_HOME is set to ${CARBON_HOME}"

if [ "${START_CMD}" = "PCA" ]; then
    echo "Starting python cartridge agent..."
	/usr/local/bin/start-agent.sh
	echo "Python cartridge agent started successfully"
else
    echo "Configuring wso2 ${WSO2_SERVER_TYPE} ..."
    echo "Environment variables:"
    printenv
    pushd ${CONFIGURATOR_HOME}
    python configurator.py
    popd
    echo "wso2 ${WSO2_SERVER_TYPE} configured successfully"

    echo "Starting wso2 ${WSO2_SERVER_TYPE}..."
    ${CARBON_HOME}/bin/wso2server.sh
    echo "wso2 ${WSO2_SERVER_TYPE} - ${WSO2_SERVER_VERSION} started successfully"
fi