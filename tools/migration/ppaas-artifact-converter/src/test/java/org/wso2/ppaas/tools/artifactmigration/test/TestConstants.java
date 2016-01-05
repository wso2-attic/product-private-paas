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

import java.io.File;

/**
 * Constants for test class
 */
class TestConstants {

    //Configuration HTTP Client Test
    public static final String ENDPOINT = System.getProperty("endpoint");
    public static final int BUFFER_SIZE = 32768;
    public static final int IDLE_TIMEOUT = 300000;
    public static final String SERVLET_CONTEXT_PATH = "/stratos/admin";
    public static final String SERVLET_CONTEXT_PATH2 = "/migration/admin";

    //Configuration StratosV400MockServelet Test
    public static final String PARTITION_PATH = "/partition";
    public static final String AUTOSCALE_POLICY_PATH = "/policy/autoscale";
    public static final String DEPLOYMENT_POLICY_PATH = "/policy/deployment";
    public static final String CARTRIDGE_PATH = "/cartridge/list";
    public static final String TEST_ARTIFACTS_PATH = "test_artifacts";

    //Configuration StratosV400MockServelet2 Test
    public static final String SUBSCRIPTION_PATH = "/cartridge/list/subscribed/all";
    public static final String TEST_ARTIFACTS_PATH2 = "test_artifacts";

}
