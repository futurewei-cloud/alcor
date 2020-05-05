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
import com.futurewei.alcor.dataplane.entity.PortProgramInfo;
import com.futurewei.alcor.dataplane.service.GoalStateService;
import com.futurewei.alcor.dataplane.service.NodeManager;
import com.futurewei.alcor.dataplane.utils.GoalStateUtil;
import com.futurewei.alcor.schema.Common;
import com.futurewei.alcor.schema.Goalstate;
import com.futurewei.alcor.schema.Port.PortConfiguration.HostInfo;
import lombok.Data;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static com.futurewei.alcor.schema.Port.PortState;
import static com.futurewei.alcor.schema.Subnet.SubnetState;

@Data
public class PortGoalStateServiceImpl implements GoalStateService {

    private PortProgramInfo portProgramInfo;
    private ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

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

    GoalStateProvisionerClient gRpcClientForEpHost = null;
    MessageClient kafkaClient = null;
    public PortGoalStateServiceImpl(PortProgramInfo portProgramInfo) {
        this.portProgramInfo = portProgramInfo;
    }

    @Override
    public long[] SendGoalStateToHosts() {

        long[] recordedTimeStamp = new long[3];
        Logger logger = LoggerFactory.getLogger();

        PortState customerPortState = this.portProgramInfo.getCustomerPortState();
        HostInfo epHost = this.portProgramInfo.getEpHost();
        SubnetState customerSubnetState = this.portProgramInfo.getCustomerSubnetState();
        HostInfo[] transitSwitchHostsForSubnet = this.portProgramInfo.getTransitSwitchHosts();

        boolean isFastPath = portProgramInfo.isFastPath(); // not sure
        if (!isFastPath) {
            this.setKafkaClient(new MessageClient(new GoalStateMessageConsumerFactory(), new GoalStateMessageProducerFactory()));
        }
        logger.log(Level.INFO, "EP :" + customerPortState.getConfiguration().getId() + "|name:" + customerPortState.getConfiguration().getName() + "| fastpath: " + isFastPath);

        ////////////////////////////////////////////////////////////////////////////
        // Step 1: Go to EP host, update_endpoint
        ////////////////////////////////////////////////////////////////////////////
        final Goalstate.GoalState gsPortState = GoalStateUtil.CreateGoalState(
                Common.OperationType.INFO,
                customerSubnetState,
                transitSwitchHostsForSubnet,
                Common.OperationType.CREATE,
                customerPortState,
                epHost);

        if (isFastPath) {
            System.out.println("Send port id :" + customerPortState.getConfiguration().getId() + " with fast path to ep host " + epHost);
            this.setGRpcClientForEpHost(new GoalStateProvisionerClient(epHost.getIpAddress(), NodeManager.GRPC_SERVER_PORT));
            this.getGRpcClientForEpHost().PushNetworkResourceStates(gsPortState);
        } else {
            String topicForEndpoint = Config.PRODUCER_CLIENT_ID + epHost.getIpAddress();
            this.getKafkaClient().runProducer(topicForEndpoint, gsPortState);
        }

        recordedTimeStamp[0] = System.nanoTime();

        ////////////////////////////////////////////////////////////////////////////
        // Step 2: Go to switch hosts in current subnet, update_ep and update_substrate
        ////////////////////////////////////////////////////////////////////////////
        final Goalstate.GoalState gsPortStateForSwitch = GoalStateUtil.CreateGoalState(
                Common.OperationType.INFO,
                customerSubnetState,
                transitSwitchHostsForSubnet,
                Common.OperationType.CREATE_UPDATE_SWITCH,
                customerPortState,
                epHost);
        // we could parallel here and also try to use async mode for grpc as well
         Arrays.stream(transitSwitchHostsForSubnet).parallel().map(switchForSubnet-> {
             cachedThreadPool.submit(()-> {
            if (isFastPath) {
                logger.log(Level.INFO, "Send port id :" + customerPortState.getConfiguration().getId() + " to transit switch with fast path " + switchForSubnet);
                GoalStateProvisionerClient gRpcClientForSwitchHost = new GoalStateProvisionerClient(switchForSubnet.getIpAddress(), NodeManager.GRPC_SERVER_PORT);
                gRpcClientForSwitchHost.PushNetworkResourceStates(gsPortStateForSwitch);
            } else {
                String topicForSwitch = Config.PRODUCER_CLIENT_ID + switchForSubnet.getIpAddress();
                this.getKafkaClient().runProducer(topicForSwitch, gsPortStateForSwitch);
            }
             });
             return switchForSubnet;
            //wait for the end
         }).collect(Collectors.toList());

        recordedTimeStamp[1] = System.nanoTime();

        ////////////////////////////////////////////////////////////////////////////
        // Step 3: Go to EP host, update_agent_md and update_agent_ep
        ////////////////////////////////////////////////////////////////////////////
        final Goalstate.GoalState gsFinalizedPortState = GoalStateUtil.CreateGoalState(
                Common.OperationType.INFO,
                customerSubnetState,
                transitSwitchHostsForSubnet,
                Common.OperationType.FINALIZE,
                customerPortState,
                epHost);

        if (isFastPath) {
            logger.log(Level.INFO, "Send port id :" + customerPortState.getConfiguration().getId() + " with fast path to ep host");
            this.getGRpcClientForEpHost().PushNetworkResourceStates(gsFinalizedPortState);
        } else {
            String topicForEndpoint = Config.PRODUCER_CLIENT_ID + epHost.getIpAddress();
            this.getKafkaClient().runProducer(topicForEndpoint, gsFinalizedPortState);
        }

        recordedTimeStamp[2] = System.nanoTime();

        return recordedTimeStamp;
    }
}
