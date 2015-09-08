/*
 * Copyright 2005-2015 WSO2, Inc. (http://wso2.com)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.ppaas.configurator.tests;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class ConfiguratorTestCase extends ConfiguratorTestManager {
    private static final Log log = LogFactory.getLog(ConfiguratorTestCase.class);
    private static final int STARTUP_TIMEOUT = 120000;
    private static final String SUITE_NAME = "suite-1";

    @BeforeSuite
    public void setupAgentStartupTest() {
        // start Configurator with configurations provided in resource path
        Map<String,String> environment = new HashMap<String, String>();
        environment.put("CONFIG_PARAM_CLUSTERING","true");
        environment.put("CONFIG_PARAM_LOCAL_MEMBER_HOST","127.0.1.1");
        environment.put("CONFIG_PARAM_PORT_OFFSET","2");

        setup(SUITE_NAME,environment);
    }

    /**
     * TearDown method for test method Configurator
     */
    @AfterSuite
    public void tearDownAgentStartupTest() {
        log.info("Test suite "+SUITE_NAME+"Completed");
    }

    @Test(timeOut = STARTUP_TIMEOUT)
    public void testAxis2XmlEnv() {
        String axis2FilePath = "repository" + PATH_SEP + "conf" + PATH_SEP + "axis2" +
                PATH_SEP + "axis2.xml";
        String xpathExpression;
        String configuredValue;
        //Check clustering property
        xpathExpression = "/axisconfig/clustering[@class='org.wso2.carbon.core.clustering.hazelcast.HazelcastClusteringAgent']/@enable";
        configuredValue = readXML(axis2FilePath, xpathExpression);
        Assert.assertEquals(configuredValue,"true");

        //Check CONFIG_PARAM_LOCAL_MEMBER_HOST
        xpathExpression="/axisconfig/clustering/parameter[@name='localMemberHost']/text()";
        configuredValue=readXML(axis2FilePath,xpathExpression);
        Assert.assertEquals(configuredValue,"127.0.1.1");
    }

    @Test(timeOut = STARTUP_TIMEOUT)
    public void testCarbonXmlEnv() {
        String axis2FilePath = "repository" + PATH_SEP + "conf" + PATH_SEP + "carbon.xml";
        String xpathExpression;
        String configuredValue;
        //Check offset property
        xpathExpression = "/Server/Ports/Offset/text()";
        configuredValue = readXML(axis2FilePath, xpathExpression);
        Assert.assertEquals(configuredValue,"2");

    }

    @Test(timeOut = STARTUP_TIMEOUT)
    public void testAxis2XmlModuleIni() {
        String axis2FilePath = "repository" + PATH_SEP + "conf" + PATH_SEP + "axis2" +
                PATH_SEP + "axis2.xml";
        String xpathExpression;
        String configuredValue;
        //Check local member port property
        xpathExpression="/axisconfig/clustering/parameter[@name='localMemberPort']/text()";
        configuredValue=readXML(axis2FilePath,xpathExpression);
        Assert.assertEquals(configuredValue,"4000");

    }


}
