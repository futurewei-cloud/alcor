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
package com.futurewei.alcor.dataplane.service.impl;

import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.common.message.MessageClient;
import com.futurewei.alcor.dataplane.config.Config;
import com.futurewei.alcor.dataplane.config.grpc.GoalStateProvisionerClient;
import com.futurewei.alcor.dataplane.config.message.GoalStateMessageConsumerFactory;
import com.futurewei.alcor.dataplane.config.message.GoalStateMessageProducerFactory;
import com.futurewei.alcor.dataplane.entity.SubnetProgramInfo;
import com.futurewei.alcor.dataplane.service.GoalStateService;
import com.futurewei.alcor.dataplane.service.NodeManager;
import com.futurewei.alcor.dataplane.utils.GoalStateUtil;
import com.futurewei.alcor.schema.Common;
import com.futurewei.alcor.schema.Goalstate;
import com.futurewei.alcor.schema.Port.PortConfiguration.HostInfo;

import java.util.logging.Level;

import static com.futurewei.alcor.schema.Subnet.SubnetState;
import static com.futurewei.alcor.schema.Vpc.VpcState;
public class SubnetGoalStateServiceImpl implements GoalStateService {

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
    public SubnetGoalStateServiceImpl(SubnetProgramInfo subnetProgramInfo) {
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
                System.out.println("Send Subnet id :" + customerSubnetState.getConfiguration().getId() + " with fast path to switch host" + transitSwitch);
                System.out.println("GS: " + gsVpcState.toString());
                GoalStateProvisionerClient gRpcClientForEpHost = new GoalStateProvisionerClient(transitSwitch.getIpAddress(), NodeManager.GRPC_SERVER_PORT);
                gRpcClientForEpHost.PushNetworkResourceStates(gsVpcState);
            } else {
                // This block is reserved for future usage
                String topic = Config.PRODUCER_CLIENT_ID + transitSwitch.getIpAddress();
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
                logger.log(Level.INFO, "Send VPC id :" + customerSubnetState.getConfiguration().getVpcId() + " with fast path");
                GoalStateProvisionerClient gRpcClient = new GoalStateProvisionerClient(transitRouter.getIpAddress(), NodeManager.GRPC_SERVER_PORT);
                gRpcClient.PushNetworkResourceStates(gsSubnetState);
            } else {
                // This block is reserved for future usage
                String topic = Config.PRODUCER_CLIENT_ID + transitRouter.getIpAddress();
                this.getKafkaClient().runProducer(topic, gsSubnetState);
            }
        }

        recordedTimeStamp[2] = System.nanoTime();

        return recordedTimeStamp;
    }
}
