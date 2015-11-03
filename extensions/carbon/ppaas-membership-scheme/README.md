# Private PaaS Membership Scheme for Carbon Cluster Discovery

Private PaaS membership scheme is a Carbon membership scheme implemented for discovering the Hazelcast cluster
of a Carbon server via the topology.

### How it works
Once a Carbon server starts it will wait until the topology gets initialized via the message broker. Then it will
query the member IP addresses in the given cluster in the topology. Thereafter Hazelcast network configuration 
will be initialized with the above IP addresses. As a result the above Hazelcast instance will get connected all 
the other members in the cluster. In addition once a new member is added to the cluster, all the other members
will get connected to the new member.

### Installation

1. Apply Carbon kernel patch0012. This includes a modification in the Carbon Core component for
allowing to add third party membership schemes.

2. Copy following JAR files to the dropins directory of the Carbon server:

```
activemq_client_5.10.0_1.0.0.jar
geronimo_j2ee_management_1.1_spec_1.0.1_1.0.0.jar
hawtbuf_1.9_1.0.0.jar
org.apache.commons.lang3_3.1.0.jar
org.apache.stratos.common-4.1.1.jar
org.apache.stratos.messaging-4.1.1.jar
private-paas-membership-scheme-4.1.1.jar
```

3. Update axis2.xml with the following configuration, cluster id parameter need to contain the
cluster id of the relevant carbon server cluster:


```
<clustering class="org.wso2.carbon.core.clustering.hazelcast.HazelcastClusteringAgent"
                enable="true">

    <parameter name="membershipScheme">private-paas</parameter>
    <parameter name="membershipSchemeClassName">org.wso2.carbon.ppaas.PrivatePaaSBasedMembershipScheme</parameter>
    <parameter name="clusterIds">cluster-1,cluster-2</parameter>  
</clustering>
```
