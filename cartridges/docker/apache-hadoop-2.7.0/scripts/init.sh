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

# run script sets the configurable parameters for the cartridge agent in agent.conf and
# starts the cartridge agent process.

if [ ${START_CMD} = 'PCA' ]; then
    echo "Starting python cartridge agent..."
	/usr/local/bin/start-agent.sh
	echo "Python cartridge agent started successfully"
else
    echo "Configuring Hadoop..."
    echo ${CLUSTER}
    echo "clustering true or not"

    if [ ${CLUSTER} = 'true' ]; then
	    sleep 3
        ip=`ip addr | grep 'state UP' -A2 | tail -n1 | awk '{print $2}' | cut -f1  -d'/'`
        export CONFIG_PARAM_HADOOP_MASTER=${ip}
	    echo ${ip}
	    echo "Environment variables:"
        printenv
        pushd ${CONFIGURATOR_HOME}
        python configurator.py
        popd
        echo "Hadoop Namenode configured successfully"
    
        echo "Starting Hadoop Namenode..."
        ${HADOOP_HOME}/bin/hadoop namenode -format
        ${HADOOP_HOME}/sbin/start-all.sh
	    echo "Hadoop Namenode started successfully"
    else

    echo "Environment variables:"
    printenv
    pushd ${CONFIGURATOR_HOME}
    python configurator.py
    popd
    echo "Hadoop Datanode configured successfully"
    
    echo "Starting Hadoop..."
    ${HADOOP_HOME}/sbin/hadoop-daemon.sh start datanode
    echo "Hadoop Datanode started successfully"

    fi    
fi
