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

import mdsclient
from plugins.contracts import ICartridgeAgentPlugin
from xml.dom.minidom import parse
import socket
from modules.util.log import LogFactory
import time
import subprocess
import os


class WSO2DASStartupHandler(ICartridgeAgentPlugin):
    def run_plugin(self, values):
        log = LogFactory().get_log(__name__)

        # start server
        log.info("Starting WSO2 DAS...")
        profile = os.environ['CONFIG_PARAM_PROFILE']
        log.info("Profile : %s " % profile)
        start_command = None
        if profile:
            if profile == "receiver":
                start_command = "exec ${CARBON_HOME}/bin/wso2server.sh start -DdisableAnalyticsExecution=true -DdisableAnalyticsEngine=true"
            elif profile == "analytics":
                start_command = "exec ${CARBON_HOME}/bin/wso2server.sh start -DdisableEventSink=true"
            elif profile == "dashboard":
                start_command = "exec ${CARBON_HOME}/bin/wso2server.sh start -DdisableEventSink=true -DdisableAnalyticsExecution=true -DdisableAnalyticsEngine=true"
            elif profile == "default":
                start_command = "exec ${CARBON_HOME}/bin/wso2server.sh start"
            else:
                log.info("Invalid profile :" + profile)
        log.info("Start command : %s" % start_command)
        env_var = os.environ.copy()
        p = subprocess.Popen(start_command, env=env_var, shell=True)
        output, errors = p.communicate()
        log.debug("WSO2 DAS started successfully")
