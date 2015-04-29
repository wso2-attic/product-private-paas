/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.stratos.cartridge.agent.artifact.deployment.synchronizer.git;

import java.util.Map;

/**
 * GitOperationResult represents the result of git client operations performed by cartridge agent
 */
public class GitOperationResult {

    private boolean success;
    private Map<String, Long> modifiedArtifacts;

    /**
     * Represent the status of the git operation
     *
     * @return true if the git operation is success, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Represents the modified artifacts related to this git operation
     *
     * @return Modified artifacts and their associated last modified time
     */
    public Map<String, Long> getModifiedArtifacts() {
        return modifiedArtifacts;
    }

    public void setModifiedArtifacts(Map<String, Long> modifiedArtifacts) {
        this.modifiedArtifacts = modifiedArtifacts;
    }

}
