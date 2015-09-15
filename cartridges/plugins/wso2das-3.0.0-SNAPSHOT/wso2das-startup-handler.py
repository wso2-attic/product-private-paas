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
from entity import *
import pymysql as db


class WSO2DASStartupHandler(ICartridgeAgentPlugin):
    log = LogFactory().get_log(__name__)

    CONST_SERVICE_NAME = "SERVICE_NAME"
    CONST_APPLICATION_ID = "APPLICATION_ID"
    CONST_MB_IP = "MB_IP"
    CONST_PORT_MAPPING_MGT_CONSOLE = "mgt-console"
    CONST_MYSQL_HOST = "MYSQL_HOST"
    CONST_MYSQL_ROOT_USERNAME = "MYSQL_ROOT_USERNAME"
    CONST_MYSQL_ROOT_PASSWORD = "MYSQL_ROOT_PASSWORD"
    CONST_ZOOKEEPER_SERVICE_NAME = "das-zookeeper"
    CONST_HBASE_SERVICE_NAME = "hbase"
    CONST_PPAAS_MEMBERSHIP_SCHEME = "private-paas"
    CONST_PORT_MAPPING_MGT_HTTP_TRANSPORT = "mgt-http"
    CONST_PORT_MAPPING_MGT_HTTPS_TRANSPORT = "mgt-https"
    CONST_PROTOCOL_HTTP = "http"
    CONST_PROTOCOL_HTTPS = "https"
    CONST_PORT_MAPPINGS = "PORT_MAPPINGS"

    CONST_DAS_RECEIVER_SERVICE_NAME = "das-receiver"
    CONST_DAS_RECEIVER_MGT_SERVICE_NAME = "das-receiver-manager"
    CONST_DAS_ANALYTICS_SERVICE_NAME = "das-analytics"
    CONST_DAS_ANALYTICS_MGT_SERVICE_NAME = "das-analytics-manager"
    CONST_DAS_DASHBOARD_SERVICE_NAME = "das-dashboard"

    CONST_ANALYTICS_FS_DB = "ANALYTICS_FS_DB"
    CONST_ANALYTICS_FS_DB_USER_NAME = "FS_user"
    CONST_ANALYTICS_FS_DB_PASSWORD = "fs123"
    CONST_ANALYTICS_PROCESSED_DATA_STORE = "ANALYTICS_PROCESSED_DATA_STORE"
    CONST_ANALYTICS_PDS_DB_USER_NAME = "DS_user"
    CONST_ANALYTICS_PDS_DB_PASSWORD = "ds123"

    ENV_CONFIG_PARAM_MEMBERSHIP_SCHEME = 'CONFIG_PARAM_MEMBERSHIP_SCHEME'
    ENV_CONFIG_PARAM_HBASE_REGIONSERVER_DATA = "CONFIG_PARAM_HBASE_REGIONSERVER_DATA"
    ENV_CONFIG_PARAM_LOCAL_MEMBER_HOST = "CONFIG_PARAM_LOCAL_MEMBER_HOST"
    ENV_CONFIG_PARAM_CLUSTER_IDs = 'CONFIG_PARAM_CLUSTER_IDs'

    ENV_CONFIG_PARAM_WSO2_ANALYTICS_WSO2_ANALYTICS_FS_DB_URL = "CONFIG_PARAM_WSO2_ANALYTICS_WSO2_ANALYTICS_FS_DB_URL"
    ENV_CONFIG_PARAM_WSO2_ANALYTICS_WSO2_ANALYTICS_FS_DB_USER_NAME = "CONFIG_PARAM_WSO2_ANALYTICS_WSO2_ANALYTICS_FS_DB_USER_NAME"
    ENV_CONFIG_PARAM_WSO2_ANALYTICS_WSO2_ANALYTICS_FS_DB_PASSWORD = "CONFIG_PARAM_WSO2_ANALYTICS_WSO2_ANALYTICS_FS_DB_PASSWORD"

    ENV_CONFIG_PARAM_WSO2_ANALYTICS_PROCESSED_DATA_STORE_DB_URL = "CONFIG_PARAM_WSO2_ANALYTICS_PROCESSED_DATA_STORE_DB_URL"
    ENV_CONFIG_PARAM_WSO2_ANALYTICS_PROCESSED_DATA_STORE_DB_USER_NAME = "CONFIG_PARAM_WSO2_ANALYTICS_PROCESSED_DATA_STORE_DB_USER_NAME"
    ENV_CONFIG_PARAM_WSO2_ANALYTICS_PROCESSED_DATA_STORE_DB_PASSWORD = "CONFIG_PARAM_WSO2_ANALYTICS_PROCESSED_DATA_STORE_DB_PASSWORD"

    ENV_CONFIG_PARAM_MB_IP = "CONFIG_PARAM_MB_IP"
    ENV_CONFIG_PARAM_PROFILE = "CONFIG_PARAM_PROFILE"
    ENV_CONFIG_PARAM_ZK_HOST = "CONFIG_PARAM_ZK_HOST"
    ENV_CONFIG_PARAM_HBASE_MASTER_HOST = "CONFIG_PARAM_HBASE_MASTER_HOST"
    ENV_CONFIG_PARAM_CLUSTERING = 'CONFIG_PARAM_CLUSTERING'

    ENV_CONFIG_PARAM_HTTP_PROXY_PORT = 'CONFIG_PARAM_HTTP_PROXY_PORT'
    ENV_CONFIG_PARAM_HTTPS_PROXY_PORT = 'CONFIG_PARAM_HTTPS_PROXY_PORT'
    ENV_CONFIG_PARAM_HOST_NAME = 'CONFIG_PARAM_HOST_NAME'
    ENV_CONFIG_PARAM_CARBON_SPARK_MASTER_COUNT = 'CONFIG_PARAM_CARBON_SPARK_MASTER_COUNT'


    def run_plugin(self, values):

        profile = os.environ[WSO2DASStartupHandler.ENV_CONFIG_PARAM_PROFILE]
        app_id = values[WSO2DASStartupHandler.CONST_APPLICATION_ID]
        mb_ip = values[WSO2DASStartupHandler.CONST_MB_IP]
        clustering = values.get(WSO2DASStartupHandler.ENV_CONFIG_PARAM_CLUSTERING, 'false')
        membership_scheme = values.get(WSO2DASStartupHandler.ENV_CONFIG_PARAM_MEMBERSHIP_SCHEME,
                                       WSO2DASStartupHandler.CONST_PPAAS_MEMBERSHIP_SCHEME)
        service_name = values[WSO2DASStartupHandler.CONST_SERVICE_NAME]
        port_mappings_str = values[WSO2DASStartupHandler.CONST_PORT_MAPPINGS].replace("'", "")

        WSO2DASStartupHandler.log.info("Profile : %s " % profile)
        WSO2DASStartupHandler.log.info("Application ID: %s" % app_id)
        WSO2DASStartupHandler.log.info("Mb IP: %s" % mb_ip)
        WSO2DASStartupHandler.log.info("Clustering: %s" % clustering)
        WSO2DASStartupHandler.log.info("Membership Scheme: %s" % membership_scheme)
        WSO2DASStartupHandler.log.info("Service Name: %s" % service_name)
        WSO2DASStartupHandler.log.info("Port mapping: %s" % port_mappings_str)

        mgt_http_proxy_port = self.read_proxy_port(port_mappings_str,
                                                   WSO2DASStartupHandler.CONST_PORT_MAPPING_MGT_HTTP_TRANSPORT,
                                                   WSO2DASStartupHandler.CONST_PROTOCOL_HTTP)
        mgt_https_proxy_port = self.read_proxy_port(port_mappings_str,
                                                    WSO2DASStartupHandler.CONST_PORT_MAPPING_MGT_HTTPS_TRANSPORT,
                                                    WSO2DASStartupHandler.CONST_PROTOCOL_HTTPS)

        self.export_env_var(WSO2DASStartupHandler.ENV_CONFIG_PARAM_HTTP_PROXY_PORT, mgt_http_proxy_port)
        self.export_env_var(WSO2DASStartupHandler.ENV_CONFIG_PARAM_HTTPS_PROXY_PORT, mgt_https_proxy_port)

        self.export_env_var(WSO2DASStartupHandler.ENV_CONFIG_PARAM_MB_IP, mb_ip)

        zookeeper_cluster = self.get_clusters_from_topology(WSO2DASStartupHandler.CONST_ZOOKEEPER_SERVICE_NAME)
        zookeeper_ip = self.get_zookeeper_member_ips(zookeeper_cluster,app_id)
        hbase_master_ip = self.read_member_ip_from_topology(WSO2DASStartupHandler.CONST_HBASE_SERVICE_NAME, app_id)

        self.export_env_var(WSO2DASStartupHandler.ENV_CONFIG_PARAM_ZK_HOST, zookeeper_ip)
        self.export_env_var(WSO2DASStartupHandler.ENV_CONFIG_PARAM_HBASE_MASTER_HOST, hbase_master_ip)

        # export CONFIG_PARAM_MEMBERSHIP_SCHEME
        self.export_env_var(WSO2DASStartupHandler.ENV_CONFIG_PARAM_MEMBERSHIP_SCHEME, membership_scheme)

        # set hostname
        member_ip = socket.gethostbyname(socket.gethostname())
        self.set_host_name(app_id, service_name, member_ip)

        if clustering == 'true' and membership_scheme == self.CONST_PPAAS_MEMBERSHIP_SCHEME:
            service_list = self.get_service_list_for_clustering(service_name)
            self.set_cluster_ids(app_id, service_list)
            member_ip = socket.gethostbyname(socket.gethostname())
            self.export_env_var(WSO2DASStartupHandler.ENV_CONFIG_PARAM_LOCAL_MEMBER_HOST, member_ip)

        self.map_hbase_hostname()

        if profile == "analytics":
            mgt_count = self.get_member_count_for_service(app_id,WSO2DASStartupHandler.CONST_DAS_ANALYTICS_MGT_SERVICE_NAME)
            member_count = self.get_member_count_for_service(app_id,WSO2DASStartupHandler.CONST_DAS_ANALYTICS_SERVICE_NAME)

            self.export_env_var(WSO2DASStartupHandler.ENV_CONFIG_PARAM_CARBON_SPARK_MASTER_COUNT,mgt_count+member_count)


        # configure server
        WSO2DASStartupHandler.log.info("Configuring WSO2 DAS ...")
        config_command = "python ${CONFIGURATOR_HOME}/configurator.py"
        env_var = os.environ.copy()
        p = subprocess.Popen(config_command, env=env_var, shell=True)
        output, errors = p.communicate()
        WSO2DASStartupHandler.log.info("WSO2 DAS configured successfully")


        # start server
        WSO2DASStartupHandler.log.info("Starting WSO2 DAS...")
        profile = os.environ['CONFIG_PARAM_PROFILE']
        WSO2DASStartupHandler.log.info("Profile : %s " % profile)
        start_command = None
        if profile:
            if profile == "receiver":
                start_command = "exec ${CARBON_HOME}/bin/wso2server.sh start -Dsetup -DdisableAnalyticsExecution=true -DdisableAnalyticsEngine=true"
            elif profile == "analytics":
                start_command = "exec ${CARBON_HOME}/bin/wso2server.sh start -Dsetup -DdisableEventSink=true"
            elif profile == "dashboard":
                start_command = "exec ${CARBON_HOME}/bin/wso2server.sh start -Dsetup -DdisableEventSink=true -DdisableAnalyticsExecution=true -DdisableAnalyticsEngine=true"
            elif profile == "default":
                start_command = "exec ${CARBON_HOME}/bin/wso2server.sh -Dsetup start"
            else:
                WSO2DASStartupHandler.log.info("Invalid profile :" + profile)
        WSO2DASStartupHandler.log.info("Start command : %s" % start_command)
        env_var = os.environ.copy()
        p = subprocess.Popen(start_command, env=env_var, shell=True)
        output, errors = p.communicate()
        WSO2DASStartupHandler.log.debug("WSO2 DAS started successfully")


    def get_member_count_for_service(self,app_id,service_name):
        """
        Return member count for a service
        :return: member_count
        """
        clusters = self.get_clusters_from_topology(service_name)

        if clusters is not None:
            for cluster in clusters:
                if cluster.app_id == app_id:
                    members = cluster.get_members()

        if members is not None:
            for member in members:
                properties = member.properties
                if properties is not None:
                    member_count=properties["MIN_COUNT"]

                    return member_count


    def get_zookeeper_member_ips(self, zookeeper_cluster,app_id):
        """
        returns zookeeper member ip list
        :return: default_member_ip_list
        """
        default_private_ip_list = ""

        if zookeeper_cluster is not None:
            for cluster in zookeeper_cluster:
                if cluster.app_id == app_id:
                    members = cluster.get_members()

        if members is not None:
            for member in members:
                member_ip = member.member_default_private_ip

                if member_ip is not None:
                    default_private_ip_list = default_private_ip_list + member_ip + ","

        return default_private_ip_list[:-1]


    def get_service_list_for_clustering(self, service_name):
        """
        returns the particular service list for a given service name
        :return: service_list
        """
        if service_name == WSO2DASStartupHandler.CONST_DAS_RECEIVER_SERVICE_NAME or service_name == \
                WSO2DASStartupHandler.CONST_DAS_RECEIVER_MGT_SERVICE_NAME:
            return [WSO2DASStartupHandler.CONST_DAS_RECEIVER_SERVICE_NAME,
                    WSO2DASStartupHandler.CONST_DAS_RECEIVER_MGT_SERVICE_NAME]

        elif service_name == WSO2DASStartupHandler.CONST_DAS_ANALYTICS_SERVICE_NAME or service_name == \
                WSO2DASStartupHandler.CONST_DAS_ANALYTICS_MGT_SERVICE_NAME:
            return [WSO2DASStartupHandler.CONST_DAS_ANALYTICS_SERVICE_NAME,
                    WSO2DASStartupHandler.CONST_DAS_ANALYTICS_MGT_SERVICE_NAME]

        elif service_name == WSO2DASStartupHandler.CONST_DAS_DASHBOARD_SERVICE_NAME:
            return [WSO2DASStartupHandler.CONST_DAS_DASHBOARD_SERVICE_NAME]


    def create_database(self, app_id, databasename, username, password, remote_host):
        """
        create database for given values
        :return: void
        """
        remote_username = self.get_data_from_meta_data_service(app_id, WSO2DASStartupHandler.CONST_MYSQL_ROOT_USERNAME)
        remote_password = self.get_data_from_meta_data_service(app_id, WSO2DASStartupHandler.CONST_MYSQL_ROOT_PASSWORD)

        WSO2DASStartupHandler.log.info("mysql server conf [host]:%s [username]:%s [password]:%s", remote_host,
                                       remote_username, remote_password)
        con = None
        try:
            con = db.connect(host=remote_host, user=remote_username, passwd=remote_password)
            cur = con.cursor()
            cur.execute('CREATE DATABASE IF NOT EXISTS ' + databasename + ' character set latin1;')
            cur.execute('USE ' + databasename + ';')
            cur.execute(
                "GRANT ALL ON " + databasename + ".* TO " + username + "@'%' IDENTIFIED BY '" + password + "';")
            WSO2DASStartupHandler.log.info("Database %s created successfully" % databasename)
        except db.Error, e:
            WSO2DASStartupHandler.log.error("Error in creating database %d: %s" % (e.args[0], e.args[1]))

        finally:
            if con:
                con.close()


    def get_data_from_meta_data_service(self, app_id, receive_data):
        """
        Get data from meta data service
        :return: received data
        """
        mds_response = None
        while mds_response is None:
            WSO2DASStartupHandler.log.info(
                "Waiting for " + receive_data + " to be available from metadata service for app ID: %s" % app_id)
            time.sleep(1)
            mds_response = mdsclient.get(app=True)
            if mds_response is not None and mds_response.properties.get(receive_data) is None:
                mds_response = None

        return mds_response.properties[receive_data]


    def map_hbase_hostname(self):
        """
        populate hostnames of hbase regionservers
        :return: void
        """
        mds_response = mdsclient.get(app=True)
        if mds_response is not None and mds_response.properties.get(
                WSO2DASStartupHandler.ENV_CONFIG_PARAM_HBASE_REGIONSERVER_DATA) is None:
            return
        else:
            hbase_rs_hostmap = mds_response.properties[WSO2DASStartupHandler.ENV_CONFIG_PARAM_HBASE_REGIONSERVER_DATA]

            WSO2DASStartupHandler.log.info("Hbase RS hostnames : %s" % hbase_rs_hostmap)

            if isinstance(hbase_rs_hostmap, (str, unicode)):
                hbase_list = hbase_rs_hostmap.split(":")
                config_command = "echo " + hbase_list[1] + "    " + hbase_list[0] + "  >> /etc/hosts"
                WSO2DASStartupHandler.log.info("Config command %s" % config_command)
                env_var = os.environ.copy()
                p = subprocess.Popen(config_command, env=env_var, shell=True)
                output, errors = p.communicate()
                WSO2DASStartupHandler.log.info("Entry added to /etc/hosts")
            else:
                for entry in hbase_rs_hostmap:
                    hbase_list = entry.split(":")
                    config_command = "echo " + hbase_list[1] + "    " + hbase_list[0] + "  >> /etc/hosts"
                    WSO2DASStartupHandler.log.info("Config command %s" % config_command)
                    env_var = os.environ.copy()
                    p = subprocess.Popen(config_command, env=env_var, shell=True)
                    output, errors = p.communicate()


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
                WSO2DASStartupHandler.log.error("[Service] %s is not available in topology" % service_name)

        return clusters


    def get_portal_ip(self, service_name, app_id):
        """
        Return portal ip of mgt-console
        :return: portal_ip
        """
        portal_ip = None
        clusters = self.get_clusters_from_topology(service_name)

        if clusters is not None:
            for cluster in clusters:
                if cluster.app_id == app_id:
                    kubernetesServices = cluster.get_kubernetesServices()

        if kubernetesServices is not None:
            for kb_service in kubernetesServices:
                if kb_service.portName == WSO2DASStartupHandler.CONST_PORT_MAPPING_MGT_CONSOLE:
                    portal_ip = kb_service.portalIP
        else:
            WSO2DASStartupHandler.log.error("Kubernetes Services are not available for [Service] %s" % service_name)

        return portal_ip


    def export_env_var(self, variable, value):
        """
        Export value as an environment variable
        :return: void
        """
        if value is not None:
            os.environ[variable] = value
            WSO2DASStartupHandler.log.info("Exported environment variable %s: %s" % (variable, value))
        else:
            WSO2DASStartupHandler.log.warn("Could not export environment variable %s " % variable)


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

    def read_member_ip_from_topology(self, service_name, app_id):
        """
        get member ip from topology
        :return: member ip
        """
        members = None
        member_ip = None

        clusters = self.get_clusters_from_topology(service_name)

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

    def update_hosts_file(self, ip_address, host_name):
        """
        Updates /etc/hosts file with clustering hostnames
        :return: void
        """
        config_command = "echo %s  %s >> /etc/hosts" % (ip_address, host_name)
        env_var = os.environ.copy()
        p = subprocess.Popen(config_command, env=env_var, shell=True)
        output, errors = p.communicate()
        WSO2DASStartupHandler.log.info(
            "Successfully updated [ip_address] %s & [hostname] %s in etc/hosts" % (ip_address, host_name))