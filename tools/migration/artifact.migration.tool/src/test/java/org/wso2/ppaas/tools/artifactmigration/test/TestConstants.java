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
package org.wso2.ppaas.tools.artifactmigration.test;

import org.apache.commons.lang.StringUtils;

import java.io.File;

/**
 * Constants for test class
 */
class TestConstants {

    //Configuration HTTP Client Test
    public static final String ENDPOINT = System.getProperty("endpoint");
    public static final int BUFFER_SIZE = 32768;
    public static final int IDLE_TIMEOUT = 300000;
    public static final String SERVLET_CONTEXT_PATH = File.separator + "stratos" + File.separator + "admin";
    public static final String SERVLET_CONTEXT_PATH2 = File.separator + "migration" + File.separator + "admin";
    public static final String KEYSTORE_PATH = getResourcesFolderPath() + File.separator + "wso2carbon.jks";

    //Configuration StratosV400MockServelet Test
    public static final String PARTITION_PATH = File.separator + "partition";
    public static final String AUTOSCALE_POLICY_PATH = File.separator + "policy" + File.separator + "autoscale";
    public static final String DEPLOYMENT_POLICY_PATH = File.separator + "policy" + File.separator + "deployment";
    public static final String CARTRIDGE_PATH = File.separator + "cartridge" + File.separator + "list";
    public static final String DOMAIN_PATH =
            File.separator + "cartridge" + File.separator + "PHP" + File.separator + "subscription" + File.separator
                    + "myphp" + File.separator + "domains";
    //Configuration StratosV400MockServelet2 Test
    public static final String SUBSCRIPTION_PATH =
            File.separator + "cartridge" + File.separator + "list" + File.separator + "subscribed" + File.separator
                    + "all";
    //Configuration of paths of the output files created
    public static final String OUTPUT_DIRECTORY =
            System.getProperty("user.dir") + File.separator + "target" + File.separator + "test-classes"
                    + File.separator + "output-artifacts" + File.separator;
    public static final String CREATED_PARTITION_TEST =
            OUTPUT_DIRECTORY + "network-partitions" + File.separator + "openstack" + File.separator
                    + "network-partition-1.json";
    public static final String CREATED_AUTOSCALE_TEST =
            OUTPUT_DIRECTORY + "autoscaling-policies" + File.separator + "simpleAutoscalePolicy.json";
    public static final String CREATED_DEPLOYMENT_TEST =
            OUTPUT_DIRECTORY + "deployment-policies" + File.separator + "economyDeploymentPolicy.json";
    public static final String CREATED_CARTRIDGE_TEST = OUTPUT_DIRECTORY + "cartridges" + File.separator + "PHP.json";
    //Certificate path for test cases
    public static final String TEST_CERTIFICATE =
            System.getProperty("user.dir") + File.separator + "target" + File.separator + "test-classes"
                    + File.separator + "wso2carbon.jks";
    //Configuration of paths for deploying scripts
    public static final String TEST_DIRECTORY_SOURCE_SCRIPT =
            getResourcesFolderPath() + File.separator + "scripts" + File.separator;
    private static final String TEST_ARTIFACTS_PATH = "test_artifacts";
    //Configuration of paths of the output testing files
    private static final String TEST_OUTPUTS =
            getResourcesFolderPath() + File.separator + "test-outputs" + File.separator;
    public static final String PARTITION_TEST_WITH = TEST_OUTPUTS + "network-partition-1.json";
    public static final String AUTOSCALE_TEST_WITH = TEST_OUTPUTS + "simpleAutoscalePolicy.json";
    public static final String DEPLOYMENT_TEST_WITH = TEST_OUTPUTS + "economyDeployment.json";
    public static final String CARTRIDGE_TEST_WITH = TEST_OUTPUTS + "PHP.json";
    //Configuration input test file paths
    private static final String TEST_INPUTS =
            getResourcesFolderPath() + File.separator + TestConstants.TEST_ARTIFACTS_PATH + File.separator;
    public static final String PARTITION_TEST_INPUT = TEST_INPUTS + "test_partition_P1.json";
    public static final String AUTOSCALE_TEST_INPUT = TEST_INPUTS + "test_AutoscalePolicy.json";
    public static final String DEPLOYMENT_TEST_INPUT = TEST_INPUTS + "test_DeploymentPolicy.json";
    public static final String CARTRIDGE_TEST_INPUT = TEST_INPUTS + "test_PHP_cartridges.json";
    public static final String SUBSCRIPTION_TEST_INPUT = TEST_INPUTS + "test_subscription.json";
    public static final String DOMAIN_MAPPING_TEST_INPUT = TEST_INPUTS + "test_domainMappings_PHP.json";

    private static String getResourcesFolderPath() {
        String path = HttpClientTest.class.getResource("/").getPath();
        return StringUtils.removeEnd(path, File.separator);

    }
}
