# ------------------------------------------------------------------------
#
# Copyright 2005-2015 WSO2, Inc. (http://wso2.com)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License
#
# ------------------------------------------------------------------------


import mdsclient
from plugins.contracts import ICartridgeAgentPlugin
from xml.dom.minidom import parse
import socket
from modules.util.log import LogFactory
import time
import subprocess
import os


class MYSQLStartupHandler(ICartridgeAgentPlugin):
    def publish_metadata(self, properties_data):
        log = LogFactory().get_log(__name__)
        publish_data = mdsclient.MDSPutRequest()
        publish_data.properties = properties_data
        mdsclient.put(publish_data, app=True)
        log.info("Published metadata: %s " % publish_data)

    def run_plugin(self, values):
        log = LogFactory().get_log(__name__)
        MYSQL_ROOT_PASSWORD = os.environ["MYSQL_ROOT_PASSWORD"];
        TEMP_FILE_PATH="/tmp/temp.sql"
        log.info("MYSQL_ROOT_PASSWORD : %s" % MYSQL_ROOT_PASSWORD)
        f = open(TEMP_FILE_PATH, "w+")
        f.write(
            "USE mysql;\n"
            "FLUSH PRIVILEGES;\n"
            "GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;\n"
            "UPDATE user SET password=PASSWORD('" + MYSQL_ROOT_PASSWORD + "') WHERE user='root';")
        f.close()

        log.info("Temp File created")

        mysql_command = "/usr/sbin/mysqld --bootstrap --verbose=0 < "+TEMP_FILE_PATH
        env_var = os.environ.copy()
        p = subprocess.Popen(mysql_command, env=env_var, shell=True)
        output, errors = p.communicate()
        log.info("%s file executed" %TEMP_FILE_PATH)

        mysql_start_command = "service mysql restart"
        p = subprocess.Popen(mysql_start_command, env=env_var, shell=True)
        output, errors = p.communicate()
        log.debug("mysql started successfully")

        # get local ip as to export to metadata
        get_local_ip_cmd = "awk 'NR==1 {print $1}' /etc/hosts"
        local_ip = subprocess.check_output(get_local_ip_cmd, shell=True)

        if local_ip is not None:
            local_ip = local_ip[0:-1]
        log.info("local IP from /etc/hosts : %s " % local_ip)

        # publishing to metadata service
        mysql_host = {"key": "MYSQL_HOST", "values": local_ip}
        mysql_password = {"key": "MYSQL_ROOT_PASSWORD", "values": MYSQL_ROOT_PASSWORD}
        mysql_username = {"key": "MYSQL_ROOT_USERNAME", "values": "root"}

        self.publish_metadata(mysql_host)
        self.publish_metadata(mysql_username)
        self.publish_metadata(mysql_password)




