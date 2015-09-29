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

from plugins.contracts import ICartridgeAgentPlugin
from modules.util.log import LogFactory
from entity import *
import subprocess
import os


class WSO2LogstashHandler(ICartridgeAgentPlugin):
    """
    Configures and starts configurator, carbon server
    """
    log = LogFactory().get_log(__name__)

    # class constants
    CONST_SERVICE_NAME = "SERVICE_NAME"
    ENV_CONFIG_PARAM_SERVICE_TYPE = "CONFIG_PARAM_SERVICE_TYPE"
    ENV_CONFIG_PARAM_ELASTIC_SEARCH_IP = "CONFIG_PARAM_ELASTIC_SEARCH_IP"
    ENV_CONFIG_PARAM_LOG_FILE_PATH = "CONFIG_PARAM_LOG_FILE_PATH"

    def run_plugin(self, values):
        # read from 'values'
        service_type = values[self.CONST_SERVICE_NAME]
        elastic_search_ip = values.get(self.ENV_CONFIG_PARAM_ELASTIC_SEARCH_IP)
        WSO2LogstashHandler.log.info("Service Type: %s" % service_type)
        WSO2LogstashHandler.log.info("Elastic Search IP: %s" % elastic_search_ip)
        self.export_env_var(self.ENV_CONFIG_PARAM_SERVICE_TYPE, service_type)
        carbon_home = os.environ.get('CARBON_HOME')
        self.export_env_var(self.ENV_CONFIG_PARAM_LOG_FILE_PATH, "%s/repository/logs/wso2carbon.log" % carbon_home)

        if elastic_search_ip is not None:

            # start configurator
            WSO2LogstashHandler.log.info("Configuring WSO2 ...")
            config_command = "python ${CONFIGURATOR_HOME}/configurator.py"
            env_var = os.environ.copy()
            p = subprocess.Popen(config_command, env=env_var, shell=True)
            output, errors = p.communicate()
            WSO2LogstashHandler.log.info("Logstash configured successfully")

            # start logstash server
            WSO2LogstashHandler.log.info("Starting Logstash ...")
            start_command = "/opt/logstash-1.5.4/bin/logstash -f ${CARBON_HOME}/logstash.conf"
            env_var = os.environ.copy()
            p = subprocess.Popen(start_command, env=env_var, shell=True)
            output, errors = p.communicate()
            WSO2LogstashHandler.log.info("Logstash  started successfully ..")

    @staticmethod
    def export_env_var(variable, value):
        """
        exports key value pairs as env. variables

        :return: void
        """
        if value is not None:
            os.environ[variable] = value
            WSO2LogstashHandler.log.info("Exported environment variable %s: %s" % (variable, value))
        else:
            WSO2LogstashHandler.log.warn("Could not export environment variable %s " % variable)