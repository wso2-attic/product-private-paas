/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.stratos.rest.endpoint.bean.autoscaler.policy.autoscale;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement public class LoadThresholds {

    private RequestsInFlight requestsInFlight;
    private MemoryConsumption memoryConsumption;
    private LoadAverage loadAverage;

    public void setRequestsInFlight(RequestsInFlight requestsInFlight) {
        this.requestsInFlight = requestsInFlight;
    }

    public RequestsInFlight getRequestsInFlight() {
        return requestsInFlight;
    }

    public void setMemoryConsumption(MemoryConsumption memoryConsumption) {
        this.memoryConsumption = memoryConsumption;
    }

    public MemoryConsumption getMemoryConsumption() {
        return memoryConsumption;
    }

    public void setLoadAverage(LoadAverage requestsInFlight) {
        this.loadAverage = loadAverage;
    }

    public LoadAverage getLoadAverage() {
        return loadAverage;
    }

}
