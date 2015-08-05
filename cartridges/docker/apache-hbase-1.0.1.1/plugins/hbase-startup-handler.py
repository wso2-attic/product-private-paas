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
import subprocess
import os
import time

class HbaseStartupHandler(ICartridgeAgentPlugin):

    def run_plugin(self, values):
        log = LogFactory().get_log(__name__)

        log.info("Reading environment variables...")
        clustering_enable = os.environ.get('CLUSTER')
        log.info(clustering_enable)

        configured = os.environ.get('CONFIGURED')
        log.info(configured)

        while configured is not 'true':
            time.sleep(5)
            configured = os.environ.get('CONFIGURED')
            log.info(configured)
            log.info("Waiting for configurations completion.")

        if clustering_enable == 'true':

            # start server
            log.info("Starting Hbase Master node ...")

            start_command = "exec ${HBASE_HOME}/bin/start-hbase.sh"
            env_var = os.environ.copy()
            p = subprocess.Popen(start_command, env=env_var, shell=True)
            output, errors = p.communicate()

            log.info("Hbase Master node started successfully")


        else:

            # start server
            log.info("Starting Hbase Regionserver ...")

            start_command = "exec ${HBASE_HOME}/bin/hbase-daemon.sh start regionserver"
            env_var = os.environ.copy()
            p = subprocess.Popen(start_command, env=env_var, shell=True)
            output, errors = p.communicate()

            log.info("Hbase Regionserver started successfully")
