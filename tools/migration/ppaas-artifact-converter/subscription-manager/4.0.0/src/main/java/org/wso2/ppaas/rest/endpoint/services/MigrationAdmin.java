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
package org.wso2.ppaas.rest.endpoint.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.stratos.manager.dto.Cartridge;
import org.apache.stratos.manager.dto.SubscriptionInfo;
import org.wso2.ppaas.rest.endpoint.Utils;
import org.wso2.ppaas.rest.endpoint.annotation.AuthorizationAction;
import org.wso2.ppaas.rest.endpoint.annotation.SuperTenantService;
import org.wso2.ppaas.rest.endpoint.bean.subscription.domain.SubscriptionDomainBean;
import org.wso2.ppaas.rest.endpoint.exception.RestAPIException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.ppaas.rest.endpoint.bean.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/admin/")
public class MigrationAdmin extends AbstractAdmin {
    private static Log log = LogFactory.getLog(MigrationAdmin.class);
    @Context
    HttpServletRequest httpServletRequest;

    @POST
    @Path("/init")
    @AuthorizationAction("/permission/protected/manage/monitor/tenants")
    public StratosAdminResponse initialize()
            throws RestAPIException {


        StratosAdminResponse stratosAdminResponse = new StratosAdminResponse();
        stratosAdminResponse.setMessage("Successfully logged in");
        return stratosAdminResponse;
    }

    /*
    This method gets called by the client who are interested in using session mechanism to authenticate themselves in
    subsequent calls. This method call get authenticated by the basic authenticator.
    Once the authenticated call received, the method creates a session.

     */
    @GET
    @Path("/cookie")
    @Produces("application/json")
    @Consumes("application/json")
    @AuthorizationAction("/permission/protected/manage/monitor/tenants")
    public Response getCookie() {
        HttpSession httpSession = httpServletRequest.getSession(true);//create session if not found
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        httpSession.setAttribute("userName", carbonContext.getUsername());
        httpSession.setAttribute("tenantDomain", carbonContext.getTenantDomain());
        httpSession.setAttribute("tenantId", carbonContext.getTenantId());

        String sessionId = httpSession.getId();
        return Response.ok().header("WWW-Authenticate", "Basic").type(MediaType.APPLICATION_JSON).
                entity(Utils.buildAuthenticationSuccessMessage(sessionId)).build();
    }


    @GET
    @Path("/cartridge/list/subscribed")
    @Produces("application/json")
    @Consumes("application/json")
    @AuthorizationAction("/permission/protected/manage/monitor/tenants")
    public Cartridge[] getSubscribedCartridges() throws RestAPIException {
        List<Cartridge> cartridgeList = ServiceUtils.getSubscriptions(null, null, getConfigContext());
        // Following is very important when working with axis2
        return cartridgeList.isEmpty() ? new Cartridge[0] : cartridgeList.toArray(new Cartridge[cartridgeList.size()]);
    }

    @GET
    @Path("/cartridge/list/subscribed/all")
    @Produces("application/json")
    @Consumes("application/json")
    @AuthorizationAction("/permission/protected/manage/monitor/tenants")
    @SuperTenantService(true)
    public CartridgeInfoBean[] getAllSubscriptions() throws RestAPIException {
        List<CartridgeInfoBean> cartridgeList = ServiceUtils.getAllSubscriptions();
        // Following is very important when working with axis2
        return cartridgeList.isEmpty() ? new CartridgeInfoBean[0] :
                cartridgeList.toArray(new CartridgeInfoBean[cartridgeList.size()]);
    }

    @POST
    @Path("/cartridge/subscribe")
    @Produces("application/json")
    @Consumes("application/json")
    @AuthorizationAction("/permission/protected/manage/monitor/tenants")
    public SubscriptionInfo subscribe(CartridgeInfoBean cartridgeInfoBean) throws RestAPIException {

        return ServiceUtils.subscribeToCartridge(cartridgeInfoBean,
                getConfigContext(),
                getUsername(),
                getTenantDomain());
    }

    @POST
    @Path("/cartridge/subscribe/bulk")
    @Produces("application/json")
    @Consumes("application/json")
    @AuthorizationAction("/permission/protected/manage/monitor/tenants")
    @SuperTenantService(true)
    // allow super tenant to bulk subscribe
    public SubscriptionInfo[] bulkSubscribe(CartridgeInfoBeanWrapper subscriptionWrapper) throws RestAPIException {

        return ServiceUtils.bulkSubscribe(subscriptionWrapper.getCartridgeInfoBean());
    }


    @POST
    @Path("/cartridge/subscribe/tenant")
    @Produces("application/json")
    @Consumes("application/json")
    @AuthorizationAction("/permission/protected/manage/monitor/tenants")
    @SuperTenantService(true)
    // allow super tenant to subscribe a tenant to a cartridge forcefully
    public SubscriptionInfo subscribeTenantToCartridge(CartridgeInfoBean cartridgeInfoBean) throws RestAPIException {

        return ServiceUtils.subscribeTenantToCartridge(cartridgeInfoBean);
    }

    @POST
    @Path("/cartridge/unsubscribe")
    @Consumes("application/json")
    @AuthorizationAction("/permission/protected/manage/monitor/tenants")
    public StratosAdminResponse unsubscribe(String alias) throws RestAPIException {
        return ServiceUtils.unsubscribe(alias, getTenantDomain());
    }


    @DELETE
    @Path("/cartridge/unsubscribe/all")
    @Consumes("application/json")
    @AuthorizationAction("/permission/protected/manage/monitor/tenants")
    @SuperTenantService(true)
    public StratosAdminResponse bulkUnsubscribe() throws RestAPIException {

        if (log.isDebugEnabled()) {
            log.debug("Unsubscribing all tenants from all cartridges");
        }
        return ServiceUtils.bulkUnsubscribe();
    }


    @POST
    @Path("/cartridge/unsubscribe/tenant/{tenantDomain}/{alias}")
    @Consumes("application/json")
    @Produces("application/json")
    @AuthorizationAction("/permission/protected/manage/monitor/tenants")
    public StratosAdminResponse unsubscribeTenant(@PathParam("alias") String alias,
                                                  @PathParam("tenantDomain") String unsubscribingTenantDomain)
            throws RestAPIException {

        if (log.isDebugEnabled()) {
            log.debug(String.format("Unsubscribing cartridge with alias %s for tenant %s by %s", alias,
                    unsubscribingTenantDomain, getTenantDomain()));
        }
        return ServiceUtils.unsubsribeForTenant(alias, getTenantDomain(), unsubscribingTenantDomain);
    }

    @POST
    @Path("/cartridge/{cartridgeType}/subscription/{subscriptionAlias}/domains")
    @Consumes("application/json")
    @AuthorizationAction("/permission/protected/manage/monitor/tenants")
    public Response addSubscriptionDomains(@PathParam("cartridgeType") String cartridgeType,
                                           @PathParam("subscriptionAlias") String subscriptionAlias,
                                           SubscriptionDomainRequest request) throws RestAPIException {

        StratosAdminResponse stratosAdminResponse =
                ServiceUtils.addSubscriptionDomains(getConfigContext(), cartridgeType, subscriptionAlias, request);
        return Response.ok().entity(stratosAdminResponse).build();
    }

    @POST
    @Path("/cartridge/subscription/domains/bulk")
    @Consumes("application/json")
    @AuthorizationAction("/permission/protected/manage/monitor/tenants")
    public Response bulkAddSubscriptionDomains(BulkSubscriptionDomainsWrapper bulkSubscriptionDomainsWrapper)
            throws RestAPIException {

        StratosAdminResponse stratosAdminResponse =
                ServiceUtils.bulkAddSubscriptionDomains(
                        bulkSubscriptionDomainsWrapper.getSubscriptionDomainWrapperList());
        return Response.ok().entity(stratosAdminResponse).build();
    }

    @GET
    @Path("/cartridge/{cartridgeType}/subscription/{subscriptionAlias}/domains")
    @Consumes("application/json")
    @AuthorizationAction("/permission/protected/manage/monitor/tenants")
    public Response getSubscriptionDomains(@PathParam("cartridgeType") String cartridgeType,
                                           @PathParam("subscriptionAlias") String subscriptionAlias)
            throws RestAPIException {
        SubscriptionDomainBean[] subscriptionDomainBean =
                ServiceUtils.getSubscriptionDomains(getConfigContext(), cartridgeType, subscriptionAlias)
                        .toArray(new SubscriptionDomainBean[0]);

        if (subscriptionDomainBean.length == 0) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            return Response.ok().entity(subscriptionDomainBean).build();
        }
    }

    @GET
    @Path("/cartridge/subscription/domains/all")
    @Consumes("application/json")
    @AuthorizationAction("/permission/protected/manage/monitor/tenants")
    @SuperTenantService(true)
    public Response getAllSubscriptionDomains() throws RestAPIException {
        SubscriptionDomainBean[] subscriptionDomainBean =
                ServiceUtils.getAllSubscriptionDomains().toArray(new SubscriptionDomainBean[0]);

        if (subscriptionDomainBean.length == 0) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            return Response.ok().entity(subscriptionDomainBean).build();
        }
    }

    @GET
    @Path("/cartridge/{cartridgeType}/subscription/{subscriptionAlias}/domains/{domainName}")
    @Consumes("application/json")
    @AuthorizationAction("/permission/protected/manage/monitor/tenants")
    public Response getSubscriptionDomain(@PathParam("cartridgeType") String cartridgeType,
                                          @PathParam("subscriptionAlias") String subscriptionAlias,
                                          @PathParam("domainName") String domainName) throws RestAPIException {
        SubscriptionDomainBean subscriptionDomainBean =
                ServiceUtils.getSubscriptionDomain(getConfigContext(), cartridgeType, subscriptionAlias, domainName);
        if (subscriptionDomainBean.domainName == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            return Response.ok().entity(subscriptionDomainBean).build();
        }
    }

    @DELETE
    @Path("/cartridge/{cartridgeType}/subscription/{subscriptionAlias}/domains/{domainName}")
    @Consumes("application/json")
    @AuthorizationAction("/permission/protected/manage/monitor/tenants")
    public StratosAdminResponse removeSubscriptionDomain(@PathParam("cartridgeType") String cartridgeType,
                                                         @PathParam("subscriptionAlias") String subscriptionAlias,
                                                         @PathParam("domainName") String domainName)
            throws RestAPIException {
        return ServiceUtils.removeSubscriptionDomain(getConfigContext(), cartridgeType, subscriptionAlias, domainName);
    }
}
