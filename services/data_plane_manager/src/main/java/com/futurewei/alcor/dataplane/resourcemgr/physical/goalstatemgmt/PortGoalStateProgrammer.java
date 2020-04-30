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
package com.futurewei.alcor.dataplane.resourcemgr.physical.goalstatemgmt;

import com.futurewei.alcor.dataplane.comm.config.IKafkaConfiguration;
import com.futurewei.alcor.dataplane.comm.grpc.GoalStateProvisionerClient;
import com.futurewei.alcor.dataplane.comm.message.GoalStateMessageConsumerFactory;
import com.futurewei.alcor.dataplane.comm.message.GoalStateMessageProducerFactory;
import com.futurewei.alcor.dataplane.comm.message.MessageClient;
import com.futurewei.alcor.dataplane.logging.Logger;
import com.futurewei.alcor.dataplane.logging.LoggerFactory;
import com.futurewei.alcor.dataplane.model.HostInfo;
import com.futurewei.alcor.dataplane.model.PortState;
import com.futurewei.alcor.dataplane.model.SubnetState;
import com.futurewei.alcor.schema.Common;
import com.futurewei.alcor.schema.Goalstate;
import com.futurewei.alcor.dataplane.utilities.GoalStateUtil;
import lombok.Data;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Data
public class PortGoalStateProgrammer extends GoalStateProgrammer {

    private PortProgramInfo portProgramInfo;
    private ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    public PortGoalStateProgrammer(PortProgramInfo portProgramInfo) {
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

        boolean isFastPath = customerPortState.isFastPath();
        if (!isFastPath) {
            this.setKafkaClient(new MessageClient(new GoalStateMessageConsumerFactory(), new GoalStateMessageProducerFactory()));
        }
        logger.log(Level.INFO, "EP :" + customerPortState.getId() + "|name:" + customerPortState.getName() + "| fastpath: " + isFastPath);

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
            System.out.println("Send port id :" + customerPortState.getId() + " with fast path to ep host " + epHost);
            this.setGRpcClientForEpHost(new GoalStateProvisionerClient(epHost.getHostIpAddress(), epHost.getGRPCServerPort()));
            this.getGRpcClientForEpHost().PushNetworkResourceStates(gsPortState);
        } else {
            String topicForEndpoint = IKafkaConfiguration.PRODUCER_CLIENT_ID + epHost.getId();
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
                logger.log(Level.INFO, "Send port id :" + customerPortState.getId() + " to transit switch with fast path " + switchForSubnet);
                GoalStateProvisionerClient gRpcClientForSwitchHost = new GoalStateProvisionerClient(switchForSubnet.getHostIpAddress(), switchForSubnet.getGRPCServerPort());
                gRpcClientForSwitchHost.PushNetworkResourceStates(gsPortStateForSwitch);
            } else {
                String topicForSwitch = IKafkaConfiguration.PRODUCER_CLIENT_ID + switchForSubnet.getId();
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
            logger.log(Level.INFO, "Send port id :" + customerPortState.getId() + " with fast path to ep host");
            this.getGRpcClientForEpHost().PushNetworkResourceStates(gsFinalizedPortState);
        } else {
            String topicForEndpoint = IKafkaConfiguration.PRODUCER_CLIENT_ID + epHost.getId();
            this.getKafkaClient().runProducer(topicForEndpoint, gsFinalizedPortState);
        }

        recordedTimeStamp[2] = System.nanoTime();

        return recordedTimeStamp;
    }
}
