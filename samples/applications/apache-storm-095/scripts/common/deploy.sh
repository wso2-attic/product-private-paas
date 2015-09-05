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

prgdir=`dirname "$0"`
script_path=`cd "$prgdir"; pwd`
product_type="apache-storm"
product_version="095"
product="${product_type}-${product_version}"
artifacts_path=`cd "${script_path}/../../artifacts"; pwd`
zk_cartridges_path=`cd "${script_path}/../../../../cartridges/${iaas}/zookeeper-346"; pwd`
iaas_cartridges_path=`cd "${script_path}/../../../../cartridges/${iaas}/${product}"; pwd`
cartridges_groups_path=`cd "${script_path}/../../../../cartridge-groups/${product}"; pwd`
autoscaling_policies_path=`cd "${script_path}/../../../../autoscaling-policies"; pwd`
network_partitions_path=`cd "${script_path}/../../../../network-partitions/${iaas}"; pwd`
deployment_policies_path=`cd "${script_path}/../../../../deployment-policies"; pwd`
application_policies_path=`cd "${script_path}/../../../../application-policies"; pwd`

network_partition_id="network-partition-1"
deployment_policy_id="deployment-policy-1"
autoscaling_policy_id="autoscaling-policy-1"
application_policy_id="application-policy-1"

set -e

if [[ -z "${iaas}" ]]; then
    echo "Usage: deploy.sh [iaas]"
    exit
fi

echo ${autoscaling_policies_path}/${autoscaling_policy_id}.json
echo "Adding autoscale policy..."
curl -X POST -H "Content-Type: application/json" -d "@${autoscaling_policies_path}/${autoscaling_policy_id}.json" -k -v -u admin:admin https://${host_ip}:${host_port}/api/autoscalingPolicies

echo "Adding network partitions..."
curl -X POST -H "Content-Type: application/json" -d "@${network_partitions_path}/${network_partition_id}.json" -k -v -u admin:admin https://${host_ip}:${host_port}/api/networkPartitions

echo "Adding deployment policy..."
curl -X POST -H "Content-Type: application/json" -d "@${deployment_policies_path}/${deployment_policy_id}.json" -k -v -u admin:admin https://${host_ip}:${host_port}/api/deploymentPolicies

echo "Adding Zookeeper cartridge..."
curl -X POST -H "Content-Type: application/json" -d "@${zk_cartridges_path}/zookeeper-346.json" -k -v -u admin:admin https://${host_ip}:${host_port}/api/cartridges

echo "Adding ${product_type} - ${product_version} Nimbus cartridge..."
curl -X POST -H "Content-Type: application/json" -d "@${iaas_cartridges_path}/${product}-nimbus.json" -k -v -u admin:admin https://${host_ip}:${host_port}/api/cartridges

echo "Adding ${product_type} - ${product_version} UI cartridge..."
curl -X POST -H "Content-Type: application/json" -d "@${iaas_cartridges_path}/${product}-ui.json" -k -v -u admin:admin https://${host_ip}:${host_port}/api/cartridges

echo "Adding ${product_type} - ${product_version} Supervisor cartridge..."
curl -X POST -H "Content-Type: application/json" -d "@${iaas_cartridges_path}/${product}-supervisor.json" -k -v -u admin:admin https://${host_ip}:${host_port}/api/cartridges

echo "Adding ${product_type} - ${product_version} cartridge Group ..."
curl -X POST -H "Content-Type: application/json" -d "@${cartridges_groups_path}/${product}-group.json" -k -v -u admin:admin https://${host_ip}:${host_port}/api/cartridgeGroups

sleep 1
echo "Adding application policy..."
curl -X POST -H "Content-Type: application/json" -d "@${application_policies_path}/${application_policy_id}.json" -k -v -u admin:admin https://${host_ip}:${host_port}/api/applicationPolicies

sleep 1
echo "Adding ${product_type} - ${product_version} application..."
curl -X POST -H "Content-Type: application/json" -d "@${artifacts_path}/${product}-application.json" -k -v -u admin:admin https://${host_ip}:${host_port}/api/applications

sleep 1
echo "Deploying application..."
curl -X POST -H "Content-Type: application/json" -k -v -u admin:admin https://${host_ip}:${host_port}/api/applications/${product}/deploy/${application_policy_id}