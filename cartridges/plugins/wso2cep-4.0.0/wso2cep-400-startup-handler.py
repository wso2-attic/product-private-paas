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
from entity import *
import subprocess
import socket
import os


class WSO2StartupHandler(ICartridgeAgentPlugin):
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
    CONST_PROTOCOL_HTTP = "http"
    CONST_PROTOCOL_HTTPS = "https"
    CONST_PPAAS_MEMBERSHIP_SCHEME = "private-paas"
    CONST_ZOOKEEPER_DEFAULT_PORT = "2888"
    CONST_CEP_MANAGER_MEMBER_PORT = "8904"
    CONST_STORM_TYPE = 'CONFIG_PARAM_STORM_TYPE'
    CONST_PRODUCT = "CEP"
    CONST_ZOOKEEPER_SERVICE_TYPE = "zookeeper"
    CONST_NIMBUS_SERVICE_TYPE = "storm-nimbus"

    SERVICES = ["wso2cep-manager", "wso2cep-worker"]

    # list of environment variables exported by the plugin
    ENV_CONFIG_PARAM_SUB_DOMAIN = 'CONFIG_PARAM_SUB_DOMAIN'
    ENV_CONFIG_PARAM_MB_HOST = 'CONFIG_PARAM_MB_HOST'
    ENV_CONFIG_PARAM_CLUSTER_IDs = 'CONFIG_PARAM_CLUSTER_IDs'
    ENV_CONFIG_PARAM_HTTP_PROXY_PORT = 'CONFIG_PARAM_HTTP_PROXY_PORT'
    ENV_CONFIG_PARAM_HTTPS_PROXY_PORT = 'CONFIG_PARAM_HTTPS_PROXY_PORT'
    ENV_CONFIG_PARAM_PT_HTTP_PROXY_PORT = 'CONFIG_PARAM_PT_HTTP_PROXY_PORT'
    ENV_CONFIG_PARAM_PT_HTTPS_PROXY_PORT = 'CONFIG_PARAM_PT_HTTPS_PROXY_PORT'
    ENV_CONFIG_PARAM_HOST_NAME = 'CONFIG_PARAM_HOST_NAME'
    ENV_CONFIG_PARAM_MGT_HOST_NAME = 'CONFIG_PARAM_MGT_HOST_NAME'
    ENV_CONFIG_PARAM_LOCAL_MEMBER_HOST = 'CONFIG_PARAM_LOCAL_MEMBER_HOST'
    ENV_ZOOKEEPER_HOSTNAMES = "CONFIG_PARAM_ZOOKEEPER_HOSTNAMES"
    ENV_NIMBUS_HOSTNAME = "CONFIG_PARAM_NIMBUS_HOSTNAME"
    ENV_CONFIG_PARAM_MANAGER_MEMBERS = "CONFIG_PARAM_MANAGER_MEMBERS"
    ENV_CONFIG_PARAM_MANAGER = "CONFIG_PARAM_MANAGER"
    ENV_CONFIG_PARAM_WORKER = "CONFIG_PARAM_WORKER"

    # clustering related environment variables read from payload_parameters
    ENV_CONFIG_PARAM_CLUSTERING = 'CONFIG_PARAM_CLUSTERING'
    ENV_CONFIG_PARAM_MEMBERSHIP_SCHEME = 'CONFIG_PARAM_MEMBERSHIP_SCHEME'


    def run_plugin(self, values):
        # read from 'values'
        port_mappings_str = values[self.CONST_PORT_MAPPINGS].replace("'", "")
        app_id = values[self.CONST_APPLICATION_ID]
        mb_ip = values[self.CONST_MB_IP]
        service_type = values[self.CONST_SERVICE_NAME]
        my_cluster_id = values[self.CONST_CLUSTER_ID]
        clustering = values.get(self.ENV_CONFIG_PARAM_CLUSTERING, 'false')
        membership_scheme = values.get(self.ENV_CONFIG_PARAM_MEMBERSHIP_SCHEME)
        # read topology from PCA TopologyContext
        topology = TopologyContext.topology

        # log above values
        WSO2StartupHandler.log.info("Port Mappings: %s" % port_mappings_str)
        WSO2StartupHandler.log.info("Application ID: %s" % app_id)
        WSO2StartupHandler.log.info("MB IP: %s" % mb_ip)
        WSO2StartupHandler.log.info("Service Name: %s" % service_type)
        WSO2StartupHandler.log.info("Cluster ID: %s" % my_cluster_id)
        WSO2StartupHandler.log.info("Clustering: %s" % clustering)
        WSO2StartupHandler.log.info("Membership Scheme: %s" % membership_scheme)

        # export Proxy Ports as Env. variables - used in catalina-server.xml
        mgt_http_proxy_port = self.read_proxy_port(port_mappings_str, self.CONST_PORT_MAPPING_MGT_HTTP_TRANSPORT,
                                                   self.CONST_PROTOCOL_HTTP)
        mgt_https_proxy_port = self.read_proxy_port(port_mappings_str, self.CONST_PORT_MAPPING_MGT_HTTPS_TRANSPORT,
                                                    self.CONST_PROTOCOL_HTTPS)
        self.export_env_var(self.ENV_CONFIG_PARAM_HTTP_PROXY_PORT, mgt_http_proxy_port)
        self.export_env_var(self.ENV_CONFIG_PARAM_HTTPS_PROXY_PORT, mgt_https_proxy_port)

        # set sub-domain
        sub_domain = None
        if service_type.endswith(self.CONST_MANAGER):
            sub_domain = self.CONST_MGT
            self.export_env_var(self.ENV_CONFIG_PARAM_MANAGER, 'true')
            self.export_env_var(self.ENV_CONFIG_PARAM_WORKER, 'false')
        elif service_type.endswith(self.CONST_WORKER):
            sub_domain = self.CONST_WORKER
            self.export_env_var(self.ENV_CONFIG_PARAM_MANAGER, 'false')
            self.export_env_var(self.ENV_CONFIG_PARAM_WORKER, 'true')

        self.export_env_var(self.ENV_CONFIG_PARAM_SUB_DOMAIN, sub_domain)

        # if CONFIG_PARAM_MEMBERSHIP_SCHEME is not set, set the private-paas membership scheme as default one
        if clustering == 'true' and membership_scheme is None:
            membership_scheme = self.CONST_PPAAS_MEMBERSHIP_SCHEME
            self.export_env_var(self.ENV_CONFIG_PARAM_MEMBERSHIP_SCHEME, membership_scheme)

        # check if clustering is enabled
        if clustering == 'true':
            # set hostnames
            self.export_host_names(topology, app_id)
            # check if membership scheme is set to 'private-paas'
            if membership_scheme == self.CONST_PPAAS_MEMBERSHIP_SCHEME:
                # export Cluster_Ids as Env. variables - used in axis2.xml
                self.export_cluster_ids(topology, app_id, service_type, my_cluster_id)
                # export mb_ip as Env.variable - used in jndi.properties
                self.export_env_var(self.ENV_CONFIG_PARAM_MB_HOST, mb_ip)

        # set local ip as CONFIG_PARAM_LOCAL_MEMBER_HOST
        local_ip = socket.gethostbyname(socket.gethostname())
        self.export_env_var(self.ENV_CONFIG_PARAM_LOCAL_MEMBER_HOST, local_ip)

        zookeeper_ips_list = []
        zk_hostnames_string = None
        zookeeper_cluster = self.get_cluster_of_service(topology, self.CONST_ZOOKEEPER_SERVICE_TYPE, app_id)
        if zookeeper_cluster is not None:
            member_map = zookeeper_cluster.member_map
            for member in member_map:
                default_private_ip = member_map[member].member_default_private_ip
                zookeeper_ips_list.append(default_private_ip)

            zk_hostnames_string = self.generate_dictionary_str_from_array(zookeeper_ips_list,
                                                                      self.CONST_ZOOKEEPER_DEFAULT_PORT)
        self.export_env_var(self.ENV_ZOOKEEPER_HOSTNAMES, zk_hostnames_string)

        nimbus_cluster = self.get_cluster_of_service(topology, self.CONST_NIMBUS_SERVICE_TYPE, app_id)
        nimbus_private_ip = None
        if nimbus_cluster is not None:
            member_map = nimbus_cluster.member_map
            for member in member_map:
                nimbus_private_ip = member_map[member].member_default_private_ip

        self.export_env_var(self.ENV_NIMBUS_HOSTNAME, nimbus_private_ip)

        # start configurator
        WSO2StartupHandler.log.info("Configuring WSO2 %s..." % self.CONST_PRODUCT)
        config_command = "python ${CONFIGURATOR_HOME}/configurator.py"
        env_var = os.environ.copy()
        p = subprocess.Popen(config_command, env=env_var, shell=True)
        output, errors = p.communicate()
        WSO2StartupHandler.log.info("WSO2 %s configured successfully" % self.CONST_PRODUCT)

        # start server
        WSO2StartupHandler.log.info("Starting WSO2 %s ..." % self.CONST_PRODUCT)
        if service_type.endswith(self.CONST_WORKER):
            start_command = "exec ${CARBON_HOME}/bin/wso2server.sh start"
        else:
            start_command = "exec ${CARBON_HOME}/bin/wso2server.sh -Dsetup start"
        env_var = os.environ.copy()
        p = subprocess.Popen(start_command, env=env_var, shell=True)
        output, errors = p.communicate()
        WSO2StartupHandler.log.info("WSO2 %s started successfully" % self.CONST_PRODUCT)

    def export_host_names(self, topology, app_id):
        """
        Set hostnames of services read from topology for worker manager instances
        exports MgtHostName and HostName

        :return: void
        """
        mgt_host_name = None
        host_name = None
        cep_mgr_private_ip_list = []
        for service_name in self.SERVICES:
            if service_name.endswith(self.CONST_MANAGER):
                mgr_cluster = self.get_cluster_of_service(topology, service_name, app_id)
                if mgr_cluster is not None:
                    mgt_host_name = mgr_cluster.hostnames[0]
                    member_map = mgr_cluster.member_map
                    for member in member_map:
                        default_private_ip = member_map[member].member_default_private_ip
                        cep_mgr_private_ip_list.append(default_private_ip)

            elif service_name.endswith(self.CONST_WORKER):
                worker_cluster = self.get_cluster_of_service(topology, service_name, app_id)
                if worker_cluster is not None:
                    host_name = worker_cluster.hostnames[0]

        cep_managers_string = self.generate_dictionary_str_from_array(cep_mgr_private_ip_list,
                                                                      self.CONST_CEP_MANAGER_MEMBER_PORT)

        self.export_env_var(self.ENV_CONFIG_PARAM_MANAGER_MEMBERS, cep_managers_string)
        self.export_env_var(self.ENV_CONFIG_PARAM_MGT_HOST_NAME, mgt_host_name)
        self.export_env_var(self.ENV_CONFIG_PARAM_HOST_NAME, host_name)

    def export_cluster_ids(self, topology, app_id, service_type, my_cluster_id):
        """
        Set clusterIds of services read from topology for worker manager instances
        else use own clusterId

        :return: void
        """
        cluster_ids = []
        cluster_id_of_service = None
        if service_type.endswith(self.CONST_MANAGER) or service_type.endswith(self.CONST_WORKER):
            for service_name in self.SERVICES:
                cluster_of_service = self.get_cluster_of_service(topology, service_name, app_id)
                if cluster_of_service is not None:
                    cluster_id_of_service = cluster_of_service.cluster_id
                if cluster_id_of_service is not None:
                    cluster_ids.append(cluster_id_of_service)
        else:
            cluster_ids.append(my_cluster_id)
        # If clusterIds are available, export them as environment variables
        if cluster_ids:
            cluster_ids_string = ",".join(cluster_ids)
            self.export_env_var(self.ENV_CONFIG_PARAM_CLUSTER_IDs, cluster_ids_string)

    @staticmethod
    def get_cluster_of_service(topology, service_name, app_id):
        cluster_obj = None
        clusters = None
        if topology is not None:
            if topology.service_exists(service_name):
                service = topology.get_service(service_name)
                if service is not None:
                    clusters = service.get_clusters()
                else:
                    WSO2StartupHandler.log.warn("[Service] %s is None" % service_name)
            else:
                WSO2StartupHandler.log.warn("[Service] %s is not available in topology" % service_name)
        else:
            WSO2StartupHandler.log.warn("Topology is empty.")

        if clusters is not None:
            for cluster in clusters:
                if cluster.app_id == app_id:
                    cluster_obj = cluster

        return cluster_obj

    @staticmethod
    def read_proxy_port(port_mappings_str, port_mapping_name, port_mapping_protocol):
        """
        returns proxy port of the requested port mapping

        :return: void
        """

        # port mappings format: NAME:mgt-http|PROTOCOL:http|PORT:30001|PROXY_PORT:0|TYPE:NodePort;
        # NAME:mgt-https|PROTOCOL:https|PORT:30002|PROXY_PORT:0|TYPE:NodePort;
        # NAME:pt-http|PROTOCOL:http|PORT:30003|PROXY_PORT:7280|TYPE:ClientIP;
        # NAME:pt-https|PROTOCOL:https|PORT:30004|PROXY_PORT:7243|TYPE:NodePort

        if port_mappings_str is not None:
            port_mappings_array = port_mappings_str.split(";")
            if port_mappings_array:

                for port_mapping in port_mappings_array:
                    # WSO2StartupHandler.log.debug("port_mapping: %s" % port_mapping)
                    name_value_array = port_mapping.split("|")
                    name = name_value_array[0].split(":")[1]
                    protocol = name_value_array[1].split(":")[1]
                    proxy_port = name_value_array[3].split(":")[1]
                    # If PROXY_PORT is not set, set PORT as the proxy port (ex:Kubernetes),
                    if proxy_port == '0':
                        proxy_port = name_value_array[2].split(":")[1]

                    if name == port_mapping_name and protocol == port_mapping_protocol:
                        return proxy_port

    @staticmethod
    def export_env_var(variable, value):
        """
        exports key value pairs as env. variables

        :return: void
        """
        if value is not None:
            os.environ[variable] = value
            WSO2StartupHandler.log.info("Exported environment variable %s: %s" % (variable, value))
        else:
            WSO2StartupHandler.log.warn("Could not export environment variable %s " % variable)


    @staticmethod
    def generate_dictionary_str_from_array(list, value):
        if list:
            dictionary_str = '['
            for member_ip in list:
                if member_ip is not list[-1]:
                    dictionary_str += member_ip + ":" + value + ','
                else:
                    dictionary_str += member_ip + ":" + value
            dictionary_str += ']'
        else:
            WSO2StartupHandler.log.warn(
                "Array is empty, hence cannot generate a dictionary String with value %s" % value)
        return dictionary_str