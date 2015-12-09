package org.wso2.ppaas.tools.artifactmigration;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.wso2.ppaas.tools.artifactmigration.loader.Constants;

import java.io.Console;
import java.io.File;
import java.io.IOException;

public class ConversionTool {

    private static final Logger log = Logger.getLogger(Transformation.class);
    private static ConversionTool instance = null;

    private ConversionTool() {
    }

    public static ConversionTool getInstance() {
        if (instance == null) {
            synchronized (Transformation.class) {
                if (instance == null) {
                    instance = new ConversionTool();
                }
            }
        }
        return instance;
    }

    public void handleConsoleInputs() {
        if (log.isInfoEnabled()) {
            log.info("CLI started...");
        }
        Console console = System.console();

        System.out.println("Enter the Base URL: ");
        Constants.BASE_URL = console.readLine();

        System.out.println("Enter the User name: ");
        Constants.USER_NAME = console.readLine();

        System.out.println("Enter the Password: ");
        char[] passwordChars = console.readPassword();
        Constants.PASSWORD = new String(passwordChars);;
    }

    public void startTransformation(){

        if (log.isInfoEnabled()) {
            log.info("Artifact Migration started...");
        }
        boolean isSuccess = true;
        try {
            Transformation.getInstance().transformNetworkPartitionList();
            Transformation.getInstance().transformAutoscalePolicyList();
            Transformation.getInstance().transformDeploymentPolicyList();
            Transformation.getInstance().transformCartridgeList();
            //Transformation.getInstance().transformSubscriptionList();

        } catch (Exception e) {
            isSuccess = false;
            log.error("Error while converting the artifacts ", e);
            System.out.println("Error while transforming NetworkPartition list. See log for more details.");
        }
        if (isSuccess)
            System.out.println("Conversion completed successfully");
    }
    public void addScriptDirectory(){
        File sourceLocation= new File(Constants.DIRECTORY_SOURCE_SCRIPT);
        File targetLocation= new File(Constants.ROOT_DIRECTORY+Constants.DIRECTORY_OUTPUT_SCRIPT);
        try {
            FileUtils.copyDirectoryToDirectory( sourceLocation,targetLocation);
        } catch (IOException e) {
            log.error("Error in copying scripts directory ", e);
        }
    }
}
