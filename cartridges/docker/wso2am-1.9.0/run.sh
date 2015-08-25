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

# Start an AM cluster with docker
memberId=1
startDefaultAM() {
	name="wso2am-${memberId}-wka"
	container_id=`docker run -d -P --name ${name} wso2/am:1.9.0`
	memberId=$((memberId + 1))
	wka_member_ip=`docker inspect --format '{{ .NetworkSettings.IPAddress }}' ${container_id}`
	echo "AM wka member started: [name] ${name} [ip] ${wka_member_ip} [container-id] ${container_id}"
	sleep 1
}

startDefaultAMWithMysql() {
	name="wso2am-${memberId}-wka"
	env_values="-e CONFIG_PARAM_APIMGT_DB_URL=jdbc:mysql://172.17.42.1:3306/apimgtdb -e CONFIG_PARAM_DB_USER_NAME=root -e
	CONFIG_PARAM_DB_PASSWORD=root"
	container_id=`docker run ${env_values} -d -P --name ${name} wso2/am:1.9.0`
	memberId=$((memberId + 1))
	wka_member_ip=`docker inspect --format '{{ .NetworkSettings.IPAddress }}' ${container_id}`
	echo "AM wka member started: [name] ${name} [ip] ${wka_member_ip} [container-id] ${container_id}"
	sleep 1
}

echo "Starting an AM cluster with docker..."

startDefaultAM
#startDefaultAMWithMysql


