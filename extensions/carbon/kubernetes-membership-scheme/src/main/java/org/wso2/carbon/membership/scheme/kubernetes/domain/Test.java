/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.carbon.membership.scheme.kubernetes.domain;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
    public static void main (String[] args) throws IOException {
         String json = "        \"annotations\": {\n" +
                 "            \"applicationId\": \"wso2am-191-uacc-application-with-is\",\n" +
                 "            \"cartridgeType\": \"wso2am-191-gw-manager\",\n" +
                 "            \"clusterId\": \"wso2am-191-uacc-application-with-is.gateway-manager.wso2am-191-gw-manager.domain\",\n" +
                 "            \"clusterInstanceId\": \"wso2am-191-uacc-application-with-is-1\",\n" +
                 "            \"k8s.mesosphere.io/bindingHost\": \"10.46.12.55\",\n" +
                 "            \"k8s.mesosphere.io/executorId\": \"6f8fdd082fc306fc_k8sm-executor\",\n" +
                 "            \"k8s.mesosphere.io/offerId\": \"572751b9-9386-42ad-b0e1-79860d588e4f-O823906\",\n" +
                 "            \"k8s.mesosphere.io/portName_TCP_http-9763\": \"1030\",\n" +
                 "            \"k8s.mesosphere.io/portName_TCP_https-9443\": \"1029\",\n" +
                 "            \"k8s.mesosphere.io/portName_TCP_tcp-22\": \"1031\",\n" +
                 "            \"k8s.mesosphere.io/port_TCP_22\": \"1031\",\n" +
                 "            \"k8s.mesosphere.io/port_TCP_9443\": \"1029\",\n" +
                 "            \"k8s.mesosphere.io/port_TCP_9763\": \"1030\",\n" +
                 "            \"k8s.mesosphere.io/slaveId\": \"e1d02d40-1496-42d1-8ad0-774dd0cb016e-S5\",\n" +
                 "            \"k8s.mesosphere.io/taskId\": \"pod.22c48583-db19-11e5-a78c-005056ad6f59\",\n" +
                 "            \"memberId\": \"wso2am-191-uacc-application-with-is.gateway-manager.wso2am-191-gw-manager.domain7cdce2a8-107e-411f-9943-a242fe04ee30\"\n" +
                 "        }";

        String regex = "(http-9763\":)(\\s*)(\")(\\d*)(\",)";
        Pattern r = Pattern.compile(regex);
        Matcher m = r.matcher(json);
        if (m.find( )) {
            System.out.println("Found value: " + m.group(4));
        } else {
            System.out.println("NO MATCH");
        }
    }
}
