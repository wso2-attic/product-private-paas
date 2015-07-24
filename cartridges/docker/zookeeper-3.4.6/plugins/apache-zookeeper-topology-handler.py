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

class StormTopologyHandler(ICartridgeAgentPlugin):

    def run_plugin(self, values):
        log = LogFactory().get_log(__name__)

        log.info("Reading the Complete Topology in order to get the dependent ip addresses ...")
        topology = values["TOPOLOGY_JSON"]
        log.info("Topology: %s" % topology)
        topology_str = json.loads(topology)

        zookeeper_member_default_private_ip = None
        nimbus_member_default_private_ip = None

        if topology_str is not None:
            # add service map
            for service_name in topology_str["serviceMap"]:
                service_str = topology_str["serviceMap"][service_name]
                if service_name == "zookeeper" :
                    # add cluster map
                    for cluster_id in service_str["clusterIdClusterMap"]:
                        cluster_str = service_str["clusterIdClusterMap"][cluster_id]
                        # add member map
                        for member_id in cluster_str["memberMap"]:
                            member_str = cluster_str["memberMap"][member_id]
                            i=0
                            if zookeeper_member_default_private_ip is None:
                                zookeeper_member_default_private_ip = member_str["defaultPrivateIP"]
                                command = "echo $s zookeeper-$s >> / etc / hosts" %(zookeeper_member_default_private_ip,i)
                                p = subprocess.Popen(command, shell=True)
                                output, errors = p.communicate()
                                log.info("Successfully updated zookeeper hostname: %s in etc/hosts" % zookeeper_member_default_private_ip)


        # configure server
        log.info("Configuring Apache Zookeeper...")
        config_command = "exec /opt/ppaas-configurator-4.1.0-SNAPSHOT/configurator.py"
        env_var = os.environ.copy()
        p = subprocess.Popen(config_command, env=env_var, shell=True)
        output, errors = p.communicate()
        log.info("Apache zookeeper configured successfully")

