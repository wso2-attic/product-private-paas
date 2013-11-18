#
# Class sc::params
#
# This class manages sc parameters
#
# Parameters:
#
# Usage: Uncomment the variable and assign a value to override the example.pp value
#
#

class stratos::params {

    $domain               = 'example.com'
    $package_repo         = "http://downloads.${domain}"
    $depsync_svn_repo     = "https://svn.${domain}/wso2/repo/"
    $local_package_dir    = '/mnt/packs'
    $log_path             = '/var/log/apache-stratos'

# MySQL server configuration details
    $mysql_server         = "mysql.${domain}"
    $mysql_port           = '3306'

    $billing_db_server    = "mysql.${domain}"
    $billing_db_port      = '3306'

    $max_connections      = '100000'
    $max_active           = '150'
    $max_wait             = '360000'

# Database details
    $registry_user        = 'registry'
    $registry_password    = 'registry'
    $registry_database    = 'governance'

    $userstore_user       = 'userstore'
    $userstore_password   = 'userstore'
    $userstore_database   = 'userstore'

    $billing_db_user      = 'billing'
    $billing_db_password  = 'billing'
    $billing_database     = 'billing'

# ELB configuration
    $elb_hostname="elb.${domain}"
    $enable_autoscaler=true
    $enable_embedded_autoscaler=false
    $elb_port_offset=0
    $elb_port="9443"

# MB configuration
    $mb_hostname="mb.${domain}"
    $mb_https_port=""
    $mb_listen_port="5677"

# SC configuration
    $stratos_foundation_db="stratos_foundation"
    $stratos_foundation_db_user="scdbuser"
    $stratos_foundation_db_pass="scdbpassword"
    $sc_https_port="9445"
    $sc_http_port="9765"
    $sc_port_offset=2
    $sc_hostname="sc.${domain}"
    $elb_ip="elb ip" ##############
    $cassandra_port="9163"
    $sc_ip=$hostip #############
    $agent_ip="agent ip" ############
    $keypair_path="<keypair_path>" #########


# CC configuration
    $cc_https_port="9444"
    $cc_port_offset=1
    $cc_hostname="cc.${domain}"
    $mb_cassandra_host="cassandra.${domain}"
    $mb_cassandra_port="9161"

# Agent configuration
    $agent_clustering_port="4025"
    $agent_hostname="agent.${domain}"
    $agent_http_port="9767"
    $agent_https_port="9447"
    $agent_port_offset=4


# BAM configuration
    $bam_port_offset=3
    $bam_port="9446"
    $bam_ip=$hostip
    $bam_receiver_port="7614"
    $bam_receiver_secured_port="7714"
    $bam_hostname="bam.${domain}"
    $bam_path="wso2bam-2.3.0"
    $bam_pack="wso2bam-2.3.0.zip"


# IaaS Providers
# Set <iaas-name>_provider_enabled parameter to true to enable desired IaaS. A hybrid cloud could be
# created using multiple IaaSs.

# EC2
    $ec2_provider_enabled=false
    $ec2_identity="<ec2_identity>"
    $ec2_credential="<ec2_credential>"
    $ec2_keypair_name="<ec2_keypair_name>"
    $ec2_owner_id="<ec2_owner_id>"
    $ec2_scaleup_order=1
    $ec2_scaledown_order=2
    $ec2_region="ap-southeast-1"
    $ec2_availability_zone="<ec2_availability_zone>"
    $ec2_security_groups="<ec2_security_groups>"
    $ec2_instance_type="m1.large"
    $ec2_image_id=""
    $ec2_php_cartridge_image_id="ami-8a733bd8"
    $ec2_mysql_cartridge_image_id="ami-3e753d6c"
    $ec2_tomcat_cartridge_image_id="ami-6484cc36"

# Openstack
    $openstack_provider_enabled=true
    $openstack_identity="stratos:stratos" # Openstack project name:Openstack login user
    $openstack_credential="password" # Openstack login password
    $openstack_tenant="stratos" # openstack project name
    $openstack_project_id=$openstack_tenant
    $openstack_jclouds_endpoint="http://hostname:5000/v2.0"
    $openstack_scaleup_order=2
    $openstack_scaledown_order=3
    $openstack_keypair_name=""
    $openstack_image_id="RegionOne/" #No need to change this as of now
    $nova_region="RegionOne"
    $openstack_instance_type_tiny="RegionOne\/1"
    $openstack_instance_type_small="RegionOne\/2"
    $openstack_security_groups="security-groups"
    $openstack_php_cartridge_image_id=""
    $openstack_mysql_cartridge_image_id=""
    $openstack_tomcat_cartridge_image_id=""

# Cassandra configuration
    $cassandra_port1=9160
    $cassandra_port2=7000

# Hadoop configuration
    $hadoop_port1=5140
    $hadoop_port2=9000
    $sc_cluster_port="5001"
    $elb_cluster_port="4000"

# Git repo cofiguration. 
    $git_user="git"
    $email="git@${domain}"
    $git_hostname="git.${domain}"
    $git_ip=$hostip ################
#$axis2c_path="axis2c-1.6.2"  #########
#$axis2c_pack="axis2-1.6.2-bin.zip" ##########

}

