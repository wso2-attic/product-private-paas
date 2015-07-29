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

# Delete the HBase docker cluster
memberId=1
deleteWkaMember() {
	name="hbase-${memberId}-wka"
	docker rm ${name}
	memberId=$((memberId + 1))
	echo "HBase wka member deleted: [name] ${name}"
	sleep 1
}

deleteMember() {
	name="hbase-${memberId}"
	docker rm ${name}
	memberId=$((memberId + 1))
	echo "HBase member deleted: [name] ${name}"
	sleep 1
}

echo "Deleting the HBase docker cluster..."

deleteWkaMember
deleteMember
# deleteMember