# ----------------------------------------------------------------------------
#  Copyright 2005-2013 WSO2, Inc. http://www.wso2.org
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
# ----------------------------------------------------------------------------
#
# Class esb::params
#
# This class manages ESB parameters
#
# Parameters:
#
# Usage: Uncomment the variable and assign a value to override the wso2.pp value
#
#

class esb::params {
  #$package_repo         = 'http://ec2-54-251-18-7.ap-southeast-1.compute.amazonaws.com'
  $depsync_svn_repo     = 'https://svn.appfactory.domain.com/wso2/repo/'
  $local_package_dir    = '/mnt/packs'

  # Service subdomains
  $domain               = 'wso2.com'
  $esb_subdomain        = 'esb'
  $management_subdomain = 'management'

  $admin_username       = 'ADMIN_USER'
  $admin_password       = 'ADMIN_PASSWORD'

  # MySQL server configuration details
  #$mysql_server         = 'mysql.wso2.com'
  #$mysql_port           = '3306'
  #$max_connections      = '100000'
  #$max_active           = '150'
  #$max_wait             = '360000'

  # Database details
  $registry_user        = 'DB_USER'
  $registry_password    = 'DB_PASSWORD'
  $registry_database    = 'REGISTRY_DB'

  $userstore_user       = 'DB_USER'
  $userstore_password   = 'DB_PASSWORD'
  $userstore_database   = 'USERSTORE_DB'

  # Depsync settings
  $svn_user             = 'wso2'
  $svn_password         = 'wso2123'

  # Auto-scaler
  $auto_scaler_epr      = 'http://xxx:9863/services/AutoscalerService/'

  #LDAP settings 
  $ldap_connection_uri      = 'ldap://localhost:10389'
  $bind_dn                  = 'uid=admin,ou=system'
  $bind_dn_password         = 'adminpassword'
  $user_search_base         = 'ou=system'
  $group_search_base        = 'ou=system'
  $sharedgroup_search_base  = 'ou=SharedGroups,dc=wso2,dc=org'
}
