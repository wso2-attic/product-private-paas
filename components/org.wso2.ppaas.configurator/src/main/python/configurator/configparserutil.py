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


import ConfigParser
import os
import logging

log = logging.getLogger()


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
        convert and return multi valued properties as a dictionary e.g :- Members,port mappings
        :param property:
        :return: dictionary of properties
        """
        variable = variable.replace('[', '')
        variable = variable.replace(']', '')
        properties = variable.split(",")
        return dict(s.split(':') for s in properties)

    @staticmethod
    def get_multivalued_attributes_as_dictionary(context):
        """
        find multivalued attributes from context and convert them to dictionary
        :param context:
        :return:dictionary of properties
        """

        for key, value in context.iteritems():
            if value and value.startswith('['):
                context[key] = ConfigParserUtil.convert_properties_to_dictionary(value)
        return context

    @staticmethod
    def get_context_from_env(template_variables, default_context):
        """
        Read values from environment variables
        :param template_variables:
        :return: dictionary containing environment variables
        """
        context = {}
        while template_variables:
            var = template_variables.pop()
            if not os.environ.get(var):
                log.info("Environment variable %s is not found. Reading from module.ini", var)
                if var in default_context:
                    context[var] = os.environ.get(var, default_context[var])
                else:
                    log.warn("Variable %s is not found in module.ini or in environment variables",
                             var)
            else:
                context[var] = os.environ.get(var)

        context = ConfigParserUtil.get_multivalued_attributes_as_dictionary(context)
        return context
