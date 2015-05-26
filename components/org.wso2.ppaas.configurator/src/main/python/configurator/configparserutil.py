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

import ConfigParser
import ast


class ConfigParserUtil(ConfigParser.ConfigParser):
    def as_dictionary(self):
        """
        read configuration file and create a dictionary
        :return: configurations as a dictionary
        """
        d = dict(self._sections)
        for k in d:
            d[k] = dict(self._defaults, **d[k])
            d[k].pop('__name__', None)
        return d

    @staticmethod
    def convert_properties_to_dictionary(variable):
        """
        convert and return mulit valued properties as a dictionary e.g :- Members,port mappings
        :param property:
        :return: dictionary of well known members
        """

        properties = ast.literal_eval(variable).split(",")
        return dict(s.split(':') for s in properties)

