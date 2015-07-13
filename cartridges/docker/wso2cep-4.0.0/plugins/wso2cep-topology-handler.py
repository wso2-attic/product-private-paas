# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

import json
from plugins.contracts import ICartridgeAgentPlugin
from modules.util.log import LogFactory
import subprocess
import os


class CEPTopologyHandler(ICartridgeAgentPlugin):
    def run_plugin(self, values):
        log = LogFactory().get_log(__name__)
        # is_cep_mgr = os.environ.get("CONFIG_PARAM_MANAGER", False)
        log.info(values)
        is_cep_mgr = values["CONFIG_PARAM_MANAGER"]

        log.info("CEP Manager: %s" % is_cep_mgr)

        topology = values["TOPOLOGY_JSON"]
        topology_str = json.loads(topology)
        log.info("Topology: %s" % topology_str)

        if is_cep_mgr == 'true':
            log.info("Configuring CEP Manager Template module ..")
            log.info("Reading the Complete Topology in order to get the dependent ip addresses ...")

            zookeeper_member_default_private_ip = None
            nimbus_member_default_private_ip = None

            if topology_str is not None:
                # add service map
                for service_name in topology_str["serviceMap"]:
                    service_str = topology_str["serviceMap"][service_name]
                    if service_name == "zookeeper":
                        # add cluster map
                        for cluster_id in service_str["clusterIdClusterMap"]:
                            cluster_str = service_str["clusterIdClusterMap"][cluster_id]
                            # add member map
                            for member_id in cluster_str["memberMap"]:
                                member_str = cluster_str["memberMap"][member_id]
                                if zookeeper_member_default_private_ip is None:
                                    zookeeper_member_default_private_ip = member_str["defaultPrivateIP"]

                    if service_name == "nimbus":
                        # add cluster map
                        for cluster_id in service_str["clusterIdClusterMap"]:
                            cluster_str = service_str["clusterIdClusterMap"][cluster_id]
                            # add member map
                            for member_id in cluster_str["memberMap"]:
                                member_str = cluster_str["memberMap"][member_id]
                                if nimbus_member_default_private_ip is None:
                                    nimbus_member_default_private_ip = member_str["defaultPrivateIP"]

            if zookeeper_member_default_private_ip is not None:
                command = "sed -i \"s/^CONFIG_PARAM_ZOOKEEPER_HOST=.*/CONFIG_PARAM_ZOOKEEPER_HOST=%s/g\" %s" % (
                zookeeper_member_default_private_ip, "${CONFIGURATOR_HOME}/template-modules/wso2cep-4.0.0/module.ini")
                p = subprocess.Popen(command, shell=True)
                output, errors = p.communicate()
                log.info(
                    "Successfully updated zookeeper host: %s in WSO2 CEP template module" % zookeeper_member_default_private_ip)

            if nimbus_member_default_private_ip is not None:
                command = "sed -i \"s/^CONFIG_PARAM_NIMBUS_HOST=.*/CONFIG_PARAM_NIMBUS_HOST=%s/g\" %s" % (
                nimbus_member_default_private_ip, "${CONFIGURATOR_HOME}/template-modules/wso2cep-4.0.0/module.ini")
                p = subprocess.Popen(command, shell=True)
                output, errors = p.communicate()
                log.info(
                    "Successfully updated nimbus host: %s in WSO2 CEP template module" % nimbus_member_default_private_ip)

            # set local ip as CONFIG_PARAM_LOCAL_MEMBER_HOST
            get_local_ip_cmd = "awk 'NR==1 {print $1}' /etc/hosts"
            local_ip = subprocess.check_output(get_local_ip_cmd, shell=True)
            log.info("local IP from /etc/hosts : %s " % local_ip)

            if local_ip is not None:
                local_ip = local_ip[0:-1]
                command = "sed -i \"s/^CONFIG_PARAM_LOCAL_MEMBER_HOST=.*/CONFIG_PARAM_LOCAL_MEMBER_HOST=%s/g\" %s" % (
                local_ip, "${CONFIGURATOR_HOME}/template-modules/wso2cep-4.0.0/module.ini")
                p = subprocess.Popen(command, shell=True)
                output, errors = p.communicate()
                log.info("Successfully updated local member ip: %s in WSO2 CEP template module" % local_ip)

            # Set CONFIG_PARAM_MANAGER=true
            command = "sed -i \"s/^CONFIG_PARAM_MANAGER=.*/CONFIG_PARAM_MANAGER=%s/g\" %s" % (
            'true', "${CONFIGURATOR_HOME}/template-modules/wso2cep-4.0.0/module.ini")
            p = subprocess.Popen(command, shell=True)
            output, errors = p.communicate()
            log.info("Successfully updated config parameter manager: %s in WSO2 CEP template module" % 'true')

        cep_mgr_private_ip_list = []
        if topology_str is not None:
            # add service map
            for service_name in topology_str["serviceMap"]:
                service_str = topology_str["serviceMap"][service_name]
                if service_name == "cep-mgr":
                    # add cluster map
                    for cluster_id in service_str["clusterIdClusterMap"]:
                        cluster_str = service_str["clusterIdClusterMap"][cluster_id]
                        # add member map
                        for member_id in cluster_str["memberMap"]:
                            member_str = cluster_str["memberMap"][member_id]
                            if member_str["defaultPrivateIP"] is not None:
                                cep_mgr_private_ip_list.append(member_str["defaultPrivateIP"])

        if cep_mgr_private_ip_list:
            log.info("CEP Mgr IP List %s" % cep_mgr_private_ip_list)
            managers_string = '['
            for member_ip in cep_mgr_private_ip_list:
                if member_ip is not cep_mgr_private_ip_list[-1]:
                    managers_string += member_ip + ':8904' + ','
                else:
                    managers_string += member_ip + ':8904'
            managers_string += ']'

            command = "sed -i \"s/^CONFIG_PARAM_MANAGER_MEMBERS=.*/CONFIG_PARAM_MANAGER_MEMBERS=%s/g\" %s" % (
            managers_string, "${CONFIGURATOR_HOME}/template-modules/wso2cep-4.0.0/module.ini")
            p = subprocess.Popen(command, shell=True)
            output, errors = p.communicate()
            log.info("Successfully updated CEP Managers list: %s in WSO2 CEP template module" % managers_string)
        else:
            command = "sed -i \"s/^CONFIG_PARAM_MANAGER_MEMBERS=.*/#CONFIG_PARAM_MANAGER_MEMBERS=/g\" %s" % "${CONFIGURATOR_HOME}/template-modules/wso2cep-4.0.0/module.ini"
            p = subprocess.Popen(command, shell=True)
            output, errors = p.communicate()
            log.warn(
                "CEP Manager IPs are not found in topology, hence removing CONFIG_PARAM_MANAGER_MEMBERS property from module.ini")

            # configure server
        log.info("Configuring WSO2 CEP Manager...")
        config_command = "exec /opt/ppaas-configurator-4.1.0-SNAPSHOT/configurator.py"
        env_var = os.environ.copy()
        p = subprocess.Popen(config_command, env=env_var, shell=True)
        output, errors = p.communicate()
        log.info("WSO2 CEP configured successfully")

