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

@Path("/admin")
public class StratosV400Mock {
    private static final Log log = LogFactory.getLog(StratosV400Mock.class);

    private String readJSON(String FileName) {
        File file = new File(FileName);
        String str = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            str = IOUtils.toString(fis);
        } catch (FileNotFoundException e) {
            log.error("Error in getting the test file", e);
        } catch (IOException e) {
            log.error("Error in converting JSONs to a String ", e);
        }
        if (str != null)
            return str;
        else
            return TestConstants.ERROR_MSG;
    }

    @GET
    @Path("/partition")
    @Produces(MediaType.APPLICATION_JSON)
    public String partition() {
        return readJSON(TestConstants.PARTITION_TEST_INPUT);
    }

    @GET
    @Path("/policy/autoscale")
    @Produces(MediaType.APPLICATION_JSON)
    public String autoscale() {
        return readJSON(TestConstants.AUTOSCALE_TEST_INPUT);
    }

    @GET
    @Path("/policy/deployment")
    @Produces(MediaType.APPLICATION_JSON)
    public String deployment() {
        return readJSON(TestConstants.DEPLOYMENT_TEST_INPUT);
    }

    @GET
    @Path("/cartridge/list")
    @Produces(MediaType.APPLICATION_JSON) public String cartridge() {
        return readJSON(TestConstants.CARTRIDGE_TEST_INPUT);
    }

    @GET
    @Path("/cartridge/php/subscription/myphp/domains")
    @Produces(MediaType.APPLICATION_JSON)
    public String domainmyphp() {
        return readJSON(TestConstants.DOMAIN_MAPPING_TEST_INPUT);
    }

    @GET
    @Path("/cartridge/php/subscription/newphp/domains")
    @Produces(MediaType.APPLICATION_JSON)
    public String domainnewphp() {
        return readJSON(TestConstants.DOMAIN_MAPPING_TEST_INPUT);
    }

    @GET
    @Path("/cartridge/tomcat/subscription/mytomcat/domains")
    @Produces(MediaType.APPLICATION_JSON)
    public String domainmytomcat() {
        return readJSON(TestConstants.DOMAIN_MAPPING_TEST_INPUT);
    }

    @GET
    @Path("/cartridge/tomcat/subscription/newtomcat/domains")
    @Produces(MediaType.APPLICATION_JSON)
    public String domainnewtomcat() {
        return readJSON(TestConstants.DOMAIN_MAPPING_TEST_INPUT);
    }

    @GET
    @Path("/cartridge/list/subscribed/all")
    @Produces(MediaType.APPLICATION_JSON)
    public String subscription() {
        return readJSON(TestConstants.SUBSCRIPTION_TEST_INPUT);
    }

    @GET
    @Path("/cartridge/tenanted/list")
    @Produces(MediaType.APPLICATION_JSON)
    public String multiTenantCartridge() {
        return readJSON(TestConstants.MULTI_TENANT_CARTRIDGE_TEST_INPUT);
    }

    @GET
    @Path("/service")
    @Produces(MediaType.APPLICATION_JSON)
    public String service() {
        return readJSON(TestConstants.SERVICE_TEST_INPUT);
    }

}
