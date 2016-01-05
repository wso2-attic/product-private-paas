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
import java.io.FileNotFoundException;
import java.io.IOException;

public class StratosV400MockServelet2 extends HttpServlet {

    private static final Log log = LogFactory.getLog(StratosV400MockServelet2.class);

    private static String getResourcesFolderPath() {
        String path = HttpClientTest.class.getResource("/").getPath();
        return StringUtils.removeEnd(path, File.separator);
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if (TestConstants.SUBSCRIPTION_PATH.equals(req.getPathInfo())) {
            File file = new File(getResourcesFolderPath() + File.separator + TestConstants.TEST_ARTIFACTS_PATH2 + File.separator
                    + "test_subscription.json");
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
    }
}
