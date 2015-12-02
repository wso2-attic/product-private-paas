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

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Loads the default artifacts from the template
 */
public class TemplateLoader {

    private static final Logger log = LoggerFactory.getLogger(OldArtifactLoader.class);
    private static TemplateLoader instance = null;
    BufferedReader br;

    private TemplateLoader() {
    }

    public static TemplateLoader getInstance() {
        if (instance == null) {
            synchronized (TemplateLoader.class) {
                if (instance == null) {
                    instance = new TemplateLoader();
                }
            }
        }
        return instance;
    }

    /**
     * Generic Method to fetch template
     *
     * @param filePath    the path of the file to fetch
     * @param typeOfClass the type of the class
     * @param <T>         Generic class
     * @return the generic instance
     * @throws IOException
     */
    public <T> T fetchTemplate(String filePath, Class<T> typeOfClass) throws IOException {

        T newInstance = null;

        try {
            br = new BufferedReader(new FileReader(filePath));
            newInstance = new Gson().fromJson(br, typeOfClass);
        } catch (IOException e) {
            String msg = "JSON syntax exception in fetching cartridges";
            log.error(msg, e);
            throw new IOException(msg, e);
        } finally {
            br.close();
        }
        return newInstance;
    }
}
