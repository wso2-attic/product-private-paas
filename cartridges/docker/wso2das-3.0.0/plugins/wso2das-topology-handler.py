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
import mdsclient
import pymysql as db


class DASTopologyHandler(ICartridgeAgentPlugin):
    def create_database(self, databasename, username, password):
        log = LogFactory().get_log(__name__)
        mds_response = mdsclient.get(app=True)
        if mds_response is not None and mds_response.properties.get("MYSQL_HOST") is not None:
            remote_host = mds_response.properties["MYSQL_HOST"]
            remote_username = mds_response.properties["MYSQL_ROOT_USERNAME"]
            remote_password = mds_response.properties["MYSQL_ROOT_PASSWORD"]
            log.info("mysql server conf [host]:%s [username]:%s [password]:%s", remote_host,
                     remote_username, remote_password)
            con = None
            try:
                con = db.connect(host=remote_host, user=remote_username, passwd=remote_password)
                cur = con.cursor()
                cur.execute('CREATE DATABASE IF NOT EXISTS ' + databasename + ';')
                cur.execute('USE ' + databasename + ';')
                cur.execute(
                    'GRANT ALL PRIVILEGES ON ' + databasename + '.* TO ' + username + '@"%" IDENTIFIED BY "' + password + '";')
                log.info("Database %s created successfully" % databasename)
            except db.Error, e:
                log("Error in creating database %d: %s" % (e.args[0], e.args[1]))

            finally:
                if con:
                    con.close()
        else:
            log.error('mysql details not published to metadata service')


    def run_plugin(self, values):
        log = LogFactory().get_log(__name__)
        log.info("Values %r" % values)

        profile = os.environ['CONFIG_PARAM_PROFILE']
        log.info("Profile : %s " % profile)

        app_id = values["APPLICATION_ID"]
        log.info("Application ID: %s" % app_id)

        # Configuring Message Broker IP and Port
        CONFIG_PARAM_MB_IP = values["MB_IP"]
        log.info("Message Broker [IP] %s" , CONFIG_PARAM_MB_IP)
        os.environ['CONFIG_PARAM_MB_IP'] = CONFIG_PARAM_MB_IP
        log.info("env MB_IP=%s ", (os.environ.get('CONFIG_PARAM_MB_IP')))

        zookeeper_ip = None
        hbase_master_ip = None

        topology = values["TOPOLOGY_JSON"]
        log.info("Topology: %s" % topology)
        topology_json = json.loads(topology)

        for service_name in topology_json["serviceMap"]:
            service_str = topology_json["serviceMap"][service_name]
            if service_name == "das-zookeeper":
                # add cluster map
                for cluster_id in service_str["clusterIdClusterMap"]:
                    cluster_str = service_str["clusterIdClusterMap"][cluster_id]
                    # add member map
                    if cluster_str["appId"] == app_id:
                        for member_id in cluster_str["memberMap"]:
                            member_str = cluster_str["memberMap"][member_id]
                            if zookeeper_ip is None:
                                zookeeper_ip = member_str["defaultPrivateIP"]
                                os.environ["CONFIG_PARAM_ZK_HOST"] = zookeeper_ip

            if service_name == "hbase":
                # add cluster map
                for cluster_id in service_str["clusterIdClusterMap"]:
                    cluster_str = service_str["clusterIdClusterMap"][cluster_id]
                    # add member map
                    if cluster_str["appId"] == app_id:
                        for member_id in cluster_str["memberMap"]:
                            member_str = cluster_str["memberMap"][member_id]
                            if hbase_master_ip is None:
                                hbase_master_ip = member_str["defaultPrivateIP"]
                                os.environ["CONFIG_PARAM_HBASE_MASTER_HOST"] = hbase_master_ip

        CONFIG_PARAM_CLUSTERING = "true"
        CONFIG_PARAM_MEMBERSHIP_SCHEME = "stratos"
        os.environ['CONFIG_PARAM_CLUSTERING'] = CONFIG_PARAM_CLUSTERING
        os.environ['CONFIG_PARAM_MEMBERSHIP_SCHEME'] = CONFIG_PARAM_MEMBERSHIP_SCHEME

        log.info(
            "env CONFIG_PARAM_CLUSTERING: %s  CONFIG_PARAM_MEMBERSHIP_SCHEME:%s ",
            (os.environ.get('CONFIG_PARAM_CLUSTERING')),
            (os.environ.get('CONFIG_PARAM_MEMBERSHIP_SCHEME')))

        # Configuring Cluster IDs
        CONFIG_PARAM_CLUSTER_IDS = values["CLUSTER_ID"]
        log.info("Cluster ID : %s" % CONFIG_PARAM_CLUSTER_IDS)
        os.environ['CONFIG_PARAM_CLUSTER_IDS'] = CONFIG_PARAM_CLUSTER_IDS
        log.info(
            "env CONFIG_PARAM_CLUSTER_IDS: %s " % (os.environ.get('CONFIG_PARAM_CLUSTER_IDS')))

        # creating databases
        self.create_database('ANALYTICS_FS_DB', 'FS_user', 'fs123')
        mds_response = mdsclient.get(app=True)
        if mds_response is not None and mds_response.properties.get("MYSQL_HOST") is not None:
            remote_host = mds_response.properties.get("MYSQL_HOST")
        else:
            log.error('mysql details not published to metadata service')

        os.environ[
            'CONFIG_PARAM_WSO2_ANALYTICS_WSO2_ANALYTICS_FS_DB_URL'] = "jdbc:mysql://" + remote_host + ":3306/ANALYTICS_FS_DB?autoReconnect=true"
        os.environ[
            'CONFIG_PARAM_WSO2_ANALYTICS_WSO2_ANALYTICS_FS_DB_USER_NAME'] = "FS_user"
        os.environ[
            'CONFIG_PARAM_WSO2_ANALYTICS_WSO2_ANALYTICS_FS_DB_PASSWORD'] = "fs123"

        self.create_database('ANALYTICS_PROCESSED_DATA_STORE', 'DS_user', 'ds123')
        os.environ[
            'CONFIG_PARAM_WSO2_ANALYTICS_PROCESSED_DATA_STORE_DB_URL'] = "jdbc:mysql://" + remote_host + ":3306/ANALYTICS_PROCESSED_DATA_STORE?autoReconnect=true"
        os.environ[
            'CONFIG_PARAM_WSO2_ANALYTICS_PROCESSED_DATA_STORE_DB_USER_NAME'] = "DS_user"
        os.environ[
            'CONFIG_PARAM_WSO2_ANALYTICS_PROCESSED_DATA_STORE_DB_PASSWORD'] = "ds123"

        # configure server
        log.info("Configuring WSO2 DAS ...")
        config_command = "python /opt/ppaas-configurator-4.1.0-SNAPSHOT/configurator.py"
        env_var = os.environ.copy()
        p = subprocess.Popen(config_command, env=env_var, shell=True)
        output, errors = p.communicate()
        log.info("WSO2 DAS configured successfully")

