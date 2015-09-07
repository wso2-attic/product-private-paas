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
from entity import *
import socket


class HadoopStartupHandler(ICartridgeAgentPlugin):
    log = LogFactory().get_log(__name__)

    CONST_PORT_MAPPING_MGT_CONSOLE = "mgt-console"
    CONST_SERVICE_NAME = "SERVICE_NAME"
    CONST_HADOOP_SERVICE_NAME = "hadoop"
    CONST_APPLICATION_ID = "APPLICATION_ID"

    ENV_CONFIG_PARAM_HADOOP_MASTER = "CONFIG_PARAM_HADOOP_MASTER"
    ENV_CLUSTER = "CLUSTER"


    def run_plugin(self, values):

        app_id = values[HadoopStartupHandler.CONST_APPLICATION_ID]
        service_name = values[HadoopStartupHandler.CONST_SERVICE_NAME]
        clustering_enable = os.environ.get(HadoopStartupHandler.ENV_CLUSTER)

        HadoopStartupHandler.log.info("Application ID: %s" % app_id)
        HadoopStartupHandler.log.info("Service Name: %s" % service_name)
        HadoopStartupHandler.log.info("Clustering Enable : %s" % clustering_enable)

        if clustering_enable != 'true':
            # This is a datanode
            server_hostname = socket.gethostname()
            server_ip = socket.gethostbyname(server_hostname)

            master_ip = self.read_member_ip_from_topology(HadoopStartupHandler.CONST_HADOOP_SERVICE_NAME, app_id)
            self.export_env_var(HadoopStartupHandler.ENV_CONFIG_PARAM_HADOOP_MASTER, master_ip)

            add_host_command = "ssh root@" + master_ip + " 'bash -s' < /tmp/add-host.sh " + server_ip + " " + server_hostname
            env_var = os.environ.copy()
            p = subprocess.Popen(add_host_command, env=env_var, shell=True)
            output, errors = p.communicate()
            HadoopStartupHandler.log.info("Entry added to Hadoop master /etc/hosts")
        else:
            # This is a namenode
            member_ip = socket.gethostbyname(socket.gethostname())
            self.export_env_var(HadoopStartupHandler.ENV_CONFIG_PARAM_HADOOP_MASTER, member_ip)


        # configure server
        HadoopStartupHandler.log.info("Configuring Hadoop...")
        config_command = "python ${CONFIGURATOR_HOME}/configurator.py"
        env_var = os.environ.copy()
        p = subprocess.Popen(config_command, env=env_var, shell=True)
        output, errors = p.communicate()
        HadoopStartupHandler.log.info("Hadoop configured successfully")

        config_command = "echo JAVA_HOME=${JAVA_HOME} >> /etc/environment"
        env_var = os.environ.copy()
        p = subprocess.Popen(config_command, env=env_var, shell=True)
        output, errors = p.communicate()
        HadoopStartupHandler.log.info("Entry added to /etc/environments")


        if clustering_enable == 'true':

            # start server
            HadoopStartupHandler.log.info("Starting Hadoop Namenode ...")

            format_namenode_command = "exec ${HADOOP_HOME}/bin/hadoop namenode -format"
            env_var = os.environ.copy()
            p = subprocess.Popen(format_namenode_command, env=env_var, shell=True)
            output, errors = p.communicate()

            start_command = "exec ${HADOOP_HOME}/sbin/start-dfs.sh"
            env_var = os.environ.copy()
            p = subprocess.Popen(start_command, env=env_var, shell=True)
            output, errors = p.communicate()

            start_command = "exec ${HADOOP_HOME}/sbin/start-yarn.sh"
            env_var = os.environ.copy()
            p = subprocess.Popen(start_command, env=env_var, shell=True)
            output, errors = p.communicate()

            HadoopStartupHandler.log.info("Hadoop Namenode started successfully")

        else:

            # start server
            HadoopStartupHandler.log.info("Starting Hadoop Datanode ...")

            start_command = "exec ${HADOOP_HOME}/sbin/hadoop-daemon.sh start datanode"
            env_var = os.environ.copy()
            p = subprocess.Popen(start_command, env=env_var, shell=True)
            output, errors = p.communicate()

            HadoopStartupHandler.log.info("Hadoop Datanode started successfully")


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
                HadoopStartupHandler.log.error("[Service] %s is not available in topology" % service_name)

        return clusters


    def export_env_var(self, variable, value):
        """
        Export value as an environment variable
        :return: void
        """
        if value is not None:
            os.environ[variable] = value
            HadoopStartupHandler.log.info("Exported environment variable %s: %s" % (variable, value))
        else:
            HadoopStartupHandler.log.warn("Could not export environment variable %s " % variable)


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
                if kb_service.portName == HadoopStartupHandler.CONST_PORT_MAPPING_MGT_CONSOLE:
                    portal_ip = kb_service.portalIP
        else:
            HadoopStartupHandler.log.error("Kubernetes Services are not available for [Service] %s" % service_name)

        return portal_ip
