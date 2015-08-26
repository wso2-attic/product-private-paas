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

import org.wso2.ppaas.integration.tests.RestConstants;
import org.wso2.ppaas.integration.tests.PPaaSTestServerManager;
import org.testng.annotations.Test;

import static junit.framework.Assert.assertTrue;

/**
 * Handling users
 */
public class TenantTest extends PPaaSTestServerManager {
    private static final String RESOURCES_PATH = "/user-test";


    @Test
    public void addUser() {
        String tenantId = "tenant-1";
        boolean addedUser1 = restClient.addEntity(RESOURCES_PATH + "/" +
                        tenantId + ".json",
                RestConstants.USERS, RestConstants.USERS_NAME);
        assertTrue(addedUser1);

    }
}
