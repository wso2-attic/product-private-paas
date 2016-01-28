#!/bin/bash
# --------------------------------------------------------------

# Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
#
# WSO2 Inc. licenses this file to you under the Apache License,
# Version 2.0 (the "License"); you may not use this file except
# in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
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
var_base_url=base-url
var_username="uname"
var_password="pword"
echo "Undeploying application..."
curl -X POST -H "Content-Type: application/json" -k -v -u ${var_username}:${var_password} ${var_base_url}api/applications/application_name/undeploy
sleep 10
echo "Deleting application..."
curl -X DELETE -H "Content-Type: application/json" -k -v -u ${var_username}:${var_password} ${var_base_url}api/applications/application_name
echo "Removing cartridges..."
curl -X DELETE -H "Content-Type: application/json" -k -v -u ${var_username}:${var_password} ${var_base_url}api/cartridges/cartridge_type
echo "Removing autoscale policies..."
curl -X DELETE -H "Content-Type: application/json" -k -v -u ${var_username}:${var_password} ${var_base_url}api/autoscalingPolicies/autoscaling-policy_name
echo "Removing deployment policies..."
curl -X DELETE -H "Content-Type: application/json" -k -v -u ${var_username}:${var_password} ${var_base_url}api/deploymentPolicies/deployment-policy_name
echo "Removing application policies..."
curl -X DELETE -H "Content-Type: application/json" -k -v -u ${var_username}:${var_password} ${var_base_url}api/applicationPolicies/application-policy_name
echo "Removing network partitions..."

