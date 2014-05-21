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
# Class apimanager::params
#
# This class manages APIM parameters
#
# Parameters:
#
# Usage: Uncomment the variable and assign a value to override the nodes.pp value
#
#

class apimanager::params {

  $domain               = 'wso2.com'
  #$package_repo         = 
  $local_package_dir    = '/mnt/packs'
  $depsync_svn_repo     = 'https://svn.appfactory.domain.com/wso2/repo/'

  # Service subdomains
  $am_subdomain         = 'am' 
  $gateway_subdomain    = 'gateway'
  $keymanager_subdomain =  'keymanager'
  $apistore_subdomain   = 'store'
  $publisher_subdomain  = 'publisher'
  $management_subdomain = 'manager'

  # Service ports
  $gateway_mgt_https_port= '8243'
  $gateway_http_port     = '8280'
  $gateway_https_port    = '8243'
  $gateway_https_nio_proxy_port    = '8243'
  $gateway_http_nio_proxy_port    = '8280'
  $gateway_https_proxy_port    = '8243'
  $gateway_http_proxy_port    = '8280'
  $keymanager_mgt_https_port= '9443'
  $keymanager_http_port     = '8280'
  $keymanager_https_port    = '8243'

  $admin_username       = 'ADMIN_USER'
  $admin_password       = 'ADMIN_PASSWORD'

  # BAM settings
  $usage_tracking        = "false"
 
  $registry_user        =  'DB_USER'
  $registry_password    = 'DB_PASSWORD'
  $registry_database    = 'REGISTRY_DB'

  $userstore_user       = 'DB_USER'
  $userstore_password   = 'DB_PASSWORD'
  $userstore_database   = 'USERSTORE_DB'
  
  $configdb_user        = 'DB_USER'
  $configdb_password    = 'DB_PASSWORD'

  # apimanager database 
  $apim_database        = 'APIM_DB'
  $apim_user            = 'DB_USER'
  $apim_password        = 'DB_PASSWORD'

  # stats database
  $amstats_user         = 'DB_USER'
  $amstats_password     = 'DB_PASSWORD'
  $amstats_database     = 'STATS_DB'

  # Depsync settings
  $svn_user             = 'wso2'
  $svn_password         = 'wso2123'

  # Auto-scaler
  $auto_scaler_epr      = 'http://xxx:9863/services/AutoscalerService/'

}
