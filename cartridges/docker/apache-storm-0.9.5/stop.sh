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

# stop an AM cluster with docker
memberId=1
stopSupervisor() {
	name="apache-storm-${memberId}-supervisor"
	docker stop ${name}
	echo "Apache storm supervisor stopped"
	sleep 1
}

# stop an AM cluster with docker
memberId=1
stopNimbus() {
	name="apache-storm-${memberId}-nimbus"
	docker stop ${name}
	echo "Apache storm nimbus stopped"
	sleep 1
}

# stop an CEP cluster with docker
memberId=1
stopZookeeper() {
	name="apache-zookeeper-${memberId}"
	docker stop ${name}
	echo "Apache zookeeper stopped"
	sleep 1
}

stopUI() {
	name="apache-storm-${memberId}-ui"
	docker stop ${name}
	echo "Apache storm ui stopped"
	sleep 1
}

echo "Stopping an Storm cluster with docker..."
stopZookeeper
stopNimbus
stopSupervisor
stopUI

