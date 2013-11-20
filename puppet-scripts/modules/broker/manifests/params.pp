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

class broker::params {

    $domain               = 'example.com'
    $package_repo         = "http://downloads.${domain}"
    $depsync_svn_repo     = "https://svn.${domain}/wso2/repo/"
    $local_package_dir    = '/mnt/packs'
    $log_path             = '/var/log/apache-stratos'


# Service subdomains
    $mb_subdomain         = 'mb'
    $management_subdomain = 'management'

    $mb_listen_port       = "5677"
    
    $admin_username       = 'admin'
    $admin_password       = 'admin123'

# MySQL server configuration details
    $mysql_server         = "mysql.${domain}"
    $mysql_port           = '3306'
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

# Depsync settings
    $svn_user             = 'wso2'
    $svn_password         = 'wso2123'

#LDAP settings 
    $ldap_connection_uri      = 'ldap://localhost:10389'
    $bind_dn                  = 'uid=admin,ou=system'
    $bind_dn_password         = 'adminpassword'
    $user_search_base         = 'ou=system'
    $group_search_base        = 'ou=system'
    $sharedgroup_search_base  = 'ou=SharedGroups,dc=wso2,dc=org'

}

