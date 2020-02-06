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
package com.futurewei.alcor.controller.resourcemgr.physical.goalstatemgmt;

import com.futurewei.alcor.controller.comm.config.IKafkaConfiguration;
import com.futurewei.alcor.controller.comm.grpc.GoalStateProvisionerClient;
import com.futurewei.alcor.controller.comm.message.GoalStateMessageConsumerFactory;
import com.futurewei.alcor.controller.comm.message.GoalStateMessageProducerFactory;
import com.futurewei.alcor.controller.comm.message.MessageClient;
import com.futurewei.alcor.controller.logging.Log;
import com.futurewei.alcor.controller.logging.LogFactory;
import com.futurewei.alcor.controller.model.HostInfo;
import com.futurewei.alcor.controller.model.PortState;
import com.futurewei.alcor.controller.model.SubnetState;
import com.futurewei.alcor.controller.schema.Common;
import com.futurewei.alcor.controller.schema.Goalstate;
import com.futurewei.alcor.controller.utilities.GoalStateUtil;
import lombok.Data;

import java.util.logging.Level;

@Data
public class PortGoalStateProgrammer extends GoalStateProgrammer {

    private PortProgramInfo portProgramInfo;

    public PortGoalStateProgrammer(PortProgramInfo portProgramInfo) {
        this.portProgramInfo = portProgramInfo;
    }

    @Override
    public long[] SendGoalStateToHosts() {

        long[] recordedTimeStamp = new long[3];
        Log alcorLog = LogFactory.getLog();

        PortState customerPortState = this.portProgramInfo.getCustomerPortState();
        HostInfo epHost = this.portProgramInfo.getEpHost();
        SubnetState customerSubnetState = this.portProgramInfo.getCustomerSubnetState();
        HostInfo[] transitSwitchHostsForSubnet = this.portProgramInfo.getTransitSwitchHosts();

        boolean isFastPath = customerPortState.isFastPath();
        if (!isFastPath) {
            this.setKafkaClient(new MessageClient(new GoalStateMessageConsumerFactory(), new GoalStateMessageProducerFactory()));
        }
        alcorLog.log(Level.INFO,"EP :" + customerPortState.getId() + "|name:" + customerPortState.getName() + "| fastpath: " + isFastPath);

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

        for (HostInfo switchForSubnet : transitSwitchHostsForSubnet) {
            if (isFastPath) {
                alcorLog.log(Level.INFO, "Send port id :" + customerPortState.getId() + " to transit switch with fast path " + switchForSubnet);
                GoalStateProvisionerClient gRpcClientForSwitchHost = new GoalStateProvisionerClient(switchForSubnet.getHostIpAddress(), switchForSubnet.getGRPCServerPort());
                gRpcClientForSwitchHost.PushNetworkResourceStates(gsPortStateForSwitch);
            } else {
                String topicForSwitch = IKafkaConfiguration.PRODUCER_CLIENT_ID + switchForSubnet.getId();
                this.getKafkaClient().runProducer(topicForSwitch, gsPortStateForSwitch);
            }
        }

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
            alcorLog.log(Level.INFO, "Send port id :" + customerPortState.getId() + " with fast path to ep host");
            this.getGRpcClientForEpHost().PushNetworkResourceStates(gsFinalizedPortState);
        } else {
            String topicForEndpoint = IKafkaConfiguration.PRODUCER_CLIENT_ID + epHost.getId();
            this.getKafkaClient().runProducer(topicForEndpoint, gsFinalizedPortState);
        }

        recordedTimeStamp[2] = System.nanoTime();

        return recordedTimeStamp;
    }
}
