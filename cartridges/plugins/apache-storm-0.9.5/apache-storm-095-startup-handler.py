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
from entity import *
import subprocess
import os

class StormStartupHandler(ICartridgeAgentPlugin):
    """
    Configures and starts configurator, Storm server
    """
    log = LogFactory().get_log(__name__)

    # class constants
    CONST_ZOOKEEPER_DEFAULT_PORT = "2888"
    CONST_STORM_TYPE = 'CONFIG_PARAM_STORM_TYPE'
    ENV_ZOOKEEPER_HOSTNAMES = "CONFIG_PARAM_ZOOKEEPER_HOSTNAMES"
    ENV_NIMBUS_HOSTNAME = "CONFIG_PARAM_NIMBUS_HOSTNAME"

    def run_plugin(self, values):

        app_id = values[self.CONST_APPLICATION_ID]
        service_type = values[self.CONST_SERVICE_NAME]
        cartridge_type = values[self.CONST_STORM_TYPE]
        topology = TopologyContext.topology

        zookeeper_ips_list = []
        zookeeper_cluster = self.get_cluster_of_service(topology, service_type, app_id)
        if zookeeper_cluster is not None:
            member_map = zookeeper_cluster.member_map
            for member in member_map:
                default_private_ip = member_map[member].member_default_private_ip
                zookeeper_ips_list.append(default_private_ip)

        if zookeeper_ips_list:
            # Manipulating the cep-mgr member list to be suited for the template module
            zk_hostnames_string = '['
            for zk_member_ip in zookeeper_ips_list:
                if zk_member_ip is not zookeeper_ips_list[-1]:
                    zk_hostnames_string += zk_member_ip + ":" + self.CONST_ZOOKEEPER_DEFAULT_PORT + ','
                else:
                    zk_hostnames_string += zk_member_ip + ":" + self.CONST_ZOOKEEPER_DEFAULT_PORT
            zk_hostnames_string += ']'
            self.export_env_var(self.ENV_ZOOKEEPER_HOSTNAMES, zk_hostnames_string)


        nimbus_cluster = self.get_cluster_of_service(topology, service_type, app_id)
        if nimbus_cluster is not None:
            member_map = nimbus_cluster.member_map
            for member in member_map:
                nimbus_private_ip = member_map[member].member_default_private_ip
                self.export_env_var(self.ENV_NIMBUS_HOSTNAME, nimbus_private_ip)

        # start configurator
        StormStartupHandler.log.info("Configuring Apache Storm %s..." % cartridge_type)
        config_command = "python ${CONFIGURATOR_HOME}/configurator.py"
        env_var = os.environ.copy()
        p = subprocess.Popen(config_command, env=env_var, shell=True)
        output, errors = p.communicate()
        StormStartupHandler.log.info("Apache Storm %s configured successfully" % cartridge_type)

        # start server
        StormStartupHandler.log.info("Starting Apache Storm %s ..." % cartridge_type)
        start_command = "nohup ${CARBON_HOME}/bin/storm " + cartridge_type + " > out&"
        p = subprocess.Popen(start_command, env=env_var, shell=True)
        output, errors = p.communicate()
        StormStartupHandler.log.info("Apache Storm %s started successfully" % cartridge_type)

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
                    StormStartupHandler.log.warn("[Service] %s is None" % service_name)
            else:
                StormStartupHandler.log.warn("[Service] %s is not available in topology" % service_name)
        else:
            StormStartupHandler.log.warn("Topology is empty.")

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
            StormStartupHandler.log.info("Exported environment variable %s: %s" % (variable, value))
        else:
            StormStartupHandler.log.warn("Could not export environment variable %s " % variable)