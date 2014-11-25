package org.apache.stratos.autoscaler.rule;
/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.stratos.autoscaler.*;
import org.apache.stratos.autoscaler.algorithm.AutoscaleAlgorithm;
import org.apache.stratos.autoscaler.algorithm.OneAfterAnother;
import org.apache.stratos.autoscaler.algorithm.RoundRobin;
import org.apache.stratos.autoscaler.client.cloud.controller.CloudControllerClient;
import org.apache.stratos.autoscaler.client.cloud.controller.InstanceNotificationClient;
import org.apache.stratos.autoscaler.monitor.AbstractMonitor;
import org.apache.stratos.autoscaler.partition.PartitionManager;
import org.apache.stratos.cloud.controller.stub.pojo.MemberContext;

/**
 * This will have utility methods that need to be executed from rule file...
 */
public class RuleTasksDelegator {

    public static final double SCALE_UP_FACTOR = 0.8;   //get from config
    public static final double SCALE_DOWN_FACTOR = 0.2;

    private static final Log log = LogFactory.getLog(RuleTasksDelegator.class);

    public double getPredictedValueForNextMinute(float average, float gradient, float secondDerivative, int timeInterval){
        double predictedValue;
//        s = u * t + 0.5 * a * t * t
        if(log.isDebugEnabled()){
            log.debug(String.format("Predicting the value, [average]: %s , [gradient]: %s , [second derivative]" +
                    ": %s , [time intervals]: %s ", average, gradient, secondDerivative, timeInterval));
        }
        predictedValue = average + gradient * timeInterval + 0.5 * secondDerivative * timeInterval * timeInterval;

        return predictedValue;
    }

    public AutoscaleAlgorithm getAutoscaleAlgorithm(String partitionAlgorithm){
        AutoscaleAlgorithm autoscaleAlgorithm = null;
        if(log.isDebugEnabled()){
            log.debug(String.format("Partition algorithm is ", partitionAlgorithm));
        }
        if(Constants.ROUND_ROBIN_ALGORITHM_ID.equals(partitionAlgorithm)){

            autoscaleAlgorithm = new RoundRobin();
        } else if(Constants.ONE_AFTER_ANOTHER_ALGORITHM_ID.equals(partitionAlgorithm)){

            autoscaleAlgorithm = new OneAfterAnother();
        } else {
            if(log.isErrorEnabled()){
                log.error(String.format("Partition algorithm %s could not be identified !", partitionAlgorithm));
            }
        }
        return autoscaleAlgorithm;
    }

    public void delegateInstanceCleanup(String memberId) {

            try {

                //calling SM to send the instance notification event.
                InstanceNotificationClient.getInstance().sendMemberCleanupEvent(memberId);
                log.info("Instance clean up event sent for [member] " + memberId);
            } catch (Throwable e) {
                log.error("Cannot terminate instance", e);
            }
        }

    public void delegateSpawn(PartitionContext partitionContext, String clusterId, String lbRefType, boolean isPrimary) {
    	
        try {

            String nwPartitionId = partitionContext.getNetworkPartitionId();
            NetworkPartitionLbHolder lbHolder =
                                          PartitionManager.getInstance()
                                                          .getNetworkPartitionLbHolder(nwPartitionId);
            String lbClusterId = getLbClusterId(lbRefType, partitionContext, lbHolder);
            
            AbstractMonitor monitor = null;
            if (AutoscalerContext.getInstance().monitorExist(clusterId)) {
            	monitor = AutoscalerContext.getInstance().getMonitor(clusterId);
            } else if (AutoscalerContext.getInstance().lbMonitorExist(clusterId)) {
            	monitor = AutoscalerContext.getInstance().getLBMonitor(clusterId);
            }
            
            if (null == monitor) {
            	log.error("A cluster monitor is not found for cluster id : " + clusterId);
            	return;
            }


            //Calculate accumulation of minimum counts of all the partition of current network partition
            int minimumCountOfNetworkPartition = 0;
            for(PartitionContext partitionContextOfCurrentNetworkPartition: monitor
                    .getNetworkPartitionCtxt(nwPartitionId).getPartitionCtxts().values()){

                minimumCountOfNetworkPartition += partitionContextOfCurrentNetworkPartition.getMinimumMemberCount();
                }

            MemberContext memberContext =
                                         CloudControllerClient.getInstance()
                                                              .spawnAnInstance(partitionContext.getPartition(),
                                                                      clusterId,
                                                                      lbClusterId, partitionContext.getNetworkPartitionId(),
                                                                      isPrimary,
                                                                      minimumCountOfNetworkPartition);
            if (memberContext != null) {
                partitionContext.addPendingMember(memberContext);
                if(log.isDebugEnabled()){
                    log.debug(String.format("Pending member added, [member] %s [partition] %s", memberContext.getMemberId(),
                            memberContext.getPartition().getId()));
                }
            } else if(log.isDebugEnabled()){
                log.debug("Returned member context is null, did not add to pending members");
            }

        } catch (Throwable e) {
            String message = "Cannot spawn an instance";
            log.error(message, e);
            throw new RuntimeException(message, e);
        }
    }

    // Original method. Assume this is invoked from mincheck.drl
    
   /* public void delegateSpawn(PartitionContext partitionContext, String clusterId, String lbRefType) {
        try {

            String nwPartitionId = partitionContext.getNetworkPartitionId();
                                                         .getNetworkPartitionLbHolder(nwPartitionId);
            NetworkPartitionLbHolder lbHolder =
                                          PartitionManager.getInstance()
                                                          .getNetworkPartitionLbHolder(nwPartitionId);

            
            String lbClusterId = getLbClusterId(lbRefType, partitionContext, lbHolder);

            MemberContext memberContext =
                                         CloudControllerClient.getInstance()
                                                              .spawnAnInstance(partitionContext.getPartition(),
                                                                      clusterId,
                                                                      lbClusterId, partitionContext.getNetworkPartitionId());
            if (memberContext != null) {
                partitionContext.addPendingMember(memberContext);
                if(log.isDebugEnabled()){
                    log.debug(String.format("Pending member added, [member] %s [partition] %s", memberContext.getMemberId(),
                            memberContext.getPartition().getId()));
                }
            } else if(log.isDebugEnabled()){
                log.debug("Returned member context is null, did not add to pending members");
            }

        } catch (Throwable e) {
            String message = "Cannot spawn an instance";
            log.error(message, e);
            throw new RuntimeException(message, e);
        }
   	}*/



    public static String getLbClusterId(String lbRefType, PartitionContext partitionCtxt, 
        NetworkPartitionLbHolder networkPartitionLbHolder) {

       String lbClusterId = null;

        if (lbRefType != null) {
            if (lbRefType.equals(org.apache.stratos.messaging.util.Constants.DEFAULT_LOAD_BALANCER)) {
                lbClusterId = networkPartitionLbHolder.getDefaultLbClusterId();
//                lbClusterId = nwPartitionCtxt.getDefaultLbClusterId();
            } else if (lbRefType.equals(org.apache.stratos.messaging.util.Constants.SERVICE_AWARE_LOAD_BALANCER)) {
                String serviceName = partitionCtxt.getServiceName();
                lbClusterId = networkPartitionLbHolder.getLBClusterIdOfService(serviceName);
//                lbClusterId = nwPartitionCtxt.getLBClusterIdOfService(serviceName);
            } else {
                log.warn("Invalid LB reference type defined: [value] "+lbRefType);
            }
        }
        if (log.isDebugEnabled()){
            log.debug(String.format("Getting LB id for spawning instance [lb reference] %s ," +
                    " [partition] %s [network partition] %s [Lb id] %s ", lbRefType, partitionCtxt.getPartitionId(),
                    networkPartitionLbHolder.getNetworkPartitionId(), lbClusterId));
        }
       return lbClusterId;
    }

    public void delegateTerminate(PartitionContext partitionContext, String memberId) {
        try {

            //Moving member to pending termination list

            if(partitionContext.activeMemberAvailable(memberId)) {

                partitionContext.moveActiveMemberToTerminationPendingMembers(memberId);
            } else if (partitionContext.pendingMemberAvailable(memberId)){

                partitionContext.movePendingMemberToObsoleteMembers(memberId);
            }
        } catch (Throwable e) {
            log.error("Cannot terminate instance", e);
        }
    }


    public void terminateObsoleteInstance(String memberId) {
        try {
            CloudControllerClient.getInstance().terminate(memberId);
        } catch (Throwable e) {
            log.error("Cannot terminate instance", e);
        }
    }

    public void delegateTerminateAll(String clusterId) {
        try {

            CloudControllerClient.getInstance().terminateAllInstances(clusterId);
        } catch (Throwable e) {
            log.error("Cannot terminate instance", e);
        }
    }

    public double getLoadAveragePredictedValue (NetworkPartitionContext networkPartitionContext) {
        double loadAveragePredicted = 0.0d;
        int totalMemberCount = 0;

        for (PartitionContext partitionContext : networkPartitionContext.getPartitionCtxts().values()) {
            for (MemberStatsContext memberStatsContext : partitionContext.getMemberStatsContexts().values()) {

                float memberAverageLoadAverage = memberStatsContext.getLoadAverage().getAverage();
                float memberGredientLoadAverage = memberStatsContext.getLoadAverage().getGradient();
                float memberSecondDerivativeLoadAverage = memberStatsContext.getLoadAverage().getSecondDerivative();

                double memberPredictedLoadAverage = getPredictedValueForNextMinute(memberAverageLoadAverage, memberGredientLoadAverage, memberSecondDerivativeLoadAverage, 1);

                log.debug("Member ID : " + memberStatsContext.getMemberId() + " : Predicted Load Average : " + memberPredictedLoadAverage);

                loadAveragePredicted += memberPredictedLoadAverage;
                ++totalMemberCount;
            }
        }

        if (totalMemberCount > 0) {
            log.debug("Predicted load average : " + loadAveragePredicted / totalMemberCount);
            return loadAveragePredicted / totalMemberCount;
        }
        else {
            return 0;
        }
    }

    public double getMemoryConsumptionPredictedValue(NetworkPartitionContext networkPartitionContext) {
        double memoryConsumptionPredicted = 0.0d;
        int totalMemberCount = 0;

        for (PartitionContext partitionContext : networkPartitionContext.getPartitionCtxts().values()) {
            for (MemberStatsContext memberStatsContext : partitionContext.getMemberStatsContexts().values()) {

                float memberMemoryConsumptionAverage = memberStatsContext.getMemoryConsumption().getAverage();
                float memberMemoryConsumptionGredient = memberStatsContext.getMemoryConsumption().getGradient();
                float memberMemoryConsumptionSecondDerivative= memberStatsContext.getMemoryConsumption().getSecondDerivative();

                double memberPredictedMemoryConsumption = getPredictedValueForNextMinute(memberMemoryConsumptionAverage, memberMemoryConsumptionGredient, memberMemoryConsumptionSecondDerivative, 1);

                log.debug("Member ID : " + memberStatsContext.getMemberId() + " : Predicted Memory Consumption : " + memberPredictedMemoryConsumption);

                memoryConsumptionPredicted += memberPredictedMemoryConsumption;
                ++totalMemberCount;
            }
        }

        if (totalMemberCount > 0) {
            log.debug("Predicted memory consumption : " + memoryConsumptionPredicted / totalMemberCount);
            return memoryConsumptionPredicted / totalMemberCount;
        }
        else {
            return 0;
        }
    }

}
