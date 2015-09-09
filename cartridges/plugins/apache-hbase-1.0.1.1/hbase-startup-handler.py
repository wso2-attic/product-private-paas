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

from plugins.contracts import ICartridgeAgentPlugin
from modules.util.log import LogFactory
import subprocess
import os
import time
from entity import *
import socket
import mdsclient


class HbaseStartupHandler(ICartridgeAgentPlugin):
    log = LogFactory().get_log(__name__)

    CONST_PORT_MAPPING_MGT_CONSOLE = "mgt-console"
    CONST_SERVICE_NAME = "SERVICE_NAME"
    CONST_HADOOP_SERVICE_NAME = "hadoop"
    CONST_ZOOKEEPER_SERVICE_NAME = "das-zookeeper"
    CONST_HBASE_SERVICE_NAME = "hbase"
    CONST_APPLICATION_ID = "APPLICATION_ID"

    ENV_CLUSTER = "CLUSTER"
    ENV_CONFIG_PARAM_HDFS_HOST = "CONFIG_PARAM_HDFS_HOST"
    ENV_CONFIG_PARAM_ZOOKEEPER_HOST = "CONFIG_PARAM_ZOOKEEPER_HOST"
    ENV_CONFIG_PARAM_HBASE_MASTER = "CONFIG_PARAM_HBASE_MASTER"
    ENV_CONFIG_PARAM_HBASE_MASTER_HOSTNAME = "CONFIG_PARAM_HBASE_MASTER_HOSTNAME"
    ENV_CONFIG_PARAM_HBASE_REGIONSERVER_DATA = "CONFIG_PARAM_HBASE_REGIONSERVER_DATA"


    def run_plugin(self, values):

        app_id = values[HbaseStartupHandler.CONST_APPLICATION_ID]
        clustering_enable = os.environ.get(HbaseStartupHandler.ENV_CLUSTER)

        HbaseStartupHandler.log.info("Application ID: %s" % app_id)
        HbaseStartupHandler.log.info("Clustering Enable : %s" % clustering_enable)

        hadoop_master_ip = self.read_member_ip_from_topology(HbaseStartupHandler.CONST_HADOOP_SERVICE_NAME, app_id)

        zookeeper_cluster = self.get_clusters_from_topology(HbaseStartupHandler.CONST_ZOOKEEPER_SERVICE_NAME)
        zookeeper_ip = self.get_zookeeper_member_ips(zookeeper_cluster, app_id)

        self.export_env_var(HbaseStartupHandler.ENV_CONFIG_PARAM_HDFS_HOST, hadoop_master_ip)
        self.export_env_var(HbaseStartupHandler.ENV_CONFIG_PARAM_ZOOKEEPER_HOST, zookeeper_ip)

        if clustering_enable == 'true':
            # This is the master node

            self.remove_data_from_metadata(HbaseStartupHandler.ENV_CONFIG_PARAM_HBASE_MASTER_HOSTNAME)
            self.remove_data_from_metadata(HbaseStartupHandler.ENV_CONFIG_PARAM_HBASE_REGIONSERVER_DATA)

            master_hostname = socket.gethostname()
            self.add_data_to_meta_data_service(HbaseStartupHandler.ENV_CONFIG_PARAM_HBASE_MASTER_HOSTNAME,
                                               master_hostname)

            server_ip = socket.gethostbyname(master_hostname)
            data = master_hostname + ":" + server_ip
            self.add_data_to_meta_data_service(HbaseStartupHandler.ENV_CONFIG_PARAM_HBASE_REGIONSERVER_DATA, data)


        else:

            hbase_master_ip = self.read_member_ip_from_topology(HbaseStartupHandler.CONST_HBASE_SERVICE_NAME, app_id)
            self.export_env_var(HbaseStartupHandler.ENV_CONFIG_PARAM_HBASE_MASTER, hbase_master_ip)

            master_hostname = self.get_data_from_meta_data_service(app_id,
                                                                   HbaseStartupHandler.ENV_CONFIG_PARAM_HBASE_MASTER_HOSTNAME)
            self.export_env_var(HbaseStartupHandler.ENV_CONFIG_PARAM_HBASE_MASTER_HOSTNAME, master_hostname)

            config_command = "echo " + hbase_master_ip + "    " + master_hostname + "  >> /etc/hosts"
            env_var = os.environ.copy()
            p = subprocess.Popen(config_command, env=env_var, shell=True)
            output, errors = p.communicate()
            HbaseStartupHandler.log.info("Entry added to /etc/hosts")

            server_hostname = socket.gethostname()
            server_ip = socket.gethostbyname(server_hostname)

            add_host_command = "ssh root@" + hbase_master_ip + " 'bash -s' < /tmp/add-host.sh " + server_ip + " " + server_hostname
            env_var = os.environ.copy()
            p = subprocess.Popen(add_host_command, env=env_var, shell=True)
            output, errors = p.communicate()
            HbaseStartupHandler.log.info("Entry added to Hbase master /etc/hosts")

            data = server_hostname + ":" + server_ip
            self.add_data_to_meta_data_service(HbaseStartupHandler.ENV_CONFIG_PARAM_HBASE_REGIONSERVER_DATA, data)


        # configure server
        HbaseStartupHandler.log.info("Configuring HBase...")
        config_command = "python ${CONFIGURATOR_HOME}/configurator.py"
        env_var = os.environ.copy()
        p = subprocess.Popen(config_command, env=env_var, shell=True)
        output, errors = p.communicate()
        HbaseStartupHandler.log.info("HBase configured successfully")

        if clustering_enable == 'true':

            # start server
            HbaseStartupHandler.log.info("Starting Hbase Master node ...")
            start_command = "exec ${HBASE_HOME}/bin/start-hbase.sh"
            env_var = os.environ.copy()
            p = subprocess.Popen(start_command, env=env_var, shell=True)
            output, errors = p.communicate()
            HbaseStartupHandler.log.info("Hbase Master node started successfully")

        else:

            # start server
            HbaseStartupHandler.log.info("Starting Hbase Regionserver ...")
            start_command = "exec ${HBASE_HOME}/bin/hbase-daemon.sh start regionserver"
            env_var = os.environ.copy()
            p = subprocess.Popen(start_command, env=env_var, shell=True)
            output, errors = p.communicate()
            HbaseStartupHandler.log.info("Hbase Regionserver started successfully")


    def get_zookeeper_member_ips(self, zookeeper_cluster, app_id):
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
                HbaseStartupHandler.log.error("[Service] %s is not available in topology" % service_name)

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
                if kb_service.portName == HbaseStartupHandler.CONST_PORT_MAPPING_MGT_CONSOLE:
                    portal_ip = kb_service.portalIP
        else:
            HbaseStartupHandler.log.error("Kubernetes Services are not available for [Service] %s" % service_name)

        return portal_ip

    def export_env_var(self, variable, value):
        """
        Export value as an environment variable
        :return: void
        """
        if value is not None:
            os.environ[variable] = value
            HbaseStartupHandler.log.info("Exported environment variable %s: %s" % (variable, value))
        else:
            HbaseStartupHandler.log.warn("Could not export environment variable %s " % variable)

    def get_data_from_meta_data_service(self, app_id, receive_data):
        """
        Get data from meta data service
        :return: received data
        """
        mds_response = None
        while mds_response is None:
            HbaseStartupHandler.log.info(
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

        HbaseStartupHandler.log.info("Value added to the metadata service %s: %s" % (key, value))

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