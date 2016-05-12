/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.carbon.membership.scheme.kubernetes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.config.Config;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.HazelcastInstance;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.axis2.description.Parameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.stratos.messaging.domain.topology.Cluster;
import org.apache.stratos.messaging.domain.topology.KubernetesService;
import org.apache.stratos.messaging.message.receiver.topology.TopologyEventReceiver;
import org.apache.stratos.messaging.message.receiver.topology.TopologyManager;
import org.wso2.carbon.membership.scheme.kubernetes.api.KubernetesApiEndpoint;
import org.wso2.carbon.membership.scheme.kubernetes.domain.*;
import org.wso2.carbon.membership.scheme.kubernetes.exceptions.KubernetesMembershipSchemeException;
import org.wso2.carbon.utils.xml.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MesosBasedKubernetesMembershipScheme extends KubernetesMembershipScheme {

    private static final Log log = LogFactory.getLog(MesosBasedKubernetesMembershipScheme.class);
    private static final String HZ_CLUSTERING_PORT_MAPPING_NAME = "hz-clustering";
    private static final String POD_KIND = "Pod";

    private boolean shuttingDown;
    // this node's member id
    private String memberId;
    private static final String PARAMETER_NAME_CLUSTER_IDS = "CLUSTER_IDS";
    private static final String PODS_API_CONTEXT = "/api/v1/namespaces/%s/pods/";
    private static final String PARAMETER_NAME_MEMBER_ID = "MEMBER_ID";

    public MesosBasedKubernetesMembershipScheme(Map<String, Parameter> parameters, String primaryDomain,
                                                Config config, HazelcastInstance primaryHazelcastInstance,
                                                List<ClusteringMessage> messageBuffer) {
        super(parameters, primaryDomain, config, primaryHazelcastInstance, messageBuffer);
    }

    @Override
    public void init() throws ClusteringFault {
        try {
            log.info("Initializing Mesos based Kubernetes membership scheme...");

            nwConfig.getJoin().getMulticastConfig().setEnabled(false);
            nwConfig.getJoin().getAwsConfig().setEnabled(false);
            TcpIpConfig tcpIpConfig = nwConfig.getJoin().getTcpIpConfig();
            tcpIpConfig.setEnabled(true);

            // Try to read parameters from env variables
            String kubernetesMaster = System.getenv(KubernetesMembershipSchemeConstants.PARAMETER_NAME_KUBERNETES_MASTER);
            String kubernetesNamespace = System.getenv(KubernetesMembershipSchemeConstants.PARAMETER_NAME_KUBERNETES_NAMESPACE);
            String kubernetesMasterUsername = System.getenv(KubernetesMembershipSchemeConstants.PARAMETER_NAME_KUBERNETES_MASTER_USERNAME);
            String kubernetesMasterPassword = System.getenv(KubernetesMembershipSchemeConstants.PARAMETER_NAME_KUBERNETES_MASTER_PASSWORD);
            String skipMasterVerificationValue = System.getenv(KubernetesMembershipSchemeConstants.PARAMETER_NAME_KUBERNETES_MASTER_SKIP_SSL_VERIFICATION);
            String clusterIds = System.getenv(PARAMETER_NAME_CLUSTER_IDS);
            memberId = System.getenv(PARAMETER_NAME_MEMBER_ID);

            // If not available read from clustering configuration
            if(StringUtils.isEmpty(kubernetesMaster)) {
                kubernetesMaster = getParameterValue(KubernetesMembershipSchemeConstants.PARAMETER_NAME_KUBERNETES_MASTER);
                if(StringUtils.isEmpty(kubernetesMaster)) {
                    throw new ClusteringFault("Kubernetes master parameter not found");
                }
            }
            if(StringUtils.isEmpty(kubernetesNamespace)) {
                kubernetesNamespace = getParameterValue(KubernetesMembershipSchemeConstants.PARAMETER_NAME_KUBERNETES_NAMESPACE, "default");
            }

            if(StringUtils.isEmpty(kubernetesMasterUsername)) {
                kubernetesMasterUsername = getParameterValue(KubernetesMembershipSchemeConstants.PARAMETER_NAME_KUBERNETES_MASTER_USERNAME, "");
            }

            if(StringUtils.isEmpty(kubernetesMasterPassword)) {
                kubernetesMasterPassword = getParameterValue(KubernetesMembershipSchemeConstants.PARAMETER_NAME_KUBERNETES_MASTER_PASSWORD, "");
            }

            if (StringUtils.isEmpty(skipMasterVerificationValue)){
                skipMasterVerificationValue = getParameterValue(KubernetesMembershipSchemeConstants.PARAMETER_NAME_KUBERNETES_MASTER_SKIP_SSL_VERIFICATION, "false");
            }

            skipMasterSSLVerification = Boolean.parseBoolean(skipMasterVerificationValue);

            log.info(String.format("Mesos kubernetes clustering configuration: [master] %s [namespace] %s  [skip-master-ssl-verification] %s",
                    kubernetesMaster, kubernetesNamespace, skipMasterSSLVerification));

            if (StringUtils.isEmpty(clusterIds)) {
                clusterIds = getParameterValue(PARAMETER_NAME_CLUSTER_IDS);
            }

            if(clusterIds == null) {
                throw new RuntimeException(PARAMETER_NAME_CLUSTER_IDS + " parameter not found");
            }

            if (memberId == null) {
                throw new RuntimeException(PARAMETER_NAME_MEMBER_ID + " parameter not found in " +
                        "System parameters");
            }
            String[] clusterIdArray = clusterIds.split(",");

            if (!waitForTopologyInitialization()) {
                return;
            }

            List<KubernetesService> kubernetesServices = new ArrayList<>();

            try {
                TopologyManager.acquireReadLock();
                for (String clusterId : clusterIdArray) {
                    org.apache.stratos.messaging.domain.topology.Cluster cluster =
                            TopologyManager.getTopology().getCluster(clusterId.trim());
                    if (cluster == null) {
                        throw new RuntimeException("Cluster not found in topology: [cluster-id]" + clusterId);
                    }

                    if (cluster.isKubernetesCluster()) {
                        log.info("Reading Kubernetes services of cluster: [cluster-id] " + clusterId);
                        kubernetesServices.addAll(getKubernetesServicesOfCluster(cluster));
                    } else {
                        log.info("Cluster " + clusterId + " is not a Kubernetes cluster");
                    }
                }
            } finally {
                TopologyManager.releaseReadLock();
            }

            for (KubernetesService k8sService : kubernetesServices) {
                // check if the Service is related to clustering, by checking if the service name
                // is equal to the port mapping name. Only that particular Service will be selected
                if (HZ_CLUSTERING_PORT_MAPPING_NAME.equalsIgnoreCase(k8sService.getPortName())) {
                    log.info("Found the relevant Service [ " + k8sService.getId() + " ] for the " +
                            "port mapping name: " + HZ_CLUSTERING_PORT_MAPPING_NAME);
                    log.info("Kubernetes service: " +  k8sService.getId() + ", clustering port: " + k8sService.getContainerPort());
                    List<String> hostIPandPortTuples = findHostIPandPortTuples(kubernetesMaster,
                            kubernetesNamespace, k8sService.getId(), kubernetesMasterUsername,
                            kubernetesMasterPassword, k8sService.getContainerPort());
                    for (String hostIPandPortTuple : hostIPandPortTuples) {
                        tcpIpConfig.addMember(hostIPandPortTuple);
                        log.info("Member added to cluster configuration: [host-ip,host-port] " + hostIPandPortTuple);
                    }
                }
            }
            log.info("Mesos based Kubernetes membership scheme initialized successfully");
        } catch (Exception e) {
            log.error(e);
            throw new ClusteringFault("Mesos based Kubernetes membership initialization failed", e);
        }
    }

    /**
     * Returns the Kubernetes Services of the given cluster
     *
     * @param cluster Cluster object
     * @return Kubernetes Service List if exists, else null
     */
    private List<KubernetesService> getKubernetesServicesOfCluster (Cluster cluster) {
        return cluster.getKubernetesServices();
    }

    /**
     * Wait until the Topology is initialized
     *
     * @return true if the topology is initialized
     */
    private boolean waitForTopologyInitialization() {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        TopologyEventReceiver topologyEventReceiver = new TopologyEventReceiver();
        topologyEventReceiver.setExecutorService(executorService);
        topologyEventReceiver.execute();
        if (log.isInfoEnabled()) {
            log.info("Topology receiver thread started");
        }

        final Thread currentThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                shuttingDown = true;
                try {
                    currentThread.join();
                } catch (InterruptedException ignore) {
                }
            }
        });

        log.info("Waiting for topology to be initialized...");
        while (!TopologyManager.getTopology().isInitialized()) {
            try {
                if(shuttingDown) {
                    return false;
                }
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {
                return false;
            }
        }
        log.info("Topology initialized");
        return true;
    }

    /**
     * Finds the relevant host ips and port, which is mapping to the pods that should be clustered
     *
     * @param kubernetesMaster K8s master url/hostname
     * @param namespace K8s namespace
     * @param serviceName K8s service name to get the pods from
     * @param username K8s API username (if secured)
     * @param password K8s API password (if secured)
     * @param definedClusteringPort port defined for clustering in the port mappings for the
     *                              cartridge
     * @return List of ip:port tuples
     * @throws KubernetesMembershipSchemeException
     */
    private List<String> findHostIPandPortTuples(String kubernetesMaster, String namespace, String
            serviceName, String username, String password, int definedClusteringPort)
            throws KubernetesMembershipSchemeException {

        List<String> hostIpPortTuples = new ArrayList<>();

        // get the pod names
        Set<String> podNames = getPodNamesForService(kubernetesMaster, namespace, serviceName, username, password);

        if (podNames.isEmpty()) {
            log.warn("No pod names were found for Service: " + serviceName);
        }

        // for each pod name, get the mesos host IP, port tuple
        for (String podName : podNames) {
            String hostIpPortTuple = getHostIpPortTupleForPod(kubernetesMaster, namespace, podName,
                    username, password, definedClusteringPort);
            if (hostIpPortTuple != null) {
                hostIpPortTuples.add(hostIpPortTuple);
                log.info("Host Ip and Port " + hostIpPortTuple + " added to host Ips list");
            }
        }

        return hostIpPortTuples;
    }

    /**
     * Finds a single ip and port tuple for a given pod
     *
     * @param kubernetesMaster K8s master url/hostname
     * @param namespace K8s namespace
     * @param podName K8s pod name
     * @param username K8s API username (if secured)
     * @param password K8s API password (if secured)
     * @param definedClusteringPort port defined for clustering in the port mappings for the
     *                              cartridge
     *
     * @return name of ip:port tuple for this pod
     * @throws KubernetesMembershipSchemeException
     */
    private String getHostIpPortTupleForPod(String kubernetesMaster, String namespace, String podName,
                                            String username, String password, int definedClusteringPort)
            throws KubernetesMembershipSchemeException {
        // use the Pods API to get the ip of the host machine
        final String apiContext = String.format(PODS_API_CONTEXT, namespace);

        // Create k8s api endpoint URL
        URL podUrl = createUrl(kubernetesMaster, apiContext + podName);

        // Create http/https k8s api endpoint
        KubernetesApiEndpoint apiEndpoint = createAPIEndpoint(podUrl);

        // Create the connection and read k8s service endpoints
        Pod pod;
        try {
            pod = getPod(connectAndRead(apiEndpoint, username, password));

        } catch (IOException e) {
            throw new KubernetesMembershipSchemeException("Could not get the Endpoints", e);
        } finally {
            apiEndpoint.disconnect();
        }

        log.info("Status of the Pod: " + podName + " -> phase: " + pod.getStatus().getPhase() +
                ", host IP: " + pod.getStatus().getHostIP());

        String exposedClusteringPortByHost;
        try {
            // get the port by manually parsing annotations section; this is done to avoid the
            // complexity of a custom deserializer
            exposedClusteringPortByHost = getExposedClusteringPort(connectAndRead(apiEndpoint,
                    username, password), Integer.toString(definedClusteringPort));
            if (exposedClusteringPortByHost == null) {
                throw new KubernetesMembershipSchemeException("Unable to find clustering port for pod: " + podName);
            }
        } finally {
            apiEndpoint.disconnect();
        }

        // set host machine IP as the public address and port of nwConfig,
        // if the pod with name 'podName' is relevant to this JVM
        if (memberId.equals(pod.getMetadata().getAnnotations().getMemberId())) {
            log.info("Pod name relevant to this member: " + podName + ", setting host IP " + pod
                    .getStatus().getHostIP() + " as the public address");
            nwConfig.setPublicAddress(pod.getStatus().getHostIP());
            nwConfig.setPort(Integer.parseInt(exposedClusteringPortByHost.trim()));
        }
        return pod.getStatus().getHostIP() + ":" + exposedClusteringPortByHost.trim();
    }

    /**
     * Uses a regex to find the mapped port in the host for the port mapping for clustering port
     * defined in cartridge definition
     *
     * @param inputStream input stream to match the regex against
     * @param definedClusteringPort the defined clustering port in port mapping to be used in the
     *                              regex
     * @return port exposed by host if found, else null
     */
    private String getExposedClusteringPort(InputStream inputStream, String definedClusteringPort) {
        // parses the json manually, not good :(
        String regex = "(port_TCP_"+ definedClusteringPort +"\":)(\\s*)(\")(\\d*)(\",)";
        //String regex = "(portName_TCP_http-" + definedClusteringPort + "\":)(\\s*)(\")(\\d*)(\",)";
        Pattern r = Pattern.compile(regex);
        Matcher m = r.matcher(new Scanner(inputStream).useDelimiter("\\Z").next());
        if (m.find( )) {
            String matchingValue = m.group(4);
            log.info("Found matching value for regex [ " + regex + " ]: " + matchingValue);
            return matchingValue;
        } else {
            log.info("No matching value for regex [ " + regex + " ]: was found!");
            return null;
        }
    }

    protected Pod getPod (InputStream inputStream) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(inputStream, Pod.class);
    }

    /**
     * Finds the pod names for a given K8s Service name
     *
     * @param kubernetesMaster K8s master url/hostname
     * @param namespace K8s namespace
     * @param serviceName K8s service name to get the pods from
     * @param username K8s API username (if secured)
     * @param password K8s API password (if secured)
     *
     * @return Set of Pod names corresponding to the Service name specified
     * @throws KubernetesMembershipSchemeException
     */
    private Set<String> getPodNamesForService(String kubernetesMaster, String namespace, String serviceName,
                                              String username, String password) throws KubernetesMembershipSchemeException {

        // use the Endpoints API to get the pod name
        final String apiContext = String.format(KubernetesMembershipSchemeConstants.ENDPOINTS_API_CONTEXT, namespace);
        final Set<String> podNames = new HashSet<>();

        // Create k8s api endpoint URL
        URL apiEndpointUrl = createUrl(kubernetesMaster, apiContext + serviceName);

        // Create http/https k8s api endpoint
        KubernetesApiEndpoint apiEndpoint = createAPIEndpoint(apiEndpointUrl);

        // Create the connection and read k8s service endpoints
        Endpoints endpoints = null;
        try {
            endpoints = getEndpoints(connectAndRead(apiEndpoint, username, password));

        } catch (IOException e) {
            throw new KubernetesMembershipSchemeException("Could not get the Endpoints", e);

        } finally {
            apiEndpoint.disconnect();
        }

        if (endpoints != null) {
            if (endpoints.getSubsets() != null && !endpoints.getSubsets().isEmpty()) {
                for (Subset subset : endpoints.getSubsets()) {
                    for (Address address : subset.getAddresses()) {
                        if (address.getTargetRef() != null) {
                            if (POD_KIND.equalsIgnoreCase(address.getTargetRef().getKind())) {
                                podNames.add(address.getTargetRef().getName());
                                log.info("Added pod: " + address.getTargetRef().getName() + " for service: "+ serviceName);
                            } else {
                                log.info("TargetRef is not of Pod type for address " + address
                                        .getIp() + ", type found: " + address.getTargetRef().getKind());
                            }
                        } else {
                            log.info("TargetRef is null for address " + address.getIp());
                        }
                    }
                }
            }
        } else {
            log.info("No endpoints found at " + apiEndpointUrl.toString());
        }

        return podNames;
    }
}
