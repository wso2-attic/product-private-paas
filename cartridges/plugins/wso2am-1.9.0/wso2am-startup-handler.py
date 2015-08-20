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
import os
import mdsclient
import time
import socket


class WSO2AMStartupHandler(ICartridgeAgentPlugin):
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
    CONST_CLUSTER_ID = "CLUSTER_ID"
    CONST_SERVICE_NAME = "SERVICE_NAME"
    CONST_STRATOS_MEMBERSHIP_SCHEME = "stratos"
    CONST_KEYMANAGER = "KeyManager"
    CONST_GATEWAY_MANAGER = "Gateway-Manager"
    CONST_GATEWAY_WORKER = "Gateway-Worker"
    CONST_PUBLISHER = "Publisher"
    CONST_STORE = "Store"
    CONST_PPAAS_MEMBERSHIP_SCHEME = "private-paas"
    CONST_WORKER = "worker"
    CONST_MANAGER = "manager"
    CONST_MGT = "mgt"

    GATEWAY_SERVICES = ["wso2am-190-gw-manager", "wso2am-190-gw-worker"]
    PUB_STORE_SERVICES = ["wso2am-190-pub", "wso2am-190-store"]

    # list of environment variables exported by the plugin
    ENV_CONFIG_PARAM_MB_HOST = 'CONFIG_PARAM_MB_HOST'
    ENV_CONFIG_PARAM_CLUSTER_IDs = 'CONFIG_PARAM_CLUSTER_IDs'
    ENV_CONFIG_PARAM_HTTPS_PROXY_PORT = 'CONFIG_PARAM_HTTPS_PROXY_PORT'
    ENV_CONFIG_PARAM_PT_HTTP_PROXY_PORT = 'CONFIG_PARAM_PT_HTTP_PROXY_PORT'
    ENV_CONFIG_PARAM_PT_HTTPS_PROXY_PORT = 'CONFIG_PARAM_PT_HTTPS_PROXY_PORT'
    ENV_CONFIG_PARAM_CLUSTERING = 'CONFIG_PARAM_CLUSTERING'
    ENV_CONFIG_PARAM_MEMBERSHIP_SCHEME = 'CONFIG_PARAM_MEMBERSHIP_SCHEME'
    ENV_CONFIG_PARAM_PROFILE = 'CONFIG_PARAM_PROFILE'
    ENV_CONFIG_PARAM_LOADBALANCER_IP = 'CONFIG_PARAM_LOADBALANCER_IP'
    ENV_CONFIG_PARAM_KEYMANAGER_IP = 'CONFIG_PARAM_KEYMANAGER_IP'
    ENV_CONFIG_PARAM_GATEWAY_IP = 'CONFIG_PARAM_GATEWAY_IP'
    ENV_CONFIG_PARAM_PUBLISHER_IP = 'CONFIG_PARAM_PUBLISHER_IP'
    ENV_CONFIG_PARAM_STORE_IP = 'CONFIG_PARAM_STORE_IP'
    ENV_CONFIG_PARAM_THRIFTSERVERHOST = 'CONFIG_PARAM_THRIFTSERVERHOST'
    ENV_CONFIG_PARAM_WKA_MEMBERS = 'CONFIG_PARAM_WKA_MEMBERS'
    ENV_CONFIG_PARAM_SUB_DOMAIN = 'CONFIG_PARAM_SUB_DOMAIN'
    ENV_CONFIG_PARAM_WORKER_HOST_NAME = 'CONFIG_PARAM_WORKER_HOST_NAME'

    def run_plugin(self, values):

        # read Port_mappings, Application_Id, MB_IP and Topology, clustering, membership_scheme from 'values'
        port_mappings_str = values[self.CONST_PORT_MAPPINGS].replace("'", "")
        app_id = values[self.CONST_APPLICATION_ID]
        mb_ip = values[self.CONST_MB_IP]
        service_name = values[self.CONST_SERVICE_NAME]
        profile = os.environ.get(self.ENV_CONFIG_PARAM_PROFILE)
        load_balancer_ip = os.environ.get(self.ENV_CONFIG_PARAM_LOADBALANCER_IP)
        membership_scheme = values.get(self.ENV_CONFIG_PARAM_MEMBERSHIP_SCHEME)
        clustering = values.get(self.ENV_CONFIG_PARAM_CLUSTERING, 'false')
        my_cluster_id = values[self.CONST_CLUSTER_ID]
        worker_hostname = os.environ.get(self.ENV_CONFIG_PARAM_WORKER_HOST_NAME)

        # log above values
        WSO2AMStartupHandler.log.info("Port Mappings: %s" % port_mappings_str)
        WSO2AMStartupHandler.log.info("Application ID: %s" % app_id)
        WSO2AMStartupHandler.log.info("MB IP: %s" % mb_ip)
        WSO2AMStartupHandler.log.info("Service Name: %s" % service_name)
        WSO2AMStartupHandler.log.info("Profile: %s" % profile)
        WSO2AMStartupHandler.log.info("Membership Scheme: %s" % membership_scheme)
        WSO2AMStartupHandler.log.info("Cluster ID: %s" % my_cluster_id)
        WSO2AMStartupHandler.log.info("Clustering: %s" % clustering)
        WSO2AMStartupHandler.log.info("Worker Hostname: %s" % worker_hostname)

        # set sub-domain
        sub_domain = None
        if service_name.endswith(self.CONST_MANAGER):
            sub_domain = self.CONST_MGT
        elif service_name.endswith(self.CONST_WORKER):
            sub_domain = self.CONST_WORKER
        if sub_domain is not None:
            self.export_env_var(self.ENV_CONFIG_PARAM_SUB_DOMAIN, sub_domain)

        # export Proxy Ports as Env. variables - used in catalina-server.xml
        self.set_proxy_ports(port_mappings_str)

        # if CONFIG_PARAM_MEMBERSHIP_SCHEME is not set, set the private-paas membership scheme as default one
        if membership_scheme is None:
            membership_scheme = self.CONST_PPAAS_MEMBERSHIP_SCHEME
            self.export_env_var(self.ENV_CONFIG_PARAM_MEMBERSHIP_SCHEME, membership_scheme)

        if clustering == 'true' and membership_scheme == self.CONST_PPAAS_MEMBERSHIP_SCHEME:
            # export Cluster_Ids as Env. variables - used in axis2.xml
            service_list = None

            if service_name in self.GATEWAY_SERVICES:
                service_list = self.GATEWAY_SERVICES
            elif service_name in self.PUB_STORE_SERVICES:
                service_list = self.PUB_STORE_SERVICES

            self.set_cluster_ids(app_id, service_name, my_cluster_id, service_list)
            # export mb_ip as Env.variable - used in jndi.properties
            if mb_ip is not None:
                self.export_env_var(self.ENV_CONFIG_PARAM_MB_HOST, mb_ip)

        if profile == self.CONST_KEYMANAGER:

            isKubernetes = self.check_for_kubernetes_cluster(service_name, app_id)
            service_ip = self.configure_service_ip(load_balancer_ip, isKubernetes, service_name, app_id)
            gateway_ip = self.configure_profile(app_id, self.ENV_CONFIG_PARAM_KEYMANAGER_IP, service_ip,
                                                self.ENV_CONFIG_PARAM_GATEWAY_IP)
            self.export_env_var(self.ENV_CONFIG_PARAM_GATEWAY_IP, gateway_ip)
            start_command = "exec ${CARBON_HOME}/bin/wso2server.sh -Dprofile=api-key-manager start"

        elif profile == self.CONST_GATEWAY_MANAGER:

            isKubernetes = self.check_for_kubernetes_cluster(service_name, app_id)
            service_ip = self.configure_service_ip(load_balancer_ip, isKubernetes, service_name, app_id)
            keymanager_ip = self.configure_profile(app_id, self.ENV_CONFIG_PARAM_GATEWAY_IP, service_ip,
                                                   self.ENV_CONFIG_PARAM_KEYMANAGER_IP)
            self.export_env_var(self.ENV_CONFIG_PARAM_KEYMANAGER_IP, keymanager_ip)

            member_ip = socket.gethostbyname(socket.gethostname())
            self.update_hosts_file(member_ip, worker_hostname)

            #  self.export_env_var(self.ENV_CONFIG_PARAM_THRIFTSERVERHOST, keymanager_ip)
            start_command = "exec ${CARBON_HOME}/bin/wso2server.sh -Dprofile=gateway-manager start"

        elif profile == self.CONST_GATEWAY_WORKER:

            # isKubernetes = self.check_for_kubernetes_cluster(service_name, app_id)
            # service_ip = self.configure_service_ip(load_balancer_ip, isKubernetes, service_name, app_id)
            # keymanager_ip = self.configure_profile(app_id, self.ENV_CONFIG_PARAM_GATEWAY_IP, service_ip,
            #                                       self.ENV_CONFIG_PARAM_KEYMANAGER_IP)
            keymanager_ip = self.get_data_from_meta_data_service(app_id, self.ENV_CONFIG_PARAM_KEYMANAGER_IP)
            self.export_env_var(self.ENV_CONFIG_PARAM_KEYMANAGER_IP, keymanager_ip)
            # self.export_env_var(self.ENV_CONFIG_PARAM_THRIFTSERVERHOST, keymanager_ip)

            member_ip = socket.gethostbyname(socket.gethostname())
            self.update_hosts_file(member_ip, worker_hostname)

            start_command = "exec ${CARBON_HOME}/bin/wso2server.sh -Dprofile=gateway-worker start"

        elif profile == self.CONST_PUBLISHER:

            isKubernetes = self.check_for_kubernetes_cluster(service_name, app_id)
            service_ip = self.configure_service_ip(load_balancer_ip, isKubernetes, service_name, app_id)
            store_ip = self.configure_profile(app_id, self.ENV_CONFIG_PARAM_PUBLISHER_IP, service_ip,
                                              self.ENV_CONFIG_PARAM_STORE_IP)
            wka_member_value = "[" + store_ip + ":4000]"
            self.export_env_var(self.ENV_CONFIG_PARAM_STORE_IP, store_ip)
            self.export_env_var(self.ENV_CONFIG_PARAM_WKA_MEMBERS, wka_member_value)

            keymanager_ip = self.get_data_from_meta_data_service(app_id, self.ENV_CONFIG_PARAM_KEYMANAGER_IP)
            gateway_ip = self.get_data_from_meta_data_service(app_id, self.ENV_CONFIG_PARAM_GATEWAY_IP)
            self.export_env_var(self.ENV_CONFIG_PARAM_KEYMANAGER_IP, keymanager_ip)
            self.export_env_var(self.ENV_CONFIG_PARAM_GATEWAY_IP, gateway_ip)
            start_command = "exec ${CARBON_HOME}/bin/wso2server.sh -Dprofile=api-publisher start"

        elif profile == self.CONST_STORE:

            isKubernetes = self.check_for_kubernetes_cluster(service_name, app_id)
            service_ip = self.configure_service_ip(load_balancer_ip, isKubernetes, service_name, app_id)
            publisher_ip = self.configure_profile(app_id, self.ENV_CONFIG_PARAM_STORE_IP, service_ip,
                                                  self.ENV_CONFIG_PARAM_PUBLISHER_IP)
            wka_member_value = "[" + publisher_ip + ":4000]"
            self.export_env_var(self.ENV_CONFIG_PARAM_PUBLISHER_IP, publisher_ip)
            self.export_env_var(self.ENV_CONFIG_PARAM_WKA_MEMBERS, wka_member_value)

            keymanager_ip = self.get_data_from_meta_data_service(app_id, self.ENV_CONFIG_PARAM_KEYMANAGER_IP)
            gateway_ip = self.get_data_from_meta_data_service(app_id, self.ENV_CONFIG_PARAM_GATEWAY_IP)
            self.export_env_var(self.ENV_CONFIG_PARAM_KEYMANAGER_IP, keymanager_ip)
            self.export_env_var(self.ENV_CONFIG_PARAM_GATEWAY_IP, gateway_ip)
            start_command = "exec ${CARBON_HOME}/bin/wso2server.sh -Dprofile=api-store start"

        else:
            start_command = "exec ${CARBON_HOME}/bin/wso2server.sh start"  # start configurator

        WSO2AMStartupHandler.log.info("Configuring WSO2 API Manager...")
        config_command = "python ${CONFIGURATOR_HOME}/configurator.py"
        env_var = os.environ.copy()
        p = subprocess.Popen(config_command, env=env_var, shell=True)
        output, errors = p.communicate()
        WSO2AMStartupHandler.log.info("WSO2 API Manager configured successfully")

        # start server
        WSO2AMStartupHandler.log.info("Starting WSO2 API Manager...")
        env_var = os.environ.copy()
        p = subprocess.Popen(start_command, env=env_var, shell=True)
        output, errors = p.communicate()
        WSO2AMStartupHandler.log.info("WSO2 API Manager started successfully")


    def configure_service_ip(self, service_ip, isKubernetes, service_name, app_id):
        if service_ip is None:
            if isKubernetes:
                service_ip = self.read_portal_ip_of_service(service_name, app_id)
                WSO2AMStartupHandler.log.info("Service IP takes from portalIp")
            else:
                service_ip = self.read_member_ip_from_topology(service_name, app_id)
                WSO2AMStartupHandler.log.info("Service IP takes from topology")

        return service_ip


    def configure_profile(self, app_id, publish_property, value, receive_property):
        self.remove_data_from_metadata(publish_property)
        self.add_values_to_meta_data_service(publish_property, value)

        return self.get_data_from_meta_data_service(app_id, receive_property)


    def get_data_from_meta_data_service(self, app_id, receive_data):
        mds_response = None
        while mds_response is None:
            WSO2AMStartupHandler.log.info(
                "Waiting for " + receive_data + " to be available from metadata service for app ID: %s" % app_id)
            time.sleep(3)
            mds_response = mdsclient.get(app=True)
            if mds_response is not None and mds_response.properties.get(receive_data) is None:
                mds_response = None

        return mds_response.properties[receive_data]


    def add_values_to_meta_data_service(self, key, value):
        mdsclient.MDSPutRequest()
        data = {"key": key, "values": [value]}
        mdsclient.put(data, app=True)


    def remove_data_from_metadata(self, key):
        mds_response = mdsclient.get(app=True)

        if mds_response is not None and mds_response.properties.get(key) is not None:
            read_data = mds_response.properties[key]
            check_str = isinstance(read_data, (str, unicode))

            if check_str == True:
                mdsclient.delete_property_value(key, read_data)
            else:
                for entry in read_data:
                    mdsclient.delete_property_value(key, entry)


    def set_cluster_ids(self, app_id, service_type, my_cluster_id, service_list):
        """
        Set clusterIds of services read from topology for worker manager instances
        else use own clusterId
        :return: void
        """
        cluster_ids = []

        for service_name in service_list:
            cluster_id_of_service = self.read_cluster_id_of_service(service_name, app_id)
            if cluster_id_of_service is not None:
                cluster_ids.append(cluster_id_of_service)

        # If clusterIds are available, set them as environment variables
        if cluster_ids:
            cluster_ids_string = ",".join(cluster_ids)
            self.export_env_var(self.ENV_CONFIG_PARAM_CLUSTER_IDs, cluster_ids_string)


    def read_member_ip_from_topology(self, service_name, app_id):
        clusters = None
        topology = TopologyContext().get_topology()
        members = None
        member_ip = None

        if topology.service_exists(service_name):
            service = topology.get_service(service_name)
            clusters = service.get_clusters()
        else:
            WSO2AMStartupHandler.log.error("[Service] %s is not available in topology" % service_name)

        if clusters is not None:
            for cluster in clusters:
                if cluster.app_id == app_id:
                    members = cluster.get_members()

        if members is not None:
            for member in members:
                member_ip = member.member_default_private_ip

        if member_ip is None:
            server_hostname = socket.gethostname()
            member_ip = socket.gethostbyname(server_hostname)

        return member_ip


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
                    WSO2AMStartupHandler.log.debug("port_mapping: %s" % port_mapping)
                    name_value_array = port_mapping.split("|")
                    name = name_value_array[0].split(":")[1]
                    protocol = name_value_array[1].split(":")[1]
                    proxy_port = name_value_array[3].split(":")[1]

                    # If PROXY_PORT is not set,
                    if proxy_port == "0":
                        proxy_port = name_value_array[2].split(":")[1]

                    if name == self.CONST_PORT_MAPPING_MGT_CONSOLE and protocol == self.CONST_PROTOCOL_HTTPS:
                        mgt_console_https_port = proxy_port
                    if name == self.CONST_PORT_MAPPING_PT_HTTP_TRANSPORT and protocol == self.CONST_PROTOCOL_HTTP:
                        pt_http_port = proxy_port
                    if name == self.CONST_PORT_MAPPING_PT_HTTPS_TRANSPORT and protocol == self.CONST_PROTOCOL_HTTPS:
                        pt_https_port = proxy_port

        # export environment variables
        self.export_env_var(self.ENV_CONFIG_PARAM_HTTPS_PROXY_PORT, mgt_console_https_port)
        self.export_env_var(self.ENV_CONFIG_PARAM_PT_HTTP_PROXY_PORT, pt_http_port)
        self.export_env_var(self.ENV_CONFIG_PARAM_PT_HTTPS_PROXY_PORT, pt_https_port)


    def export_env_var(self, variable, value):
        if value is not None:
            os.environ[variable] = value
            WSO2AMStartupHandler.log.info("Exported environment variable %s: %s" % (variable, value))
        else:
            WSO2AMStartupHandler.log.warn("Could not export environment variable %s " % variable)


    def read_portal_ip_of_service(self, service_name, app_id):
        clusters = None
        topology = TopologyContext().get_topology()
        portalIp_value = None

        if topology.service_exists(service_name):
            service = topology.get_service(service_name)
            clusters = service.get_clusters()
        else:
            WSO2AMStartupHandler.log.error("[Service] %s is not available in topology" % service_name)

        if clusters is not None:
            for cluster in clusters:
                if cluster.app_id == app_id:
                    kubernetesServices = cluster.get_kubernetesServices()

        if kubernetesServices is not None:
            for kb_service in kubernetesServices:
                if kb_service.portName == self.CONST_PORT_MAPPING_MGT_CONSOLE:
                    portalIp_value = kb_service.portalIP
        else:
            WSO2AMStartupHandler.log.error("Kubernetes Services are not available for [Service] %s" % service_name)

        return portalIp_value


    def check_for_kubernetes_cluster(self, service_name, app_id):
        isKubernetes = False
        clusters = None
        topology = TopologyContext().get_topology()

        if topology.service_exists(service_name):
            service = topology.get_service(service_name)
            clusters = service.get_clusters()
        else:
            WSO2AMStartupHandler.log.error("[Service] %s is not available in topology" % service_name)

        if clusters is not None:
            for cluster in clusters:
                if cluster.app_id == app_id:
                    isKubernetes = cluster.is_kubernetes_cluster

        return isKubernetes


    def read_cluster_id_of_service(self, service_name, app_id):
        cluster_id = None
        clusters = None
        topology = TopologyContext.topology

        if topology is not None:
            if topology.service_exists(service_name):
                service = topology.get_service(service_name)
                clusters = service.get_clusters()
            else:
                WSO2AMStartupHandler.log.warn("[Service] %s is not available in topology" % service_name)

            if clusters is not None:
                for cluster in clusters:
                    if cluster.app_id == app_id:
                        cluster_id = cluster.cluster_id

        return cluster_id


    def update_hosts_file(self, ip_address, host_name):
        """
        Updates /etc/hosts file with clustering hostnames
        :return: void
        """
        config_command = "echo %s  %s >> /etc/hosts" % (ip_address, host_name)
        env_var = os.environ.copy()
        p = subprocess.Popen(config_command, env=env_var, shell=True)
        output, errors = p.communicate()
        WSO2AMStartupHandler.log.info(
            "Successfully updated [ip_address] %s & [hostname] %s in etc/hosts" % (ip_address, host_name))
