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

package org.apache.stratos.cartridge.agent.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.stratos.common.util.CommandUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.Map;

/**
 * Cartridge agent extension utility methods.
 */
public class ExtensionUtils {
    private static final Log log = LogFactory.getLog(ExtensionUtils.class);

    private static String getExtensionsDir() {
        String extensionsDir = System.getProperty(CartridgeAgentConstants.EXTENSIONS_DIR);
        if (StringUtils.isBlank(extensionsDir)) {
            throw new RuntimeException(String.format("System property not found: %s", CartridgeAgentConstants.EXTENSIONS_DIR));
        }
        return extensionsDir;
    }

    private static String prepareCommand(String scriptFile) throws FileNotFoundException {
        String extensionsDir = getExtensionsDir();
        String filePath = (extensionsDir.endsWith(File.separator)) ?
                extensionsDir + scriptFile :
                extensionsDir + File.separator + scriptFile;

        File file = new File(filePath);
        if (file.exists() && !file.isDirectory()) {
            return filePath;
        }

        throw new FileNotFoundException("Script file not found:" + filePath);
    }

    private static Map<String, String> cleanProcessParameters(Map<String, String> envParameters){
        Iterator<Map.Entry<String,String>> iter = envParameters.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String,String> entry = iter.next();
            if(entry.getValue() == null){
                iter.remove();
            }
        }
        return envParameters;
    }

    public static void executeStartServersExtension(Map<String, String> envParameters) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Executing start servers extension");
            }
            String script = System.getProperty(CartridgeAgentConstants.START_SERVERS_SCRIPT);
            String command = prepareCommand(script);
            cleanProcessParameters(envParameters);
            String output = CommandUtils.executeCommand(command, envParameters);
            if (log.isDebugEnabled()) {
                log.debug("Start server script returned:" + output);
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Could not execute start servers extension", e);
            }
        }
    }

    public static void executeCleanupExtension() {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Executing cleanup extension");
            }
            String script = System.getProperty(CartridgeAgentConstants.CLEAN_UP_SCRIPT);
            String command = prepareCommand(script);
            String output = CommandUtils.executeCommand(command);
            if (log.isDebugEnabled()) {
                log.debug("Cleanup script returned:" + output);
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Could not execute cleanup extension", e);
            }
        }
    }

    public static void executeInstanceStartedExtension(Map<String, String> envParameters) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Executing instance started extension");
            }
            String script = System.getProperty(CartridgeAgentConstants.INSTANCE_STARTED_SCRIPT);
            String command = prepareCommand(script);
            cleanProcessParameters(envParameters);
            String output = CommandUtils.executeCommand(command, envParameters);
            if (log.isDebugEnabled()) {
                log.debug("Instance started script returned:" + output);
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Could not execute instance started extension", e);
            }
        }
    }

    public static void executeInstanceActivatedExtension() {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Executing instance activated extension");
            }
            String script = System.getProperty(CartridgeAgentConstants.INSTANCE_ACTIVATED_SCRIPT);
            String command = prepareCommand(script);
            String output = CommandUtils.executeCommand(command);
            if (log.isDebugEnabled()) {
                log.debug("Instance activated script returned:" + output);
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Could not execute instance activated extension", e);
            }
        }
    }

    public static void executeArtifactsUpdatedExtension(Map<String, String> envParameters) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Executing artifacts updated extension");
            }
            String script = System.getProperty(CartridgeAgentConstants.ARTIFACTS_UPDATED_SCRIPT);
            String command = prepareCommand(script);
            cleanProcessParameters(envParameters);
            String output = CommandUtils.executeCommand(command, envParameters);
            if (log.isDebugEnabled()) {
                log.debug("Artifacts updated script returned:" + output);
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Could not execute artifacts updated extension", e);
            }
        }
    }

    public static void executeCopyArtifactsExtension(String source, String destination) {
        try {
            if(log.isDebugEnabled()) {
                log.debug("Executing artifacts copy extension");
            }
            String command = prepareCommand(CartridgeAgentConstants.ARTIFACTS_COPY_SCRIPT);
            CommandUtils.executeCommand(command +" " + source + " " + destination );
        }
        catch (Exception e) {
            log.error("Could not execute artifacts copy extension", e);
        }
    }

    /*
    This will execute the volume mounting script which format and mount the
    persistance volumes.
     */
    public static void executeVolumeMountExtension(String persistenceMappingsPayload) {
        try {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Executing volume mounting extension: [payload] %s", persistenceMappingsPayload));
            }
            String script = System.getProperty(CartridgeAgentConstants.MOUNT_VOLUMES_SCRIPT);
            String command = prepareCommand(script);
            //String payloadPath = System.getProperty(CartridgeAgentConstants.PARAM_FILE_PATH);
            // add payload file path as argument so inside the script we can source
            // it  to get the env variables set by the startup script
            String output = CommandUtils.executeCommand(command + " " + persistenceMappingsPayload);
            if (log.isDebugEnabled()) {
                log.debug("Volume mount script returned:" + output);
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Could not execute volume mounting extension", e);
            }
        }
    }

    public static void executeMemberActivatedExtension(Map<String, String> envParameters) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Executing member activated extension");
            }
            String script = System.getProperty(CartridgeAgentConstants.MEMBER_ACTIVATED_SCRIPT);
            String command = prepareCommand(script);
            cleanProcessParameters(envParameters);
            String output = CommandUtils.executeCommand(command, envParameters);
            if (log.isDebugEnabled()) {
                log.debug("Member activated script returned:" + output);
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Could not execute member activated extension", e);
            }
        }
    }

    public static void executeMemberTerminatedExtension(Map<String, String> envParameters) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Executing member terminated extension");
            }
            String script = System.getProperty(CartridgeAgentConstants.MEMBER_TERMINATED_SCRIPT);
            String command = prepareCommand(script);
            cleanProcessParameters(envParameters);
            String output = CommandUtils.executeCommand(command, envParameters);
            if (log.isDebugEnabled()) {
                log.debug("Member terminated script returned:" + output);
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Could not execute member terminated extension", e);
            }
        }
    }

    public static void executeMemberStartedExtension(Map<String, String> envParameters) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Executing member started extension");
            }
            String script = System.getProperty(CartridgeAgentConstants.MEMBER_STARTED_SCRIPT);
            String command = prepareCommand(script);
            cleanProcessParameters(envParameters);
            String output = CommandUtils.executeCommand(command, envParameters);
            if (log.isDebugEnabled()) {
                log.debug("Member started script returned:" + output);
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Could not execute member started extension", e);
            }
        }
    }

    public static void executeMemberSuspendedExtension(Map<String, String> envParameters) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Executing member suspended extension");
            }
            String script = System.getProperty(CartridgeAgentConstants.MEMBER_SUSPENDED_SCRIPT);
            String command = prepareCommand(script);
            cleanProcessParameters(envParameters);
            String output = CommandUtils.executeCommand(command, envParameters);
            if (log.isDebugEnabled()) {
                log.debug("Member suspended script returned:" + output);
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Could not execute member suspended extension", e);
            }
        }
    }

    public static void executeCompleteTopologyExtension(Map<String, String> envParameters) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Executing complete topology extension");
            }
            String script = System.getProperty(CartridgeAgentConstants.COMPLETE_TOPOLOGY_SCRIPT);
            String command = prepareCommand(script);
            cleanProcessParameters(envParameters);
            String output = CommandUtils.executeCommand(command, envParameters);
            if (log.isDebugEnabled()) {
                log.debug("Complete topology script returned:" + output);
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Could not execute complete topology extension", e);
            }
        }
    }

    public static void executeCompleteTenantExtension(Map<String, String> envParameters) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Executing complete tenant extension");
            }
            String script = System.getProperty(CartridgeAgentConstants.COMPLETE_TENANT_SCRIPT);
            String command = prepareCommand(script);
            cleanProcessParameters(envParameters);
            String output = CommandUtils.executeCommand(command, envParameters);
            if (log.isDebugEnabled()) {
                log.debug("Complete tenant script returned:" + output);
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Could not execute complete tenant extension", e);
            }
        }
    }

    public static void executeSubscriptionDomainAddedExtension(Map<String, String> envParameters) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Executing subscription domain added extension");
            }
            String script = System.getProperty(CartridgeAgentConstants.SUBSCRIPTION_DOMAIN_ADDED_SCRIPT);
            String command = prepareCommand(script);
            cleanProcessParameters(envParameters);
            String output = CommandUtils.executeCommand(command, envParameters);
            if (log.isDebugEnabled()) {
                log.debug("Subscription domain added script returned:" + output);
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Could not execute subscription domain added extension", e);
            }
        }
    }

    public static void executeSubscriptionDomainRemovedExtension(Map<String, String> envParameters) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Executing subscription domain removed extension");
            }
            String script = System.getProperty(CartridgeAgentConstants.SUBSCRIPTION_DOMAIN_REMOVED_SCRIPT);
            String command = prepareCommand(script);
            cleanProcessParameters(envParameters);
            String output = CommandUtils.executeCommand(command, envParameters);
            if (log.isDebugEnabled()) {
                log.debug("Subscription domain removed script returned:" + output);
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Could not execute subscription domain removed extension", e);
            }

        }
    }
}
