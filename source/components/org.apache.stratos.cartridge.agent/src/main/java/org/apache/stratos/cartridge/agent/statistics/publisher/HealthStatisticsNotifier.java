/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.stratos.cartridge.agent.statistics.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.stratos.cartridge.agent.config.CartridgeAgentConfiguration;
import org.apache.stratos.cartridge.agent.event.publisher.CartridgeAgentEventPublisher;
import org.apache.stratos.cartridge.agent.util.CartridgeAgentConstants;
import org.apache.stratos.cartridge.agent.util.CartridgeAgentUtils;
import org.wso2.securevault.SecretResolver;
import java.util.LinkedList;
import java.util.Queue;
import java.util.List;

/**
 * Health statistics notifier thread for publishing statistics periodically to CEP.
 */
public class HealthStatisticsNotifier implements Runnable {
    private static final Log log = LogFactory.getLog(HealthStatisticsNotifier.class);

    private final HealthStatisticsPublisher statsPublisher;
    private long statsPublisherInterval = 15000;
    private boolean terminated;

    //For storing the values to publish it when restarting the server
    private static boolean restarting = false;
    private Queue<Double> memoryConsumptionList;
    private Queue<Double> loadAverageList;
    //Stack size
    private static final int MEMORY_CONSUMPTION_SIZE = 4;
    private static final int LOAD_AVERAGE_SIZE = 4;
    //Time in seconds to delay after the ports are open when checking for server restart is over
    private static final long DELAY_RESTART_MODE = 30000;

    public HealthStatisticsNotifier() {
    	
        SecretResolver secretResolver = CartridgeAgentConfiguration.getInstance().getSecretResolver();
        String trustStorePasswordProperty = "truststore.password";
        String trustStorePasswordValue = System.getProperty(trustStorePasswordProperty);
        String alias = trustStorePasswordProperty;

        // Resolve the secret password.
        if (log.isDebugEnabled()) {
            log.debug(String.format("Trying to decrypt property: %s", trustStorePasswordProperty));
        }
        if (trustStorePasswordValue.equalsIgnoreCase("secretAlias:" + alias)) {
            if (secretResolver != null && secretResolver.isInitialized()) {
                if (log.isDebugEnabled()) {
                    log.info("SecretResolver is initialized.");
                }
                if (secretResolver.isTokenProtected(alias)) {
                    if (log.isDebugEnabled()) {
                        log.info("SecretResolver [" + alias + "] is token protected");
                    }
                    trustStorePasswordValue = secretResolver.resolve(alias);
                    if (log.isDebugEnabled()) {
                        log.debug("SecretResolver [" + alias + "] is decrypted properly");
                    }
                }
            }
        }
        System.setProperty("javax.net.ssl.trustStorePassword", trustStorePasswordValue);
    	
        this.statsPublisher = new HealthStatisticsPublisher();

        String interval = System.getProperty("stats.notifier.interval");
        if (interval != null) {
            statsPublisherInterval = Long.getLong(interval);
        }
    }

    @Override
    public void run() {
        while (!terminated) {
            try {
                try {
                    Thread.sleep(statsPublisherInterval);
                } catch (InterruptedException ignore) {
                }

                if (statsPublisher.isEnabled()) {

                    //If the servers are restarting, checks if all the ports are open to turn the restarting mode off
                    if (isRestarting()) {
                        List<Integer> ports = CartridgeAgentConfiguration.getInstance().getPorts();
                        String ipAddress = CartridgeAgentConfiguration.getInstance().getListenAddress();
                        if (CartridgeAgentUtils.checkPortsActive(ipAddress, ports)) {
                            //Switching restarting mode off
                            setRestarting(false);
                        }
                    }

                    if (!isRestarting()) {
                        //If it is not restarting, publish the actual stats
                        if (!isRestarting()) {
                            double memoryConsumption = HealthStatisticsReader.getMemoryConsumption();
                            if(log.isDebugEnabled()) {
                                log.debug(String.format("Publishing memory consumption: %f", memoryConsumption));
                            }
                            statsPublisher.publish(
                                    CartridgeAgentConfiguration.getInstance().getClusterId(),
                                    CartridgeAgentConfiguration.getInstance().getNetworkPartitionId(),
                                    CartridgeAgentConfiguration.getInstance().getMemberId(),
                                    CartridgeAgentConfiguration.getInstance().getPartitionId(),
                                    CartridgeAgentConstants.MEMORY_CONSUMPTION,
                                    memoryConsumption
                            );
                            memoryConsumptionStack(memoryConsumption);
                            double loadAverage = HealthStatisticsReader.getLoadAverage();
                            if(log.isDebugEnabled()) {
                                log.debug(String.format("Publishing load average: %f", loadAverage));
                            }
                            statsPublisher.publish(
                                    CartridgeAgentConfiguration.getInstance().getClusterId(),
                                    CartridgeAgentConfiguration.getInstance().getNetworkPartitionId(),
                                    CartridgeAgentConfiguration.getInstance().getMemberId(),
                                    CartridgeAgentConfiguration.getInstance().getPartitionId(),
                                    CartridgeAgentConstants.LOAD_AVERAGE,
                                    loadAverage
                            );
                            loadAverageStack(loadAverage);
                        }
                    } else {
                        //If it is restarting, publish the average value stored in the stack
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Publishing memory consumption at restart mode: %f", average(memoryConsumptionList)));
                        }
                        statsPublisher.publish(
                                CartridgeAgentConfiguration.getInstance().getClusterId(),
                                CartridgeAgentConfiguration.getInstance().getNetworkPartitionId(),
                                CartridgeAgentConfiguration.getInstance().getMemberId(),
                                CartridgeAgentConfiguration.getInstance().getPartitionId(),
                                CartridgeAgentConstants.MEMORY_CONSUMPTION,
                                average(memoryConsumptionList)
                        );
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Publishing load average at restart mode: %f", average(loadAverageList)));
                        }
                        statsPublisher.publish(
                                CartridgeAgentConfiguration.getInstance().getClusterId(),
                                CartridgeAgentConfiguration.getInstance().getNetworkPartitionId(),
                                CartridgeAgentConfiguration.getInstance().getMemberId(),
                                CartridgeAgentConfiguration.getInstance().getPartitionId(),
                                CartridgeAgentConstants.LOAD_AVERAGE,
                                average(loadAverageList)
                        );
                    }
                } else if (log.isWarnEnabled()) {
                    log.warn("Statistics publisher is disabled");
                }
            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error("Could not publish health statistics", e);
                }
            }
        }
    }

    /**
     * Terminate load balancer statistics notifier thread.
     */
    public void terminate() {
        terminated = true;
    }

    public void memoryConsumptionStack(double memoryConsumption) {
        if (this.memoryConsumptionList == null) {
            this.memoryConsumptionList = new LinkedList<Double>();
        } else if (this.memoryConsumptionList.size() == MEMORY_CONSUMPTION_SIZE) {
            this.memoryConsumptionList.remove();
        }
        memoryConsumptionList.add(memoryConsumption);
    }
    public void loadAverageStack(double loadAverage) {
        if (this.loadAverageList == null) {
            this.loadAverageList = new LinkedList<Double>();
        } else if (this.loadAverageList.size() == LOAD_AVERAGE_SIZE) {
            this.loadAverageList.remove();
        }
        loadAverageList.add(loadAverage);
    }
    public double average(Queue<Double> list) {
        double sum=0;
        for(Double value : list) {
            sum += value.doubleValue();
        }
        return sum/list.size();
    }
    public boolean isRestarting() {
        return  HealthStatisticsNotifier.restarting;
    }
    public static void setRestarting(boolean restarting) {
        HealthStatisticsNotifier.restarting = restarting;
    }
}
