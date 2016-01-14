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
    public static final int BUFFER_SIZE = 32768;
    public static final int IDLE_TIMEOUT = 300000;
    public static final String KEYSTORE_PATH = getResourcesFolderPath() + File.separator + "wso2carbon.jks";

    //Configuration StratosV400MockServelet Test
    private static final String TEST_ARTIFACTS_PATH = "test_artifacts";

    //Configuration of path for the folder of the output files created
    public static final String OUTPUT_DIRECTORY =
            System.getProperty("user.dir") + File.separator + "target" + File.separator + "test-classes"
                    + File.separator + "output-artifacts" + File.separator;

    //Configuration of path for the folder of the output testing files
    public static final String TEST_OUTPUTS =
            getResourcesFolderPath() + File.separator + "test-outputs" + File.separator;

    public static final String OUTPUT_PARTITION = "network-partitions" + File.separator + "openstack" + File.separator
            + "network-partition-1.json";
    public static final String OUTPUT_AUTOSCALE = "autoscaling-policies" + File.separator + "simpleAutoscalePolicy.json";
    public static final String OUTPUT_DEPLOYMENT = "deployment-policies" + File.separator + "economyDeploymentPolicy.json";
    public static final String OUTPUT_CARTRIDGE = "cartridges" + File.separator + "PHP.json";

    //Configuration input test file paths
    public static final String TEST_INPUTS =
            getResourcesFolderPath() + File.separator + TestConstants.TEST_ARTIFACTS_PATH + File.separator;
    public static String PARTITION_TEST_INPUT= TEST_INPUTS+ File.separator+"test_partitions.json";
    public static String AUTOSCALE_TEST_INPUT=TEST_INPUTS+ File.separator+"test_autoscalepolicies.json";
    public static String DEPLOYMENT_TEST_INPUT=TEST_INPUTS+ File.separator+"test_deploymentpolicies.json";
    public static String CARTRIDGE_TEST_INPUT=TEST_INPUTS+ File.separator+"test_cartridges.json";
    public static String SUBSCRIPTION_TEST_INPUT=TEST_INPUTS+ File.separator+"test_subscription.json";
    public static String DOMAIN_MAPPING_TEST_INPUT=TEST_INPUTS+ File.separator+"test_domainmappings.json";

    public static final String ERROR_MSG="{\"Error\":{ \"errorCode\": \" -1234\", \"errorMessage\": \" Unexpected error. Returned JSON String is a null. Please check the test cases \"}}";
    private static String getResourcesFolderPath() {
        String path = HttpClientTest.class.getResource("/").getPath();
        return StringUtils.removeEnd(path, File.separator);

    }
}
