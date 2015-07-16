#!/bin/bash
# --------------------------------------------------------------
#
#  Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#  http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#
# --------------------------------------------------------------

# Start a DAS instance with docker
memberId=1
CONFIG_PARAM_ZK_HOST=10.100.7.80
memberId=1
startWkaMember () {
	name="wso2das-$1-${memberId}"
    container_id=`docker run -e CONFIG_PARAM_ZK_HOST=10.100.7.80 -e CONFIG_PARAM_PROFILE=$1 -e CONFIG_PARAM_CLUSTERING=true -d -P --name ${name} wso2/das:3.0.0-SNAPSHOT`
    wka_member_ip=`docker inspect --format '{{ .NetworkSettings.IPAddress }}' ${container_id}`
    echo "WSO2 DAS $1 started: [name] ${name} [ip] ${wka_member_ip} [container-id] ${container_id}"
    memberId=$((memberId + 1))
}

startMember() {
	name="wso2das-$1-${memberId}"
    container_id=`docker run -e CONFIG_PARAM_ZK_HOST=10.100.7.80 -e CONFIG_PARAM_CLUSTERING=true -e CONFIG_PARAM_WKA_MEMBERS="[${wka_member_ip}:4100]" -e CONFIG_PARAM_PROFILE=$1 -d -P --name ${name} wso2/das:3.0.0-SNAPSHOT`
    member_ip=`docker inspect --format '{{ .NetworkSettings.IPAddress }}' ${container_id}`
    echo "WSO2 DAS $1 started: [name] ${name} [ip] ${member_ip} [container-id] ${container_id}"
    memberId=$((memberId + 1))
}

startDeafultPack() {
	name="wso2das-$1-${memberId}"
    container_id=`docker run -e CONFIG_PARAM_PROFILE=default -d -P --name ${name} wso2/das:3.0.0-SNAPSHOT`
    member_ip==`docker inspect --format '{{ .NetworkSettings.IPAddress }}' ${container_id}`
    echo "WSO2 DAS $1 started: [name] ${name} [ip] ${member_ip} [container-id] ${container_id}"
    memberId=$((memberId + 1))
}


if [ "$#" -eq 1 ]; then
    startWkaMember $1
    startMember $1
else
    startDeafultPack
fi


