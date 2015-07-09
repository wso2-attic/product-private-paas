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

# Start an AM cluster with docker
memberId=1
startSupervisor() {
	name="apache-storm-${memberId}-supervisor"
	container_id=`docker run -e STORM_TYPE=supervisor -e ZOOKEEPER_HOSTNAME=192.168.59.3 -e NIMBUS_HOSTNAME=${nimbus_member_ip} -d -P --name ${name} apache/storm-supervisor:0.9.5`
	supervisor_member_ip=`docker inspect --format '{{ .NetworkSettings.IPAddress }}' ${container_id}`
	echo "Apache storm supervisor started: [name] ${name} [ip] ${supervisor_member_ip} [container-id] ${container_id}"
	sleep 1
}

# Start an AM cluster with docker
memberId=1
startNimbus() {
	name="apache-storm-${memberId}-nimbus"
	container_id=`docker run -e STORM_TYPE=nimbus -e ZOOKEEPER_HOSTNAME=${zookeeper_member_ip} -d -P --name ${name} apache/storm-supervisor:0.9.5`
	nimbus_member_ip=`docker inspect --format '{{ .NetworkSettings.IPAddress }}' ${container_id}`
	echo "Apache storm nimbus started: [name] ${name} [ip] ${nimbus_member_ip} [container-id] ${container_id}"
	sleep 1
}

# Start an CEP cluster with docker
memberId=1
startZookeeper() {
	name="apache-zookeeper-${memberId}"
	container_id=`docker run -d -P --name ${name} apache/zookeeper:3.4.6`
	zookeeper_member_ip=`docker inspect --format '{{ .NetworkSettings.IPAddress }}' ${container_id}`
	echo "Zookeeper started: [name] ${name} [ip] ${zookeeper_member_ip} [container-id] ${container_id}"
	sleep 1
}

startUI() {
	name="wso2cep-${memberId}"
	name="apache-storm-${memberId}-ui"
	container_id=`docker run -e STORM_TYPE=supervisor -e ZOOKEEPER_HOSTNAME=192.168.59.3 -e NIMBUS_HOSTNAME=${nimbus_member_ip} -d -P --name ${name} apache/storm-supervisor:0.9.5`
	ui_member_ip=`docker inspect --format '{{ .NetworkSettings.IPAddress }}' ${container_id}`
	echo "Apache storm UI started: [name] ${name} [ip] ${ui_member_ip} [container-id] ${container_id}"
	sleep 1
}

echo "Starting an Storm cluster with docker..."
startZookeeper
startNimbus
startSupervisor
startUI

