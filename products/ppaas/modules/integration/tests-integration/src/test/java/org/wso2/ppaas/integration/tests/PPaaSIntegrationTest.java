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

package org.wso2.ppaas.integration.tests;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.ppaas.integration.common.rest.IntegrationMockClient;
import org.wso2.ppaas.integration.common.rest.RestClient;

public class PPaaSIntegrationTest {
    private static final Log log = LogFactory.getLog(PPaaSIntegrationTest.class);
    protected AutomationContext ppaasAutomationCtx;
    protected String adminUsername;
    protected String adminPassword;
    protected RestClient restClient;
    protected IntegrationMockClient mockIaasApiClient;

    public PPaaSIntegrationTest() throws Exception {
        ppaasAutomationCtx = new AutomationContext("PPAAS", "ppaas-001", TestUserMode.SUPER_TENANT_ADMIN);
        adminUsername = ppaasAutomationCtx.getConfigurationValue
                ("/automation/userManagement/superTenant/tenant/admin/user/userName");
        adminPassword = ppaasAutomationCtx.getConfigurationValue
                ("/automation/userManagement/superTenant/tenant/admin/user/password");
        restClient =
                new RestClient(ppaasAutomationCtx.getContextUrls().getWebAppURL(), adminUsername, adminPassword);
        String mockIaaSEndpoint = ppaasAutomationCtx.getContextUrls().getWebAppURL() + "/mock-iaas/api";
        mockIaasApiClient = new IntegrationMockClient(mockIaaSEndpoint);
        log.info("Mock IaaS endpoint URL: " + mockIaaSEndpoint);
    }
}