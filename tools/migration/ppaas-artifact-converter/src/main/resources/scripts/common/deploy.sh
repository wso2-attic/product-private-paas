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
iaas=$1
host_ip="localhost"
host_port=9443
var_username="uname"
var_password="pword"
script_path=`pwd`
artifacts_path="${script_path}/../../artifacts"
iaas_cartridges_path="${script_path}/../../../../cartridges"
autoscaling_policies_path="${script_path}/../../../../autoscaling-policies"
network_partitions_path="${script_path}/../../../../network-partitions/${iaas}"
deployment_policies_path="${script_path}/../../../../deployment-policies"
application_policies_path="${script_path}/../../../../application-policies"
echo ${autoscaling_policies_path}/autoscaling-policy-1.json
echo "Adding autoscale policy..."
curl -X POST -H "Content-Type: application/json" -d "@${autoscaling_policies_path}/autoscaling-policy_name.json" -k -v -u ${var_username}:${var_password} https://${host_ip}:${host_port}/api/autoscalingPolicies
echo "Adding deployment policy..."
curl -X POST -H "Content-Type: application/json" -d "@${deployment_policies_path}/deployment-policy_name.json" -k -v -u ${var_username}:${var_password} https://${host_ip}:${host_port}/api/deploymentPolicies
echo "Adding cartridge..."
curl -X POST -H "Content-Type: application/json" -d "@${iaas_cartridges_path}/cartridge_name.json" -k -v -u ${var_username}:${var_password} https://${host_ip}:${host_port}/api/cartridges
sleep 1
echo "Adding application policy..."
curl -X POST -H "Content-Type: application/json" -d "@${application_policies_path}/application-policy-1.json" -k -v -u ${var_username}:${var_password} https://${host_ip}:${host_port}/api/applicationPolicies
sleep 1
echo "Adding application..."
curl -X POST -H "Content-Type: application/json" -d "@${artifacts_path}/application_name.json" -k -v -u ${var_username}:${var_password} https://${host_ip}:${host_port}/api/applications
sleep 1
echo "Deploying application..."
curl -X POST -H "Content-Type: application/json" -k -v -u ${var_username}:${var_password} https://${host_ip}:${host_port}/api/applications/cartridgeName/deploy/application-policy-1
