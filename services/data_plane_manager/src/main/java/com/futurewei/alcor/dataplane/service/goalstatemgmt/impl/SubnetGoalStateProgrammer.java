/*
Copyright 2019 The Alcor Authors.

Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/
package com.futurewei.alcor.dataplane.service.goalstatemgmt.impl;

import com.futurewei.alcor.dataplane.comm.config.IKafkaConfiguration;
import com.futurewei.alcor.dataplane.comm.grpc.GoalStateProvisionerClient;
import com.futurewei.alcor.dataplane.comm.message.GoalStateMessageConsumerFactory;
import com.futurewei.alcor.dataplane.comm.message.GoalStateMessageProducerFactory;
import com.futurewei.alcor.dataplane.comm.message.MessageClient;
import com.futurewei.alcor.dataplane.logging.Logger;
import com.futurewei.alcor.dataplane.logging.LoggerFactory;
import com.futurewei.alcor.dataplane.model.HostInfo;
import com.futurewei.alcor.dataplane.model.SubnetState;
import com.futurewei.alcor.dataplane.model.VpcState;
import com.futurewei.alcor.dataplane.service.goalstatemgmt.GoalStateProgrammer;
import com.futurewei.alcor.dataplane.service.goalstatemgmt.SubnetProgramInfo;
import com.futurewei.alcor.dataplane.utils.GoalStateUtil;
import com.futurewei.alcor.schema.Common;
import com.futurewei.alcor.schema.Goalstate;

import java.util.logging.Level;

public class SubnetGoalStateProgrammer implements GoalStateProgrammer {

    private SubnetProgramInfo subnetProgramInfo;
    private boolean isFastPath;
    GoalStateProvisionerClient gRpcClientForEpHost = null;

    public GoalStateProvisionerClient getGRpcClientForEpHost() {
        return gRpcClientForEpHost;
    }

    public void setGRpcClientForEpHost(GoalStateProvisionerClient gRpcClientForEpHost) {
        this.gRpcClientForEpHost = gRpcClientForEpHost;
    }

    public MessageClient getKafkaClient() {
        return kafkaClient;
    }

    public void setKafkaClient(MessageClient kafkaClient) {
        this.kafkaClient = kafkaClient;
    }

    MessageClient kafkaClient = null;
    public SubnetGoalStateProgrammer(SubnetProgramInfo subnetProgramInfo) {
        this.subnetProgramInfo = subnetProgramInfo;
        this.isFastPath = true;
    }

    @Override
    public long[] SendGoalStateToHosts() {

        long[] recordedTimeStamp = new long[3];
        recordedTimeStamp[0] = System.nanoTime();

        SubnetState customerSubnetState = this.subnetProgramInfo.getCustomerSubnetState();
        HostInfo[] transitSwitchHosts = this.subnetProgramInfo.getTransitSwitchHosts();
        VpcState customerVpcState = this.subnetProgramInfo.getCustomerVpcState();
        HostInfo[] transitRouterHosts = this.subnetProgramInfo.getTransitRouterHosts();

        if (!this.isFastPath) {
            this.setKafkaClient(new MessageClient(new GoalStateMessageConsumerFactory(), new GoalStateMessageProducerFactory()));
        }

        ////////////////////////////////////////////////////////////////////////////
        // Step 1: Go to switch hosts in current subnet, call update_vpc and update_substrate
        ////////////////////////////////////////////////////////////////////////////
        final Goalstate.GoalState gsVpcState = GoalStateUtil.CreateGoalState(
                Common.OperationType.CREATE_UPDATE_SWITCH,
                customerVpcState,
                transitRouterHosts,
                Common.OperationType.CREATE_UPDATE_GATEWAY,
                new SubnetState[]{customerSubnetState},
                new HostInfo[][]{transitSwitchHosts});

        for (HostInfo transitSwitch : transitSwitchHosts) {
            if (this.isFastPath) {
                System.out.println("Send Subnet id :" + customerSubnetState.getId() + " with fast path to switch host" + transitSwitch);
                System.out.println("GS: " + gsVpcState.toString());
                GoalStateProvisionerClient gRpcClientForEpHost = new GoalStateProvisionerClient(transitSwitch.getHostIpAddress(), transitSwitch.getGRPCServerPort());
                gRpcClientForEpHost.PushNetworkResourceStates(gsVpcState);
            } else {
                // This block is reserved for future usage
                String topic = IKafkaConfiguration.PRODUCER_CLIENT_ID + transitSwitch.getId();
                this.getKafkaClient().runProducer(topic, gsVpcState);
            }
        }

        recordedTimeStamp[1] = System.nanoTime();

        ////////////////////////////////////////////////////////////////////////////
        // Step 2: Go to router hosts in current vpc, call update_substrate only
        ////////////////////////////////////////////////////////////////////////////
        final Goalstate.GoalState gsSubnetState = GoalStateUtil.CreateGoalState(
                Common.OperationType.CREATE_UPDATE_ROUTER,
                new SubnetState[]{customerSubnetState},
                new HostInfo[][]{transitSwitchHosts});
        Logger logger = LoggerFactory.getLogger();
        for (HostInfo transitRouter : transitRouterHosts) {
            if (this.isFastPath) {
                logger.log(Level.INFO, "Send VPC id :" + customerSubnetState.getVpcId() + " with fast path");
                GoalStateProvisionerClient gRpcClient = new GoalStateProvisionerClient(transitRouter.getHostIpAddress(), transitRouter.getGRPCServerPort());
                gRpcClient.PushNetworkResourceStates(gsSubnetState);
            } else {
                // This block is reserved for future usage
                String topic = IKafkaConfiguration.PRODUCER_CLIENT_ID + transitRouter.getId();
                this.getKafkaClient().runProducer(topic, gsSubnetState);
            }
        }

        recordedTimeStamp[2] = System.nanoTime();

        return recordedTimeStamp;
    }
}
