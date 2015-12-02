#!/bin/bash
echo "-------------------------------------------------------"
echo "Starting PPaaS Artifact Migration Tool (4.0.0 to 4.0.1)"
echo "-------------------------------------------------------"
script_path="$( cd -P "$( dirname "$SOURCE" )" && pwd )/`dirname $0`"
lib_path=${script_path}/../lib/
class_path=`echo ${lib_path}/*.jar | tr ' ' ':'`


java -cp "${class_path}" ${properties} ${debug} org.wso2.ppaas.tools.artifactmigration.ArtifactConverter$*