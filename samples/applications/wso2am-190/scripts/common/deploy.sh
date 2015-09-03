#!/bin/bash
# --------------------------------------------------------------
#  Copyright 2005-2015 WSO2, Inc. http://www.wso2.org
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
# --------------------------------------------------------------
iaas=$1
host_ip="localhost"
host_port=9443

prgdir=`dirname "$0"`
script_path=`cd "$prgdir"; pwd`
product_type="am"
product_version="190"
product="wso2${product_type}-${product_version}"
artifacts_path=`cd "${script_path}/../../artifacts"; pwd`
iaas_cartridges_path=`cd "${script_path}/../../../../cartridges/${iaas}/${product}"; pwd`
cartridges_groups_path=`cd "${script_path}/../../../../cartridge-groups/${product}"; pwd`
autoscaling_policies_path=`cd "${script_path}/../../../../autoscaling-policies"; pwd`
network_partitions_path=`cd "${script_path}/../../../../network-partitions/${iaas}"; pwd`
deployment_policies_path=`cd "${script_path}/../../../../deployment-policies/${iaas}"; pwd`
application_policies_path=`cd "${script_path}/../../../../application-policies/${iaas}"; pwd`

network_partition_id="network-partition-${iaas}"
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

echo "Adding WSO2 ${product_type} - ${product_version} Gateway Manager cartridge..."
curl -X POST -H "Content-Type: application/json" -d "@${iaas_cartridges_path}/${product}-gw-manager.json" -k -v -u admin:admin https://${host_ip}:${host_port}/api/cartridges

echo "Adding WSO2 ${product_type} - ${product_version} Gateway Worker cartridge..."
curl -X POST -H "Content-Type: application/json" -d "@${iaas_cartridges_path}/${product}-gw-worker.json" -k -v -u admin:admin https://${host_ip}:${host_port}/api/cartridges

echo "Adding WSO2 ${product_type} - ${product_version} Keymanager cartridge..."
curl -X POST -H "Content-Type: application/json" -d "@${iaas_cartridges_path}/${product}-km.json" -k -v -u admin:admin https://${host_ip}:${host_port}/api/cartridges

echo "Adding WSO2 ${product_type} - ${product_version} Publisher cartridge..."
curl -X POST -H "Content-Type: application/json" -d "@${iaas_cartridges_path}/${product}-pub.json" -k -v -u admin:admin https://${host_ip}:${host_port}/api/cartridges

echo "Adding WSO2 ${product_type} - ${product_version} Store cartridge..."
curl -X POST -H "Content-Type: application/json" -d "@${iaas_cartridges_path}/${product}-store.json" -k -v -u admin:admin https://${host_ip}:${host_port}/api/cartridges

echo "Adding WSO2 ${product_type} - ${product_version} cartridge Group 1 ..."
curl -X POST -H "Content-Type: application/json" -d "@${cartridges_groups_path}/${product}-group1.json" -k -v -u admin:admin https://${host_ip}:${host_port}/api/cartridgeGroups

echo "Adding WSO2 ${product_type} - ${product_version} cartridge Group 2 ..."
curl -X POST -H "Content-Type: application/json" -d "@${cartridges_groups_path}/${product}-group2.json" -k -v -u admin:admin https://${host_ip}:${host_port}/api/cartridgeGroups

sleep 1
echo "Adding application policy..."
curl -X POST -H "Content-Type: application/json" -d "@${application_policies_path}/${application_policy_id}.json" -k -v -u admin:admin https://${host_ip}:${host_port}/api/applicationPolicies

sleep 1
echo "Adding WSO2 ${product_type} - ${product_version} application..."
curl -X POST -H "Content-Type: application/json" -d "@${artifacts_path}/${product}-application.json" -k -v -u admin:admin https://${host_ip}:${host_port}/api/applications

sleep 1
echo "Deploying application..."
curl -X POST -H "Content-Type: application/json" -k -v -u admin:admin https://${host_ip}:${host_port}/api/applications/${product}-application/deploy/${application_policy_id}
