/*
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ppaas.rest.endpoint.bean.cartridge.definition;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class PersistenceBean {

    public boolean isRequired;

    public List<VolumeBean> volume;

    public String toString () {
        return " [ Persistence Required : " +isRequired  + "\n" +
                "volume: " + getVolumes() + "] ";
    }

    private String getVolumes() {
        StringBuilder volumeBuilder = new StringBuilder();
        if(volume != null && !volume.isEmpty()) {
            for(VolumeBean volumeBean : volume) {
                volumeBuilder.append(volumeBean.toString());
            }
        }
        return volumeBuilder.toString();
    }
}
