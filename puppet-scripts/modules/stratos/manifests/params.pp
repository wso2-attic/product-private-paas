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

#    $domain               = 'example.com'
#    $package_repo         = "http://downloads.${domain}"
#    $depsync_svn_repo     = "https://svn.${domain}/wso2/repo/"
#    $local_package_dir    = '/mnt/packs'
#    $log_path             = '/var/log/apache-stratos'
#
## IP addresses 
#
#    $puppetmaster_ip = "10.33.14.2"
#    $downloads_ip    = "10.33.14.2"
#    $svn_ip          = "10.33.14.2"
#    $sc_ip           = "10.33.14.16"
#    $cc_ip           = "10.33.14.27"
#    $agent_ip        = "10.33.14.22"
#    $elb_ip          = "10.33.14.20"
#    $mb_ip           = "10.33.14.11"
#    $git_ip          = "10.33.14.27"
#    $cassandra_ip    = "10.33.14.23"
#    $bam_ip          = "10.33.14.25"
#    $mysql_ip        = "10.33.14.28"
#
## MySQL server configuration details
#    $mysql_server         = "mysql.${domain}"
#    $mysql_port           = '3306'
#
#    $billing_db_server    = "mysql.${domain}"
#    $billing_db_port      = '3306'
#
#    $max_connections      = '100000'
#    $max_active           = '150'
#    $max_wait             = '360000'
#
## Database details
#    $registry_user        = 'registry'
#    $registry_password    = 'registry'
#    $registry_database    = 'governance'
#
#    $userstore_user       = 'userstore'
#    $userstore_password   = 'userstore'
#    $userstore_database   = 'userstore'
#
#    $billing_db_user      = 'billing'
#    $billing_db_password  = 'billing'
#    $billing_database     = 'billing'
#
## ELB configuration
#    $elb_hostname              ="elb.${domain}"
#    $enable_autoscaler         =true
#    $enable_embedded_autoscaler=false
#    $elb_port_offset           =0
#    $elb_port                  ="9443"
#
## MB configuration
#    $mb_hostname="mb.${domain}"
#    $mb_https_port=""
#    $mb_listen_port="5677"
#
## SC configuration
#    $stratos_foundation_db="stratos_foundation"
#    $stratos_foundation_db_user="scdbuser"
#    $stratos_foundation_db_pass="scdbpassword"
#    $sc_https_port="9445"
#    $sc_http_port="9765"
#    $sc_port_offset=2
#    $sc_hostname="sc.${domain}"
#    $cassandra_port="9163"
#    $keypair_path="/home/ubuntu/sshkey.pem"
#
#
## CC configuration
#    $cc_https_port="9444"
#    $cc_port_offset=1
#    $cc_hostname="cc.${domain}"
#    $mb_cassandra_host="cassandra.${domain}"
#    $mb_cassandra_port="9161"
#
## Agent configuration
#    $agent_clustering_port="4025"
#    $agent_hostname="agent.${domain}"
#    $agent_http_port="9767"
#    $agent_https_port="9447"
#    $agent_port_offset=4
#
#
## BAM configuration
#    $bam_port_offset=3
#    $bam_port="9446"
#    $bam_receiver_port="7614"
#    $bam_receiver_secured_port="7714"
#    $bam_hostname="bam.${domain}"
#
#
## IaaS Providers
## Set <iaas-name>_provider_enabled parameter to true to enable desired IaaS. A hybrid cloud could be
## created using multiple IaaSs.
#    $iaas_provider                      ='ec2' # values {ec2, openstack}
##    $ec2_provider_enabled		='true'
##    $openstack_provider_enabled		='false'
#
## EC2
#    $ec2_identity			="ec2_identity"
#    $ec2_credential			="ec2_credential"
#    $ec2_keypair_name			="ec2_keypair_name"
#    $ec2_owner_id			="ec2_owner_id"
#    $ec2_scaleup_order			=1
#    $ec2_scaledown_order		=2
#    $ec2_region				="ap-southeast-1"
#    $ec2_availability_zone		="ec2_availability_zone"
#    $ec2_security_groups		="ec2_security_groups"
#    $ec2_instance_type			="m1.large"
#    $ec2_image_id			="ami-0g7532f4"
#    $ec2_php_cartridge_image_id		="ami-8a733bd8"
#    $ec2_mysql_cartridge_image_id	="ami-3e753d6c"
#    $ec2_tomcat_cartridge_image_id	="ami-6484cc36"
#
## Openstack
#    $openstack_identity			="stratos:stratos" # Openstack project name:Openstack login user
#    $openstack_credential		="password" # Openstack login password
#    $openstack_tenant			="stratos" # openstack project name
#    $openstack_project_id 		=$openstack_tenant
#    $openstack_jclouds_endpoint		="http://hostname:5000/"
#    $openstack_scaleup_order		=2
#    $openstack_scaledown_order		=3
#    $openstack_keypair_name		="mykey"
#    $openstack_image_id			="RegionOne/" #No need to change this as of now
#    $nova_region			="RegionOne"
#    $openstack_instance_type_tiny	="RegionOne\/1"
#    $openstack_instance_type_small	="RegionOne\/2"
#    $openstack_security_groups		="security-groups"
#    $openstack_php_cartridge_image_id	="omi-3e753d6c"
#    $openstack_mysql_cartridge_image_id	="omi-3e753d6c"
#    $openstack_tomcat_cartridge_image_id="omi-3e753d6c"
#
## Cassandra configuration
#    $cassandra_port1=9160
#    $cassandra_port2=7000
#
## Hadoop configuration
#    $hadoop_port1=5140
#    $hadoop_port2=9000
#    $sc_cluster_port="5001"
#    $elb_cluster_port="4000"
#
## Git repo cofiguration. 
#    $git_user="git"
#    $email="git@${domain}"
#    $git_hostname="git.${domain}"
#


}

