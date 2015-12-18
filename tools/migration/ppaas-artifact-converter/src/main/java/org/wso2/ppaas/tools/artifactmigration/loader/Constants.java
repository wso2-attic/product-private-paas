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
package org.wso2.ppaas.tools.artifactmigration.loader;

import org.apache.commons.lang.StringUtils;

import java.io.File;

/**
 * Constants Details
 */
public class Constants {

    public static final String STRATOS = "stratos" + File.separator + "admin" + File.separator;
    private static final String MIGRATION = "migration" + File.separator + "admin" + File.separator;

    // 4.1.0 constants outputs
    public static final String ROOT_DIRECTORY =
            System.getProperty("user.dir") + File.separator + ".." + File.separator + "output-artifacts"
                    + File.separator;

    // Do not use forward slash at the beginning instead use it in the base url.
    public static final String URL_PARTITION = STRATOS + "partition";
    public static final String URL_POLICY_AUTOSCALE = STRATOS + "policy" + File.separator + "autoscale";
    public static final String URL_CARTRIDGE = STRATOS + "cartridge" + File.separator + "list";
    public static final String URL_POLICY_DEPLOYMENT = STRATOS + "policy" + File.separator + "deployment";
    public static final String URL_SUBSCRIPTION =
            MIGRATION + "cartridge" + File.separator + "list" + File.separator + "subscribed" + File.separator + "all";

    // Do not use forward slash at the beginning instead use it in the root directory.
    public static final String DIRECTORY_POLICY_AUTOSCALE = "autoscaling-policies";
    public static final String DIRECTORY_NETWORK_PARTITION = "network-partitions";
    public static final String DIRECTORY_POLICY_DEPLOYMENT = "deployment-policies";
    public static final String DIRECTORY_APPLICATION = "applications";
    public static final String DIRECTORY_CARTRIDGE = "cartridges";
    public static final String DIRECTORY_POLICY_APPLICATION = "application-policies";

    private static final String DIRECTORY_SOURCE_SCRIPT =
            System.getProperty("user.dir") + File.separator + ".." + File.separator + "resources" + File.separator
                    + "scripts";
    public static final String DIRECTORY_SOURCE_SCRIPT_DEPLOY = Constants.DIRECTORY_SOURCE_SCRIPT + File.separator+ "common"+ File.separator+ "deploy.sh";
    public static final String DIRECTORY_SOURCE_SCRIPT_EC2 = Constants.DIRECTORY_SOURCE_SCRIPT + File.separator + "ec2";
    public static final String DIRECTORY_SOURCE_SCRIPT_GCE = Constants.DIRECTORY_SOURCE_SCRIPT + File.separator + "gce";
    public static final String DIRECTORY_SOURCE_SCRIPT_KUBERNETES = Constants.DIRECTORY_SOURCE_SCRIPT + File.separator + "kubernetes";
    public static final String DIRECTORY_SOURCE_SCRIPT_MOCK = Constants.DIRECTORY_SOURCE_SCRIPT + File.separator + "mock";
    public static final String DIRECTORY_SOURCE_SCRIPT_OPENSTACK = Constants.DIRECTORY_SOURCE_SCRIPT + File.separator + "openstack";

    public static final String DIRECTORY_OUTPUT_SCRIPT = "applications";
    public static final String DIRECTORY_OUTPUT_SCRIPT_DEPLOY = File.separator + "scripts" + File.separator + "common";
    public static final String FILE_SOURCE_SCRIPT_DEPLOY= File.separator + "deploy.sh";

    //Default values for the application policy
    public static final String APPLICATION_POLICY_ID = "autoscaling-policy-1";
    public static final String APPLICATION_POLICY_ALGO = "one-after-another";

    //Default file names for application policy jsons
    public static final String FILENAME_APPLICATION_SIGNUP = "application-signup.json";
    public static final String FILENAME_DOMAIN_MAPPING = "domain-mapping.json";

    //Configuration details
    public static final String BASE_URL400 = "baseUrl400";
    public static final String USERNAME400 = "userName400";
    public static final String PASSWORD400 = "password400";
    public static final String USERNAME410 = "userName410";
    public static final String PASSWORD410 = "password410";

    public static final String JSON_EXTENSION  = ".json";

    //Rest client class configuration variables
    public static final String CERTIFICATE_PATH = StringUtils.removeEnd(System.getProperty("user.dir") +File.separator +".."+File.separator + "resources", File.separator)+ "/wso2carbon.jks";
    public static final char[] CERTIFICATE_PASSWORD = "wso2carbon".toCharArray();
    public static final boolean ENABLE_SELF_CERTIFIED= true;
}