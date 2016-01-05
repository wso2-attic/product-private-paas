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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class StratosV400MockServelet extends HttpServlet {

    public static final String PARTITION_PATH = "/partition";
    public static final String AUTOSCALE_POLICY_PATH= "/policy/autoscale";
    public static final String DEPLOYMENT_POLICY_PATH= "/policy/deployment";
    public static final String CARTRIDGE_PATH= "/cartridge/list";
    public static final String TEST_ARTIFACTS_PATH = "test_artifacts";
    private static final Log log = LogFactory.getLog(StratosV400MockServelet.class);

    private static String getResourcesFolderPath() {
        String path = HttpClientTest.class.getResource("/").getPath();
        return StringUtils.removeEnd(path, File.separator);
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.info("Request context path: " + req.getContextPath());
        log.info("Request URI: " + req.getRequestURI());
        log.info("Request query string: " + req.getQueryString());
        log.info("Request method: " + req.getMethod());
        log.info("Request servlet path: " + req.getServletPath());
        log.info("Request path info: " + req.getPathInfo());

        if (PARTITION_PATH.equals(req.getPathInfo())) {
            File file = new File(getResourcesFolderPath() + File.separator + TEST_ARTIFACTS_PATH + File.separator
                    + "test_partition_P1.json");
            FileInputStream fis = new FileInputStream(file);
            String str = IOUtils.toString(fis, "UTF-8");
            resp.getWriter().print(str);
            resp.getWriter().flush();
        }
        if (AUTOSCALE_POLICY_PATH.equals(req.getPathInfo())) {
            File file = new File(getResourcesFolderPath() + File.separator + TEST_ARTIFACTS_PATH + File.separator
                    + "test_AutoscalePolicy.json");
            FileInputStream fis = new FileInputStream(file);
            String str = IOUtils.toString(fis, "UTF-8");
            resp.getWriter().print(str);
            resp.getWriter().flush();
        }
        if (DEPLOYMENT_POLICY_PATH.equals(req.getPathInfo())) {
            File file = new File(getResourcesFolderPath() + File.separator + TEST_ARTIFACTS_PATH + File.separator
                    + "test_DeploymentPolicy.json");
            FileInputStream fis = new FileInputStream(file);
            String str = IOUtils.toString(fis, "UTF-8");
            resp.getWriter().print(str);
            resp.getWriter().flush();
        }
        if (CARTRIDGE_PATH.equals(req.getPathInfo())) {
            File file = new File(getResourcesFolderPath() + File.separator + TEST_ARTIFACTS_PATH + File.separator
                    + "test_PHP_cartridges.json");
            FileInputStream fis = new FileInputStream(file);
            String str = IOUtils.toString(fis, "UTF-8");
            resp.getWriter().print(str);
            resp.getWriter().flush();
        }
    }
}
