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


import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.wso2.ppaas.tools.artifactmigration.Transformer;
import java.io.File;
import static org.junit.Assert.assertTrue;

public class HttpClientTest {
    private static final Logger log = Logger.getLogger(HttpClientTest.class);
    @BeforeClass public static void startClient(){
        try {
            HttpClientSetUp Client= new HttpClientSetUp();
            Client.startServer();
        } catch (Exception e) {
            log.error("Error while starting the server", e);
        }
    }
    @Test(timeout = 60000) public void transformNetworkPartitionListTest() throws Exception {
        Transformer.transformNetworkPartitionList();
        File partitionfile1 = new File(TestConstants.CREATED_PARTITION_TEST);
        File partitionfile2 = new File(TestConstants.PARTITION_TEST_WITH);
        assertTrue(FileUtils.contentEquals(partitionfile1, partitionfile2));
    }

    @Test(timeout = 60000) public void transformAutoscalePolicyListTest() throws Exception {
        Transformer.transformAutoscalePolicyList();
        File autoscalefile1 = new File(TestConstants.CREATED_AUTOSCALE_TEST);
        File autoscalefile2 = new File(TestConstants.AUTOSCALE_TEST_WITH);
        assertTrue(FileUtils.contentEquals(autoscalefile1, autoscalefile2));
    }

    @Test(timeout = 60000) public void transformDeploymentPolicyList() throws Exception {
        Transformer.transformDeploymentPolicyList();
        File deploymentfile1 = new File(TestConstants.CREATED_DEPLOYMENT_TEST);
        File deploymentfile2 = new File(TestConstants.DEPLOYMENT_TEST_WITH);
        assertTrue(FileUtils.contentEquals(deploymentfile1, deploymentfile2));
    }

    @Test(timeout = 60000) public void transformCartridgeList() throws Exception {
        Transformer.transformCartridgeList();
        File cartridgefile1 = new File(TestConstants.CREATED_CARTRIDGE_TEST);
        File cartridgefile2 = new File(TestConstants.CARTRIDGE_TEST_WITH);
        assertTrue(FileUtils.contentEquals(cartridgefile1, cartridgefile2));

    }
}
