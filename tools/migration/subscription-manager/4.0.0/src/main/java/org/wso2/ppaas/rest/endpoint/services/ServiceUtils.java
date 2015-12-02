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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.stratos.cloud.controller.stub.pojo.CartridgeInfo;
import org.apache.stratos.cloud.controller.stub.pojo.Property;
import org.apache.stratos.manager.client.CloudControllerServiceClient;
import org.apache.stratos.manager.deploy.service.ServiceDeploymentManager;
import org.apache.stratos.manager.dto.Cartridge;
import org.apache.stratos.manager.dto.SubscriptionInfo;
import org.apache.stratos.manager.exception.*;
import org.apache.stratos.manager.subscription.*;
import org.apache.stratos.manager.topology.model.TopologyClusterInformationModel;
import org.apache.stratos.manager.utils.ApplicationManagementUtil;
import org.apache.stratos.messaging.domain.topology.Cluster;
import org.apache.stratos.messaging.domain.topology.Member;
import org.wso2.ppaas.rest.endpoint.CartridgeSubscriptionManager;
import org.wso2.ppaas.rest.endpoint.DataInsertionAndRetrievalManager;
import org.wso2.ppaas.rest.endpoint.ServiceHolder;
import org.wso2.ppaas.rest.endpoint.bean.CartridgeInfoBean;
import org.wso2.ppaas.rest.endpoint.bean.StratosAdminResponse;
import org.wso2.ppaas.rest.endpoint.bean.SubscriptionDomainRequest;
import org.wso2.ppaas.rest.endpoint.bean.SubscriptionDomainWrapper;
import org.wso2.ppaas.rest.endpoint.bean.subscription.domain.SubscriptionDomainBean;
import org.wso2.ppaas.rest.endpoint.bean.util.converter.PojoConverter;
import org.wso2.ppaas.rest.endpoint.exception.RestAPIException;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.tenant.Tenant;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class ServiceUtils {
    public static final String IS_VOLUME_REQUIRED = "volume.required";
    public static final String SHOULD_DELETE_VOLUME = "volume.delete.on.unsubscription";
    public static final String VOLUME_SIZE = "volume.size.gb";
    public static final String DEVICE_NAME = "volume.device.name";

    private static Log log = LogFactory.getLog(ServiceUtils.class);
    private static CartridgeSubscriptionManager cartridgeSubsciptionManager = new CartridgeSubscriptionManager();
    private static ServiceDeploymentManager serviceDeploymentManager = new ServiceDeploymentManager();
    private static DataInsertionAndRetrievalManager dataInsertionAndRetrievalManager =
            new DataInsertionAndRetrievalManager();


    public static List<CartridgeInfoBean> getAllSubscriptions() throws RestAPIException {
        log.info("Getting all subscribed cartridges.");
        try {
            String availableCartridges[] = CloudControllerServiceClient.getServiceClient().getRegisteredCartridges();
            List<CartridgeInfoBean> cartridgeInfoBeans = new ArrayList<CartridgeInfoBean>();
            for (String cartridgeType : availableCartridges) {
                Collection<CartridgeSubscription> subscriptions =
                        cartridgeSubsciptionManager.getCartridgeSubscriptionsForType(cartridgeType);
                for (CartridgeSubscription subscription : subscriptions) {
                    Cartridge cartridge = getCartridgeFromSubscription(subscription);
                    if (cartridge == null) {
                        continue;
                    }

                    // Ignoring the LB cartridges since they are not shown to the user.
                    if (cartridge.isLoadBalancer()) {
                        continue;
                    }
                    CartridgeInfoBean cartridgeInfoBean = new CartridgeInfoBean();

                    cartridgeInfoBean.setCartridgeType(subscription.getCartridgeInfo().getType());
                    cartridgeInfoBean.setAlias(subscription.getAlias());
                    cartridgeInfoBean.setAutoscalePolicy(subscription.getAutoscalingPolicyName());
                    cartridgeInfoBean.setDeploymentPolicy(subscription.getDeploymentPolicyName());
                    cartridgeInfoBean.setRepoURL(subscription.getRepository().getUrl());
                    cartridgeInfoBean.setPrivateRepo(subscription.getRepository().isPrivateRepository());
                    cartridgeInfoBean.setRepoUsername(subscription.getRepository().getUserName());
                    cartridgeInfoBean.setRepoPassword(subscription.getRepository().getPassword());
                    cartridgeInfoBean.setDataCartridgeType(subscription.getType());
                    cartridgeInfoBean.setCommitsEnabled(subscription.getRepository().isCommitEnabled());
                    if (subscription.getCartridgeInfo().getPersistence() != null) {
                        cartridgeInfoBean.setPersistanceRequired(
                                subscription.getCartridgeInfo().getPersistence().getPersistanceRequired());
                    }
                    cartridgeInfoBean.setSize("");
                    cartridgeInfoBean.setRemoveOnTermination(false);
                    cartridgeInfoBean.setServiceGroup(subscription.getCartridgeInfo().getServiceGroup());

                    List<String> domains = new ArrayList<>();
                    for (SubscriptionDomain subscriptionDomain : subscription.getSubscriptionDomains()) {
                        domains.add(subscriptionDomain.getDomainName());
                    }
                    cartridgeInfoBean.setDomains(domains);
                    cartridgeInfoBean.setSubscribingTenantDomain(subscription.getSubscriber().getTenantDomain());
                    cartridgeInfoBeans.add(cartridgeInfoBean);
                }
            }
            return cartridgeInfoBeans;
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RestAPIException(e.getMessage(), e);
        }
    }

    static List<Cartridge> getSubscriptions(String cartridgeSearchString, String serviceGroup,
                                            ConfigurationContext configurationContext) throws RestAPIException {

        List<Cartridge> cartridges = new ArrayList<Cartridge>();
        log.info("Getting subscribed cartridges. Search String: " + cartridgeSearchString);

        try {
            Pattern searchPattern = getSearchStringPattern(cartridgeSearchString);

            Collection<CartridgeSubscription> subscriptions =
                    cartridgeSubsciptionManager.getCartridgeSubscriptions(ApplicationManagementUtil.
                            getTenantId(configurationContext), null);

            if (subscriptions != null && !subscriptions.isEmpty()) {

                for (CartridgeSubscription subscription : subscriptions) {

                    if (!cartridgeMatches(subscription.getCartridgeInfo(), subscription, searchPattern)) {
                        continue;
                    }
                    Cartridge cartridge = getCartridgeFromSubscription(subscription);
                    if (cartridge == null) {
                        continue;
                    }
                    Cluster cluster = TopologyClusterInformationModel.getInstance()
                            .getCluster(ApplicationManagementUtil.getTenantId(configurationContext)
                                    , cartridge.getCartridgeType(), cartridge.getCartridgeAlias());
                    String cartridgeStatus = "Inactive";
                    int activeMemberCount = 0;
                    if (cluster != null) {
                        Collection<Member> members = cluster.getMembers();
                        for (Member member : members) {
                            if (member.isActive()) {
                                cartridgeStatus = "Active";
                                activeMemberCount++;
                            }
                        }
                    }
                    cartridge.setActiveInstances(activeMemberCount);
                    cartridge.setStatus(cartridgeStatus);

                    // Ignoring the LB cartridges since they are not shown to the user.
                    if (cartridge.isLoadBalancer()) {
                        continue;
                    }
                    if (StringUtils.isNotEmpty(serviceGroup)) {
                        if (cartridge.getServiceGroup() != null && serviceGroup.equals(cartridge.getServiceGroup())) {
                            cartridges.add(cartridge);
                        }
                    } else {
                        cartridges.add(cartridge);
                    }
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("There are no subscribed cartridges");
                }
            }
        }
        catch (Exception e) {
            String msg = "Error while getting subscribed cartridges. Cause: " + e.getMessage();
            log.error(msg, e);
            throw new RestAPIException(msg, e);
        }

        Collections.sort(cartridges);

        if (log.isDebugEnabled()) {
            log.debug("Returning subscribed cartridges " + cartridges.size());
        }

        /*if(cartridges.isEmpty()) {
            String msg = "Cannot find any subscribed Cartridge, matching the given string: "+cartridgeSearchString;
            log.error(msg);
            throw new RestAPIException(msg);
        }*/

        return cartridges;
    }


    private static Cartridge getCartridgeFromSubscription(CartridgeSubscription subscription) throws RestAPIException {

        if (subscription == null) {
            return null;
        }
        try {
            Cartridge cartridge = new Cartridge();
            cartridge.setCartridgeType(subscription.getCartridgeInfo()
                    .getType());
            cartridge.setMultiTenant(subscription.getCartridgeInfo()
                    .getMultiTenant());
            cartridge
                    .setProvider(subscription.getCartridgeInfo().getProvider());
            cartridge.setVersion(subscription.getCartridgeInfo().getVersion());
            cartridge.setDescription(subscription.getCartridgeInfo()
                    .getDescription());
            cartridge.setDisplayName(subscription.getCartridgeInfo()
                    .getDisplayName());
            cartridge.setCartridgeAlias(subscription.getAlias());
            cartridge.setHostName(subscription.getHostName());
            cartridge.setMappedDomain(subscription.getMappedDomain());
            if (subscription.getRepository() != null) {
                cartridge.setRepoURL(subscription.getRepository().getUrl());
            }

            if (subscription instanceof DataCartridgeSubscription) {
                DataCartridgeSubscription dataCartridgeSubscription = (DataCartridgeSubscription) subscription;
                cartridge.setDbHost(dataCartridgeSubscription.getDBHost());
                cartridge.setDbUserName(dataCartridgeSubscription
                        .getDBUsername());
                cartridge
                        .setPassword(dataCartridgeSubscription.getDBPassword());
            }

            if (subscription.getLbClusterId() != null
                    && !subscription.getLbClusterId().isEmpty()) {
                cartridge.setLbClusterId(subscription.getLbClusterId());
            }

            cartridge.setStatus(subscription.getSubscriptionStatus());
            cartridge.setPortMappings(subscription.getCartridgeInfo()
                    .getPortMappings());

            if (subscription.getCartridgeInfo().getLbConfig() != null &&
                    subscription.getCartridgeInfo().getProperties() != null) {
                for (Property property : subscription.getCartridgeInfo().getProperties()) {
                    if (property.getName().equals("load.balancer")) {
                        cartridge.setLoadBalancer(true);
                    }
                }
            }
            if (subscription.getCartridgeInfo().getServiceGroup() != null) {
                cartridge.setServiceGroup(subscription.getCartridgeInfo().getServiceGroup());
            }
            return cartridge;

        }
        catch (Exception e) {
            String msg = "Unable to extract the Cartridge from subscription. Cause: " + e.getMessage();
            log.error(msg);
            throw new RestAPIException(msg);
        }

    }

    static Pattern getSearchStringPattern(String searchString) {
        if (log.isDebugEnabled()) {
            log.debug("Creating search pattern for " + searchString);
        }
        if (searchString != null) {
            // Copied from org.wso2.carbon.webapp.mgt.WebappAdmin.doesWebappSatisfySearchString(WebApplication, String)
            String regex = searchString.toLowerCase().replace("..?", ".?").replace("..*", ".*").replaceAll("\\?", ".?")
                    .replaceAll("\\*", ".*?");
            if (log.isDebugEnabled()) {
                log.debug("Created regex: " + regex + " for search string " + searchString);
            }

            Pattern pattern = Pattern.compile(regex);
            return pattern;
        }
        return null;
    }


    static boolean cartridgeMatches(CartridgeInfo cartridgeInfo, CartridgeSubscription cartridgeSubscription,
                                    Pattern pattern) {
        if (pattern != null) {
            boolean matches = false;
            if (cartridgeInfo.getDisplayName() != null) {
                matches = pattern.matcher(cartridgeInfo.getDisplayName().toLowerCase()).find();
            }
            if (!matches && cartridgeInfo.getDescription() != null) {
                matches = pattern.matcher(cartridgeInfo.getDescription().toLowerCase()).find();
            }
            if (!matches && cartridgeSubscription.getType() != null) {
                matches = pattern.matcher(cartridgeSubscription.getType().toLowerCase()).find();
            }
            if (!matches && cartridgeSubscription.getAlias() != null) {
                matches = pattern.matcher(cartridgeSubscription.getAlias().toLowerCase()).find();
            }
            return matches;
        }
        return true;
    }

    private static ConfigurationContext getConfigContext() {

        // If a tenant has been set, then try to get the ConfigurationContext of that tenant
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        ConfigurationContextService configurationContextService =
                (ConfigurationContextService) carbonContext.getOSGiService(ConfigurationContextService.class);
        ConfigurationContext mainConfigContext = configurationContextService.getServerConfigContext();
        String domain = carbonContext.getTenantDomain();
        if (domain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(domain)) {
            return TenantAxisUtils.getTenantConfigurationContext(domain, mainConfigContext);
        } else if (carbonContext.getTenantId() == MultitenantConstants.SUPER_TENANT_ID) {
            return mainConfigContext;
        } else {
            throw new UnsupportedOperationException("Tenant domain unidentified. " +
                    "Upstream code needs to identify & set the tenant domain & tenant ID. " +
                    " The TenantDomain SOAP header could be set by the clients or " +
                    "tenant authentication should be carried out.");
        }
    }

    private static String getTenantDomain() {
        return CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
    }

    private static String getUsername() {
        return CarbonContext.getThreadLocalCarbonContext().getUsername();
    }

    public static SubscriptionInfo[] bulkSubscribe(List<CartridgeInfoBean> cartridgeInfoBeans) throws RestAPIException {
        if (cartridgeInfoBeans == null) {
            throw new RestAPIException("Cartridge subscription list is null");
        }
        ArrayList<SubscriptionInfo> subscriptionInfoList = new ArrayList<>();
        try {
            for (CartridgeInfoBean cartridgeInfoBean : cartridgeInfoBeans) {
                SubscriptionInfo subscriptionInfo;
                if (cartridgeInfoBean.getSubscribingTenantDomain().equals("carbon.super")) {
                    subscriptionInfo =
                            subscribeToCartridge(cartridgeInfoBean, getConfigContext(), getUsername(),
                                    getTenantDomain());
                } else {
                    subscriptionInfo = subscribeTenantToCartridge(cartridgeInfoBean);
                }
                subscriptionInfoList.add(subscriptionInfo);
            }
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RestAPIException(e.getMessage(), e);
        }
        return subscriptionInfoList.toArray(new SubscriptionInfo[subscriptionInfoList.size()]);
    }


    static SubscriptionInfo subscribeTenantToCartridge(CartridgeInfoBean cartridgeInfoBean) throws RestAPIException {
        if (cartridgeInfoBean == null) {
            throw new RestAPIException("Cartridge subscription entry is null");
        }
        // The subscribing tenant domain must be specified, else can't continue
        if (cartridgeInfoBean.getSubscribingTenantDomain() == null ||
                cartridgeInfoBean.getSubscribingTenantDomain().isEmpty()) {
            throw new RestAPIException(
                    "Subscribing tenant domain [ " + cartridgeInfoBean.getSubscribingTenantDomain() + " ] is invalid");
        }

        // get the tenant id
        int tenantId;
        try {
            tenantId = getTenantId(cartridgeInfoBean.getSubscribingTenantDomain());

        }
        catch (UserStoreException e) {
            throw new RestAPIException(e);
        }

        // check if there is a valid tenant with this tenantId
        try {
            if (!isTenantValid(tenantId)) {
                throw new RestAPIException("Unable to find tenant " + cartridgeInfoBean.getSubscribingTenantDomain());
            }

        }
        catch (UserStoreException e) {
            throw new RestAPIException(e);
        }

        // get tenant admin username
        String tenantAdminUsername;
        try {
            tenantAdminUsername = getTenantAdminUsername(tenantId);

        }
        catch (UserStoreException e) {
            throw new RestAPIException(e);
        }

        if (tenantAdminUsername == null) {
            throw new RestAPIException("Unable to get admin username for tenant id: " + tenantId);
        }

        // start tenant flow
        if (log.isDebugEnabled()) {
            log.debug("Starting tenant flow for tenant: " + cartridgeInfoBean.getSubscribingTenantDomain() + ", id: " +
                    tenantId);
        }

        try {
            PrivilegedCarbonContext.startTenantFlow();
            setTenantInfomationToPrivilegedCC(cartridgeInfoBean.getSubscribingTenantDomain(), tenantId,
                    tenantAdminUsername);
            return subscribe(cartridgeInfoBean, tenantId, tenantAdminUsername,
                    cartridgeInfoBean.getSubscribingTenantDomain());

        }
        catch (Exception e) {
            throw new RestAPIException(e.getMessage(), e);

        }
        finally {
            PrivilegedCarbonContext.endTenantFlow();
            if (log.isDebugEnabled()) {
                log.debug("Ended tenant flow for tenant: " + cartridgeInfoBean.getSubscribingTenantDomain() + ", id: " +
                        tenantId);
            }
        }
    }

    static SubscriptionInfo subscribeToCartridge(CartridgeInfoBean cartridgeInfoBean,
                                                 ConfigurationContext configurationContext, String tenantUsername,
                                                 String tenantDomain) throws RestAPIException {

        try {
            return subscribe(cartridgeInfoBean, ApplicationManagementUtil.getTenantId(configurationContext),
                    tenantUsername, tenantDomain);

        }
        catch (Exception e) {
            throw new RestAPIException(e.getMessage(), e);
        }
    }

    private static SubscriptionInfo subscribe(CartridgeInfoBean cartridgeInfoBean, int tenantId, String tenantUsername,
                                              String tenantDomain)
            throws ADCException, PolicyException, UnregisteredCartridgeException,
            InvalidCartridgeAliasException, DuplicateCartridgeAliasException, RepositoryRequiredException,
            AlreadySubscribedException, RepositoryCredentialsRequiredException, InvalidRepositoryException,
            RepositoryTransportException, RestAPIException {

        SubscriptionData subscriptionData = new SubscriptionData();
        subscriptionData.setCartridgeType(cartridgeInfoBean.getCartridgeType());
        subscriptionData.setCartridgeAlias(cartridgeInfoBean.getAlias().trim());
        subscriptionData.setAutoscalingPolicyName(cartridgeInfoBean.getAutoscalePolicy());
        subscriptionData.setDeploymentPolicyName(cartridgeInfoBean.getDeploymentPolicy());
        subscriptionData.setTenantDomain(tenantDomain);
        subscriptionData.setTenantId(tenantId);
        subscriptionData.setTenantAdminUsername(tenantUsername);
        subscriptionData.setRepositoryType("git");
        subscriptionData.setRepositoryURL(cartridgeInfoBean.getRepoURL());
        subscriptionData.setRepositoryUsername(cartridgeInfoBean.getRepoUsername());
        subscriptionData.setRepositoryPassword(cartridgeInfoBean.getRepoPassword());
        subscriptionData.setCommitsEnabled(cartridgeInfoBean.isCommitsEnabled());
        subscriptionData.setServiceGroup(cartridgeInfoBean.getServiceGroup());
        //subscriptionData.setServiceName(cartridgeInfoBean.getServiceName()); // For MT cartridges

        if (cartridgeInfoBean.isPersistanceRequired()) {
            // Add persistence related properties to PersistenceContext
            PersistenceContext persistenceContext = new PersistenceContext();
            persistenceContext.setPersistanceRequiredProperty(IS_VOLUME_REQUIRED,
                    String.valueOf(cartridgeInfoBean.isPersistanceRequired()));
            persistenceContext.setSizeProperty(VOLUME_SIZE, cartridgeInfoBean.getSize());
            persistenceContext.setDeleteOnTerminationProperty(SHOULD_DELETE_VOLUME,
                    String.valueOf(cartridgeInfoBean.isRemoveOnTermination()));
            subscriptionData.setPersistanceCtxt(persistenceContext);
        }

        //subscribe
        return cartridgeSubsciptionManager.subscribeToCartridgeWithProperties(subscriptionData);

    }


    static StratosAdminResponse unsubscribe(String alias, String tenantDomain) throws RestAPIException {

        int tenantId;
        try {
            tenantId = getTenantId(tenantDomain);
        }
        catch (UserStoreException e) {
            throw new RestAPIException("No tenant found for the tenant domain " + tenantDomain);
        }

        try {
            cartridgeSubsciptionManager.unsubscribeFromCartridge(tenantDomain, alias);

        }
        catch (ADCException e) {
            String msg = "Failed to unsubscribe from [alias] " + alias + ". Cause: " + e.getMessage();
            log.error(msg, e);
            throw new RestAPIException(msg, e);

        }
        catch (NotSubscribedException e) {
            log.error(e.getMessage(), e);
            throw new RestAPIException(e.getMessage(), e);
        }

        StratosAdminResponse stratosAdminResponse = new StratosAdminResponse();
        stratosAdminResponse.setMessage("Successfully terminated the subscription with alias " + alias);
        return stratosAdminResponse;
    }

    public static StratosAdminResponse bulkUnsubscribe() throws RestAPIException {
        List<CartridgeInfoBean> cartridgeInfoBeanList = getAllSubscriptions();
        try {
            for (CartridgeInfoBean cartridgeInfoBean : cartridgeInfoBeanList) {
                int unsubscribingTenantId;
                String unsubscribingTenantDomain = cartridgeInfoBean.getSubscribingTenantDomain();
                String alias = cartridgeInfoBean.getAlias();
                try {
                    unsubscribingTenantId = getTenantId(unsubscribingTenantDomain);
                    if (log.isDebugEnabled()) {
                        log.debug("Id of the tenant to be unsubscribed " + unsubscribingTenantId);
                    }

                    if (unsubscribingTenantId == MultitenantConstants.INVALID_TENANT_ID) {
                        String message = "No tenant found for the domain " + unsubscribingTenantDomain;
                        log.error(message);
                        throw new RestAPIException(message);
                    }
                }
                catch (UserStoreException e) {
                    log.error(e.getMessage(), e);
                    throw new RestAPIException("No tenant found for the tenant domain " + unsubscribingTenantDomain);
                }

                if (isSuperTenant(unsubscribingTenantId)) {
                    unsubscribe(cartridgeInfoBean.getAlias(), getTenantDomain());

                } else {
                    String tenantAdminUsername;
                    try {
                        tenantAdminUsername = getTenantAdminUsername(unsubscribingTenantId);
                        if (log.isDebugEnabled()) {
                            log.debug("Tenant admin name of tenant to be unsubscribed " + tenantAdminUsername);
                        }
                    }
                    catch (UserStoreException e) {
                        log.error(e.getMessage(), e);
                        throw new RestAPIException("Could not find tenant admin username for " + unsubscribingTenantId,
                                e);
                    }

                    try {
                        if (!isSuperTenant(unsubscribingTenantId)) {
                            if (log.isDebugEnabled()) {
                                log.debug(String.format(
                                        "Provided tenant domain %s is not super tenant domain, hence starting tenant flow",
                                        unsubscribingTenantDomain));
                            }
                            PrivilegedCarbonContext.startTenantFlow();
                            setTenantInfomationToPrivilegedCC(unsubscribingTenantDomain, unsubscribingTenantId,
                                    tenantAdminUsername);
                        }

                        cartridgeSubsciptionManager.unsubscribeFromCartridge(unsubscribingTenantDomain, alias);
                    }
                    catch (ADCException e) {
                        String msg = "Failed to unsubscribe from [alias] " + alias + ". Cause: " + e.getMessage();
                        log.error(msg, e);
                        throw new RestAPIException(msg, e);

                    }
                    catch (NotSubscribedException e) {
                        log.error(e.getMessage(), e);
                        throw new RestAPIException(e.getMessage(), e);
                    }
                    finally {
                        PrivilegedCarbonContext.endTenantFlow();
                        if (log.isDebugEnabled()) {
                            log.debug("Ended tenant flow for tenant: " + unsubscribingTenantDomain + ", tenant id: " +
                                    unsubscribingTenantId);
                        }
                    }
                }
            }
            StratosAdminResponse stratosAdminResponse = new StratosAdminResponse();
            stratosAdminResponse.setMessage("Successfully terminated all subscriptions for all tenants");
            return stratosAdminResponse;
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RestAPIException(e.getMessage(), e);
        }
    }

    static StratosAdminResponse unsubsribeForTenant(String alias, String tenantDomain, String unsubscribingTenantDomain)
            throws RestAPIException {

        int unsubscribingTenantId;
        try {
            unsubscribingTenantId = getTenantId(unsubscribingTenantDomain);
            if (log.isDebugEnabled()) {
                log.debug("Id of the tenant to be unsubscribed " + unsubscribingTenantId);
            }

            if (unsubscribingTenantId == MultitenantConstants.INVALID_TENANT_ID) {
                String message = "No tenant found for the domain " + unsubscribingTenantDomain;
                log.error(message);
                throw new RestAPIException(message);
            }
        }
        catch (UserStoreException e) {
            throw new RestAPIException("No tenant found for the tenant domain " + unsubscribingTenantDomain);
        }

        String tenantAdminUsername;
        try {
            tenantAdminUsername = getTenantAdminUsername(unsubscribingTenantId);
            if (log.isDebugEnabled()) {
                log.debug("Tenant admin name of tenant to be unsubscribed " + tenantAdminUsername);
            }
        }
        catch (UserStoreException e) {
            throw new RestAPIException("Could not find tenant admin username for " + unsubscribingTenantId, e);
        }

        try {
            if (!isSuperTenant(unsubscribingTenantId)) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format(
                            "Provided tenant domain %s is not super tenant domain, hence starting tenant flow",
                            unsubscribingTenantDomain));
                }
                PrivilegedCarbonContext.startTenantFlow();
                setTenantInfomationToPrivilegedCC(unsubscribingTenantDomain, unsubscribingTenantId,
                        tenantAdminUsername);
            }

            cartridgeSubsciptionManager.unsubscribeFromCartridge(unsubscribingTenantDomain, alias);
        }
        catch (ADCException e) {
            String msg = "Failed to unsubscribe from [alias] " + alias + ". Cause: " + e.getMessage();
            log.error(msg, e);
            throw new RestAPIException(msg, e);

        }
        catch (NotSubscribedException e) {
            log.error(e.getMessage(), e);
            throw new RestAPIException(e.getMessage(), e);
        }
        finally {
            PrivilegedCarbonContext.endTenantFlow();
            if (log.isDebugEnabled()) {
                log.debug("Ended tenant flow for tenant: " + unsubscribingTenantDomain + ", tenant id: " +
                        unsubscribingTenantId);
            }
        }

        StratosAdminResponse stratosAdminResponse = new StratosAdminResponse();
        stratosAdminResponse.setMessage("Successfully terminated the subscription with alias " + alias);
        return stratosAdminResponse;
    }

    private static boolean isSuperTenant(int unsubscribingTenantId) {
        return MultitenantConstants.SUPER_TENANT_ID == unsubscribingTenantId;
    }

    public static StratosAdminResponse bulkAddSubscriptionDomains(
            List<SubscriptionDomainWrapper> subscriptionDomainWrapperList) throws RestAPIException {
        try {
            for (SubscriptionDomainWrapper subscriptionDomainWrapper : subscriptionDomainWrapperList) {
                addSubscriptionDomains(getConfigContext(), subscriptionDomainWrapper.getCartridgeType(),
                        subscriptionDomainWrapper.getSubscriptionAlias(), subscriptionDomainWrapper.getRequest());
            }
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RestAPIException(e.getMessage(), e);
        }
        StratosAdminResponse stratosAdminResponse = new StratosAdminResponse();
        stratosAdminResponse.setMessage("Successfully added domains to cartridge subscription");
        return stratosAdminResponse;
    }

    public static StratosAdminResponse addSubscriptionDomains(ConfigurationContext configurationContext,
                                                              String cartridgeType,
                                                              String subscriptionAlias,
                                                              SubscriptionDomainRequest request)
            throws RestAPIException {
        try {
            int tenantId = ApplicationManagementUtil.getTenantId(configurationContext);

            for (SubscriptionDomainBean subscriptionDomain : request.domains) {
                boolean isDomainExists =
                        isSubscriptionDomainExists(configurationContext, cartridgeType, subscriptionAlias,
                                subscriptionDomain.domainName);
                if (isDomainExists) {
                    String message = "Subscription domain " + subscriptionDomain.domainName + " exists";
                    throw new RestAPIException(Status.INTERNAL_SERVER_ERROR, message);
                }
            }

            for (SubscriptionDomainBean subscriptionDomain : request.domains) {
                cartridgeSubsciptionManager.addSubscriptionDomain(tenantId, subscriptionAlias,
                        subscriptionDomain.domainName, subscriptionDomain.applicationContext);
            }
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RestAPIException(e.getMessage(), e);
        }

        StratosAdminResponse stratosAdminResponse = new StratosAdminResponse();
        stratosAdminResponse.setMessage("Successfully added domains to cartridge subscription");
        return stratosAdminResponse;
    }

    public static boolean isSubscriptionDomainExists(ConfigurationContext configurationContext, String cartridgeType,
                                                     String subscriptionAlias, String domain) throws RestAPIException {
        try {
            int tenantId = ApplicationManagementUtil.getTenantId(configurationContext);
            SubscriptionDomainBean subscriptionDomain = PojoConverter
                    .populateSubscriptionDomainPojo(cartridgeSubsciptionManager.getSubscriptionDomain(tenantId,
                            subscriptionAlias, domain));

            if (subscriptionDomain.domainName != null) {
                return true;
            } else {
                return false;
            }
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RestAPIException(e.getMessage(), e);
        }

    }

    public static List<SubscriptionDomainBean> getSubscriptionDomains(ConfigurationContext configurationContext,
                                                                      String cartridgeType,
                                                                      String subscriptionAlias)
            throws RestAPIException {
        try {
            int tenantId = ApplicationManagementUtil.getTenantId(configurationContext);
            return PojoConverter.populateSubscriptionDomainPojos(
                    cartridgeSubsciptionManager.getSubscriptionDomains(tenantId, subscriptionAlias));
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RestAPIException(e.getMessage(), e);
        }
    }


    public static List<SubscriptionDomainBean> getAllSubscriptionDomains() throws RestAPIException {
        TenantManager tenantManager = ServiceHolder.getTenantManager();
        Tenant[] tenants;
        List<SubscriptionDomainBean> subscriptionDomainBeanList = new ArrayList<>();

        try {
            tenants = (Tenant[]) tenantManager.getAllTenants();
        }
        catch (Exception e) {
            String msg = "Error in retrieving the tenant information";
            log.error(msg, e);
            throw new RestAPIException(msg);
        }
        for (Tenant tenant : tenants) {
            try {
                int tenantId = tenant.getId();
                Collection<CartridgeSubscription> cartridgeSubscriptions =
                        dataInsertionAndRetrievalManager.getCartridgeSubscriptions(tenantId);

                if (cartridgeSubscriptions != null) {
                    for (CartridgeSubscription cartridgeSubscription : cartridgeSubscriptions) {
                        List<SubscriptionDomainBean> tenantSubscriptionDomains = PojoConverter
                                .populateSubscriptionDomainPojos(cartridgeSubsciptionManager.getSubscriptionDomains(
                                        tenantId, cartridgeSubscription.getAlias()));
                        subscriptionDomainBeanList.addAll(tenantSubscriptionDomains);
                    }
                }
            }
            catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new RestAPIException(e.getMessage(), e);
            }
        }
        return subscriptionDomainBeanList;
    }

    public static SubscriptionDomainBean getSubscriptionDomain(ConfigurationContext configurationContext,
                                                               String cartridgeType,
                                                               String subscriptionAlias, String domain)
            throws RestAPIException {
        try {
            int tenantId = ApplicationManagementUtil
                    .getTenantId(configurationContext);
            SubscriptionDomainBean subscriptionDomain = PojoConverter.populateSubscriptionDomainPojo(
                    cartridgeSubsciptionManager.getSubscriptionDomain(tenantId,
                            subscriptionAlias, domain));

            if (subscriptionDomain == null) {
                String message = "Could not find a subscription [domain] " + domain + " for Cartridge [type] "
                        + cartridgeType + " and [alias] " + subscriptionAlias;
                log.error(message);
                throw new RestAPIException(Status.NOT_FOUND, message);
            }

            return subscriptionDomain;

        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RestAPIException(e.getMessage(), e);
        }
    }

    public static StratosAdminResponse removeSubscriptionDomain(ConfigurationContext configurationContext,
                                                                String cartridgeType,
                                                                String subscriptionAlias, String domain)
            throws RestAPIException {
        try {
            int tenantId = ApplicationManagementUtil.getTenantId(configurationContext);
            cartridgeSubsciptionManager.removeSubscriptionDomain(tenantId, subscriptionAlias, domain);
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RestAPIException(e.getMessage(), e);
        }

        StratosAdminResponse stratosAdminResponse = new StratosAdminResponse();
        stratosAdminResponse.setMessage("Successfully removed domains from cartridge subscription");
        return stratosAdminResponse;
    }

    private static int getTenantId(String tenantDomain) throws UserStoreException {

        return ServiceHolder.getRealmService().getTenantManager().getTenantId(tenantDomain);
    }

    private static boolean isTenantValid(int tenantId) throws UserStoreException {

        return ServiceHolder.getRealmService().getTenantManager().getTenant(tenantId) != null;
    }

    private static String getTenantAdminUsername(int tenantId) throws UserStoreException {

        return ServiceHolder.getRealmService().getTenantManager().getTenant(tenantId).getRealmConfig()
                .getAdminUserName();
    }

    private static PrivilegedCarbonContext setTenantInfomationToPrivilegedCC(String tenantDomain, int tenantId,
                                                                             String username) {

        // setting the correct tenant info for downstream code..
        PrivilegedCarbonContext privilegedCC = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        privilegedCC.setTenantDomain(tenantDomain);
        privilegedCC.setTenantId(tenantId);
        privilegedCC.setUsername(username);

        return privilegedCC;
    }
}
