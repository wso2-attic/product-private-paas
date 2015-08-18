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

# base node
node 'base' {

#essential variables
  $ppaas_version        = '4.1.0-SNAPSHOT'
  $configurator_name    = 'ppaas-configurator'
  $configurator_version = '4.1.0-SNAPSHOT'
  $pca_name             = 'apache-stratos-python-cartridge-agent'
  $pca_version          = '4.1.0'
  $package_repo         = 'PACKAGE_REPO'
  $local_package_dir    = '/mnt/packs'
  $mb_ip                = '192.168.30.245'
  $mb_port              = '1183'
  $mb_url               = 'tcp://192.168.30.245:1883'
  $mb_type    = 'activemq' #in wso2 mb case, value should be 'wso2mb'
  $cep_url              = 'tcp://192.168.30.96:7611' #eg: tcp://10.100.2.32:7611,tcp://10.100.2.33:7611
  $cep_ip = "192.168.30.96"
  $cep_port="7711"
  $cep_username="admin"
  $cep_password="admin"
  $bam_ip                  ='192.168.30.96'
  $bam_port                ='7611'
  $bam_secure_port         ='7711'
  $bam_username        ='admin'
  $bam_password      ='admin'
  $truststore_password  = 'wso2carbon'
  $java_distribution  = 'jdk-7u72-linux-x64.gz'
  $java_name    = 'jdk1.7.0_72'
  $stratos_version  = '4.1.0'
  $member_type_ip       = 'private'
  $lb_httpPort    = '80'
  $lb_httpsPort  = '443'
  $lb_private_ip  = ''
  $lb_public_ip  = ''
  $disable_sso    = 'true'
  $idp_hostname    = 'identity.stratos.com'
  $idp_port    = '443'
  $tomcat_version  = '7.0.52'
  $enable_log_publisher = 'false'
  $server_ip            = $ipaddress
  $using_dns    = 'USING_DNS'
  $greg_url    = 'https://localhost/registry'
  $metadata_service_url = 'https://192.168.30.96:9443'
  $agent_log_level = "INFO"
  $extensions_dir = '${script_path}/../extensions'

  require stratos_base
}
