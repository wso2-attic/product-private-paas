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

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.wso2.ppaas.tools.artifactmigration.exception.TransformationException;

/**
 * Main entry point of the tool
 */
public class Main {
    private static final Logger log = Logger.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            ConversionTool.readInitialConfiguration();
            ConversionTool.handleConsoleInputs();
            ConversionTool.startTransformation();
        } catch (ConfigurationException e) {
            log.error("Error in reading initial configuration  ", e);
        } catch (TransformationException e) {
            log.error("Error in artifact transformation  ", e);
        }
    }
}