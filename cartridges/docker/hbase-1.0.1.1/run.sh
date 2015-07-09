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

# Start an HBase cluster with docker
memberId=1
startWkaMember() {
	name="hbase-${memberId}"
	container_id=`docker run -e CONFIG_PARAM_HDFS_HOST=localhost -e CONFIG_PARAM_ZOOKEEPER_HOST=localhost -e  CLUSTER=true -d -p 16010:16010 -p 16000:16000 --name ${name} wso2/hbase:1.0.1.1`
	memberId=$((memberId + 1))
	wka_member_ip=`docker inspect --format '{{ .NetworkSettings.IPAddress }}' ${container_id}`
	wka_member_container_id=container_id
	echo "Hadoop Master started: [name] ${name} [ip] ${wka_member_ip} [container-id] ${container_id}"
	sleep 1
}

startMember() {
	name="hbase-${memberId}"
	container_id=`docker run -e CONFIG_PARAM_HDFS_HOST=localhost -e CONFIG_PARAM_ZOOKEEPER_HOST="${wka_member_ip}" -e CONFIG_PARAM_HBASE_MASTER="${wka_member_ip}"  -e CONFIG_PARAM_HBASE_MASTER_HOSTNAME="${wka_member_container_id}" -d -P --name ${name} wso2/hbase:1.0.1.1`
	memberId=$((memberId + 1))
	member_ip=`docker inspect --format '{{ .NetworkSettings.IPAddress }}' ${container_id}`
	echo "Hadoop datanode started: [name] ${name} [ip] ${member_ip} [container-id] ${container_id}"
	sleep 1
}

echo "Starting an Hadoop cluster with docker..."
startWkaMember
startMember
# startMember
# startMember
