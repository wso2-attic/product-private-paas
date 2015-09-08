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

import json
from plugins.contracts import ICartridgeAgentPlugin
from modules.util.log import LogFactory
from entity import *
import subprocess
import os

class ZookeeperStartupHandler(ICartridgeAgentPlugin):
    """
    Configures and starts configurator, Zookeeper server
    """
    log = LogFactory().get_log(__name__)

    CONST_APPLICATION_ID = "APPLICATION_ID"
    CONST_SERVICE_NAME = "SERVICE_NAME"

    def run_plugin(self, values):

        app_id = values[self.CONST_APPLICATION_ID]
        service_type = values[self.CONST_SERVICE_NAME]
        topology = TopologyContext.topology
        zookeeper_cluster = None
        member_map = None
        default_private_ip = None

        zookeeper_cluster = self.get_cluster_of_service(topology, service_type, app_id)
        if zookeeper_cluster is not None:
            member_map = zookeeper_cluster.member_map
            i = 1
            for member in member_map:
                default_private_ip = member_map[member].member_default_private_ip
                command = "echo %s  zookeeper-%s >> /etc/hosts" % (default_private_ip, i)
                p = subprocess.Popen(command, shell=True)
                output, errors = p.communicate()
                ZookeeperStartupHandler.log.info(
                    "Successfully updated zookeeper-%s ip: %s in etc/hosts" % (i, default_private_ip))
                i = i + 1

        # start configurator
        ZookeeperStartupHandler.log.info("Configuring Zookeeper ...")
        config_command = "python ${CONFIGURATOR_HOME}/configurator.py"
        env_var = os.environ.copy()
        p = subprocess.Popen(config_command, env=env_var, shell=True)
        output, errors = p.communicate()
        ZookeeperStartupHandler.log.info("Zookeeper configured successfully")

        # start server
        ZookeeperStartupHandler.log.info("Starting Zookeeper ...")
        start_command = "${CARBON_HOME}/bin/zkServer.sh start"
        p = subprocess.Popen(start_command, env=env_var, shell=True)
        output, errors = p.communicate()
        ZookeeperStartupHandler.log.info("Zookeeper started successfully")

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
                    ZookeeperStartupHandler.log.warn("[Service] %s is None" % service_name)
            else:
                ZookeeperStartupHandler.log.warn("[Service] %s is not available in topology" % service_name)
        else:
            ZookeeperStartupHandler.log.warn("Topology is empty.")

        if clusters is not None:
            for cluster in clusters:
                if cluster.app_id == app_id:
                    cluster_obj = cluster

        return cluster_obj

    @staticmethod
    def export_env_var(variable, value):
        """
        exports key value pairs as env. variables

        :return: void
        """
        if value is not None:
            os.environ[variable] = value
            ZookeeperStartupHandler.log.info("Exported environment variable %s: %s" % (variable, value))
        else:
            ZookeeperStartupHandler.log.warn("Could not export environment variable %s " % variable)