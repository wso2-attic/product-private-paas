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

package org.wso2.ppaas.integration.common;

import java.io.File;

public class Util {
    public static final String CARBON_ZIP_KEY = "carbon.zip";
    public static final String ACTIVEMQ_BIND_ADDRESS = "activemq.bind.address";
    public static final String CARBON_CONF_PATH = "repository" + File.separator + "conf";
    public static final String CARBON_HOME_KEY = "carbon.home";
    public static final String BASE_PATH = CarbonTestServerManager.class.getResource(File.separator).getPath();
}