import os
from plugins.contracts import ICartridgeAgentPlugin
from modules.util.log import LogFactory
from modules.topology.topologycontext import *
import mdsclient
import plugins.configurator.configurator

class WkaMemberConfigurator(ICartridgeAgentPlugin):

    def __init__(self):
        self.log = None
        self.my_member_id = None

    def publish_metadata(self, properties_data):
        publish_data = mdsclient.MDSPutRequest()
        publish_data.properties = properties_data
        mdsclient.put(publish_data, app=True)

    def add_to_restart_queue(self, member_id):
        data = {"key":"restart","values":member_id}
        self.log.info("Publishing members to be restarted data=%s" % (data))
        self.publish_metadata(data)

    def publish_as_wka_member(self, ip):
        private_ip = ip
        data = {"key":"wka","values":private_ip}
        self.log.info("Publishing wka members data=%s " % (data))
        self.publish_metadata(data)

    def remove_me_from_queue(self):
        self.log.info("Removing me %s from restart queue" % self.my_member_id);
        mdsclient.delete_property_value("restart", self.my_member_id)

    def publish_wka_members(self, service_name, cluster_id):
        topology = TopologyContext.get_topology()
        service = topology.get_service(service_name)
        cluster = service.get_cluster(cluster_id)

        members = cluster.get_members()
        wka_members=[]
        local_member_port=4000
        for member in members:
            if(member.member_id == self.my_member_id):
                self.log.info("My Ips %s , %s" % (member.member_default_private_ip, member.member_default_public_ip))
                self.publish_as_wka_member(member.member_default_private_ip)
            else:
                self.log.info("Other WKA members memberid=%s privateip=%s, public ip=%s " % (member.member_id, member.member_default_private_ip, member.member_default_public_ip))
                wka_members.append(member.member_default_private_ip+':'+str(local_member_port))
                self.add_to_restart_queue(member.member_id)

        #configure me with other wka members
        # remove me from queue if i am there
        local_members = ','.join(map(str, wka_members))
        local_members= "'{}'".format(local_members)
        self.log.info("*** local_members=%s " % (local_members))

        os.environ['STRATOS_MEMBERS'] = str(local_members)
        self.log.info("env local members=%s" % (os.environ.get('STRATOS_MEMBERS')))

        return None, None

    @staticmethod
    def isTrue(str):
        #should be an utility method
        return str.lower() in ("true", "True", "1" , "yes", "Yes")

    def fetch_wka_members(self):
	local_member_port=4000
        mds_response = mdsclient.get(app=True)
        wka_members_ips=[]
        if mds_response is not None:
            wka_members_ips = mds_response.properties.get("wka")

        for wka_member_ip in wka_members_ips:
            self.log.info("WKA members %s=" % wka_member_ip)
            wka_members_ips.append(wka_member_ip +':'+str(local_member_port))

        self.log.info("WKA members %s " % wka_members_ips)

        wka_members_ips = ','.join(map(str, wka_members_ips))
        wka_members_ips= "'{}'".format(wka_members_ips)
        self.log.info("local_members=%s " % (wka_members_ips))

        os.environ['STRATOS_MEMBERS'] = str(wka_members_ips)
        self.log.info("env local members=%s" % (os.environ.get('STRATOS_MEMBERS')))

    def execute_clustring_configurater(self):
        configurator.configure()


    def run_plugin(self, values):
        self.log = LogFactory().get_log(__name__)
        self.log.info("Starting Clustering Configuration")

        clusterId = values['CLUSTER_ID']
        self.log.info("CLUSTER_ID %s" % clusterId)

        service_name = values['SERVICE_NAME']
        self.log.info("SERVICE_NAME %s" % service_name)

        cluering_type = values['CLUSTERING_TYPE']
        self.log.info("CLUSTERING_TYPE %s" % cluering_type)

        is_wka_member = values['WKA_MEMBER']
        self.log.info("WKA_MEMBER %s" % is_wka_member)

        self.my_member_id = values['MEMBER_ID']
        self.log.info("MEMBER_ID %s" % self.my_member_id)

        sub_domain = values['SUB_DOMAIN']
        sub_domain= "'{}'".format(sub_domain)
        self.log.info("SUB_DOMAIN %s" % (sub_domain))

        os.environ['STRATOS_SUB_DOMAIN'] = str(sub_domain)
        self.log.info("env clustering  SUB_DOMAIN=%s" % (os.environ.get('SUB_DOMAIN','worker')))

        if WkaMemberConfigurator.isTrue(is_wka_member):
            self.log.info("This is a WKA member")
            self.remove_me_from_queue()
            self.publish_wka_members(service_name, clusterId)
        else:
            self.log.info("This is not a WKA member")
            self.fetch_wka_members()

        self.execute_clustring_configurater()

