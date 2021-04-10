/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
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
import com.futurewei.alcor.controller.model.SubnetState;
import com.futurewei.alcor.controller.model.VpcState;
import com.futurewei.alcor.controller.schema.Common;
import com.futurewei.alcor.controller.schema.Goalstate;
import com.futurewei.alcor.controller.utilities.GoalStateUtil;

import java.util.logging.Level;

public class SubnetGoalStateProgrammer extends GoalStateProgrammer {

    private SubnetProgramInfo subnetProgramInfo;
    private boolean isFastPath;

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
