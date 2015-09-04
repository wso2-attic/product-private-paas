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
    CONST_PORT_MAPPING_MGT_HTTP_TRANSPORT = "mgt-http"
    CONST_PORT_MAPPING_MGT_HTTPS_TRANSPORT = "mgt-https"
    CONST_PORT_MAPPING_PT_HTTP_TRANSPORT = "pt-http"
    CONST_PORT_MAPPING_PT_HTTPS_TRANSPORT = "pt-https"
    CONST_PROTOCOL_HTTP = "http"
    CONST_PROTOCOL_HTTPS = "https"
    CONST_PORT_MAPPINGS = "PORT_MAPPINGS"
    CONST_APPLICATION_ID = "APPLICATION_ID"
    CONST_MB_IP = "MB_IP"
    CONST_CLUSTER_ID = "CLUSTER_ID"
    CONST_SERVICE_NAME = "SERVICE_NAME"
    CONST_KEY_MANAGER = "KeyManager"
    CONST_GATEWAY_MANAGER = "Gateway-Manager"
    CONST_GATEWAY_WORKER = "Gateway-Worker"
    CONST_PUBLISHER = "Publisher"
    CONST_STORE = "Store"
    CONST_PUBSTORE = "PubStore"
    CONST_PPAAS_MEMBERSHIP_SCHEME = "private-paas"
    CONST_WORKER = "worker"
    CONST_MANAGER = "manager"
    CONST_MGT = "mgt"
    CONST_KEY_MANAGER_SERVICE_NAME = "wso2am-190-km"
    CONST_GATEWAY_MANAGER_SERVICE_NAME = "wso2am-190-gw-manager"
    CONST_GATEWAY_WORKER_SERVICE_NAME = "wso2am-190-gw-worker"
    CONST_PUBLISHER_SERVICE_NAME = "wso2am-190-publisher"
    CONST_STORE_SERVICE_NAME = "wso2am-190-store"
    CONST_PUBLISHER_STORE_NAME = "wso2am-190-pub-store"
    CONST_CONFIG_PARAM_KEYMANAGER_PORTS = 'CONFIG_PARAM_KEYMANAGER_PORTS'
    CONST_CONFIG_PARAM_GATEWAY_PORTS = 'CONFIG_PARAM_GATEWAY_PORTS'
    CONST_CONFIG_PARAM_GATEWAY_WORKER_PORTS = 'CONFIG_PARAM_GATEWAY_WORKER_PORTS'
    CONST_KUBERNETES = "KUBERNETES"
    CONST_VM = "VM"
    CONST_EXTERNAL_LB_FOR_KUBERNETES = "EXTERNAL_LB_FOR_KUBERNETES"

    GATEWAY_SERVICES = [CONST_GATEWAY_MANAGER_SERVICE_NAME, CONST_GATEWAY_WORKER_SERVICE_NAME]
    PUB_STORE_SERVICES = [CONST_PUBLISHER_SERVICE_NAME, CONST_STORE_SERVICE_NAME]
    PUB_STORE = [CONST_PUBLISHER_STORE_NAME]

    # list of environment variables exported by the plugin
    ENV_CONFIG_PARAM_MB_HOST = 'CONFIG_PARAM_MB_HOST'
    ENV_CONFIG_PARAM_CLUSTER_IDs = 'CONFIG_PARAM_CLUSTER_IDs'
    ENV_CONFIG_PARAM_HTTP_PROXY_PORT = 'CONFIG_PARAM_HTTP_PROXY_PORT'
    ENV_CONFIG_PARAM_HTTPS_PROXY_PORT = 'CONFIG_PARAM_HTTPS_PROXY_PORT'
    ENV_CONFIG_PARAM_PT_HTTP_PROXY_PORT = 'CONFIG_PARAM_PT_HTTP_PROXY_PORT'
    ENV_CONFIG_PARAM_PT_HTTPS_PROXY_PORT = 'CONFIG_PARAM_PT_HTTPS_PROXY_PORT'
    ENV_CONFIG_PARAM_CLUSTERING = 'CONFIG_PARAM_CLUSTERING'
    ENV_CONFIG_PARAM_MEMBERSHIP_SCHEME = 'CONFIG_PARAM_MEMBERSHIP_SCHEME'
    ENV_CONFIG_PARAM_PROFILE = 'CONFIG_PARAM_PROFILE'
    ENV_CONFIG_PARAM_LB_IP = 'CONFIG_PARAM_LB_IP'
    ENV_CONFIG_PARAM_KEYMANAGER_IP = 'CONFIG_PARAM_KEYMANAGER_IP'
    ENV_CONFIG_PARAM_GATEWAY_IP = 'CONFIG_PARAM_GATEWAY_IP'
    ENV_CONFIG_PARAM_PUBLISHER_IP = 'CONFIG_PARAM_PUBLISHER_IP'
    ENV_CONFIG_PARAM_STORE_IP = 'CONFIG_PARAM_STORE_IP'
    ENV_CONFIG_PARAM_SUB_DOMAIN = 'CONFIG_PARAM_SUB_DOMAIN'
    ENV_CONFIG_PARAM_HOST_NAME = 'CONFIG_PARAM_HOST_NAME'
    ENV_CONFIG_PARAM_MGT_HOST_NAME = 'CONFIG_PARAM_MGT_HOST_NAME'
    ENV_CONFIG_PARAM_KEYMANAGER_HTTPS_PROXY_PORT = 'CONFIG_PARAM_KEYMANAGER_HTTPS_PROXY_PORT'
    ENV_CONFIG_PARAM_GATEWAY_HTTPS_PROXY_PORT = 'CONFIG_PARAM_GATEWAY_HTTPS_PROXY_PORT'
    ENV_CONFIG_PARAM_GATEWAY_WORKER_IP = 'CONFIG_PARAM_GATEWAY_WORKER_IP'
    ENV_CONFIG_PARAM_GATEWAY_WORKER_PT_HTTP_PROXY_PORT = 'CONFIG_PARAM_GATEWAY_WORKER_PT_HTTP_PROXY_PORT'
    ENV_CONFIG_PARAM_GATEWAY_WORKER_PT_HTTPS_PROXY_PORT = 'CONFIG_PARAM_GATEWAY_WORKER_PT_HTTPS_PROXY_PORT'

    # This is payload parameter which enables to use an external lb when using kubernetes. Use true when using with kub.
    ENV_CONFIG_PARAM_USE_EXTERNAL_LB_FOR_KUBERNETES = 'CONFIG_PARAM_USE_EXTERNAL_LB_FOR_KUBERNETES'

    def run_plugin(self, values):

        # read Port_mappings, Application_Id, MB_IP and Topology, clustering, membership_scheme from 'values'
        port_mappings_str = values[WSO2AMStartupHandler.CONST_PORT_MAPPINGS].replace("'", "")
        app_id = values[WSO2AMStartupHandler.CONST_APPLICATION_ID]
        mb_ip = values[WSO2AMStartupHandler.CONST_MB_IP]
        service_name = values[WSO2AMStartupHandler.CONST_SERVICE_NAME]
        profile = os.environ.get(WSO2AMStartupHandler.ENV_CONFIG_PARAM_PROFILE)
        load_balancer_ip = os.environ.get(WSO2AMStartupHandler.ENV_CONFIG_PARAM_LB_IP)
        membership_scheme = values.get(WSO2AMStartupHandler.ENV_CONFIG_PARAM_MEMBERSHIP_SCHEME,
                                       WSO2AMStartupHandler.CONST_PPAAS_MEMBERSHIP_SCHEME)
        clustering = values.get(WSO2AMStartupHandler.ENV_CONFIG_PARAM_CLUSTERING, 'false')
        my_cluster_id = values[WSO2AMStartupHandler.CONST_CLUSTER_ID]
        external_lb = values.get(WSO2AMStartupHandler.ENV_CONFIG_PARAM_USE_EXTERNAL_LB_FOR_KUBERNETES, 'false')

        # log above values
        WSO2AMStartupHandler.log.info("Port Mappings: %s" % port_mappings_str)
        WSO2AMStartupHandler.log.info("Application ID: %s" % app_id)
        WSO2AMStartupHandler.log.info("MB IP: %s" % mb_ip)
        WSO2AMStartupHandler.log.info("Service Name: %s" % service_name)
        WSO2AMStartupHandler.log.info("Profile: %s" % profile)
        WSO2AMStartupHandler.log.info("Load Balancer IP: %s" % load_balancer_ip)
        WSO2AMStartupHandler.log.info("Membership Scheme: %s" % membership_scheme)
        WSO2AMStartupHandler.log.info("Clustering: %s" % clustering)
        WSO2AMStartupHandler.log.info("Cluster ID: %s" % my_cluster_id)

        # export Proxy Ports as Env. variables - used in catalina-server.xml
        mgt_http_proxy_port = self.read_proxy_port(port_mappings_str,
                                                   WSO2AMStartupHandler.CONST_PORT_MAPPING_MGT_HTTP_TRANSPORT,
                                                   WSO2AMStartupHandler.CONST_PROTOCOL_HTTP)
        mgt_https_proxy_port = self.read_proxy_port(port_mappings_str,
                                                    WSO2AMStartupHandler.CONST_PORT_MAPPING_MGT_HTTPS_TRANSPORT,
                                                    WSO2AMStartupHandler.CONST_PROTOCOL_HTTPS)
        pt_http_proxy_port = self.read_proxy_port(port_mappings_str,
                                                  WSO2AMStartupHandler.CONST_PORT_MAPPING_PT_HTTP_TRANSPORT,
                                                  WSO2AMStartupHandler.CONST_PROTOCOL_HTTP)
        pt_https_proxy_port = self.read_proxy_port(port_mappings_str,
                                                   WSO2AMStartupHandler.CONST_PORT_MAPPING_PT_HTTPS_TRANSPORT,
                                                   WSO2AMStartupHandler.CONST_PROTOCOL_HTTPS)
        self.export_env_var(WSO2AMStartupHandler.ENV_CONFIG_PARAM_HTTP_PROXY_PORT, mgt_http_proxy_port)
        self.export_env_var(WSO2AMStartupHandler.ENV_CONFIG_PARAM_HTTPS_PROXY_PORT, mgt_https_proxy_port)
        self.export_env_var(WSO2AMStartupHandler.ENV_CONFIG_PARAM_PT_HTTP_PROXY_PORT, pt_http_proxy_port)
        self.export_env_var(WSO2AMStartupHandler.ENV_CONFIG_PARAM_PT_HTTPS_PROXY_PORT, pt_https_proxy_port)

        # set sub-domain
        self.populate_sub_domains(service_name)

        # export CONFIG_PARAM_MEMBERSHIP_SCHEME
        self.export_env_var(WSO2AMStartupHandler.ENV_CONFIG_PARAM_MEMBERSHIP_SCHEME, membership_scheme)

        if clustering == 'true' and membership_scheme == self.CONST_PPAAS_MEMBERSHIP_SCHEME:
            service_list = None

            if service_name in self.GATEWAY_SERVICES:
                service_list = self.GATEWAY_SERVICES
            elif service_name in self.PUB_STORE_SERVICES:
                service_list = self.PUB_STORE_SERVICES
            elif service_name in self.PUB_STORE:
                service_list = self.PUB_STORE

            # set cluster ids for private-paas clustering schema in axis2.xml
            self.set_cluster_ids(app_id, service_list)

            # export mb_ip as Env.variable - used in jndi.properties
            self.export_env_var(self.ENV_CONFIG_PARAM_MB_HOST, mb_ip)

        if profile == self.CONST_KEY_MANAGER:
            # this is for key_manager profile
            # remove previous data from metadata service
            # add new values to meta data service - key manager ip and mgt-console port
            # retrieve values from meta data service - gateway ip, gw mgt console port, pt http and https ports
            # check deployment is vm, if vm update /etc/hosts with values
            # export retrieve values as environment variables
            # set the start command

            self.remove_data_from_metadata(self.ENV_CONFIG_PARAM_KEYMANAGER_IP)
            self.remove_data_from_metadata(self.CONST_CONFIG_PARAM_KEYMANAGER_PORTS)

            self.add_data_to_meta_data_service(self.ENV_CONFIG_PARAM_KEYMANAGER_IP, load_balancer_ip)
            self.add_data_to_meta_data_service(self.CONST_CONFIG_PARAM_KEYMANAGER_PORTS,
                                               "Ports:" + mgt_https_proxy_port)

            gateway_ip = self.get_data_from_meta_data_service(app_id, self.ENV_CONFIG_PARAM_GATEWAY_IP)
            gateway_ports = self.get_data_from_meta_data_service(app_id, self.CONST_CONFIG_PARAM_GATEWAY_PORTS)
            gateway_worker_ip = self.get_data_from_meta_data_service(app_id, self.ENV_CONFIG_PARAM_GATEWAY_WORKER_IP)
            gateway_worker_ports = self.get_data_from_meta_data_service(app_id,
                                                                        self.CONST_CONFIG_PARAM_GATEWAY_WORKER_PORTS)

            environment_type = self.find_environment_type(external_lb, service_name, app_id)

            if environment_type == WSO2AMStartupHandler.CONST_KUBERNETES:
                gateway_host = gateway_ip
                gateway_worker_host = gateway_worker_ip
            else:
                gateway_host_name = self.get_host_name_from_cluster(self.CONST_GATEWAY_MANAGER_SERVICE_NAME, app_id)
                gateway_worker_host_name = self.get_host_name_from_cluster(self.CONST_GATEWAY_WORKER_SERVICE_NAME, app_id)
                gateway_host = gateway_host_name
                gateway_worker_host = gateway_worker_host_name

                self.update_hosts_file(gateway_ip, gateway_host_name)
                self.update_hosts_file(gateway_worker_ip, gateway_worker_host_name)

            member_ip = socket.gethostbyname(socket.gethostname())
            self.set_host_name(app_id, service_name, member_ip)
            self.export_env_var("CONFIG_PARAM_LOCAL_MEMBER_HOST", member_ip)
            self.export_env_var(self.ENV_CONFIG_PARAM_GATEWAY_IP, gateway_host)
            self.set_gateway_ports(gateway_ports)
            self.export_env_var(self.ENV_CONFIG_PARAM_GATEWAY_WORKER_IP, gateway_worker_host)
            self.set_gateway_worker_ports(gateway_worker_ports)

            start_command = "exec ${CARBON_HOME}/bin/wso2server.sh -Dprofile=api-key-manager start"

        elif profile == self.CONST_GATEWAY_MANAGER:
            # this is for gateway manager profile
            # remove previous data from metadata service
            # add new values to meta data service - gateway ip, mgt-console port, pt http and https ports
            # retrieve values from meta data service - keymanager ip and mgt console port
            # check deployment is vm, if vm update /etc/hosts with values
            # export retrieve values as environment variables
            # export hostname for gateway-manager
            # set the start command

            self.remove_data_from_metadata(self.ENV_CONFIG_PARAM_GATEWAY_IP)
            self.remove_data_from_metadata(self.CONST_CONFIG_PARAM_GATEWAY_PORTS)

            self.add_data_to_meta_data_service(self.ENV_CONFIG_PARAM_GATEWAY_IP, load_balancer_ip)
            port_list = "Ports:" + mgt_https_proxy_port
            self.add_data_to_meta_data_service(self.CONST_CONFIG_PARAM_GATEWAY_PORTS, port_list)

            keymanager_ip = self.get_data_from_meta_data_service(app_id, self.ENV_CONFIG_PARAM_KEYMANAGER_IP)
            keymanager_ports = self.get_data_from_meta_data_service(app_id, self.CONST_CONFIG_PARAM_KEYMANAGER_PORTS)

            environment_type = self.find_environment_type(external_lb, service_name, app_id)

            if environment_type == WSO2AMStartupHandler.CONST_KUBERNETES:
                keymanager_host = keymanager_ip
            else:
                keymanager_host_name = self.get_host_name_from_cluster(self.CONST_KEY_MANAGER_SERVICE_NAME, app_id)
                keymanager_host = keymanager_host_name
                self.update_hosts_file(keymanager_ip, keymanager_host_name)

            self.export_env_var(self.ENV_CONFIG_PARAM_KEYMANAGER_IP, keymanager_host)
            km_port = self.set_keymanager_ports(keymanager_ports)

            member_ip = socket.gethostbyname(socket.gethostname())
            self.set_host_names_for_gw(app_id, member_ip)
            self.export_env_var("CONFIG_PARAM_LOCAL_MEMBER_HOST", member_ip)
            set_system_properties = "-Dkm.ip=" + keymanager_ip + " -Dkm.port=" + km_port
            start_command = "exec ${CARBON_HOME}/bin/wso2server.sh -Dprofile=gateway-manager " + set_system_properties + " start"


        elif profile == self.CONST_GATEWAY_WORKER:
            # this is for gateway worker profile
            # remove previous data from metadata service
            # retrieve values from meta data service - keymanager ip and mgt console port
            # export retrieve values as environment variables
            # check deployment is vm, if vm update /etc/hosts with values
            # export hostname for gateway-worker
            # set the start command

            self.remove_data_from_metadata(self.ENV_CONFIG_PARAM_GATEWAY_WORKER_IP)
            self.remove_data_from_metadata(self.CONST_CONFIG_PARAM_GATEWAY_WORKER_PORTS)

            self.add_data_to_meta_data_service(self.ENV_CONFIG_PARAM_GATEWAY_WORKER_IP, load_balancer_ip)
            port_list = "Ports:" + pt_http_proxy_port + ":" + pt_https_proxy_port
            self.add_data_to_meta_data_service(self.CONST_CONFIG_PARAM_GATEWAY_WORKER_PORTS, port_list)

            keymanager_ip = self.get_data_from_meta_data_service(app_id, self.ENV_CONFIG_PARAM_KEYMANAGER_IP)
            keymanager_ports = self.get_data_from_meta_data_service(app_id, self.CONST_CONFIG_PARAM_KEYMANAGER_PORTS)

            environment_type = self.find_environment_type(external_lb, service_name, app_id)

            if environment_type == WSO2AMStartupHandler.CONST_KUBERNETES:
                keymanager_host = keymanager_ip
            else:
                keymanager_host_name = self.get_host_name_from_cluster(self.CONST_KEY_MANAGER_SERVICE_NAME, app_id)
                keymanager_host = keymanager_host_name
                self.update_hosts_file(keymanager_ip, keymanager_host_name)

            self.export_env_var(self.ENV_CONFIG_PARAM_KEYMANAGER_IP, keymanager_host)
            km_port = self.set_keymanager_ports(keymanager_ports)

            member_ip = socket.gethostbyname(socket.gethostname())
            self.set_host_names_for_gw(app_id, member_ip)
            self.export_env_var("CONFIG_PARAM_LOCAL_MEMBER_HOST", member_ip)
            set_system_properties = "-Dkm.ip=" + keymanager_ip + " -Dkm.port=" + km_port

            start_command = "exec ${CARBON_HOME}/bin/wso2server.sh -Dprofile=gateway-worker " + set_system_properties + " start"


        elif profile == self.CONST_PUBLISHER:
            # this is for publisher profile
            # remove previous data from metadata service
            # add new values to meta data service - publisher ip
            # retrieve values from meta data service - store ip, km ip and mgt console port, gw ip, mgt console port, pt http and https ports
            # check deployment is vm, if vm update /etc/hosts with values
            # export retrieve values as environment variables
            # export hostname for publisher
            # set the start command

            self.remove_data_from_metadata(self.ENV_CONFIG_PARAM_PUBLISHER_IP)

            self.add_data_to_meta_data_service(self.ENV_CONFIG_PARAM_PUBLISHER_IP, load_balancer_ip)

            store_ip = self.get_data_from_meta_data_service(app_id, self.ENV_CONFIG_PARAM_STORE_IP)
            keymanager_ip = self.get_data_from_meta_data_service(app_id, self.ENV_CONFIG_PARAM_KEYMANAGER_IP)
            keymanager_ports = self.get_data_from_meta_data_service(app_id, self.CONST_CONFIG_PARAM_KEYMANAGER_PORTS)
            gateway_ip = self.get_data_from_meta_data_service(app_id, self.ENV_CONFIG_PARAM_GATEWAY_IP)
            gateway_ports = self.get_data_from_meta_data_service(app_id, self.CONST_CONFIG_PARAM_GATEWAY_PORTS)
            gateway_worker_ip = self.get_data_from_meta_data_service(app_id, self.ENV_CONFIG_PARAM_GATEWAY_WORKER_IP)
            gateway_worker_ports = self.get_data_from_meta_data_service(app_id,
                                                                        self.CONST_CONFIG_PARAM_GATEWAY_WORKER_PORTS)

            environment_type = self.find_environment_type(external_lb, service_name, app_id)

            if environment_type == WSO2AMStartupHandler.CONST_KUBERNETES:
                keymanager_host = keymanager_ip
                gateway_host = gateway_ip
                gateway_worker_host = gateway_worker_ip
                store_host = store_ip
            else:
                keymanager_host_name = self.get_host_name_from_cluster(self.CONST_KEY_MANAGER_SERVICE_NAME, app_id)
                gateway_host_name = self.get_host_name_from_cluster(self.CONST_GATEWAY_MANAGER_SERVICE_NAME, app_id)
                gateway_worker_host_name = self.get_host_name_from_cluster(self.CONST_GATEWAY_WORKER_SERVICE_NAME, app_id)
                store_host_name = self.get_host_name_from_cluster(self.CONST_STORE_SERVICE_NAME, app_id)
                keymanager_host = keymanager_host_name
                gateway_host = gateway_host_name
                gateway_worker_host = gateway_worker_host_name
                store_host = store_host_name

                self.update_hosts_file(keymanager_ip, keymanager_host_name)
                self.update_hosts_file(gateway_ip, gateway_host_name)
                self.update_hosts_file(gateway_worker_ip, gateway_worker_host_name)
                self.update_hosts_file(store_ip, store_host_name)

            self.export_env_var(self.ENV_CONFIG_PARAM_STORE_IP, store_host)
            self.export_env_var(self.ENV_CONFIG_PARAM_KEYMANAGER_IP, keymanager_host)
            self.set_keymanager_ports(keymanager_ports)
            self.export_env_var(self.ENV_CONFIG_PARAM_GATEWAY_IP, gateway_host)
            self.set_gateway_ports(gateway_ports)
            self.export_env_var(self.ENV_CONFIG_PARAM_GATEWAY_WORKER_IP, gateway_worker_host)
            self.set_gateway_worker_ports(gateway_worker_ports)

            member_ip = socket.gethostbyname(socket.gethostname())
            self.set_host_name(app_id, service_name, member_ip)
            self.export_env_var("CONFIG_PARAM_LOCAL_MEMBER_HOST", member_ip)
            start_command = "exec ${CARBON_HOME}/bin/wso2server.sh -Dprofile=api-publisher start"


        elif profile == self.CONST_STORE:
            # this is for store profile
            # remove previous data from metadata service
            # add new values to meta data service - store ip
            # retrieve values from meta data service - publisher ip, km ip and mgt console port, gw ip, mgt console port, pt http and https ports
            # check deployment is vm, if vm update /etc/hosts with values
            # export retrieve values as environment variables
            # export hostname for store
            # set the start command

            self.remove_data_from_metadata(self.ENV_CONFIG_PARAM_STORE_IP)

            self.add_data_to_meta_data_service(self.ENV_CONFIG_PARAM_STORE_IP, load_balancer_ip)

            publisher_ip = self.get_data_from_meta_data_service(app_id, self.ENV_CONFIG_PARAM_PUBLISHER_IP)
            keymanager_ip = self.get_data_from_meta_data_service(app_id, self.ENV_CONFIG_PARAM_KEYMANAGER_IP)
            keymanager_ports = self.get_data_from_meta_data_service(app_id, self.CONST_CONFIG_PARAM_KEYMANAGER_PORTS)
            gateway_ip = self.get_data_from_meta_data_service(app_id, self.ENV_CONFIG_PARAM_GATEWAY_IP)
            gateway_ports = self.get_data_from_meta_data_service(app_id, self.CONST_CONFIG_PARAM_GATEWAY_PORTS)
            gateway_worker_ip = self.get_data_from_meta_data_service(app_id, self.ENV_CONFIG_PARAM_GATEWAY_WORKER_IP)
            gateway_worker_ports = self.get_data_from_meta_data_service(app_id,
                                                                        self.CONST_CONFIG_PARAM_GATEWAY_WORKER_PORTS)

            environment_type = self.find_environment_type(external_lb, service_name, app_id)

            if environment_type == WSO2AMStartupHandler.CONST_KUBERNETES:
                keymanager_host = keymanager_ip
                gateway_host = gateway_ip
                gateway_worker_host = gateway_worker_ip
                publisher_host = publisher_ip
            else:
                keymanager_host_name = self.get_host_name_from_cluster(self.CONST_KEY_MANAGER_SERVICE_NAME, app_id)
                gateway_host_name = self.get_host_name_from_cluster(self.CONST_GATEWAY_MANAGER_SERVICE_NAME, app_id)
                gateway_worker_host_name = self.get_host_name_from_cluster(self.CONST_GATEWAY_WORKER_SERVICE_NAME, app_id)
                publisher_host_name = self.get_host_name_from_cluster(self.CONST_PUBLISHER_SERVICE_NAME, app_id)
                keymanager_host = keymanager_host_name
                gateway_host = gateway_host_name
                gateway_worker_host = gateway_worker_host_name
                publisher_host = publisher_host_name

                self.update_hosts_file(keymanager_ip, keymanager_host_name)
                self.update_hosts_file(gateway_ip, gateway_host_name)
                self.update_hosts_file(gateway_worker_ip, gateway_worker_host_name)
                self.update_hosts_file(publisher_ip, publisher_host_name)

            self.export_env_var(self.ENV_CONFIG_PARAM_STORE_IP, publisher_host)
            self.export_env_var(self.ENV_CONFIG_PARAM_KEYMANAGER_IP, keymanager_host)
            self.set_keymanager_ports(keymanager_ports)
            self.export_env_var(self.ENV_CONFIG_PARAM_GATEWAY_IP, gateway_host)
            self.set_gateway_ports(gateway_ports)
            self.export_env_var(self.ENV_CONFIG_PARAM_GATEWAY_WORKER_IP, gateway_worker_host)
            self.set_gateway_worker_ports(gateway_worker_ports)

            member_ip = socket.gethostbyname(socket.gethostname())
            self.set_host_name(app_id, service_name, member_ip)
            self.export_env_var("CONFIG_PARAM_LOCAL_MEMBER_HOST", member_ip)
            start_command = "exec ${CARBON_HOME}/bin/wso2server.sh -Dprofile=api-store start"


        elif profile == self.CONST_PUBSTORE:
            # Publisher and Store runs on a same node (PubStore profile)
            # retrieve values from meta data service - store ip, km ip and mgt console port, gw ip, mgt console port, pt http and https ports
            # check deployment is vm, if vm update /etc/hosts with values
            # export retrieve values as environment variables
            # export hostname for pubStore
            # set the start command

            keymanager_ip = self.get_data_from_meta_data_service(app_id, self.ENV_CONFIG_PARAM_KEYMANAGER_IP)
            keymanager_ports = self.get_data_from_meta_data_service(app_id, self.CONST_CONFIG_PARAM_KEYMANAGER_PORTS)
            gateway_ip = self.get_data_from_meta_data_service(app_id, self.ENV_CONFIG_PARAM_GATEWAY_IP)
            gateway_ports = self.get_data_from_meta_data_service(app_id, self.CONST_CONFIG_PARAM_GATEWAY_PORTS)
            gateway_worker_ip = self.get_data_from_meta_data_service(app_id, self.ENV_CONFIG_PARAM_GATEWAY_WORKER_IP)
            gateway_worker_ports = self.get_data_from_meta_data_service(app_id,
                                                                        self.CONST_CONFIG_PARAM_GATEWAY_WORKER_PORTS)

            environment_type = self.find_environment_type(external_lb, service_name, app_id)

            if environment_type == WSO2AMStartupHandler.CONST_KUBERNETES:
                keymanager_host = keymanager_ip
                gateway_host = gateway_ip
                gateway_worker_host = gateway_worker_ip
            else:
                keymanager_host_name = self.get_host_name_from_cluster(self.CONST_KEY_MANAGER_SERVICE_NAME, app_id)
                gateway_host_name = self.get_host_name_from_cluster(self.CONST_GATEWAY_MANAGER_SERVICE_NAME, app_id)
                gateway_worker_host_name = self.get_host_name_from_cluster(self.CONST_GATEWAY_WORKER_SERVICE_NAME, app_id)
                keymanager_host = keymanager_host_name
                gateway_host = gateway_host_name
                gateway_worker_host = gateway_worker_host_name

                self.update_hosts_file(keymanager_ip, keymanager_host_name)
                self.update_hosts_file(gateway_ip, gateway_host_name)
                self.update_hosts_file(gateway_worker_ip, gateway_worker_host_name)

            self.export_env_var(self.ENV_CONFIG_PARAM_KEYMANAGER_IP, keymanager_host)
            self.set_keymanager_ports(keymanager_ports)
            self.export_env_var(self.ENV_CONFIG_PARAM_GATEWAY_IP, gateway_host)
            self.set_gateway_ports(gateway_ports)
            self.export_env_var(self.ENV_CONFIG_PARAM_GATEWAY_WORKER_IP, gateway_worker_host)
            self.set_gateway_worker_ports(gateway_worker_ports)

            member_ip = socket.gethostbyname(socket.gethostname())
            self.set_host_name(app_id, service_name, member_ip)
            self.export_env_var("CONFIG_PARAM_LOCAL_MEMBER_HOST", member_ip)
            start_command = "exec ${CARBON_HOME}/bin/wso2server.sh start"

        else:
            # This is the default profile
            # for kubernetes, load balancer ip should specify and no need for vm
            # expose gateway ip, pt http and https ports (This is to access from external)
            # set start command

            if load_balancer_ip is not None:
                gateway_ip = load_balancer_ip
                gateway_pt_http_pp = pt_http_proxy_port
                gateway_pt_https_pp = pt_https_proxy_port
                self.export_env_var(self.ENV_CONFIG_PARAM_GATEWAY_WORKER_IP, gateway_ip)
                self.export_env_var(self.ENV_CONFIG_PARAM_GATEWAY_WORKER_PT_HTTP_PROXY_PORT, gateway_pt_http_pp)
                self.export_env_var(self.ENV_CONFIG_PARAM_GATEWAY_WORKER_PT_HTTPS_PROXY_PORT, gateway_pt_https_pp)

            start_command = "exec ${CARBON_HOME}/bin/wso2server.sh start"

        # start configurator
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


    def set_keymanager_ports(self, keymanager_ports):
        """
        Expose keymanager ports
        :return: void
        """
        keymanager_mgt_https_pp = None
        if keymanager_ports is not None:
            keymanager_ports_array = keymanager_ports.split(":")
            if keymanager_ports_array:
                keymanager_mgt_https_pp = keymanager_ports_array[1]

        self.export_env_var(self.ENV_CONFIG_PARAM_KEYMANAGER_HTTPS_PROXY_PORT, str(keymanager_mgt_https_pp))

        return keymanager_mgt_https_pp

    def set_gateway_ports(self, gateway_ports):
        """
        Expose gateway ports
        Input- Ports:30003
        :return: void
        """
        gateway_mgt_https_pp = None

        if gateway_ports is not None:
            gateway_ports_array = gateway_ports.split(":")
            if gateway_ports_array:
                gateway_mgt_https_pp = gateway_ports_array[1]

        self.export_env_var(self.ENV_CONFIG_PARAM_GATEWAY_HTTPS_PROXY_PORT, str(gateway_mgt_https_pp))


    def set_gateway_worker_ports(self, gateway_worker_ports):
        """
        Expose gateway worker ports
        :return: void
        """
        gateway_pt_http_pp = None
        gateway_pt_https_pp = None

        if gateway_worker_ports is not None:
            gateway_wk_ports_array = gateway_worker_ports.split(":")
            if gateway_wk_ports_array:
                gateway_pt_http_pp = gateway_wk_ports_array[1]
                gateway_pt_https_pp = gateway_wk_ports_array[2]

        self.export_env_var(self.ENV_CONFIG_PARAM_GATEWAY_WORKER_PT_HTTP_PROXY_PORT, str(gateway_pt_http_pp))
        self.export_env_var(self.ENV_CONFIG_PARAM_GATEWAY_WORKER_PT_HTTPS_PROXY_PORT, str(gateway_pt_https_pp))

    def populate_sub_domains(self, service_name):
        """
        set sub domain based on the service name
        for manager, sub domain as mgt
        for worker, sub domain as worker
        :return: void
        """
        sub_domain = None
        if service_name.endswith(self.CONST_MANAGER):
            sub_domain = self.CONST_MGT
        elif service_name.endswith(self.CONST_WORKER):
            sub_domain = self.CONST_WORKER
        if sub_domain is not None:
            self.export_env_var(self.ENV_CONFIG_PARAM_SUB_DOMAIN, sub_domain)

    def read_proxy_port(self, port_mappings_str, port_mapping_name, port_mapping_protocol):
        """
        returns proxy port of the requested port mapping
        :return: void
        """
        # port mappings format: NAME:mgt-http|PROTOCOL:http|PORT:30001|PROXY_PORT:0|TYPE:NodePort;
        #                       NAME:mgt-https|PROTOCOL:https|PORT:30002|PROXY_PORT:0|TYPE:NodePort;
        #                       NAME:pt-http|PROTOCOL:http|PORT:30003|PROXY_PORT:8280|TYPE:ClientIP;
        #                       NAME:pt-https|PROTOCOL:https|PORT:30004|PROXY_PORT:8243|TYPE:NodePort

        service_proxy_port = None
        if port_mappings_str is not None:
            port_mappings_array = port_mappings_str.split(";")
            if port_mappings_array:

                for port_mapping in port_mappings_array:
                    name_value_array = port_mapping.split("|")
                    name = name_value_array[0].split(":")[1]
                    protocol = name_value_array[1].split(":")[1]
                    proxy_port = name_value_array[3].split(":")[1]
                    # If PROXY_PORT is not set, set PORT as the proxy port (ex:Kubernetes),
                    if proxy_port == '0':
                        proxy_port = name_value_array[2].split(":")[1]

                    if name == port_mapping_name and protocol == port_mapping_protocol:
                        service_proxy_port = proxy_port

        return service_proxy_port


    def get_data_from_meta_data_service(self, app_id, receive_data):
        """
        Get data from meta data service
        :return: received data
        """
        mds_response = None
        while mds_response is None:
            WSO2AMStartupHandler.log.info(
                "Waiting for " + receive_data + " to be available from metadata service for app ID: %s" % app_id)
            time.sleep(1)
            mds_response = mdsclient.get(app=True)
            if mds_response is not None and mds_response.properties.get(receive_data) is None:
                mds_response = None

        return mds_response.properties[receive_data]


    def add_data_to_meta_data_service(self, key, value):
        """
        add data to meta data service
        :return: void
        """
        mdsclient.MDSPutRequest()
        data = {"key": key, "values": [value]}
        mdsclient.put(data, app=True)


    def remove_data_from_metadata(self, key):
        """
        remove data from meta data service
        :return: void
        """
        mds_response = mdsclient.get(app=True)

        if mds_response is not None and mds_response.properties.get(key) is not None:
            read_data = mds_response.properties[key]
            check_str = isinstance(read_data, (str, unicode))

            if check_str == True:
                mdsclient.delete_property_value(key, read_data)
            else:
                check_int = isinstance(read_data, int)
                if check_int == True:
                    mdsclient.delete_property_value(key, read_data)
                else:
                    for entry in read_data:
                        mdsclient.delete_property_value(key, entry)


    def set_cluster_ids(self, app_id, service_list):
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


    def export_env_var(self, variable, value):
        """
        Export value as an environment variable
        :return: void
        """
        if value is not None:
            os.environ[variable] = value
            WSO2AMStartupHandler.log.info("Exported environment variable %s: %s" % (variable, value))
        else:
            WSO2AMStartupHandler.log.warn("Could not export environment variable %s " % variable)


    def read_cluster_id_of_service(self, service_name, app_id):
        """
        Get the cluster_id of a service read from topology
        :return: cluster_id
        """
        cluster_id = None
        clusters = self.get_clusters_from_topology(service_name)

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


    def set_host_names_for_gw(self, app_id, member_ip):
        """
        Set hostnames of services read from topology for worker manager instances
        exports MgtHostName and HostName
        :return: void
        """
        for service_name in self.GATEWAY_SERVICES:
            if service_name.endswith(self.CONST_MANAGER):
                mgt_host_name = self.get_host_name_from_cluster(service_name, app_id)
            elif service_name.endswith(self.CONST_WORKER):
                host_name = self.get_host_name_from_cluster(service_name, app_id)
                self.update_hosts_file(member_ip, host_name)

        self.export_env_var(self.ENV_CONFIG_PARAM_MGT_HOST_NAME, mgt_host_name)
        self.export_env_var(self.ENV_CONFIG_PARAM_HOST_NAME, host_name)


    def set_host_name(self, app_id, service_name, member_ip):
        """
        Set hostname of service read from topology for any service name
        export hostname and update the /etc/hosts
        :return: void
        """
        host_name = self.get_host_name_from_cluster(service_name, app_id)
        self.export_env_var(self.ENV_CONFIG_PARAM_HOST_NAME, host_name)
        self.update_hosts_file(member_ip, host_name)


    def get_host_name_from_cluster(self, service_name, app_id):
        """
        Get hostname for a service
        :return: hostname
        """
        clusters = self.get_clusters_from_topology(service_name)

        if clusters is not None:
            for cluster in clusters:
                if cluster.app_id == app_id:
                    hostname = cluster.hostnames[0]

        return hostname

    def check_for_kubernetes_cluster(self, service_name, app_id):
        """
        Check the deployment is kubernetes
        :return: True
        """
        isKubernetes = False
        clusters = self.get_clusters_from_topology(service_name)

        if clusters is not None:
            for cluster in clusters:
                if cluster.app_id == app_id:
                    isKubernetes = cluster.is_kubernetes_cluster

        return isKubernetes


    def get_clusters_from_topology(self, service_name):
        """
        get clusters from topology
        :return: clusters
        """
        clusters = None
        topology = TopologyContext().get_topology()

        if topology is not None:
            if topology.service_exists(service_name):
                service = topology.get_service(service_name)
                clusters = service.get_clusters()
            else:
                WSO2AMStartupHandler.log.error("[Service] %s is not available in topology" % service_name)

        return clusters


    def find_environment_type(self, external_lb, service_name, app_id):
        """
        Check for vm or kubernetes
        :return: Vm or Kubernetes
        """

        if external_lb == 'true':
            return WSO2AMStartupHandler.CONST_EXTERNAL_LB_FOR_KUBERNETES
        else:
            isKubernetes = self.check_for_kubernetes_cluster(service_name, app_id)

            if isKubernetes:
                return WSO2AMStartupHandler.CONST_KUBERNETES
            else:
                return WSO2AMStartupHandler.CONST_VM


