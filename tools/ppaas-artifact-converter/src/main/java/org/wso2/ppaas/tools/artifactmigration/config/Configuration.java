package org.wso2.ppaas.tools.artifactmigration.config;

/**
 * Configuration Details
 */
public class Configuration {


    // 4.0.0 config
    public static String BASE_URL = "http://127.0.0.1:8080/admin/";
    public static String USER_NAME = "admin";
    public static String PASSWORD = "admin";

    // 4.1.0 config templates
    public static String ROOT_TEMPLATE_DIRECTORY = System.getProperty("user.dir") + "/../templates/";

    // 4.1.0 config outputs
    public static String ROOT_DIRECTORY = System.getProperty("user.dir") + "/../output-artifacts/";

    // Do not use forward slash at the beginning instead use it in the base url.
    public static final String URL_PARTITION = "partition";
    public static final String URL_POLICY_AUTOSCALE = "policy/autoscale";
    public static final String URL_CARTRIDGE = "cartridge/list";
    public static final String URL_POLICY_DEPLOYMENT = "policy/deployment";

    // Do not use forward slash at the beginning instead use it in the root directory.
    public static final String DIRECTORY_TEMPLATE_POLICY_AUTOSCALE = "autoscaling-policies/autoscaling-policy-1.json";
    public static final String DIRECTORY_TEMPLATE_NETWORK_PARTITION = "network-partitions/gce/network-partition-1.json";
    public static final String DIRECTORY_TEMPLATE_POLICY_DEPLOYMENT = "deployment-policies/deployment-policy-1.json";
    public static final String DIRECTORY_TEMPLATE_APPLICATION = "applications/simple/single-cartridge-app/artifacts/application.json";

    // Do not use forward slash at the beginning instead use it in the root directory.
    public static final String DIRECTORY_POLICY_AUTOSCALE = "autoscaling-policies";
    public static final String DIRECTORY_NETWORK_PARTITION = "network-partitions";
    public static final String DIRECTORY_POLICY_DEPLOYMENT = "deployment-policies";
    public static final String DIRECTORY_APPLICATION = "applications";


}