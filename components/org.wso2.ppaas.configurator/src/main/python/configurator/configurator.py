#!/usr/bin/python

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


from distutils.dir_util import copy_tree
import logging
import logging.config
import os

from jinja2 import Environment, FileSystemLoader, meta

import constants
from configparserutil import ConfigParserUtil

PATH = os.path.dirname(os.path.abspath(__file__))

logging.config.fileConfig(os.path.join(PATH, 'conf', 'logging_config.ini'))
log = logging.getLogger(__name__)

PACK_LOCATION = None
READ_FROM_ENVIRONMENT = None
TEMPLATE_DIRECTORY = None
TEMPLATE_ENVIRONMENT = Environment(
    autoescape=False,
    loader=FileSystemLoader(os.path.abspath(os.sep)),
    trim_blocks=False)

def render_template(template_filename, default_context):
    """
    parse the xml file and return the content as a text

    :param template_filename: template filename path
    :param default_context: dictionary containing configurations read from module.ini
    :return: xml as a string
    """
    if READ_FROM_ENVIRONMENT == "true":
        template_source = \
            TEMPLATE_ENVIRONMENT.loader.get_source(TEMPLATE_ENVIRONMENT, template_filename)[0]
        parsed_content = TEMPLATE_ENVIRONMENT.parse(template_source)
        variables = meta.find_undeclared_variables(parsed_content)
        log.debug("Template variables : %s", variables)
        context = ConfigParserUtil.get_context_from_env(variables, default_context)
    else:
        context = default_context

    # Adding CARBON_HOME to context as it is required for registry db
    context[constants.CONFIG_SETTINGS_CARBON_HOME] = PACK_LOCATION
    log.info("Final context generated for rendering %s : %s ", os.path.basename(template_filename),
             context)
    log.info("Rendering template: %s \n", template_filename)
    return TEMPLATE_ENVIRONMENT.get_template(template_filename).render(context)


def generate_file_from_template(template_path, output_path, context):
    """
    Generate file from the given template file

    :param template_path: Path to the template file
    :param output_path: Path to the output file
    :param context: Dictionary containing values to be used by jinja engine
    :return: None
    """
    directory = os.path.dirname(output_path)
    if not os.path.exists(directory):
        os.makedirs(directory)
    with open(output_path, 'w') as xml_file:
        log.info("Rendering output file: " + output_path)
        content = render_template(template_path, context)
        log.debug("Creating content: " + content)
        xml_file.write(content)


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
    log.debug("Configuration file content %s", configurations)
    settings = configurations[constants.CONFIG_SETTINGS]
    global PACK_LOCATION
    PACK_LOCATION = os.environ.get(constants.CONFIG_SETTINGS_CARBON_HOME,
                                   settings[constants.CONFIG_SETTINGS_CARBON_HOME])
    log.info("CARBON_HOME : %s" % PACK_LOCATION)
    context = configurations[constants.CONFIG_PARAMS]

    log.info("Context generated: %s", context)
    # if read_env_variables is true context will be generated from environment variables
    # if read_env_variables is not true context will be read from config.ini
    if settings["READ_FROM_ENVIRONMENT"] == "true":
        global READ_FROM_ENVIRONMENT
        READ_FROM_ENVIRONMENT = "true"
        log.info("Reading from environment")
    else:
        # Converting multi-valued params to dictionary
        context = ConfigParserUtil.get_multivalued_attributes_as_dictionary(context)
    return context


def traverse(root_dir, context):
    """
    traverse through the folder structure and generate xml files

    :param root_dir: path to the template/{wso2_server}/conf folder
    :param context: dictionary containing values to be used by jinja engine
    :return:None
    """
    log.info("Starting to configure: " + root_dir)
    for dir_name, subdirList, fileList in os.walk(root_dir):
        for file_name in fileList:
            # generating the relative path of the template
            template_file_name = os.path.join(dir_name, file_name)
            log.debug("Template file name: %s " % template_file_name)
            config_file_name = \
                os.path.splitext(os.path.relpath(os.path.join(dir_name, file_name), root_dir))[0]
            config_file_name = os.path.join(PACK_LOCATION,
                                            config_file_name)
            log.debug("Template file: %s ", template_file_name)
            log.debug("Output configuration file: %s ", config_file_name)
            generate_file_from_template(template_file_name, config_file_name, context)


def copy_files_to_pack(source):
    """
    Copy files in the template's files directory to pack preserving the structure provided
    :param source: path to files directory in template folder
    :return:
    """
    result = copy_tree(source, PACK_LOCATION, verbose=1)
    log.info("Files copied: %s", result)


def configure():
    """
    Main method    :return: None
    """
    log.info("Configurator started")
    # traverse through the template directory
    global TEMPLATE_DIRECTORY
    TEMPLATE_DIRECTORY = os.environ.get("CONFIGURATOR_TEMPLATE_PATH", constants.TEMPLATE_DIRECTORY)
    log.info("Scanning template directory :%s" % TEMPLATE_DIRECTORY)
    for dirName in os.listdir(os.path.join(PATH, TEMPLATE_DIRECTORY)):
        if dirName == ".gitkeep":
            continue

        module_file_path = os.path.join(TEMPLATE_DIRECTORY, dirName,
                                        constants.CONFIG_FILE_NAME)
        template_dir = os.path.join(PATH, TEMPLATE_DIRECTORY, dirName,
                                    constants.TEMPLATE_FOLDER_NAME)
        files_dir = os.path.join(PATH, TEMPLATE_DIRECTORY, dirName,
                                 constants.FILES_DIRECTORY_NAME)
        if os.path.isfile(module_file_path):
            log.info("module.ini file found: %s", module_file_path)
        else:
            log.error("module.ini file not found in path: %s", module_file_path)
            return
        log.info("Template directory: %s", template_dir)
        context = generate_context(module_file_path)
        traverse(template_dir, context)

        # copy files if exists
        if os.path.exists(files_dir):
            log.info("Copying files...")
            copy_files_to_pack(files_dir)

    log.info("Configuration completed")


if __name__ == "__main__":
    configure()
