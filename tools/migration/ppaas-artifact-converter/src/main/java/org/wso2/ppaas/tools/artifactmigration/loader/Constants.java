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

import java.io.File;

/**
 * Constants Details
 */
public class Constants {

    // 4.0.0 constants
    public static String BASE_URL = "";
    public static String USER_NAME = "";
    public static String PASSWORD = "";

    // 4.1.0 constants templates
    public static String ROOT_TEMPLATE_DIRECTORY =
            System.getProperty("user.dir") + File.separator + ".." + File.separator + "template/";

    // 4.1.0 constants outputs
    public static String ROOT_DIRECTORY =
            System.getProperty("user.dir") + File.separator + ".." + File.separator + "output-artifacts/";

    // Do not use forward slash at the beginning instead use it in the base url.
    public static final String URL_PARTITION = "partition";
    public static final String URL_POLICY_AUTOSCALE = "policy" + File.separator + "autoscale";
    public static final String URL_CARTRIDGE = "cartridge" + File.separator + "list";
    public static final String URL_POLICY_DEPLOYMENT = "policy" + File.separator + "deployment";

    // Do not use forward slash at the beginning instead use it in the root directory.
    public static final String DIRECTORY_TEMPLATE_POLICY_AUTOSCALE =
            "autoscaling-policies" + File.separator + "autoscaling-policy-1.json";
    public static final String DIRECTORY_TEMPLATE_NETWORK_PARTITION =
            "network-partitions" + File.separator + "gce" + File.separator + "network-partition-1.json";
    public static final String DIRECTORY_TEMPLATE_POLICY_DEPLOYMENT =
            "deployment-policies" + File.separator + "deployment-policy-1.json";
    public static final String DIRECTORY_TEMPLATE_CARTRIDGE =
            "cartridges" + File.separator + "ec2" + File.separator + "c1.json";
    public static final String DIRECTORY_TEMPLATE_APPLICATION =
            "applications" + File.separator + "simple" + File.separator + "single-cartridge-app" + File.separator
                    + "artifacts" + File.separator + "application.json";

    // Do not use forward slash at the beginning instead use it in the root directory.
    public static final String DIRECTORY_POLICY_AUTOSCALE = "autoscaling-policies";
    public static final String DIRECTORY_NETWORK_PARTITION = "network-partitions";
    public static final String DIRECTORY_POLICY_DEPLOYMENT = "deployment-policies";
    public static final String DIRECTORY_APPLICATION = "applications";
    public static final String DIRECTORY_CARTRIDGE = "cartridges";

}