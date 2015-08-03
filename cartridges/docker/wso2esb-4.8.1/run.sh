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

# Start an ESB cluster with docker
memberId=1
startWkaMember() {
	name="wso2esb-${memberId}-wka"
	container_id=`docker run -e CONFIG_PARAM_CLUSTERING=true -d -P --name ${name} wso2/esb:4.8.1`
	memberId=$((memberId + 1))
	wka_member_ip=`docker inspect --format '{{ .NetworkSettings.IPAddress }}' ${container_id}`
	echo "ESB wka member started: [name] ${name} [ip] ${wka_member_ip} [container-id] ${container_id}"
	sleep 1
}

startMember() {
	name="wso2esb-${memberId}"
	container_id=`docker run -e CONFIG_PARAM_CLUSTERING=true -e CONFIG_PARAM_WKA_MEMBERS="[${wka_member_ip}:4100]" -d -P --name ${name} wso2/esb:4.8.1`
	memberId=$((memberId + 1))
	member_ip=`docker inspect --format '{{ .NetworkSettings.IPAddress }}' ${container_id}`
	echo "ESB member started: [name] ${name} [ip] ${member_ip} [container-id] ${container_id}"
	sleep 1
}

echo "Starting an ESB cluster with docker..."
startWkaMember
startMember
startMember
