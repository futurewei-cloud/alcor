/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.futurewei.alcor.controller.resourcemgr.physical.goalstatemgmt;

import com.futurewei.alcor.controller.comm.config.IKafkaConfiguration;
import com.futurewei.alcor.controller.comm.grpc.GoalStateProvisionerClient;
import com.futurewei.alcor.controller.comm.message.GoalStateMessageConsumerFactory;
import com.futurewei.alcor.controller.comm.message.GoalStateMessageProducerFactory;
import com.futurewei.alcor.controller.comm.message.MessageClient;
import com.futurewei.alcor.controller.logging.Logger;
import com.futurewei.alcor.controller.logging.LoggerFactory;
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

        for (HostInfo switchForSubnet : transitSwitchHostsForSubnet) {
            if (isFastPath) {
                logger.log(Level.INFO, "Send port id :" + customerPortState.getId() + " to transit switch with fast path " + switchForSubnet);
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
