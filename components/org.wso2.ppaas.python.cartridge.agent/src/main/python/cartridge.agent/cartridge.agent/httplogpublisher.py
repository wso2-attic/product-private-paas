# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

import datetime
from threading import Thread, current_thread

from modules.databridge.agent import *
from config import Config
from exception import DataPublisherException
import constants
import urllib2
import json
import base64
import traceback


class HttpLogPublisher(Thread):
    def __init__(self, file_path, tenant_id, alias, date_time, member_id, application_id):
        Thread.__init__(self)
        self.log = LogFactory().get_log(__name__)
        self.file_path = file_path
        self.tenant_id = tenant_id
        self.alias = alias
        self.date_time = date_time
        self.member_id = member_id
        self.application_id = application_id

        self.terminated = False
        self.setName("HttpLogPublisherThread")
        self.setDaemon(True)
        self.log.debug("Created a HttpLogPublisher thread")

    def run(self):
        self.log.debug("Starting the Http LogPublisher threads")
        if os.path.isfile(self.file_path) and os.access(self.file_path, os.R_OK):
            self.log.info(
                "Starting Http log Publisher for file: " + self.file_path + ", thread: " + str(current_thread()))
            # open file and keep reading for new entries
            # with open(self.file_path, "r") as read_file:
            read_file = open(self.file_path, "r")
            # read_file.seek(os.stat(self.file_path)[6])  # go to the end of the file

            log_analyzer_url = HttpLogAnalyzerConfiguration.get_instance().log_analyzer_url
            log_analyzer_username = HttpLogAnalyzerConfiguration.get_instance().log_analyzer_username
            log_analyzer_password = HttpLogAnalyzerConfiguration.get_instance().log_analyzer_password
            auth = base64.encodestring('%s:%s' % (log_analyzer_username, log_analyzer_password)).replace('\n', '')
            
            while not self.terminated:
                where = read_file.tell()  # where the seeker is in the file
                line = read_file.readline()  # read the current line
                if not line:
                    # no new line entered
                    self.log.debug("No new log entries detected to publish.")
                    time.sleep(1)
                    read_file.seek(where)  # set seeker
                else:
                    self.log.debug("Log entry/entries detected. Publishing to Analyzing server.")

                    payload = {
                        "@logstream": "['member_id', '%s']" % self.member_id,
                        "@timestamp": "%s" % self.date_time,
                        "message": "%s" % line,
                        "alias": "%s" % self.alias,
                        "tenant_id": "%s" % self.tenant_id,
                        "member_id": "%s" % self.member_id,
                        "application_id": "%s" % self.application_id
                    }

                    request = urllib2.Request(log_analyzer_url,json.dumps(payload))
                    request.add_header('Content-Type', 'application/json')
                    request.add_header('Authorization', 'Basic %s' % auth)

                    try:
                        urllib2.urlopen(request)
                    except Exception:
                        self.log.error("Failed to publish event.")
                        self.log.error(traceback.format_exc())
                        continue

        else:
            raise DataPublisherException("Unable to read the file at path \"%s\"" % self.file_path)

    def terminate(self):
        """
        Allows the LogPublisher thread to be terminated to stop publishing to BAM/CEP. Allow a minimum of 1 second delay
        to take effect.
        """
        self.terminated = True


class HttpLogPublisherManager(Thread):
    def __init__(self, logfile_paths):
        Thread.__init__(self)
        self.setDaemon(True)

        self.log = LogFactory().get_log(__name__)

        self.logfile_paths = logfile_paths
        self.http_publishers = {}

        self.tenant_id = HttpLogPublisherManager.get_valid_tenant_id(Config.tenant_id)
        self.alias = HttpLogPublisherManager.get_alias(Config.cluster_id)
        self.date_time = HttpLogPublisherManager.get_current_date()

        self.setName("HttpLogPublisherManagerThread")
        self.log.debug("Created a HttpLogPublisherManager thread")

    def run(self):
        self.log.debug("Starting the HttpLogPublisherManager thread")
        if self.logfile_paths is not None and len(self.logfile_paths):
            for log_path in self.logfile_paths:
                # thread for each log file
                publisher = self.get_publisher(log_path)
                publisher.start()
                self.log.debug("Http Log publisher for path \"%s\" started." % log_path)

    def get_publisher(self, log_path):
        """
        Retrieve the publisher for the specified log file path. Creates a new LogPublisher if one is not available
        :return: The LogPublisher object
        :rtype : LogPublisher
        """
        if log_path not in self.http_publishers:
            self.log.debug("Creating a Http Log publisher for path \"%s\"" % log_path)
            self.http_publishers[log_path] = HttpLogPublisher(
                log_path,
                self.tenant_id,
                self.alias,
                self.date_time,
                Config.member_id,
                Config.application_id)

        return self.http_publishers[log_path]

    def terminate_publisher(self, log_path):
        """
        Terminates the LogPublisher thread associated with the specified log file
        """
        if log_path in self.http_publishers:
            self.http_publishers[log_path].terminate()

    def terminate_all_publishers(self):
        """
        Terminates all LogPublisher threads
        """
        for publisher in self.http_publishers:
            publisher.terminate()

    @staticmethod
    def get_valid_tenant_id(tenant_id):
        if tenant_id == constants.INVALID_TENANT_ID or tenant_id == constants.SUPER_TENANT_ID:
            return "0"

        return tenant_id

    @staticmethod
    def get_alias(cluster_id):
        try:
            alias = cluster_id.split("\\.")[0]
        except:
            alias = cluster_id

        return alias

    @staticmethod
    def get_current_date():
        """
        Returns the current date in iso format
        :return: Formatted date string
        :rtype : str
        """
        return datetime.datetime.now().isoformat()


class HttpLogAnalyzerConfiguration:
    __instance = None
    log = LogFactory().get_log(__name__)

    @staticmethod
    def get_instance():
        """
        Singleton instance retriever
        :return: Instance
        :rtype : DataPublisherConfiguration
        """
        if HttpLogAnalyzerConfiguration.__instance is None:
            HttpLogAnalyzerConfiguration.__instance = HttpLogAnalyzerConfiguration()

        return HttpLogAnalyzerConfiguration.__instance

    def __init__(self):
        self.enabled = False
        self.log_analyzer_url = None
        self.log_analyzer_username = None
        self.log_analyzer_password = None

        self.read_config()

    def read_config(self):
        self.enabled = Config.read_property(constants.LOG_ANALYZER_ENABLED, False)
        if not self.enabled:
            HttpLogAnalyzerConfiguration.log.info("Log Analyzer disabled")
            return

        HttpLogAnalyzerConfiguration.log.info("Log Analyzer enabled")

        self.log_analyzer_url = Config.read_property(constants.LOG_ANALYZER_URL, False)
        if self.log_analyzer_url is None:
            raise RuntimeError("System property not found: " + constants.LOG_ANALYZER_URL)

        self.log_analyzer_username = Config.read_property(constants.LOG_ANALYZER_USERNAME, False)
        if self.log_analyzer_username is None:
            raise RuntimeError("System property not found: " + constants.LOG_ANALYZER_USERNAME)

        self.log_analyzer_password = Config.read_property(constants.LOG_ANALYZER_PASSWORD, False)
        if self.log_analyzer_password is None:
            raise RuntimeError("System property not found: " + constants.LOG_ANALYZER_PASSWORD)

        HttpLogAnalyzerConfiguration.log.info("Log Analyzer configuration initialized")
