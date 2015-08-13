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
from modules.topology.topologycontext import TopologyContext
import subprocess
import os
import psutil


class WSO2ESBStartupHandler(ICartridgeAgentPlugin):
    """
    Configures and starts configurator, carbon server
    """
    log = LogFactory().get_log(__name__)

    # class constants
    CONST_PORT_MAPPINGS = "PORT_MAPPINGS"
    CONST_APPLICATION_ID = "APPLICATION_ID"
    CONST_MB_IP = "MB_IP"
    CONST_SERVICE_NAME = "SERVICE_NAME"
    CONST_CLUSTER_ID = "CLUSTER_ID"
    CONST_WORKER = "worker"
    CONST_MANAGER = "manager"
    CONST_MGT = "mgt"

    CONST_PORT_MAPPING_MGT_HTTP_TRANSPORT = "mgt-http"
    CONST_PORT_MAPPING_MGT_HTTPS_TRANSPORT = "mgt-https"
    CONST_PORT_MAPPING_PT_HTTP_TRANSPORT = "pt-http"
    CONST_PORT_MAPPING_PT_HTTPS_TRANSPORT = "pt-https"
    CONST_PROTOCOL_HTTP = "http"
    CONST_PROTOCOL_HTTPS = "https"
    CONST_ESB = "wso2esb-481"
    CONST_ESB_WORKER = "%s-worker" % CONST_ESB
    CONST_PPAAS_MEMBERSHIP_SCHEME = "private-paas"
    SERVICES = ["%s-manager" % CONST_ESB, "%s-worker" % CONST_ESB]

    # list of environment variables exported by the plugin
    ENV_CONFIG_PARAM_SUB_DOMAIN = 'CONFIG_PARAM_SUB_DOMAIN'
    ENV_CONFIG_PARAM_MB_HOST = 'CONFIG_PARAM_MB_HOST'
    ENV_CONFIG_PARAM_CLUSTER_IDs = 'CONFIG_PARAM_CLUSTER_IDs'
    ENV_CONFIG_PARAM_HTTP_PROXY_PORT = 'CONFIG_PARAM_HTTP_PROXY_PORT'
    ENV_CONFIG_PARAM_HTTPS_PROXY_PORT = 'CONFIG_PARAM_HTTPS_PROXY_PORT'
    ENV_CONFIG_PARAM_PT_HTTP_PROXY_PORT = 'CONFIG_PARAM_PT_HTTP_PROXY_PORT'
    ENV_CONFIG_PARAM_PT_HTTPS_PROXY_PORT = 'CONFIG_PARAM_PT_HTTPS_PROXY_PORT'

    # clustering related environment variables read from payload_parameters
    ENV_CONFIG_PARAM_CLUSTERING = 'CONFIG_PARAM_CLUSTERING'
    ENV_CONFIG_PARAM_MEMBERSHIP_SCHEME = 'CONFIG_PARAM_MEMBERSHIP_SCHEME'

    def run_plugin(self, values):

        # read Port_mappings, Application_Id, MB_IP and Topology, clustering, membership_scheme from 'values'
        port_mappings_str = values[self.CONST_PORT_MAPPINGS].replace("'", "")
        app_id = values[self.CONST_APPLICATION_ID]
        mb_ip = values[self.CONST_MB_IP]
        service_type = values[self.CONST_SERVICE_NAME]
        my_cluster_id = values[self.CONST_CLUSTER_ID]
        # if CONFIG_PARAM_CLUSTERING is not set, set the default value as false
        clustering = values.get(self.ENV_CONFIG_PARAM_CLUSTERING, 'false')
        membership_scheme = values.get(self.ENV_CONFIG_PARAM_MEMBERSHIP_SCHEME)

        # log above values
        WSO2ESBStartupHandler.log.info("Port Mappings: %s" % port_mappings_str)
        WSO2ESBStartupHandler.log.info("Application ID: %s" % app_id)
        WSO2ESBStartupHandler.log.info("MB IP: %s" % mb_ip)
        WSO2ESBStartupHandler.log.info("Service Name: %s" % service_type)
        WSO2ESBStartupHandler.log.info("Cluster ID: %s" % my_cluster_id)
        WSO2ESBStartupHandler.log.info("Clustering: %s" % clustering)
        WSO2ESBStartupHandler.log.info("Membership Scheme: %s" % membership_scheme)

        # export Proxy Ports as Env. variables - used in catalina-server.xml
        self.set_proxy_ports(port_mappings_str)

        # set sub-domain
        sub_domain = None
        if service_type.endswith(self.CONST_MANAGER):
            sub_domain = self.CONST_MGT
        elif service_type.endswith(self.CONST_WORKER):
            sub_domain = self.CONST_WORKER
        if sub_domain is not None:
            self.export_env_var(self.ENV_CONFIG_PARAM_SUB_DOMAIN, sub_domain)

        # if CONFIG_PARAM_MEMBERSHIP_SCHEME is not set, set the private-paas membership scheme as default one
        if membership_scheme is None:
            membership_scheme = self.CONST_PPAAS_MEMBERSHIP_SCHEME
            self.export_env_var(self.ENV_CONFIG_PARAM_MEMBERSHIP_SCHEME, membership_scheme)

        # check if clustering is enabled and membership scheme is set to 'private-paas'
        if clustering == 'true' and membership_scheme == self.CONST_PPAAS_MEMBERSHIP_SCHEME:
            # export Cluster_Ids as Env. variables - used in axis2.xml
            self.set_cluster_ids(app_id, service_type, my_cluster_id)
            # export mb_ip as Env.variable - used in jndi.properties
            if mb_ip is not None:
                self.export_env_var(self.ENV_CONFIG_PARAM_MB_HOST, mb_ip)

        # start configurator
        WSO2ESBStartupHandler.log.info("Configuring WSO2 ESB...")
        config_command = "python ${CONFIGURATOR_HOME}/configurator.py"
        env_var = os.environ.copy()
        p = subprocess.Popen(config_command, env=env_var, shell=True)
        output, errors = p.communicate()
        WSO2ESBStartupHandler.log.info("WSO2 ESB configured successfully")

        # start server
        WSO2ESBStartupHandler.log.info("Starting WSO2 ESB...")
        if service_type == self.CONST_ESB_WORKER:
            start_command = "exec ${CARBON_HOME}/bin/wso2server.sh -DworkerNode=true start"
        else:
            start_command = "exec ${CARBON_HOME}/bin/wso2server.sh -Dsetup start"
        env_var = os.environ.copy()
        p = subprocess.Popen(start_command, env=env_var, shell=True)
        output, errors = p.communicate()
        WSO2ESBStartupHandler.log.info("WSO2 ESB started successfully")

    def set_cluster_ids(self, app_id, service_type, my_cluster_id):
        """
        Set clusterIds of services read from topology for worker manager instances
        else use own clusterId

        :return: void
        """
        cluster_ids = []
        if service_type.endswith(self.CONST_MANAGER) or service_type.endswith(self.CONST_WORKER):
            for service_name in self.SERVICES:
                cluster_id_of_service = self.read_cluster_id_of_service(service_name, app_id)
                if cluster_id_of_service is not None:
                    cluster_ids.append(cluster_id_of_service)
        else:
            cluster_ids.append(my_cluster_id)
        # If clusterIds are available, set them as environment variables
        if cluster_ids:
            cluster_ids_string = ",".join(cluster_ids)
            self.export_env_var(self.ENV_CONFIG_PARAM_CLUSTER_IDs, cluster_ids_string)

    def read_cluster_id_of_service(self, service_name, app_id):
        cluster_id = None
        clusters = None
        topology = TopologyContext().get_topology()

        if topology.service_exists(service_name):
            service = topology.get_service(service_name)
            clusters = service.get_clusters()
        else:
            WSO2ESBStartupHandler.log.warn("[Service] %s is not available in topology" % service_name)

        if clusters is not None:
            for cluster in clusters:
                if cluster.app_id == app_id:
                    cluster_id = cluster.cluster_id

        return cluster_id

    def set_proxy_ports(self, port_mappings_str):
        """
        exports proxy ports from PORT_MAPPINGS as env. variables

        :return: void
        """
        mgt_http_port = None
        mgt_https_port = None
        pt_http_port = None
        pt_https_port = None

        # port mappings format: NAME:mgt-http|PROTOCOL:http|PORT:30001|PROXY_PORT:0|TYPE:NodePort;
        #                       NAME:mgt-https|PROTOCOL:https|PORT:30002|PROXY_PORT:0|TYPE:NodePort;
        #                       NAME:pt-http|PROTOCOL:http|PORT:30003|PROXY_PORT:7280|TYPE:ClientIP;
        #                       NAME:pt-https|PROTOCOL:https|PORT:30004|PROXY_PORT:7243|TYPE:NodePort
        if port_mappings_str is not None:

            port_mappings_array = port_mappings_str.split(";")
            if port_mappings_array:

                for port_mapping in port_mappings_array:
                    WSO2ESBStartupHandler.log.debug("port_mapping: %s" % port_mapping)
                    name_value_array = port_mapping.split("|")
                    name = name_value_array[0].split(":")[1]
                    protocol = name_value_array[1].split(":")[1]
                    proxy_port = name_value_array[3].split(":")[1]
                    # If PROXY_PORT is not set, set PORT as the proxy port (Kubernetes),
                    if proxy_port == '0':
                        proxy_port = name_value_array[2].split(":")[1]
                    if name == self.CONST_PORT_MAPPING_MGT_HTTP_TRANSPORT and protocol == self.CONST_PROTOCOL_HTTP:
                        mgt_http_port = proxy_port
                    if name == self.CONST_PORT_MAPPING_MGT_HTTPS_TRANSPORT and protocol == self.CONST_PROTOCOL_HTTPS:
                        mgt_https_port = proxy_port
                    if name == self.CONST_PORT_MAPPING_PT_HTTP_TRANSPORT and protocol == self.CONST_PROTOCOL_HTTP:
                        pt_http_port = proxy_port
                    if name == self.CONST_PORT_MAPPING_PT_HTTPS_TRANSPORT and protocol == self.CONST_PROTOCOL_HTTPS:
                        pt_https_port = proxy_port

        # export environment variables
        self.export_env_var(self.ENV_CONFIG_PARAM_HTTP_PROXY_PORT, mgt_http_port)
        self.export_env_var(self.ENV_CONFIG_PARAM_HTTPS_PROXY_PORT, mgt_https_port)
        self.export_env_var(self.ENV_CONFIG_PARAM_PT_HTTP_PROXY_PORT, pt_http_port)
        self.export_env_var(self.ENV_CONFIG_PARAM_PT_HTTPS_PROXY_PORT, pt_https_port)

    def export_env_var(self, variable, value):
        """
        exports key value pairs as env. variables

        :return: void
        """
        if value is not None:
            os.environ[variable] = value
            WSO2ESBStartupHandler.log.info("Exported environment variable %s: %s" % (variable, value))
        else:
            WSO2ESBStartupHandler.log.warn("Could not export environment variable %s " % variable)
