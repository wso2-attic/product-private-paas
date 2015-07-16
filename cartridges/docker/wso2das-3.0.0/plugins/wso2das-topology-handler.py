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
import subprocess
import os


class DASTopologyHandler(ICartridgeAgentPlugin):
    def run_plugin(self, values):
        log = LogFactory().get_log(__name__)
        log.info("Values %r" % values)

        app_id = values["APPLICATION_ID"]
        log.info("Application ID: %s" % app_id)

        # Configuring Message Broker IP and Port
        CONFIG_PARAM_MB_IP = values["MB_IP"]
        CONFIG_PARAM_MB_PORT = values["MB_PORT"]
        log.info("Message Broker [IP] %s  [PORT] %s", CONFIG_PARAM_MB_IP, CONFIG_PARAM_MB_PORT)
        os.environ['CONFIG_PARAM_MB_IP'] = CONFIG_PARAM_MB_IP
        os.environ['CONFIG_PARAM_MB_PORT'] = CONFIG_PARAM_MB_PORT
        log.info("env MB_IP=%s MB_PORT=%s", (os.environ.get('CONFIG_PARAM_MB_IP')),
                 (os.environ.get('CONFIG_PARAM_MB_PORT')))

        topology = values["TOPOLOGY_JSON"]
        log.info("Topology: %s" % topology)
        topology_str = json.loads(topology)

        # if topology_str is not None:
        #     # add service map
        #     for service_name in topology_str["serviceMap"]:
        #         service_str = topology_str["serviceMap"][service_name]
        #         for cluster_id in service_str["clusterIdClusterMap"]:
        #             cluster_str = service_str["clusterIdClusterMap"][cluster_id]


        # Configuring Port Mappings
        # log.info("Reading port mappings...")
        # port_mappings_str = values["PORT_MAPPINGS"]
        #
        # mgt_console_https_port = None
        #
        # # port mappings format: """NAME:mgt-console|PROTOCOL:https|PORT:4500|PROXY_PORT:9443"""
        # log.info("Port mappings: %s" % port_mappings_str)
        # if port_mappings_str is not None:
        #
        #     port_mappings_array = port_mappings_str.split(";")
        #     if port_mappings_array:
        #
        #         for port_mapping in port_mappings_array:
        #             log.debug("port_mapping: %s" % port_mapping)
        #             name_value_array = port_mapping.split("|")
        #             name = name_value_array[0].split(":")[1]
        #             protocol = name_value_array[1].split(":")[1]
        #             port = name_value_array[2].split(":")[1]
        #             if name == "mgt-console" and protocol == "https":
        #                 mgt_console_https_port = port
        #
        # log.info("Kubernetes service management console https port: %s" % mgt_console_https_port)
        # if mgt_console_https_port is not None:
        #     os.environ['CONFIG_PARAM_HTTPS_PROXY_PORT'] = mgt_console_https_port
        #     log.info(
        #         "env https proxy port: %s" % (os.environ.get('CONFIG_PARAM_HTTPS_PROXY_PORT')))

        CONFIG_PARAM_CLUSTERING="true"
        CONFIG_PARAM_MEMBERSHIP_SCHEME="stratos"
        os.environ['CONFIG_PARAM_CLUSTERING'] = CONFIG_PARAM_CLUSTERING
        os.environ['CONFIG_PARAM_MEMBERSHIP_SCHEME'] = CONFIG_PARAM_MEMBERSHIP_SCHEME

        log.info(
            "env CONFIG_PARAM_CLUSTERING: %s  CONFIG_PARAM_MEMBERSHIP_SCHEME:%s " ,(os.environ.get('CONFIG_PARAM_CLUSTERING')),(os.environ.get('CONFIG_PARAM_MEMBERSHIP_SCHEME')))

        # Configuring Cluster IDs
        CONFIG_PARAM_CLUSTER_IDS = values["CLUSTER_ID"]
        log.info("Cluster ID : %s" % CONFIG_PARAM_CLUSTER_IDS)
        os.environ['CONFIG_PARAM_CLUSTER_IDS'] = CONFIG_PARAM_CLUSTER_IDS
        log.info(
            "env CONFIG_PARAM_CLUSTER_IDS: %s " % (os.environ.get('CONFIG_PARAM_CLUSTER_IDS')))


        # configure server
        log.info("Configuring WSO2 DAS ...")
        config_command = "python /opt/ppaas-configurator-4.1.0-SNAPSHOT/configurator.py"
        env_var = os.environ.copy()
        p = subprocess.Popen(config_command, env=env_var, shell=True)
        output, errors = p.communicate()
        log.info("WSO2 DAS configured successfully")

