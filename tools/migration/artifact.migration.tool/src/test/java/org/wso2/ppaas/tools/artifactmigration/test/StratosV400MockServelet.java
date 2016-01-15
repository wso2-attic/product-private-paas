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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class StratosV400MockServelet extends HttpServlet {
    private static final Log log = LogFactory.getLog(StratosV400MockServelet.class);

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {

        if (TestConstants.PARTITION_PATH.equals(req.getPathInfo())) {
            File file = new File(TestConstants.PARTITION_TEST_INPUT);
            try {
                FileInputStream fis = new FileInputStream(file);
                String str = IOUtils.toString(fis, "UTF-8");
                resp.getWriter().print(str);
                resp.getWriter().flush();
            } catch (FileNotFoundException e) {
                log.error("Error in getting the partition test file", e);
            } catch (IOException e) {
                log.error("Error in sending the partition list as the response", e);
            }
        }
        if (TestConstants.AUTOSCALE_POLICY_PATH.equals(req.getPathInfo())) {
            File file = new File(TestConstants.AUTOSCALE_TEST_INPUT);
            try {
                FileInputStream fis = new FileInputStream(file);
                String str = IOUtils.toString(fis, "UTF-8");
                resp.getWriter().print(str);
                resp.getWriter().flush();
            } catch (FileNotFoundException e) {
                log.error("Error in getting the autoscale policy test file", e);
            } catch (IOException e) {
                log.error("Error in sending the autoscale policy list as the response", e);
            }
        }
        if (TestConstants.DEPLOYMENT_POLICY_PATH.equals(req.getPathInfo())) {
            File file = new File(TestConstants.DEPLOYMENT_TEST_INPUT);
            try {
                FileInputStream fis = new FileInputStream(file);
                String str = IOUtils.toString(fis, "UTF-8");
                resp.getWriter().print(str);
                resp.getWriter().flush();
            } catch (FileNotFoundException e) {
                log.error("Error in getting the deployment policy test file", e);
            } catch (IOException e) {
                log.error("Error in sending the deployment policy list as the response", e);
            }
        }
        if (TestConstants.CARTRIDGE_PATH.equals(req.getPathInfo())) {
            File file = new File(TestConstants.CARTRIDGE_TEST_INPUT);
            try {
                FileInputStream fis = new FileInputStream(file);
                String str = IOUtils.toString(fis, "UTF-8");
                resp.getWriter().print(str);
                resp.getWriter().flush();
            } catch (FileNotFoundException e) {
                log.error("Error in getting the cartridge test file", e);
            } catch (IOException e) {
                log.error("Error in sending the cartridge list as the response", e);
            }
        }
        if (TestConstants.DOMAIN_PATH.equals(req.getPathInfo())) {
            File file = new File(TestConstants.DOMAIN_MAPPING_TEST_INPUT);
            try {
                FileInputStream fis = new FileInputStream(file);
                String str = IOUtils.toString(fis, "UTF-8");
                resp.getWriter().print(str);
                resp.getWriter().flush();
            } catch (FileNotFoundException e) {
                log.error("Error in getting the domain mapping test file", e);
            } catch (IOException e) {
                log.error("Error in sending the domain mapping list as the response", e);
            }
        }
    }
}
