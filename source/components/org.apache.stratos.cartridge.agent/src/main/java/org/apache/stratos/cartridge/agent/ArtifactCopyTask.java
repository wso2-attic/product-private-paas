/**
 *
 */
package org.apache.stratos.cartridge.agent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.stratos.cartridge.agent.config.CartridgeAgentConfiguration;
import org.apache.stratos.cartridge.agent.extensions.ExtensionHandler;

import java.io.File;

/**
 *
 */
public class ArtifactCopyTask implements Runnable {

    private static final Log log = LogFactory.getLog(ArtifactCopyTask.class);
    private final ExtensionHandler extensionHandler;

    public ArtifactCopyTask() {
        extensionHandler = CartridgeAgent.getExtensionHandler();
    }

    @Override
    public void run() {
        if (log.isDebugEnabled()) {
            log.debug("Executing repository file listener");
        }

        // Periodically copies files from
        //		source :  APP_PATH/repo/deployment/server
        //		destination : /tmp/-1234
        // TODO improve the logic to detect the file / folder changes in super tenant location
        String src = CartridgeAgentConfiguration.getInstance().getAppPath() + "/repository/deployment/server/";
        String des = "/tmp/-1234";

        if (new File(des).exists()) {
            extensionHandler.onCopyArtifactsExtension(src, des);
        }
    }

}
