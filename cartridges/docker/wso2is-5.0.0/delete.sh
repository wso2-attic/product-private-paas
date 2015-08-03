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

# Delete the IS docker cluster
memberId=1
deleteWkaMember() {
	name="wso2is-${memberId}-wka"
	docker rm ${name}
	memberId=$((memberId + 1))
	echo "IS wka member deleted: [name] ${name}"
	sleep 1
}

deleteMember() {
	name="wso2is-${memberId}"
	docker rm ${name}
	memberId=$((memberId + 1))
	echo "IS member deleted: [name] ${name}"
	sleep 1
}

echo "Deleting the IS docker cluster..."

deleteWkaMember
deleteMember
deleteMember