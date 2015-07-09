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

# delete an AM cluster with docker
memberId=1
deleteSupervisor() {
	name="apache-storm-${memberId}-supervisor"
	docker rm ${name}
	echo "Apache storm supervisor deleted"
	sleep 1
}

# delete an AM cluster with docker
memberId=1
deleteNimbus() {
	name="apache-storm-${memberId}-nimbus"
	docker rm ${name}
	echo "Apache storm nimbus deleted"
	sleep 1
}

# delete an CEP cluster with docker
memberId=1
deleteZookeeper() {
	name="apache-zookeeper-${memberId}"
	docker rm ${name}
	echo "Apache zookeeper deleted"
	sleep 1
}

deleteUI() {
	name="apache-storm-${memberId}-ui"
	docker rm ${name}
	echo "Apache storm ui deleted"
	sleep 1
}

echo "deleteping an Storm cluster with docker..."
deleteZookeeper
deleteNimbus
deleteSupervisor
deleteUI

