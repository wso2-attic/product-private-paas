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

import mdsclient
import json
from plugins.contracts import ICartridgeAgentPlugin
from xml.dom.minidom import parse
import socket
from modules.util.log import LogFactory
import time
import subprocess
import os

class StormTopologyHandler(ICartridgeAgentPlugin):

    def run_plugin(self, values):
        log = LogFactory().get_log(__name__)

        log.info("Reading the Complete Topology in order to get the dependent ip addresses ...")

        topology_str = values["TOPOLOGY_JSON"]
        log.info("Port mappings: %s" % topology_str)


        if topology_str is not None:
            # add service map
            for service_name in topology_str["serviceMap"]:
                service_str = topology_str["serviceMap"][service_name]
                if service_name == "zookeeper" :
                    # add cluster map
                    for cluster_id in service_str["clusterIdClusterMap"]:
                        cluster_str = service_str["clusterIdClusterMap"][cluster_id]
                        # add member map
                        for member_id in cluster_str["memberMap"]:
                            member_str = cluster_str["memberMap"][member_id]
                            zookeeper_member_default_private_ip = member_str["defaultPrivateIP"]
                            break
                    break

        if zookeeper_member_default_private_ip is not None:
            command = "sed -i \"s/^#CONFIG_PARAM_HTTPS_PROXY_PORT = .*/CONFIG_PARAM_HTTPS_PROXY_PORT = %s/g\" %s" % (zookeeper_member_default_private_ip, "${CONFIGURATOR_HOME}/template-modules/wso2am-1.8.0/module.ini")
            p = subprocess.Popen(command, shell=True)
            output, errors = p.communicate()
            log.info("Successfully updated management console https proxy port: %s in AM template module" % mgt_console_https_port)

        if pt_http_port is not None:
            command = "sed -i \"s/^#CONFIG_PARAM_PT_HTTP_PROXY_PORT = .*/CONFIG_PARAM_PT_HTTP_PROXY_PORT = %s/g\" %s" % (pt_http_port, "${CONFIGURATOR_HOME}/template-modules/wso2am-1.8.0/module.ini")
            p = subprocess.Popen(command, shell=True)
            output, errors = p.communicate()
            log.info("Successfully updated pass-through http proxy port: %s in AM template module" % pt_http_port)

        if pt_https_port is not None:
            command = "sed -i \"s/^#CONFIG_PARAM_PT_HTTPS_PROXY_PORT = .*/CONFIG_PARAM_PT_HTTPS_PROXY_PORT = %s/g\" %s" % (pt_https_port, "${CONFIGURATOR_HOME}/template-modules/wso2am-1.8.0/module.ini")
            p = subprocess.Popen(command, shell=True)
            output, errors = p.communicate()
            log.info("Successfully updated pass-through https proxy port: %s in AM template module" % pt_https_port)

        # configure server
        log.info("Configuring WSO2 AM...")
        config_command = "exec /opt/ppaas-configurator-4.1.0-SNAPSHOT/configurator.py"
        env_var = os.environ.copy()
        p = subprocess.Popen(config_command, env=env_var, shell=True)
        output, errors = p.communicate()
        log.info("WSO2 AM configured successfully")

        # start server
        log.info("Starting WSO2 AM...")

        start_command = "exec ${CARBON_HOME}/bin/wso2server.sh start"
        env_var = os.environ.copy()
        p = subprocess.Popen(start_command, env=env_var, shell=True)
        output, errors = p.communicate()
        log.debug("WSO2 AM started successfully")
