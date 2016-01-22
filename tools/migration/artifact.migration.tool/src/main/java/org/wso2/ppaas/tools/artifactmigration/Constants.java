/*
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ppaas.tools.artifactmigration;

import org.apache.commons.lang.StringUtils;

import java.io.File;

/**
 * Constants Details
 */
public class Constants {

    //PPaaS 4.0.0 REST API endpoints
    public static final String STRATOS_API_PATH = "stratos" + File.separator + "admin" + File.separator;
    private static final String MIGRATION_API_PATH = "migration" + File.separator + "admin" + File.separator;
    // Do not use forward slash at the beginning instead use it in the base url.
    public static final String URL_PARTITION = STRATOS_API_PATH + "partition";
    public static final String URL_POLICY_AUTOSCALE = STRATOS_API_PATH + "policy" + File.separator + "autoscale";
    public static final String URL_POLICY_DEPLOYMENT = STRATOS_API_PATH + "policy" + File.separator + "deployment";
    public static final String URL_CARTRIDGE = MIGRATION_API_PATH + "cartridge" + File.separator + "list";
    public static final String URL_MULTI_TENANT_CARTRIDGE = MIGRATION_API_PATH + "cartridge"+ File.separator+"tenanted" + File.separator+ "list";
    public static final String URL_MULTI_TENANT_SERVICE = STRATOS_API_PATH + "service";
    public static final String DIRECTORY_OUTPUT_SCRIPT_DEPLOY = File.separator + "scripts" + File.separator + "common";
    public static final String DIRECTORY_ARTIFACTS = "artifacts";
    public static final String FILE_SOURCE_SCRIPT_DEPLOY = File.separator + "deploy.sh";
    public static final String FILE_SOURCE_SCRIPT_UNDEPLOY = File.separator + "undeploy.sh";
    //Default values for the application policy
    public static final String APPLICATION_POLICY_ID = "application-policy-";
    public static final String APPLICATION_POLICY_ALGO = "one-after-another";
    //Default value for network partition
    public static final String NETWORK_PARTITION_ID = "partition-1";
    //Default value for application names
    public static final String APPLICATION_NAME = "-application";
    //Default file names for application policy jsons and domain mapping jsons
    public static final String FILENAME_APPLICATION_SIGNUP = "application-signup.json";
    public static final String FILENAME_DOMAIN_MAPPING = "domain-mapping.json";
    //Default values for cartridges
    public static final int CARTRIDGE_MIN_VALUE = 1;
    public static final int CARTRIDGE_MAX_VALUE = 1;
    //Configuration strings
    //Login credentials configuration strings
    public static final String CONFIGURATION_FILE_NAME = "config.configuration";
    public static final String BASE_URL400 = "baseUrl400";
    public static final String USERNAME400 = "username400";
    public static final String PASSWORD400 = "password400";
    public static final String BASE_URL410 = "baseUrl410";
    public static final String USERNAME410 = "username410";
    public static final String PASSWORD410 = "password410";
    public static final String HOSTNAME = "hostname";
    //Port mapping configuration strings
    public static final String PORT = "default.port";
    public static final String PROXY_PORT = "default.proxy.port";
    public static final String PROTOCOL = "default.protocol";
    //IaaS provider configuration
    public static final String IAAS = "iaas";
    public static final String IAAS_IMAGE_ID = "iaasImageId";
    public static final String NETWORK_UUID = "networkUuID";
    //Network partition configuration strings
    public static final String NETWORK_PARTITION_DEPLOYMENT_COMMAND_PART1 = "curl -X POST -H \"Content-Type: application/json\" -d \"@${network_partitions_path}/";
    public static final String NETWORK_PARTITION_DEPLOYMENT_COMMAND_PART2 = ".json\" -k -v -u ${var_username}:${var_password} ${var_base_url}api/networkPartitions";
    public static final String NETWORK_PARTITION_UNDEPLOYMENT_COMMAND = "curl -X DELETE -H \"Content-Type: application/json\" -k -v -u ${var_username}:${var_password} ${var_base_url}api/networkPartitions/";
    //Domain mapping curl commands
    public static final String DOMAIN_MAPPING_DEPLOYMENT_CURL_COMMAND = "echo \"Adding domain mappings...\"\n"
            + "curl -X POST -H \"Content-Type: application/json\" -d \"@${artifacts_path}/domain-mapping.json\" -k -u ${var_username}:${var_password} ${var_base_url}api/applications/application_name/domainMappings";

    public static final String CERTIFICATE_PASSWORD = "wso2carbon";
    public static final boolean ENABLE_SELF_CERTIFIED = true;
    public static final String JSON_EXTENSION = ".json";
    //Default value constants
    public static final String CARTRIDGE_CATEGORY = "default";
    // Do not use forward slash at the beginning instead use it in the root directory.
    public static final String DIRECTORY_POLICY_AUTOSCALE = "autoscaling-policies";
    public static final String DIRECTORY_NETWORK_PARTITION = "network-partitions";
    public static final String DIRECTORY_POLICY_DEPLOYMENT = "deployment-policies";
    public static final String DIRECTORY_APPLICATION = "applications";
    public static final String DIRECTORY_CARTRIDGE = "cartridges";
    public static final String DIRECTORY_POLICY_APPLICATION = "application-policies";
    public static final String DIRECTORY_OUTPUT_SCRIPT = "applications";
    public static final String DIRECTORY_SOURCE_SCRIPT_DEPLOY = "common" + File.separator + "deploy.sh";
    public static final String DIRECTORY_SOURCE_SCRIPT_UNDEPLOY = "common" + File.separator + "undeploy.sh";
    public static final String DIRECTORY_SOURCE_SCRIPT_EC2 = "ec2";
    public static final String DIRECTORY_SOURCE_SCRIPT_GCE = "gce";
    public static final String DIRECTORY_SOURCE_SCRIPT_KUBERNETES = "kubernetes";
    public static final String DIRECTORY_SOURCE_SCRIPT_MOCK = "mock";
    public static final String DIRECTORY_SOURCE_SCRIPT_OPENSTACK = "openstack";
    //Rest client configurations
    public static final String CERTIFICATE_PATH =
            StringUtils.removeEnd(System.getProperty("user.dir"), File.separator) + System
                    .getProperty("certificate.path");
    //PPaaS 4.1.0 directories
    // 4.1.0 outputs root directory
    public static final String ROOT_DIRECTORY = System.getProperty("user.dir") + System.getProperty("output.path");
    public static final String DIRECTORY_SOURCE_SCRIPT =
            System.getProperty("user.dir") + System.getProperty("sourcescripts.path");


    public static final String URL_SUBSCRIPTION =
            MIGRATION_API_PATH + "cartridge" + File.separator + "list" + File.separator + "subscribed" + File.separator
                    + "all";

}
