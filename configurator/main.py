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
from distutils import dir_util
import logging
import shutil
from configparserutil import ConfigParserUtil
import os
import constants
from jinja2 import Environment, FileSystemLoader

logging.basicConfig(level=logging.DEBUG,
                    format='%(asctime)s %(levelname)-8s %(message)s',
                    datefmt='%a, %d %b %Y %H:%M:%S',
                    filename='./configurator.log',
                    filemode='w')

PATH = os.path.dirname(os.path.abspath(__file__))
TEMPLATE_ENVIRONMENT = Environment(
    autoescape=False,
    loader=FileSystemLoader(os.path.join(PATH)),
    trim_blocks=False)
PACK_LOCATION = None

# parse the xml file and return the content as a text
def render_template(template_filename, context):
    return TEMPLATE_ENVIRONMENT.get_template(template_filename).render(context)


# This method write the output generated from jinja file to xml file
# template_path : Path to the template file
# output_path : Path to the output xml file
# context : dictionary containing values to be used by jinja engine
def create_output_xml(template_path, output_path, context):
    # check output path exists
    directory = os.path.dirname(output_path)
    if not os.path.exists(directory):
        os.makedirs(directory)
    with open(output_path, 'w') as xml_file:
        content = render_template(template_path, context)
        xml_file.write(content)


# Read the config.ini and generate context based on settings
# if read_env_variables is true context will be generated from environment variables
# if read_env_variables is not true context will be read from config.ini
def generate_context(config_file_path):
    # Read configuration file
    config_parser = ConfigParserUtil()
    config_parser.read(config_file_path)
    configurations = config_parser.as_dict()

    # Reading the default values
    context = configurations["DEFAULTS"]
    settings = configurations["SETTINGS"]
    global PACK_LOCATION
    PACK_LOCATION = settings["pack_location"]
    if settings["read_env_variables"] == "true":
        logging.info("Reading from environment variables")
        for key, value in context.iteritems():
            context[key] = os.environ.get(key, context[key])

    else:
        logging.info("Reading Values from config.ini")
        param_context = configurations["PARAMS"]
        for key, value in context.iteritems():
            if key in param_context:
                context[key] = param_context[key]

    # check whether members are available in context before conversion
    if 'members' in context:
        context['members'] = ast.literal_eval(context['members'])

    logging.info("Context generated %s", context)
    return context


# traverse through the folder structure and generate xml files
# root_dir : path to the template/{wso2_server}/conf folder
# context : dictionary containing values to be used by jinja engine
def traverse(root_dir, context):
    for dirName, subdirList, fileList in os.walk(root_dir):
        for file_name in fileList:
            # generating the relative path of the template
            template_file_name1 = os.path.join(dirName, file_name)
            config_file_name = \
                os.path.splitext(os.path.relpath(os.path.join(dirName, file_name), root_dir))[0] \
                + ".xml"
            config_file_name = os.path.join("./output", config_file_name)
            create_output_xml(template_file_name1, config_file_name, context)


def main():
    logging.info("Configurator Started")
    for dirName in os.listdir(constants.TEMPLATE_PATH):
        config_file_path = os.path.join(constants.TEMPLATE_PATH, dirName,
                                        constants.CONFIG_FILE_NAME)
        template_dir = os.path.join(constants.TEMPLATE_PATH, dirName, "conf")
        context = generate_context(config_file_path)
        traverse(template_dir, context)

    dir_util.copy_tree("./output", PACK_LOCATION)
    shutil.rmtree('./output/')

if __name__ == "__main__":
    main()