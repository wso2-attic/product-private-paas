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
import socket
import mdsclient
import time

class HbaseTopologyHandler(ICartridgeAgentPlugin):

    def remove_data_from_metadata(self, key):

        mds_response = mdsclient.get(app=True)

        if mds_response is not None and mds_response.properties.get(key) is not None:
            read_data = mds_response.properties[key]
            check_str=isinstance(read_data, (str, unicode))

            if check_str == True:
                mdsclient.delete_property_value(key,read_data)
            else:
                for entry in read_data:
                    mdsclient.delete_property_value(key,entry)



    def run_plugin(self, values):

        log = LogFactory().get_log(__name__)
        log.info("Reading the Complete Topology in order to get the dependent ip addresses.")

        topology = values["TOPOLOGY_JSON"]
        app_id = values["APPLICATION_ID"]
        topology_str = json.loads(topology)
        clustering_enable= os.environ.get('CLUSTER')

        hadoop_master_ip = None
        zookeeper_ip = None
        hbase_master_ip = None

        if topology_str is not None:
            # add service map
            for service_name in topology_str["serviceMap"]:
                service_str = topology_str["serviceMap"][service_name]
                if service_name == "hadoop" :
                    # add cluster map
                    for cluster_id in service_str["clusterIdClusterMap"]:
                        cluster_str = service_str["clusterIdClusterMap"][cluster_id]
                        if cluster_str["appId"] == app_id:
                        # add member map
                            for member_id in cluster_str["memberMap"]:
                                member_str = cluster_str["memberMap"][member_id]
                                if hadoop_master_ip is None:
                                    hadoop_master_ip = member_str["defaultPrivateIP"]
                                    os.environ["CONFIG_PARAM_HDFS_HOST"] = hadoop_master_ip

                if service_name == "das-zookeeper" :
                    # add cluster map
                    for cluster_id in service_str["clusterIdClusterMap"]:
                        cluster_str = service_str["clusterIdClusterMap"][cluster_id]
                        if cluster_str["appId"] == app_id:
                        # add member map
                            for member_id in cluster_str["memberMap"]:
                                member_str = cluster_str["memberMap"][member_id]
                                if zookeeper_ip is None:
                                    zookeeper_ip = member_str["defaultPrivateIP"]
                                    os.environ["CONFIG_PARAM_ZOOKEEPER_HOST"] = zookeeper_ip

                if service_name == "hbase" :
                    # add cluster map
                    for cluster_id in service_str["clusterIdClusterMap"]:
                        cluster_str = service_str["clusterIdClusterMap"][cluster_id]
                        if cluster_str["appId"] == app_id:
                        # add member map
                            for member_id in cluster_str["memberMap"]:
                                member_str = cluster_str["memberMap"][member_id]
                                if hbase_master_ip is None:
                                    hbase_master_ip = member_str["defaultPrivateIP"]
                                    if clustering_enable != 'true':
                                        os.environ["CONFIG_PARAM_HBASE_MASTER"] = hbase_master_ip


        log.info("Configured dependent cartridge ips.")
        log.info(hadoop_master_ip)
        log.info(zookeeper_ip)
        log.info(hbase_master_ip)


        if clustering_enable == 'true':

            self.remove_data_from_metadata("CONFIG_PARAM_HBASE_MASTER_HOSTNAME")
            self.remove_data_from_metadata("CONFIG_PARAM_HBASE_REGIONSERVER_DATA")

            mdsclient.MDSPutRequest()
            master_hostname=socket.gethostname()
            master_hostname_property = {"key": "CONFIG_PARAM_HBASE_MASTER_HOSTNAME", "values": [ master_hostname ]}
            mdsclient.put(master_hostname_property, app=True)
            os.environ["CONFIG_PARAM_HBASE_MASTER_HOSTNAME"] = master_hostname
            log.info("Published CONFIG_PARAM_HBASE_MASTER_HOSTNAME to metadata")
            log.info(master_hostname)

        else:

            mds_response = None
            while mds_response is None:
                log.debug("Waiting for CONFIG_PARAM_HBASE_MASTER_HOSTNAME to be available from metadata service for app ID: %s" % values["APPLICATION_ID"])
                time.sleep(5)
                mds_response = mdsclient.get(app=True)
                if mds_response is not None and mds_response.properties.get("CONFIG_PARAM_HBASE_MASTER_HOSTNAME") is None:
                    mds_response = None

            master_hostname = mds_response.properties["CONFIG_PARAM_HBASE_MASTER_HOSTNAME"]
            os.environ["CONFIG_PARAM_HBASE_MASTER_HOSTNAME"] = master_hostname
            log.info("Received metadata for CONFIG_PARAM_HBASE_MASTER_HOSTNAME")
            log.info(master_hostname)

            config_command = "echo "+hbase_master_ip+"    "+master_hostname+"  >> /etc/hosts"
            env_var = os.environ.copy()
            p = subprocess.Popen(config_command, env=env_var, shell=True)
            output, errors = p.communicate()
            log.info("Entry added to /etc/hosts")

            server_hostname=socket.gethostname()
            server_ip=socket.gethostbyname(server_hostname)

            add_host_command = "ssh root@"+hbase_master_ip+" 'bash -s' < /tmp/add-host.sh "+server_ip+" "+server_hostname
            env_var = os.environ.copy()
            p = subprocess.Popen(add_host_command, env=env_var, shell=True)
            output, errors = p.communicate()
            log.info("Entry added to Hbase master /etc/hosts")

            mdsclient.MDSPutRequest()
            data = server_hostname+":"+server_ip
            regionserver_data_property = {"key": "CONFIG_PARAM_HBASE_REGIONSERVER_DATA", "values": [ data ]}
            mdsclient.put(regionserver_data_property, app=True)
            log.info("Published CONFIG_PARAM_HBASE_REGIONSERVER_DATA to metadata")
            log.info(data)


        # configure server
        log.info("Configuring HBase...")
        config_command = "python ${CONFIGURATOR_HOME}/configurator.py"
        env_var = os.environ.copy()
        p = subprocess.Popen(config_command, env=env_var, shell=True)
        output, errors = p.communicate()
        log.info("HBase configured successfully")
        os.environ["CONFIGURED"] = "true"


