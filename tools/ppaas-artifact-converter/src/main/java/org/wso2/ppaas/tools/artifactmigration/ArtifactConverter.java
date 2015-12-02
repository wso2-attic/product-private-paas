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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ppaas.tools.artifactmigration.loader.Constants;

import java.io.Console;

/**
 * Main entry point of the tool
 */
public class ArtifactConverter {

    private static final Logger log = LoggerFactory.getLogger(ArtifactConverter.class);

    public static void main(String[] args) {

        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");

        Console console = System.console();

        System.out.println("Enter the Base URL : ");
        Constants.BASE_URL = console.readLine();

        System.out.println("Enter the User name : ");
        Constants.USER_NAME = console.readLine();

        System.out.println("Enter the Password : ");
        char[] passwordChars = console.readPassword();
        Constants.PASSWORD = new String(passwordChars);

        boolean isSuccess = true;

        try {
            Transformation.getInstance().transformNetworkPartitionList();
            Transformation.getInstance().transformAutoscalePolicyList();
            Transformation.getInstance().transformDeploymentPolicyList();
            Transformation.getInstance().transformCartridgeList();

        } catch (Exception ex) {
            isSuccess = false;
            log.error("Error while converting the artifacts ", ex);
            System.out.println("Error while transforming NetworkPartition list.see log for more details.");
        }

        if (isSuccess)
            System.out.println("Convertion completed succesfully");

    }

}