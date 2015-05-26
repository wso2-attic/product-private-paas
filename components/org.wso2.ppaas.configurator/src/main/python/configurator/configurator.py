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

# !/usr/bin/env python
import ast
import logging
import logging.config
import os

from jinja2 import Environment, FileSystemLoader

import constants
from configparserutil import ConfigParserUtil


PATH = os.path.dirname(os.path.abspath(__file__))

logging.config.fileConfig(os.path.join(PATH, 'conf', 'logging_config.ini'))
log = logging.getLogger(__name__)

TEMPLATE_ENVIRONMENT = Environment(
    autoescape=False,
    loader=FileSystemLoader(os.path.join(PATH)),
    trim_blocks=False)
PACK_LOCATION = None


def render_template(template_filename, context):
    """
    parse the xml file and return the content as a text

    :param template_filename: template filename path
    :param context: dictionary containing configurations
    :return: xml as a string
    """
    log.info("Rendering template: %s", template_filename)
    return TEMPLATE_ENVIRONMENT.get_template(template_filename).render(context)


def create_output_xml(template_path, output_path, context):
    """
    This method write the output generated from jinja file to xml file

    :param template_path: Path to the template file
    :param output_path: Path to the output xml file
    :param context: Dictionary containing values to be used by jinja engine
    :return: None
    """
    directory = os.path.dirname(output_path)
    if not os.path.exists(directory):
        os.makedirs(directory)
    with open(output_path, 'w') as xml_file:
        content = render_template(template_path, context)
        xml_file.write(content)
        log.info("Creating output file :" + output_path)
        log.debug("Creating content: " + content)


def generate_context(config_file_path):
    """
    Read the config.ini and generate context based on settings

    :param config_file_path: location of the config.ini file
    :return: dictionary containing configurations for jinja engine
    """
    # Read configuration file
    config_parser = ConfigParserUtil()
    config_parser.optionxform = str
    config_parser.read(os.path.join(PATH, config_file_path))
    configurations = config_parser.as_dictionary()

    # Reading the default values
    context = configurations[constants.CONFIG_PARAMS]
    settings = configurations[constants.CONFIG_SETTINGS]
    global PACK_LOCATION
    PACK_LOCATION = settings["REPOSITORY_CONF_DIRECTORY"]

    # if read_env_variables is true context will be generated from environment variables
    # if read_env_variables is not true context will be read from config.ini
    if settings["READ_FROM_ENVIRONMENT"] == "true":
        log.info("Reading from environment variables")
        for key, value in context.iteritems():
            # check if value exists for given key; use default if not exists
            context[key] = os.environ.get(key, context[key])

    # Converting Members to dictionary
    if "STRATOS_MEMBERS" in context:
        context["STRATOS_MEMBERS"] = ConfigParserUtil.convert_properties_to_dictionary(
            context['STRATOS_MEMBERS'])

    if "STRATOS_PORT_MAPPING" in context:
        context["STRATOS_PORT_MAPPING"] = ConfigParserUtil.convert_properties_to_dictionary(
            context["STRATOS_PORT_MAPPING"])

    log.info("Context generated: %s", context)
    return context


def traverse(root_dir, context):
    """
    traverse through the folder structure and generate xml files

    :param root_dir: path to the template/{wso2_server}/conf folder
    :param context: dictionary containing values to be used by jinja engine
    :return:None
    """
    log.info("Starting to traverse " + root_dir)
    for dir_name, subdirList, fileList in os.walk(root_dir):
        for file_name in fileList:
            # generating the relative path of the template
            template_file_name = os.path.join(dir_name, file_name)
            log.info(template_file_name)
            log.debug("Template file name: %s " % template_file_name)
            config_file_name = \
                os.path.splitext(os.path.relpath(os.path.join(dir_name, file_name), root_dir))[0] \
                + ".xml"
            config_file_name = os.path.join(PACK_LOCATION, config_file_name)
            template_file_name = template_file_name.split("/./")[1]
            log.debug("Template file : %s ", template_file_name)
            log.debug("Output configuration file : %s ", config_file_name)
            create_output_xml(template_file_name, config_file_name, context)


def configure():
    """
    Main method    :return: None
    """
    log.info("Configurator started.")
    # traverse through the template directory
    for dirName in os.listdir(os.path.join(PATH, constants.TEMPLATE_PATH)):
        config_file_path = os.path.join(constants.TEMPLATE_PATH, dirName,
                                        constants.CONFIG_FILE_NAME)
        template_dir = os.path.join(PATH, constants.TEMPLATE_PATH, dirName, "conf")
        context = generate_context(config_file_path)
        traverse(template_dir, context)
        log.info("Copying files to %s", PACK_LOCATION)

    log.info("Configuration completed")


if __name__ == "__main__":
    configure()