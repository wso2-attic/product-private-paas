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
import mdsclient
import operator
import time
import socket

class StormStartupHandler(ICartridgeAgentPlugin):
    """
    Configures and starts configurator, Storm server
    """
    log = LogFactory().get_log(__name__)

    # class constants
    CONST_APPLICATION_ID = "APPLICATION_ID"
    CONST_SERVICE_NAME = "SERVICE_NAME"
    CONST_ZOOKEEPER_DEFAULT_PORT = "2888"
    CONST_STORM_TYPE = 'CONFIG_PARAM_STORM_TYPE'
    CONST_STORM_TYPE_SUPERVISOR = "supervisor"
    CONST_ZOOKEEPER_SERVICE_TYPE = "zookeeper"
    CONST_NIMBUS_SERVICE_TYPE = "storm-nimbus"
    CONST_MIN_COUNT = "MIN_COUNT"
    ENV_ZOOKEEPER_HOSTNAMES = "CONFIG_PARAM_ZOOKEEPER_HOSTNAMES"
    ENV_NIMBUS_HOSTNAME = "CONFIG_PARAM_NIMBUS_HOSTNAME"
    CONST_MAX_RETRY_COUNT = 5
    current_attempt = 0

    def run_plugin(self, values):

        app_id = values[self.CONST_APPLICATION_ID]
        service_type = values[self.CONST_SERVICE_NAME]
        cartridge_type = values[self.CONST_STORM_TYPE]
        topology = TopologyContext.topology

        zookeeper_ips_list = []
        zookeeper_cluster = self.get_cluster_of_service(topology, self.CONST_ZOOKEEPER_SERVICE_TYPE, app_id)
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


        nimbus_cluster = self.get_cluster_of_service(topology, self.CONST_NIMBUS_SERVICE_TYPE, app_id)
        if nimbus_cluster is not None:
            member_map = nimbus_cluster.member_map
            for member in member_map:
                nimbus_private_ip = member_map[member].member_default_private_ip
                self.export_env_var(self.ENV_NIMBUS_HOSTNAME, nimbus_private_ip)

        if cartridge_type == self.CONST_STORM_TYPE_SUPERVISOR:
            supervisor_cluster = self.get_cluster_of_service(topology, service_type, app_id)
            if supervisor_cluster is not None:
                member_map = supervisor_cluster.member_map
                member_id_member_ip_dictionary = self.get_member_id_member_ip_dictionary(member_map, service_type, app_id)

                # get number of supervisor nodes
                num_of_supervisor_instances = len(member_id_member_ip_dictionary)
                StormStartupHandler.log.info("Number of Supervisor nodes : %s" % num_of_supervisor_instances)
                sorted_member_id_member_ip_tuples = sorted(member_id_member_ip_dictionary.items(),
                                                           key=operator.itemgetter(0))
                local_ip = socket.gethostbyname(socket.gethostname())
                local_hostname = socket.gethostname()

                my_id = None
                for i, v in enumerate(sorted_member_id_member_ip_tuples):
                    if v[1] == local_ip:
                        my_id = i + 1
                self.remove_data_from_metadata("supervisor-%s" % my_id)
                self.add_data_to_meta_data_service("supervisor-%s" % my_id, local_ip + ":" + local_hostname)

                for x in range(1, num_of_supervisor_instances + 1):
                    supervisor_ip_host_tuple = self.get_data_from_meta_data_service(app_id, "supervisor-%s" % x)
                    StormStartupHandler.log.info("Storm Supervisor-%s value %s" % (x, supervisor_ip_host_tuple))
                    if supervisor_ip_host_tuple is not None and x != my_id:
                        supervisor_array = supervisor_ip_host_tuple.split(":")
                        command = "echo %s  %s >> /etc/hosts" % (supervisor_array[0], supervisor_array[1])
                        p = subprocess.Popen(command, shell=True)
                        output, errors = p.communicate()
                        StormStartupHandler.log.info(
                            "Successfully updated %s ip: %s in etc/hosts" % (supervisor_array[1], supervisor_array[0]))

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

    @staticmethod
    def add_data_to_meta_data_service(key, value):
        """
        add data to meta data service
        :return: void
        """
        mdsclient.MDSPutRequest()
        data = {"key": key, "values": [value]}
        mdsclient.put(data, app=True)

    @staticmethod
    def get_data_from_meta_data_service(app_id, receive_data):
        """
        Get data from meta data service
        :return: received data
        """
        metadata = None
        while metadata is None:
            mds_response = mdsclient.get(app=True)
            if mds_response is not None and mds_response.properties.get(receive_data) is None:
                StormStartupHandler.log.info(
                    "Waiting for " + receive_data + " to be available from metadata service for app ID: %s" % app_id)
                time.sleep(1)
            else:
                metadata = mds_response.properties[receive_data]

        return metadata

    @staticmethod
    def remove_data_from_metadata(key):
        """
        remove data from meta data service
        :return: void
        """
        mds_response = mdsclient.get(app=True)

        if mds_response is not None and mds_response.properties.get(key) is not None:
            read_data = mds_response.properties[key]
            check_str = isinstance(read_data, (str, unicode))
            check_int = isinstance(read_data, int)

            if check_str or check_int:
                mdsclient.delete_property_value(key, read_data)
            else:
                for entry in read_data:
                    mdsclient.delete_property_value(key, entry)

    def get_member_id_member_ip_dictionary(self, member_map, service_type, app_id):
        """
        Retuns a dictionary with following format {'member_id_1':"member_default_ip_1", 'member_id_2':"member_default_ip_2"}

        :return: dictionary
        """
        member_id_member_ip_dictionary = {}

        for member in member_map:
            member_id = member_map[member].member_id
            member_properties = member_map[member].properties
            if member_properties is not None:
                min_member_count = member_properties[self.CONST_MIN_COUNT]
                if int(min_member_count) <= len(member_map):
                    default_private_ip = member_map[member].member_default_private_ip
                    StormStartupHandler.log.info("Storm Supervisor [member_id] %s [member_ip]%s" % (member_id, default_private_ip))
                    if default_private_ip is not None:
                        member_id_member_ip_dictionary[member_id] = default_private_ip
                    else:
                        StormStartupHandler.log.warn(
                            "default member ip for [member_id] %s is empty, hence re-initializing topology " % member_id)
                        if self.current_attempt < self.CONST_MAX_RETRY_COUNT:
                            time.sleep(60)
                            self.current_attempt = self.current_attempt + 1
                            topology = TopologyContext.topology
                            supervisor_cluster = self.get_cluster_of_service(topology, service_type, app_id)
                            if supervisor_cluster is not None:
                                member_map = supervisor_cluster.member_map
                                self.get_member_id_member_ip_dictionary(member_map, service_type, app_id)
                else:
                    StormStartupHandler.log.warn(
                        "Member [member] %s min-count does not match, hence re-initializing topology " % member_id)
                    if self.current_attempt < self.CONST_MAX_RETRY_COUNT:
                        time.sleep(60)
                        self.current_attempt = self.current_attempt + 1
                        topology = TopologyContext.topology
                        supervisor_cluster = self.get_cluster_of_service(topology, service_type, app_id)
                        if supervisor_cluster is not None:
                            member_map = supervisor_cluster.member_map
                            self.get_member_id_member_ip_dictionary(member_map, service_type, app_id)
            else:
                StormStartupHandler.log.warn(
                    "Member [member] %s properties are empty." % member_id)
        return member_id_member_ip_dictionary