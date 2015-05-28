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
from plugins.contracts import ICartridgeAgentPlugin
from xml.dom.minidom import parse
import socket
from modules.util.log import LogFactory
import time
import subprocess
import os

class WSO2ESBStartupHandler(ICartridgeAgentPlugin):

    def run_plugin(self, values):
        log = LogFactory().get_log(__name__)

        # read kubernetes service https port
        log.info("Reading port mappings...")
        port_mappings_str = values["PORT_MAPPINGS"]
        https_port = None

        # port mappings format: "PROTOCOL:http|PORT:80|PROXY_PORT:8280,PROTOCOL:https|PORT:773|PROXY_PORT:9443"
        log.info("Port mappings: %s" % port_mappings_str)
        if port_mappings_str is not None:

            port_mappings_array = port_mappings_str.split(",")
            if port_mappings_array:

                for port_mapping in port_mappings_array:
                    log.debug("port_mapping: %s" % port_mapping)
                    name_value_array = port_mapping.split("|")
                    log.debug("name_value_array: %s" % name_value_array)
                    protocol = name_value_array[0].split(":")[1]
                    port = name_value_array[1].split(":")[1]
                    if protocol == "https":
                        https_port = port

        log.info("Kubernetes service https port: %s" % https_port)
        if https_port is not None:
            catalina_replace_command = "sed -i \"s/^STRATOS_HTTPS_PROXY_PORT = .*/STRATOS_HTTPS_PROXY_PORT = %s/g\" %s" % (https_port, "${CONFIGURATOR_HOME}/templates/wso2esb-4.8.1/configs.ini")
            p = subprocess.Popen(catalina_replace_command, shell=True)
            output, errors = p.communicate()
            log.info("Successfully updated https proxy port in ESB template module")

        # configure server
        log.info("Configuring WSO2 ESB...")
        config_command = "exec /opt/wso2configurator-4.1.0/configurator.py"
        env_var = os.environ.copy()
        p = subprocess.Popen(config_command, env=env_var, shell=True)
        output, errors = p.communicate()
        log.info("WSO2 ESB configured successfully")

        # start server
        log.info("Starting WSO2 ESB...")

        start_command = "exec ${CARBON_HOME}/bin/wso2server.sh start"
        env_var = os.environ.copy()
        p = subprocess.Popen(start_command, env=env_var, shell=True)
        output, errors = p.communicate()
        log.debug("WSO2 ESB started successfully")
