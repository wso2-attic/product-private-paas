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

# Stop the AM docker cluster
memberId=1
stopWkaMember() {
	name="wso2am-${memberId}-wka"
	docker stop ${name}
	memberId=$((memberId + 1))
	echo "AM wka member stopped: [name] ${name}"
	sleep 1
}

stopMember() {
	name="wso2am-${memberId}"
	docker stop ${name}
	memberId=$((memberId + 1))
	echo "AM member stopped: [name] ${name}"
	sleep 1
}

echo "Stopping the AM docker cluster..."
stopWkaMember
stopMember
stopMember