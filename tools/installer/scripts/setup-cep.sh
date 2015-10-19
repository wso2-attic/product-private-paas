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

#setting the public IP for bam
export public_ip=$(curl --silent http://ipecho.net/plain; echo)
export hadoop_hostname=$(hostname -f)

while getopts ":p:" opts
do
  case $opts in
    p)
        profile_list=${OPTARG}
        ;;
    \?)
        exit 1
        ;;
  esac
done

profile_list=`echo $profile_list | sed 's/^ *//g' | sed 's/ *$//g'`
if [[ !(-z $profile_list || $profile_list = "") ]]; then
    arr=$(echo $profile_list | tr " " "\n")

    for x in $arr
    do
    	if [[ $x = "default" ]]; then
            profile="default"
    	elif [[ $x = "stratos" ]]; then
            profile="stratos"
        else
            echo "Invalid profile."
            exit 1
    	fi
    done
else
    profile="default"
fi

stratos_extract_path=$stratos_extract_path"-"$profile

function start_cep() {
       echo "Starting WSO2 CEP server ..."
       nohup $cep_path/bin/wso2server.sh -DportOffset=1 &

}

# Start the CEP server
start_

cep