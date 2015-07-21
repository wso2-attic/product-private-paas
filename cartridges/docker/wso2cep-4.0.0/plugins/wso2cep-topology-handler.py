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


class CEPTopologyHandler(ICartridgeAgentPlugin):
    CONST_APPLICATION_ID = "APPLICATION_ID"
    CONST_MB_IP = "MB_IP"
    CONST_CONFIG_PARAM_MANAGER = "CONFIG_PARAM_MANAGER"
    CONST_TOPOLOGY_JSON = "TOPOLOGY_JSON"
    CONST_CEP_MANAGER_MEMBER_PORT = "8904"
    TOPOLOGY_ZOOKEEPER_SERVICE_NAME = "zookeeper"
    TOPOLOGY_NIMBUS_SERVICE_NAME = "nimbus"
    TOPOLOGY_ERVICE_MAP = "serviceMap"
    TOPOLOGY_CLUSTER_ID_CLUSTER_MAP = "clusterIdClusterMap"
    TOPOLOGY_APP_ID = "appId"
    TOPOLOGY_CLUSTER_ID = "clusterId"
    TOPOLOGY_MEMBER_MAP = "memberMap"
    TOPOLOGY_DEFAULT_PRIVATE_IP = "defaultPrivateIP"
    TOPOLOGY_KUBERNETES_SERVICES = "kubernetesServices"
    TOPOLOGY_KUBERNETES_SERVICE_PROTOCOL = "protocol"
    TOPOLOGY_KUBERNETES_SERVICE_PORTAL_IP = "portalIP"
    TOPOLOGY_CEP_MANAGER_SERVICE_NAME = "cep-mgr"
    TOPOLOGY_CEP_WORKER_SERVICE_NAME = "cep-wkr"

    # Returns clusterIdClusterMap json of given service and app_id from complete topology
    def get_cluster_id_cluster_map(self, topology_json, service, app_id):
        cluster_id_cluster_map = None
        if topology_json is not None:
            # add service map
            for service_name in topology_json[self.TOPOLOGY_ERVICE_MAP]:
                service_str = topology_json[self.TOPOLOGY_ERVICE_MAP][service_name]
                if service_name == service:
                    # add cluster map
                    for cluster_id in service_str[self.TOPOLOGY_CLUSTER_ID_CLUSTER_MAP]:
                        cluster_str = service_str[self.TOPOLOGY_CLUSTER_ID_CLUSTER_MAP][cluster_id]
                        if cluster_str[self.TOPOLOGY_APP_ID] == app_id:
                            cluster_id_cluster_map = cluster_str
        return cluster_id_cluster_map

    # Return portalIP of the given clusterIdClusterMap and protocol
    def get_portal_ip_from_cluster_map(self, cluster_id_cluster_map, protocol):
        service_portal_ip = None
        if cluster_id_cluster_map is not None:
            for kub_service in cluster_id_cluster_map[self.TOPOLOGY_KUBERNETES_SERVICES]:
                if kub_service[self.TOPOLOGY_KUBERNETES_SERVICE_PROTOCOL] == protocol:
                    service_portal_ip = kub_service[self.TOPOLOGY_KUBERNETES_SERVICE_PORTAL_IP]

        return service_portal_ip

    # Set environment variables
    def set_as_env_variable(self, key, value):
        os.environ[key] = value

    # Configure CEP Manager
    def cep_manager_config(self, topology_json, app_id):
        log = LogFactory().get_log(__name__)
        log.info("Configuring CEP Manager Template module ..")

        zookeeper_kubernetes_portal_ip = None
        zookeeper_cluster_str = self.get_cluster_id_cluster_map(topology_json, self.TOPOLOGY_ZOOKEEPER_SERVICE_NAME,
                                                                app_id)
        if zookeeper_cluster_str is not None:
            zookeeper_kubernetes_portal_ip = self.get_portal_ip_from_cluster_map(zookeeper_cluster_str, "http")

            if zookeeper_kubernetes_portal_ip is not None:
                self.set_as_env_variable('CONFIG_PARAM_ZOOKEEPER_HOST', zookeeper_kubernetes_portal_ip)
                log.info(
                    "Successfully updated zookeeper portal ip: %s in WSO2 CEP Manager template module" % zookeeper_kubernetes_portal_ip)
            else:
                log.warn("Unable to read zookeeper portal ip from topology")
        else:
            log.warn("Zookeeper clusterIdClusterMap is not available in topology for application : %s" % app_id)

        nimbus_kubernetes_portal_ip = None
        nimbus_cluster_str = self.get_cluster_id_cluster_map(topology_json, self.TOPOLOGY_NIMBUS_SERVICE_NAME, app_id)
        if nimbus_cluster_str is not None:
            nimbus_kubernetes_portal_ip = self.get_portal_ip_from_cluster_map(nimbus_cluster_str, "http")
            if zookeeper_kubernetes_portal_ip is not None:
                self.set_as_env_variable('CONFIG_PARAM_NIMBUS_HOST', nimbus_kubernetes_portal_ip)
                log.info(
                    "Successfully updated nimbus portal ip: %s in WSO2 CEP Manager template module" % nimbus_kubernetes_portal_ip)
            else:
                log.warn("Unable to read nimbus portal ip from topology")
        else:
            log.warn("Nimbus clusterIdClusterMap is not available in topology for application : %s" % app_id)

        # set local ip as CONFIG_PARAM_LOCAL_MEMBER_HOST
        get_local_ip_cmd = "awk 'NR==1 {print $1}' /etc/hosts"
        local_ip = subprocess.check_output(get_local_ip_cmd, shell=True)
        log.info("local IP from /etc/hosts : %s " % local_ip)

        if local_ip is not None:
            local_ip = local_ip[0:-1]
            if local_ip is not None:
                self.set_as_env_variable('CONFIG_PARAM_LOCAL_MEMBER_HOST', local_ip)
                log.info("Successfully updated local member ip: %s in WSO2 CEP template module" % local_ip)

    def run_plugin(self, values):
        log = LogFactory().get_log(__name__)

        # Read Application_Id, MB_IP, CONFIG_PARAM_MANAGER and Topology from 'values'
        app_id = values[self.CONST_APPLICATION_ID]
        mb_ip = values[self.CONST_MB_IP]
        is_cep_mgr = values[self.CONST_CONFIG_PARAM_MANAGER]
        topology_str = values[self.CONST_TOPOLOGY_JSON]

        # log above variables
        log.info("Application ID: %s" % app_id)
        log.info("MB IP: %s" % mb_ip)
        log.info("CEP Manager: %s" % is_cep_mgr)
        log.info("Topology: %s" % topology_str)

        topology_json = json.loads(topology_str)

        if is_cep_mgr == 'true':
            if topology_json is not None and app_id is not None:
                # Execute CEP Manager configurations
                self.cep_manager_config(topology_json, app_id)

        # Read all CEP Manager private IPs and set CONFIG_PARAM_MANAGER_MEMBERS
        cep_mgr_private_ip_list = []
        cep_mgr_cluster_str = self.get_cluster_id_cluster_map(topology_json, self.TOPOLOGY_CEP_MANAGER_SERVICE_NAME,
                                                              app_id)

        if cep_mgr_cluster_str is not None:
            for member_id in cep_mgr_cluster_str[self.TOPOLOGY_MEMBER_MAP]:
                member_str = cep_mgr_cluster_str[self.TOPOLOGY_MEMBER_MAP][member_id]
                if member_str[self.TOPOLOGY_DEFAULT_PRIVATE_IP] is not None:
                    cep_mgr_private_ip_list.append(member_str[self.TOPOLOGY_DEFAULT_PRIVATE_IP])

            if cep_mgr_private_ip_list:
                # Manipulating the cep-mgr member list to be suited for the template module
                managers_string = '['
                for member_ip in cep_mgr_private_ip_list:
                    if member_ip is not cep_mgr_private_ip_list[-1]:
                        managers_string += member_ip + ":" + self.CONST_CEP_MANAGER_MEMBER_PORT + ','
                    else:
                        managers_string += member_ip + ":" + self.CONST_CEP_MANAGER_MEMBER_PORT
                managers_string += ']'

                self.set_as_env_variable('CONFIG_PARAM_MANAGER_MEMBERS', managers_string)
                log.info("Successfully updated CEP Managers list: %s in WSO2 CEP template module" % managers_string)
            else:
                # If no manager IPs are found
                log.warn(
                    "CEP Manager IPs are not found in topology, hence CONFIG_PARAM_MANAGER_MEMBERS property is not set")
        else:
            log.warn("CEP Manager clusterIdClusterMap is not available in topology for application : %s" % app_id)

        # Read all CEP Manager/Worker cluster-ids from topology and update CONFIG_PARAM_CLUSTER_IDs in module.ini
        cep_worker_manager_cluster_ids = []
        cep_mgr_cluster_str = self.get_cluster_id_cluster_map(topology_json, self.TOPOLOGY_CEP_MANAGER_SERVICE_NAME,
                                                              app_id)
        cep_wkr_cluster_str = self.get_cluster_id_cluster_map(topology_json, self.TOPOLOGY_CEP_WORKER_SERVICE_NAME,
                                                              app_id)

        # Add both CEP worker and manager cluster IDs to list
        if cep_mgr_cluster_str is not None:
            cep_worker_manager_cluster_ids.append(cep_mgr_cluster_str[self.TOPOLOGY_CLUSTER_ID])
        if cep_wkr_cluster_str is not None:
            cep_worker_manager_cluster_ids.append(cep_wkr_cluster_str[self.TOPOLOGY_CLUSTER_ID])

        if cep_worker_manager_cluster_ids:
            cep_clusterIds = ",".join(cep_worker_manager_cluster_ids)
            self.set_as_env_variable('CONFIG_PARAM_CLUSTER_IDs', cep_clusterIds)
            log.info("Successfully updated cep cluster_ids: %s in WSO2 CEP template module" % cep_clusterIds)
        else:
            # If no cluster_ids are found in topology
            log.warn(
                "CEP Manager/Worker cluster ids are not found in topology, hence CONFIG_PARAM_CLUSTER_IDs are not set")

        # Update MB_IP in module.ini to be used by jndi.properties
        if mb_ip is not None:
            self.set_as_env_variable('CONFIG_PARAM_MB_HOST', mb_ip)
            log.info("Successfully updated mb ip: %s in WSO2 CEP template module" % mb_ip)

        # configure server
        log.info("Configuring WSO2 CEP ...")
        config_command = "python /opt/ppaas-configurator-4.1.0-SNAPSHOT/configurator.py"
        env_var = os.environ.copy()
        p = subprocess.Popen(config_command, env=env_var, shell=True)
        output, errors = p.communicate()
        log.info("WSO2 CEP configured successfully")
