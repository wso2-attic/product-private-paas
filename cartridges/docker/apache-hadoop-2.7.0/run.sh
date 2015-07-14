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

# Start an Hadoop cluster with docker
memberId=1
startWkaMember() {
	name="hadoop-${memberId}"
	container_id=`docker run -e CONFIG_PARAM_HADOOP_MASTER=localhost -e CLUSTER=true -d -P --name ${name} wso2/hadoop:2.7.0`
	memberId=$((memberId + 1))
	wka_member_ip=`docker inspect --format '{{ .NetworkSettings.IPAddress }}' ${container_id}`
	echo "Hadoop Master started: [name] ${name} [ip] ${wka_member_ip} [container-id] ${container_id}"
	sleep 1
}

startMember() {
	name="hadoop-${memberId}"
	container_id=`docker run -e CONFIG_PARAM_HADOOP_MASTER="${wka_member_ip}" -d -P --name ${name} wso2/hadoop:2.7.0`
	memberId=$((memberId + 1))
	member_ip=`docker inspect --format '{{ .NetworkSettings.IPAddress }}' ${container_id}`
	echo "Hadoop Datanode started: [name] ${name} [ip] ${member_ip} [container-id] ${container_id}"
	sleep 1
}

echo "Starting an Hadoop cluster with docker..."
startWkaMember
startMember
#startMember
#startMember
