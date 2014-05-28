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
source "$current_dir/conf/setup.conf"

function start_gitblit() {
   echo "Starting Gitblit Server ..."
   pushd $gitblit_path
   nohup $JAVA_HOME/bin/java -jar gitblit.jar --baseFolder data &
   echo "Gitblit server started"
   popd
}

# In silent mode, start BAM server and do not make any configurations
if [[ -z $silent_mode && $silent_mode = "true" ]]; then
       start_gitblit
       exit 0
fi

# Setting GitBlit
mkdir $gitblit_path
if [[ -e $gitblit_pack_path ]]; then
    tar xzf $gitblit_pack_path -C $gitblit_path
    cp -f $current_dir/config/gitblit/gitblit.properties $gitblit_path/data

    sed -i '$a internal.repo.username=admin' $stratos_extract_path-default/repository/conf/cartridge-config.properties
    sed -i '$a internal.repo.password=admin' $stratos_extract_path-default/repository/conf/cartridge-config.properties
    sed -i '$a internal.git.url=http://HOST_IP:8290' $stratos_extract_path-default/repository/conf/cartridge-config.properties
    sed -i "s@HOST_IP@$host_ip@g" $stratos_extract_path-default/repository/conf/cartridge-config.properties
    start_gitblit
else
    echo "Gitblit pack [ $gitblit_pack_path ] not found!"
    exit 1
fi

