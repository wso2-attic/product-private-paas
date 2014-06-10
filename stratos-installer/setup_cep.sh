#!/bin/bash
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

# Define error handling function
function error_handler() {
        MYSELF="$0"               # equals to script name
        LASTLINE="$1"            # argument 1: last line of error occurence
        LASTERR="$2"             # argument 2: error code of last command
        echo "ERROR in ${MYSELF}: line ${LASTLINE}: exit status of last command: ${LASTERR}"
	exit 1       
}

# Execute error_handler function on script error
trap 'error_handler ${LINENO} $?' ERR

dir=`dirname $0`
current_dir=`cd $dir;pwd`
activemq_client_libs=(activemq-broker-5.9.1.jar activemq-client-5.9.1.jar geronimo-j2ee-management_1.1_spec-1.0.1.jar geronimo-jms_1.1_spec-1.1.1.jar hawtbuf-1.9.jar)

source "$current_dir/conf/setup.conf"

unzip -q $cep_pack_path -d $stratos_path

cp -f $current_dir/../resources/cep/artifacts/streamdefinitions/stream-manager-config.xml $cep_path/repository/conf/
cp -f $jndi_template_path $cep_path/repository/conf/
cp -f $current_dir/../resources/cep/lib/* $cep_path/repository/components/lib/
cp -rf $current_dir/../resources/cep/artifacts/* $cep_path/repository/deployment/server

for activemq_client_lib in "${activemq_client_libs[@]}" 
do
  cp -f $activemq_path/lib/$activemq_client_lib $cep_path/repository/components/lib/
done
      
pushd $cep_path

echo "Setting up CEP" 
    
sed -i "s@<Offset>0</Offset>@<Offset>${cep_port_offset}</Offset>@g" repository/conf/carbon.xml
sed -i "s@MB_HOSTNAME:MB_LISTEN_PORT@$mb_ip:$mb_port@g" repository/conf/jndi.properties
sed -i "s@CEP_HOME@$cep_path@g" repository/deployment/server/outputeventadaptors/JMSOutputAdaptor.xml
sed -i "s@MB_HOSTNAME:MB_LISTEN_PORT@$mb_ip:$mb_port@g" repository/deployment/server/outputeventadaptors/JMSOutputAdaptor.xml
sed -i '$a org.apache.stratos.cep.extension.GradientFinderWindowProcessor' repository/conf/siddhi/siddhi.extension
sed -i '$a org.apache.stratos.cep.extension.SecondDerivativeFinderWindowProcessor' repository/conf/siddhi/siddhi.extension
sed -i '$a org.apache.stratos.cep.extension.FaultHandlingWindowProcessor' repository/conf/siddhi/siddhi.extension
sed -i '$a org.apache.stratos.cep.extension.ConcatWindowProcessor' repository/conf/siddhi/siddhi.extension

popd

echo "CEP setup completed."
