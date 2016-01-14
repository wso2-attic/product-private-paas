#!/bin/sh
# ----------------------------------------------------------------------------
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
# ----------------------------------------------------------------------------
STRATOS_MANAGER_HOSTNAME=localhost
STRATOS_MANAGER_PORT=9443
USERNAME=admin
PASSWORD=admin

curl -k -u $USERNAME:$PASSWORD https://$STRATOS_MANAGER_HOSTNAME:$STRATOS_MANAGER_PORT/migration/admin/cartridge/list/subscribed/all -o subscription-data.json