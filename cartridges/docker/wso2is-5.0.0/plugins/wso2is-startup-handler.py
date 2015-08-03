# ------------------------------------------------------------------------
#
# Copyright 2005-2015 WSO2, Inc. (http://wso2.com)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License
#
# ------------------------------------------------------------------------

import mdsclient
from plugins.contracts import ICartridgeAgentPlugin
from xml.dom.minidom import parse
import socket
from modules.util.log import LogFactory
import time
import subprocess
import os

class WSO2ISStartupHandler(ICartridgeAgentPlugin):

    def run_plugin(self, values):
        log = LogFactory().get_log(__name__)

        log.info("Reading port mappings...")
        port_mappings_str = values["PORT_MAPPINGS"]

        mgt_console_https_port = None
        pt_http_port = None
        pt_https_port = None

        # port mappings format: """NAME:mgt-console|PROTOCOL:https|PORT:4500|PROXY_PORT:9443"""

        log.info("Port mappings: %s" % port_mappings_str)
        if port_mappings_str is not None:

            port_mappings_array = port_mappings_str.split(";")
            if port_mappings_array:

                for port_mapping in port_mappings_array:
                    log.debug("port_mapping: %s" % port_mapping)
                    name_value_array = port_mapping.split("|")
                    name = name_value_array[0].split(":")[1]
                    protocol = name_value_array[1].split(":")[1]
                    port = name_value_array[2].split(":")[1]
                    if name == "mgt-console" and protocol == "https":
                        mgt_console_https_port = port
                    if name == "pt-http" and protocol == "http":
                        pt_http_port = port
                    if name == "pt-https" and protocol == "https":
                        pt_https_port = port

        log.info("Kubernetes service management console https port: %s" % mgt_console_https_port)

        if mgt_console_https_port is not None:
            command = "sed -i \"s/^#CONFIG_PARAM_HTTPS_PROXY_PORT = .*/CONFIG_PARAM_HTTPS_PROXY_PORT = %s/g\" %s" % (mgt_console_https_port, "${CONFIGURATOR_HOME}/templates/wso2is-5.0.0/configs.ini")
            p = subprocess.Popen(command, shell=True)
            output, errors = p.communicate()
            log.info("Successfully updated management console https proxy port: %s in IS template module" % mgt_console_https_port)

        # configure server
        log.info("Configuring WSO2 IS...")
        config_command = "exec /opt/ppaas-configurator-4.1.0-SNAPSHOT/configurator.py"
        env_var = os.environ.copy()
        p = subprocess.Popen(config_command, env=env_var, shell=True)
        output, errors = p.communicate()
        log.info("WSO2 IS configured successfully")

        # start server
        log.info("Starting WSO2 IS...")

        start_command = "exec ${CARBON_HOME}/bin/wso2server.sh start"
        env_var = os.environ.copy()
        p = subprocess.Popen(start_command, env=env_var, shell=True)
        output, errors = p.communicate()
        log.debug("WSO2 IS started successfully")
