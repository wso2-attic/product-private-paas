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
import socket
import operator
import os
import time


class ZookeeperStartupHandler(ICartridgeAgentPlugin):
    """
    Configures and starts configurator, Zookeeper server
    """
    log = LogFactory().get_log(__name__)

    CONST_APPLICATION_ID = "APPLICATION_ID"
    CONST_SERVICE_NAME = "SERVICE_NAME"
    CONST_MAX_RETRY_COUNT = 2

    def run_plugin(self, values):

        app_id = values[self.CONST_APPLICATION_ID]
        service_type = values[self.CONST_SERVICE_NAME]

        ZookeeperStartupHandler.log.info("Zookeeper Service type: %s" % service_type)
        topology = TopologyContext.topology

        zookeeper_cluster = self.get_cluster_of_service(topology, service_type, app_id)
        if zookeeper_cluster is not None:
            member_map = zookeeper_cluster.member_map
            member_id_member_ip_dictionary = self.get_member_id_member_ip_dictionary(member_map, service_type, app_id)
            ZookeeperStartupHandler.log.info("Zookeeper dictionary : %s" % member_id_member_ip_dictionary)
            sorted_member_id_member_ip_tuples = sorted(member_id_member_ip_dictionary.items(),
                                                       key=operator.itemgetter(0))
            ZookeeperStartupHandler.log.info("Zookeeper sorted tuples : %s" % sorted_member_id_member_ip_tuples)
            local_ip = socket.gethostbyname(socket.gethostname())
            my_id = None
            for i, v in enumerate(sorted_member_id_member_ip_tuples):
                if v[1] == local_ip:
                    my_id = i + 1
            # ZK data-dir is hard-coded here
            command = "echo %s >> /tmp/zookeeper/myid" % my_id
            p = subprocess.Popen(command, shell=True)
            output, errors = p.communicate()
            ZookeeperStartupHandler.log.info(
                "Successfully updated myid file with: %s in /tmp/zookeeper/myid" % my_id)
            j = 1
            for i, v in enumerate(sorted_member_id_member_ip_tuples):
                command = "echo %s  zookeeper-%s >> /etc/hosts" % (v[1], j)
                p = subprocess.Popen(command, shell=True)
                output, errors = p.communicate()
                ZookeeperStartupHandler.log.info(
                    "Successfully updated zookeeper-%s ip: %s in etc/hosts" % (j, v[1]))
                j = j + 1

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

    def get_member_id_member_ip_dictionary(self, member_map, service_type, app_id):
        """
        Retuns a dictionary with following format {'member_id_1':"member_default_ip_1", 'member_id_2':"member_default_ip_2" }

        :return: void
        """
        member_id_member_ip_dictionary = {}
        attempt = 0
        for member in member_map:
            member_id = member_map[member].member_id
            default_private_ip = member_map[member].member_default_private_ip
            ZookeeperStartupHandler.log.info("Zookeeper [member_id] %s [member_ip]%s" % (member_id, default_private_ip))
            if default_private_ip is not None:
                member_id_member_ip_dictionary[member_id] = default_private_ip
            else:
                ZookeeperStartupHandler.log.warn(
                    "default member ip for [member_id] %s is empty, hence re-initializing topology " % member_id)
                if attempt < self.CONST_MAX_RETRY_COUNT:
                    time.sleep(60)
                    attempt = attempt + 1
                    topology = TopologyContext.topology
                    zookeeper_cluster = self.get_cluster_of_service(topology, service_type, app_id)
                    if zookeeper_cluster is not None:
                        member_map = zookeeper_cluster.member_map
                        self.get_member_id_member_ip_dictionary(member_map, service_type, app_id)

        return member_id_member_ip_dictionary

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