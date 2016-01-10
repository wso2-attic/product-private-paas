package org.wso2.ppaas.tools.artifactmigration.test;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@Path("/admin") public class StratosV400Mock {
    private static final Log log = LogFactory.getLog(StratosV400Mock.class);

    @GET @Path("/partition") @Produces(MediaType.APPLICATION_JSON) public String partition() {
        File file = new File(TestConstants.PARTITION_TEST_INPUT);
        String str = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            str = IOUtils.toString(fis, "UTF-8");

        } catch (FileNotFoundException e) {
            log.error("Error in getting the partition test file", e);
        } catch (IOException e) {
            log.error("Error in sending the partition list as the response", e);
        }
        return str;
    }

    @GET @Path("/policy/autoscale") @Produces(MediaType.APPLICATION_JSON) public String autoscale() throws IOException {
        File file = new File(TestConstants.AUTOSCALE_TEST_INPUT);
        String str = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            str = IOUtils.toString(fis, "UTF-8");
        } catch (FileNotFoundException e) {
            log.error("Error in getting the autoscale policy test file", e);
        } catch (IOException e) {
            log.error("Error in sending the autoscale policy list as the response", e);
        }
        return str;
    }

    @GET @Path("/policy/deployment") @Produces(MediaType.APPLICATION_JSON) public String deployment()
            throws IOException {
        File file = new File(TestConstants.DEPLOYMENT_TEST_INPUT);
        String str = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            str = IOUtils.toString(fis, "UTF-8");
        } catch (FileNotFoundException e) {
            log.error("Error in getting the deployment policy test file", e);
        } catch (IOException e) {
            log.error("Error in sending the deployment policy list as the response", e);
        }
        return str;
    }

    @GET @Path("/cartridge/list") @Produces(MediaType.APPLICATION_JSON) public String cartridge() throws IOException {
        File file = new File(TestConstants.CARTRIDGE_TEST_INPUT);
        String str = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            str = IOUtils.toString(fis, "UTF-8");
        } catch (FileNotFoundException e) {
            log.error("Error in getting the cartridge test file", e);
        } catch (IOException e) {
            log.error("Error in sending the cartridge list as the response", e);
        }
        return str;
    }

    @GET @Path("/cartridge/PHP/subscription/myphp/domains") @Produces(MediaType.APPLICATION_JSON) public String domain()
            throws IOException {
        File file = new File(TestConstants.DOMAIN_MAPPING_TEST_INPUT);
        String str = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            str = IOUtils.toString(fis, "UTF-8");
        } catch (FileNotFoundException e) {
            log.error("Error in getting the domain mapping test file", e);
        } catch (IOException e) {
            log.error("Error in sending the domain mapping list as the response", e);
        }
        return str;
    }

    @GET @Path("/cartridge/list/subscribed/all") @Produces(MediaType.APPLICATION_JSON) public String subscription()
            throws IOException {
        File file = new File(TestConstants.SUBSCRIPTION_TEST_INPUT);
        String str = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            str = IOUtils.toString(fis, "UTF-8");
        } catch (FileNotFoundException e) {
            log.error("Error in getting the subscription data test file", e);
        } catch (IOException e) {
            log.error("Error in sending the subscription data list as the response", e);
        }
        return str;
    }

}
