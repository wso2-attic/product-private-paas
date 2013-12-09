#
#
#
#

node configs {

    $domain               = 'example.com'
    $package_repo         = "http://downloads.${domain}"
#    $depsync_svn_repo     = "https://svn.${domain}/wso2/repo/"
    $local_package_dir    = '/mnt/packs'
    $log_path             = '/var/log/apache-stratos'

# IP addresses 

    $puppetmaster_ip = "10.33.14.20"
    $downloads_ip    = "10.33.14.21"
#    $svn_ip          = "10.33.14.2"
    $sc_ip           = "10.33.14.11"
    $cc_ip           = "10.33.14.22"
    $agent_ip        = "10.33.14.25"
    $elb_ip          = "10.33.14.23"
    $mb_ip           = "10.33.14.21"
    $git_ip          = "10.33.14.20"
    $cassandra_ip    = "127.0.0.1"
    $bam_ip          = "127.0.0.1"
    $mysql_ip        = "10.33.14.20"

# Port offsets
    $elb_port_offset	=0
    $cc_port_offset	=1
    $sc_port_offset	=2
    $bam_port_offset	=3
    $agent_port_offset	=4
    $mb_port_offset     =5

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
    $registry_database    = 'registry'

    $userstore_user       = 'userstore'
    $userstore_password   = 'userstore'
    $userstore_database   = 'userstore'

    $billing_db_user      = 'billing'
    $billing_db_password  = 'billing'
    $billing_database     = 'billing'

# ELB configuration
    $elb_hostname              ="elb.${domain}"
    $enable_autoscaler         =true
    $enable_embedded_autoscaler=false
    $elb_port                  ="9443"

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
    $sc_hostname="sc.${domain}"
    $cassandra_port="9163"
    $keypair_path="/home/ubuntu/stratos.pem"


# CC configuration
    $cc_https_port="9444"
    $cc_hostname="cc.${domain}"
    $mb_cassandra_host="cassandra.${domain}"
    $mb_cassandra_port="9161"

# Agent configuration
    $agent_clustering_port="4025"
    $agent_hostname="agent.${domain}"
    $agent_http_port="9767"
    $agent_https_port="9447"


# BAM configuration
    $bam_port="9446"
    $bam_receiver_port="7614"
    $bam_receiver_secured_port="7714"
    $bam_hostname="bam.${domain}"


# IaaS Providers
# Set <iaas-name>_provider_enabled parameter to true to enable desired IaaS. A hybrid cloud could be
# created using multiple IaaSs.
    $iaas_provider			='openstack' # values {ec2, openstack}
#    $ec2_provider_enabled		='true'
#    $openstack_provider_enabled		='true'

# EC2
    $ec2_identity			="ec2_identity"
    $ec2_credential			="ec2_credential"
    $ec2_keypair_name			="ec2_keypair_name"
    $ec2_owner_id			="ec2_owner_id"
    $ec2_scaleup_order			=1
    $ec2_scaledown_order		=2
    $ec2_region				="ap-southeast-1"
    $ec2_availability_zone		="ec2_availability_zone"
    $ec2_security_groups		="ec2_security_groups"
    $ec2_instance_type			="m1.large"
    $ec2_image_id			="ami-0g7532f4"
    $ec2_php_cartridge_image_id		="ami-8a733bd8"
    $ec2_mysql_cartridge_image_id	="ami-3e753d6c"
    $ec2_tomcat_cartridge_image_id	="ami-6484cc36"

# Openstack
    $openstack_identity			="stratos:stratos" # Openstack project name:Openstack login user
    $openstack_credential		="password" # Openstack login password
    $openstack_tenant			="stratos" # openstack project name
    $openstack_project_id 		="${openstack_tenant}"
    $openstack_jclouds_endpoint		="http://192.168.16.252:5000/v2.0"
    $openstack_jclouds_endpoint_version ='2.0'
    $openstack_scaleup_order		=2
    $openstack_scaledown_order		=3
    $openstack_keypair_name		="stratos"
    $openstack_image_id			="RegionOne/" #No need to change this as of now
    $nova_region			="RegionOne"
    $openstack_instance_type_tiny	="RegionOne/1"
    $openstack_instance_type_small	="RegionOne/2"
    $openstack_security_groups		="default"
    $openstack_php_cartridge_image_id	="858b2117-ee54-42cc-ab0b-9d0f176729f0"
    $openstack_mysql_cartridge_image_id	="9e104e9d-2b0a-4b48-b72f-35a65e92ac7d"
    $openstack_tomcat_cartridge_image_id="4c04d085-2252-4478-af5a-a7439e69a6c6"

# Cassandra configuration
    $cassandra_port1=9160
    $cassandra_port2=7000

# Hadoop configuration
    $hadoop_port1=5140
    $hadoop_port2=9000
    $sc_cluster_port="5001"
    $elb_cluster_port="4000"

# Git repo cofiguration. 
    $git_user		= "git"
    $email		= "git@${domain}"
    $git_hostname	= "git.${domain}"
    $gitblit_http_port 	= "10080"
    $gitblit_https_port = "10443"
    $gitblit_repo_url   = "http://${git_hostname}:${gitblit_http_port}/git/stratosrepo.git"

}

node base inherits configs{

    file {
        "/etc/hosts":
        owner   => root,
        group   => root,
        mode    => 775,
        content => template("hosts.erb"),
    }
    
    file {
	"/etc/environment":
	owner   => root,
        group   => root,
        mode    => 775,
	content => 'PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/games:/opt/java/bin
JAVA_HOME="/opt/java"'
    }
}


node /puppetmaster.*/ inherits base {
    class { 'gitblit':
        version            => '1.3.2',
        maintenance_mode   => 'refresh',
        owner              => 'root',
        group              => 'root',
        target             => '/mnt',
    }
}

node /node002.*/ inherits base {
# https://github.com/imesh/apache-stratos-tomcat-applications.git
    class { 'broker':
        version            => '2.1.0',
        offset             => "${mb_port_offset}",
        maintenance_mode   => 'refresh',
        owner              => 'root',
        group              => 'root',
        target             => '/mnt',
    }
    
}

node /node003.*/ inherits base {

    class {'stratos::sc':
        version          => '3.0.0-incubating',
        offset           => "${sc_port_offset}",
        maintenance_mode => 'refresh',
        auto_scaler      => 'false',
        auto_failover    => false,
        owner            => 'root',
        group            => 'root',
        target           => '/mnt',
    }

}

node /node004.*/ inherits base {

    class {'stratos::cc':
        version          => '3.0.0-incubating',
        offset           => "${cc_port_offset}",
        maintenance_mode => 'refresh',
        auto_scaler      => 'false',
        auto_failover    => false,
        owner            => 'root',
        group            => 'root',
        target           => '/mnt',
    }

}

node /node005.*/ inherits base {

    class {'stratos::elb':
        version          => '3.0.0-incubating',
        offset           => "${elb_port_offset}",
        maintenance_mode => 'refresh',
        auto_scaler      => 'false',
        auto_failover    => false,
        target           => '/mnt',
    }
}

node /node006.*/ inherits base {

    class {'stratos::agent':
        version          => '3.0.0-incubating',
        offset           => "${agent_port_offset}",
        maintenance_mode => 'refresh',
        auto_scaler      => 'false',
        auto_failover    => false,
        owner            => 'root',
        group            => 'root',
        target           => '/mnt',
    }

}

#node /node007.*/ inherits base {
#
#    class { 'bam':
#        version            => '2.3.0',
#        offset           => "${bam_port_offset}",
#        maintenance_mode   => 'refresh',
#        owner              => 'root',
#        group              => 'root',
#        target             => '/mnt',
#    }
#    
#}
#
