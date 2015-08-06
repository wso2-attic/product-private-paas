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

from plugins.contracts import ICartridgeAgentPlugin
from modules.util.log import LogFactory
import entity
import subprocess
import os


class WSO2ESBStartupHandler(ICartridgeAgentPlugin):
    log = LogFactory().get_log(__name__)

    # class constants
    CONST_PORT_MAPPING_MGT_CONSOLE = "mgt-console"
    CONST_PORT_MAPPING_PT_HTTP_TRANSPORT = "pt-http"
    CONST_PORT_MAPPING_PT_HTTPS_TRANSPORT = "pt-https"
    CONST_PROTOCOL_HTTP = "http"
    CONST_PROTOCOL_HTTPS = "https"
    CONST_PORT_MAPPINGS = "PORT_MAPPINGS"
    CONST_APPLICATION_ID = "APPLICATION_ID"
    CONST_MB_IP = "MB_IP"
    CONST_SERVICE_NAME = "SERVICE_NAME"
    CONST_ESB_WORKER = "esbworker"

    # list of environment variables exported by the plugin
    ENV_CONFIG_PARAM_MB_HOST = 'CONFIG_PARAM_MB_HOST'
    ENV_CONFIG_PARAM_CLUSTER_IDs = 'CONFIG_PARAM_CLUSTER_IDs'
    ENV_CONFIG_PARAM_HTTPS_PROXY_PORT = 'CONFIG_PARAM_HTTPS_PROXY_PORT'
    ENV_CONFIG_PARAM_PT_HTTP_PROXY_PORT = 'CONFIG_PARAM_PT_HTTP_PROXY_PORT'
    ENV_CONFIG_PARAM_PT_HTTPS_PROXY_PORT = 'CONFIG_PARAM_PT_HTTPS_PROXY_PORT'

    SERVICES = ["esbworker", "esbmanager"]

    def run_plugin(self, values):

        # read Port_mappings, Application_Id, MB_IP and Topology from 'values'
        port_mappings_str = values[self.CONST_PORT_MAPPINGS]
        app_id = values[self.CONST_APPLICATION_ID]
        mb_ip = values[self.CONST_MB_IP]
        service_type = values[self.CONST_SERVICE_NAME]

        # log above values
        WSO2ESBStartupHandler.log.info("Port Mappings: %s" % port_mappings_str)
        WSO2ESBStartupHandler.log.info("Application ID: %s" % app_id)
        WSO2ESBStartupHandler.log.info("MB IP: %s" % mb_ip)
        WSO2ESBStartupHandler.log.info("Service Name: %s" % service_type)

        # export Proxy Ports as Env. variables - used in catalina-server.xml
        self.set_proxy_ports(port_mappings_str)
        # export Cluster_Ids as Env. variables - used in for axis2.xml
        self.set_cluster_ids(app_id)

        # export mb_ip as Env.variable - used in jndi.properties
        if mb_ip is not None:
            self.set_as_env_variable(self.ENV_CONFIG_PARAM_MB_HOST, mb_ip)

        # start configurator
        WSO2ESBStartupHandler.info("Configuring WSO2 ESB...")
        config_command = "python ${CONFIGURATOR_HOME}/configurator.py"
        env_var = os.environ.copy()
        p = subprocess.Popen(config_command, env=env_var, shell=True)
        output, errors = p.communicate()
        WSO2ESBStartupHandler.info("WSO2 ESB configured successfully")

        # start server
        WSO2ESBStartupHandler.info("Starting WSO2 ESB...")
        if service_type == self.CONST_ESB_WORKER:
            start_command = "exec ${CARBON_HOME}/bin/wso2server.sh -DworkerNode=true start"
        else:
            start_command = "exec ${CARBON_HOME}/bin/wso2server.sh -Dsetup start"
        env_var = os.environ.copy()
        p = subprocess.Popen(start_command, env=env_var, shell=True)
        output, errors = p.communicate()
        WSO2ESBStartupHandler.debug("WSO2 ESB started successfully")

    def set_cluster_ids(self, app_id):
        cluster_ids = []

        for service in self.SERVICES:
            cluster_id_of_service = self.read_cluster_id_of_service(service, app_id)
            if cluster_id_of_service is not None:
                cluster_ids.append(cluster_id_of_service)

        # If clusterIds are available, set them as environment variables
        if cluster_ids:
            cluster_ids_string = ",".join(cluster_ids)
            self.export_env_var(self.ENV_CONFIG_PARAM_CLUSTER_IDs, cluster_ids_string)

    def read_cluster_id_of_service(self, service, app_id):
        cluster_id = None
        topology = entity.TopologyContext.get_topology()
        service = topology.get_service(service)
        clusters = service.get_clusters()

        for cluster in clusters:
            if cluster.app_id == app_id:
                cluster.cluster_id == cluster_id
        return cluster_id

    # exports proxy ports as env. variables
    def set_proxy_ports(self, port_mappings_str):
        mgt_console_https_port = None
        pt_http_port = None
        pt_https_port = None

        # port mappings format: """NAME:mgt-console|PROTOCOL:https|PORT:4500|PROXY_PORT:9443|TYPE:NodePort;
        #                          NAME:pt-http|PROTOCOL:http|PORT:4501|PROXY_PORT:7280|TYPE:ClientIP;
        #                          NAME:pt-https|PROTOCOL:https|PORT:4502|PROXY_PORT:7243|TYPE:NodePort"""
        if port_mappings_str is not None:

            port_mappings_array = port_mappings_str.split(";")
            if port_mappings_array:

                for port_mapping in port_mappings_array:
                    WSO2ESBStartupHandler.log.debug("port_mapping: %s" % port_mapping)
                    name_value_array = port_mapping.split("|")
                    name = name_value_array[0].split(":")[1]
                    protocol = name_value_array[1].split(":")[1]
                    proxy_port = name_value_array[3].split(":")[1]

                    # If PROXY_PORT is not set,
                    if proxy_port == 0:
                        proxy_port = name_value_array[2].split(":")[1]

                    if name == self.CONST_PORT_MAPPING_MGT_CONSOLE and protocol == self.CONST_PROTOCOL_HTTPS:
                        mgt_console_https_port = proxy_port
                    if name == self.CONST_PORT_MAPPING_PT_HTTP_TRANSPORT and protocol == self.CONST_PROTOCOL_HTTP:
                        pt_http_port = proxy_port
                    if name == self.CONST_PORT_MAPPING_PT_HTTP_TRANSPORT and protocol == self.CONST_PROTOCOL_HTTPS:
                        pt_https_port = proxy_port

        # export environment variables
        self.export_env_var(self.ENV_CONFIG_PARAM_HTTPS_PROXY_PORT, mgt_console_https_port)
        self.export_env_var(self.ENV_CONFIG_PARAM_PT_HTTP_PROXY_PORT, pt_http_port)
        self.export_env_var(self.ENV_CONFIG_PARAM_PT_HTTPS_PROXY_PORT, pt_https_port)

    def export_env_var(self, variable, value):
        if value is not None:
            os.environ[variable] = value
            WSO2ESBStartupHandler.log.info("Exported environment variable %s: %s" % (variable, value))
        else:
            WSO2ESBStartupHandler.log.warn("Could not export environment variable %s " % variable)
