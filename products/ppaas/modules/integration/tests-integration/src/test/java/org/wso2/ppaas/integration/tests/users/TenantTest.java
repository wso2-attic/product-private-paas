/*
 * Copyright 2005-2015 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.ppaas.integration.tests.users;

import org.testng.annotations.Test;
import org.wso2.ppaas.integration.common.RestConstants;
import org.wso2.ppaas.integration.tests.PPaaSIntegrationTest;

import static junit.framework.Assert.assertTrue;

/**
 * Handling users
 */
public class TenantTest extends PPaaSIntegrationTest {
    private static final String RESOURCES_PATH = "/user-test";
    public static final int APPLICATION_TEST_TIMEOUT = 5 * 60 * 1000; // 5 mins

    public TenantTest() throws Exception {
    }

    @Test(description = "Add tenant user", timeOut = APPLICATION_TEST_TIMEOUT, groups = {"disabled"})
    public void addUser() throws Exception {
        String tenantId = "tenant-1";
        boolean addedUser1 = restClient
                .addEntity(RESOURCES_PATH + "/" + tenantId + ".json", RestConstants.USERS, RestConstants.USERS_NAME);
        assertTrue(addedUser1);
    }
}