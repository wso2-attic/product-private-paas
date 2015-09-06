# Copyright 2005-2015 WSO2, Inc. (http://wso2.com)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#  http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.


import mdsclient
from plugins.contracts import ICartridgeAgentPlugin
from xml.dom.minidom import parse
import socket
from modules.util.log import LogFactory
import time
import subprocess
import os


class MYSQLStartupHandler(ICartridgeAgentPlugin):
    log = LogFactory().get_log(__name__)

    CONST_MYSQL_HOST = "MYSQL_HOST"
    CONST_MYSQL_ROOT_USERNAME = "MYSQL_ROOT_USERNAME"
    CONST_MYSQL_ROOT_PASSWORD = "MYSQL_ROOT_PASSWORD"

    CONST_MYSQL_ROOT_USERNAME_VALUE = "root"


    def run_plugin(self, values):

        mysql_root_password = os.environ[MYSQLStartupHandler.CONST_MYSQL_ROOT_PASSWORD]
        temp_file_path = "/tmp/temp.sql"
        MYSQLStartupHandler.log.info("MYSQL_ROOT_PASSWORD : %s" % mysql_root_password)

        f = open(temp_file_path, "w+")
        f.write(
            "USE mysql;\n"
            "FLUSH PRIVILEGES;\n"
            "GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;\n"
            "UPDATE user SET password=PASSWORD('" + mysql_root_password + "') WHERE user='root';")
        f.close()

        MYSQLStartupHandler.log.info("Temp File created")

        mysql_command = "/usr/sbin/mysqld --bootstrap --verbose=0 < " + temp_file_path
        env_var = os.environ.copy()
        p = subprocess.Popen(mysql_command, env=env_var, shell=True)
        output, errors = p.communicate()
        MYSQLStartupHandler.log.info("%s file executed" % temp_file_path)

        mysql_start_command = "service mysql restart"
        p = subprocess.Popen(mysql_start_command, env=env_var, shell=True)
        output, errors = p.communicate()
        MYSQLStartupHandler.log.debug("mysql started successfully")

        # get local ip as to export to metadata
        get_local_ip_cmd = "awk 'NR==1 {print $1}' /etc/hosts"
        local_ip = subprocess.check_output(get_local_ip_cmd, shell=True)

        if local_ip is not None:
            local_ip = local_ip[0:-1]
        MYSQLStartupHandler.log.info("local IP from /etc/hosts : %s " % local_ip)

        self.remove_data_from_metadata(MYSQLStartupHandler.CONST_MYSQL_HOST)
        self.remove_data_from_metadata(MYSQLStartupHandler.CONST_MYSQL_ROOT_USERNAME)
        self.remove_data_from_metadata(MYSQLStartupHandler.CONST_MYSQL_ROOT_PASSWORD)

        self.add_data_to_meta_data_service(MYSQLStartupHandler.CONST_MYSQL_HOST, local_ip)
        self.add_data_to_meta_data_service(MYSQLStartupHandler.CONST_MYSQL_ROOT_USERNAME,
                                           MYSQLStartupHandler.CONST_MYSQL_ROOT_USERNAME_VALUE)
        self.add_data_to_meta_data_service(MYSQLStartupHandler.CONST_MYSQL_ROOT_PASSWORD, mysql_root_password)


    def add_data_to_meta_data_service(self, key, value):
        """
        add data to meta data service
        :return: void
        """
        mdsclient.MDSPutRequest()
        data = {"key": key, "values": [value]}
        mdsclient.put(data, app=True)

        MYSQLStartupHandler.log.info("Value added to the metadata service %s: %s" % (key, value))


    def remove_data_from_metadata(self, key):
        """
        remove data from meta data service
        :return: void
        """
        mds_response = mdsclient.get(app=True)

        if mds_response is not None and mds_response.properties.get(key) is not None:
            read_data = mds_response.properties[key]
            check_str = isinstance(read_data, (str, unicode))

            if check_str == True:
                mdsclient.delete_property_value(key, read_data)
            else:
                check_int = isinstance(read_data, int)
                if check_int == True:
                    mdsclient.delete_property_value(key, read_data)
                else:
                    for entry in read_data:
                        mdsclient.delete_property_value(key, entry)


