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
#
host_ip="{{MACHINE_IP}}"
host_port=9443

zookeeper_type="zookeeper"
zookeeper_version="346"
zookeeper="${zookeeper_type}-${zookeeper_version}"
storm_type="storm"
cep_type="wso2cep"
application_name="wso2cep-storm"

set -e

echo "Undeploying application..."
curl -X POST -H "Content-Type: application/json" -k -v -u admin:admin https://${host_ip}:${host_port}/api/applications/${application_name}-aplication/undeploy

sleep 5

echo "Deleting application..."
curl -X DELETE -H "Content-Type: application/json" -k -v -u admin:admin https://${host_ip}:${host_port}/api/applications/${application_name}-aplication

sleep 1

echo "Removing application policies..."
curl -X DELETE -H "Content-Type: application/json" -k -v -u admin:admin https://${host_ip}:${host_port}/api/applicationPolicies/application-policy-1

echo "Removing deployment policies..."
curl -X DELETE -H "Content-Type: application/json" -k -v -u admin:admin https://${host_ip}:${host_port}/api/deploymentPolicies/deployment-policy-1

echo "Removing network partitions..."
curl -X DELETE -H "Content-Type: application/json" -k -v -u admin:admin https://${host_ip}:${host_port}/api/networkPartitions/network-partition-1

echo "Removing autoscale policies..."
curl -X DELETE -H "Content-Type: application/json" -k -v -u admin:admin https://${host_ip}:${host_port}/api/autoscalingPolicies/autoscaling-policy-1

echo "Removing cartridge Groups ..."
curl -X DELETE -H "Content-Type: application/json" -k -v -u admin:admin https://${host_ip}:9443/api/cartridgeGroups/${application_name}-group

echo "Removing cartridges..."
curl -X DELETE -H "Content-Type: application/json" -k -v -u admin:admin https://${host_ip}:${host_port}/api/cartridges/${zookeeper_type}
curl -X DELETE -H "Content-Type: application/json" -k -v -u admin:admin https://${host_ip}:${host_port}/api/cartridges/${storm_type}-nimbus
curl -X DELETE -H "Content-Type: application/json" -k -v -u admin:admin https://${host_ip}:${host_port}/api/cartridges/${storm_type}-ui
curl -X DELETE -H "Content-Type: application/json" -k -v -u admin:admin https://${host_ip}:${host_port}/api/cartridges/${storm_type}-supervisor
curl -X DELETE -H "Content-Type: application/json" -k -v -u admin:admin https://${host_ip}:${host_port}/api/cartridges/${cep_type}-manager
curl -X DELETE -H "Content-Type: application/json" -k -v -u admin:admin https://${host_ip}:${host_port}/api/cartridges/${cep_type}-worker
