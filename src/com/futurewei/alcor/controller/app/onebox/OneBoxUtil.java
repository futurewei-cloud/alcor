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

package com.futurewei.alcor.controller.app.onebox;

import com.futurewei.alcor.controller.comm.grpc.GoalStateProvisionerClient;
import com.futurewei.alcor.controller.comm.message.GoalStateMessageConsumerFactory;
import com.futurewei.alcor.controller.comm.message.GoalStateMessageProducerFactory;
import com.futurewei.alcor.controller.comm.message.MessageClient;
import com.futurewei.alcor.controller.model.*;
import com.futurewei.alcor.controller.schema.Common;
import com.futurewei.alcor.controller.schema.Goalstate;
import com.futurewei.alcor.controller.utilities.GoalStateUtil;
import org.apache.catalina.Host;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

// NOTE: This file is only used for demo purpose.
//       Please don't use it in production
public class OneBoxUtil {

    private static final int THREADS_LIMIT = 100;
    private static final int TIMEOUT = 600;

    public static void CreateSubnet(SubnetState subnetState) {

        boolean isFastPath = false;
        MessageClient client = new MessageClient(new GoalStateMessageConsumerFactory(), new GoalStateMessageProducerFactory());

        // This is the combination of all the transit switch hosts

        HostInfo[][] transitSwitchHosts;
        SubnetState customerSubnetState;

        if (subnetState.getId().equalsIgnoreCase(OneBoxConfig.subnet1Id)) {
            transitSwitchHosts = new HostInfo[][]{
                    OneBoxConfig.transitSwitchHostsForSubnet1,
            };
            customerSubnetState = new SubnetState(OneBoxConfig.customerSubnetState1);

            isFastPath = true;
        } else if ((subnetState.getId().equalsIgnoreCase(OneBoxConfig.subnet2Id))) {
            transitSwitchHosts = new HostInfo[][]{
                    OneBoxConfig.transitSwitchHostsForSubnet2
            };
            customerSubnetState = new SubnetState(OneBoxConfig.customerSubnetState2);
        } else {
            transitSwitchHosts = new HostInfo[][]{
                    OneBoxConfig.transitSwitchHosts,
            };
            customerSubnetState = new SubnetState(OneBoxConfig.customerSubnetState);

            isFastPath = true;
        }

        ////////////////////////////////////////////////////////////////////////////
        // Step 1: Go to switch hosts in current subnet, call update_vpc and update_substrate
        ////////////////////////////////////////////////////////////////////////////
        final Goalstate.GoalState gsVpcState = GoalStateUtil.CreateGoalState(
                Common.OperationType.CREATE_UPDATE_SWITCH,
                OneBoxConfig.customerVpcState,
                OneBoxConfig.transitRouterHosts,
                Common.OperationType.CREATE_UPDATE_GATEWAY,
                new SubnetState[]{customerSubnetState},
                transitSwitchHosts);

        for (HostInfo transitSwitch : transitSwitchHosts[0]) {
            if (isFastPath) {
                System.out.println("Send Subnet id :" + subnetState.getId() + " with fast path");
                System.out.println("GS: " + gsVpcState.toString());
                GoalStateProvisionerClient gRpcClientForEpHost = new GoalStateProvisionerClient(OneBoxConfig.gRPCServerIp, transitSwitch.getGRPCServerPort());
                gRpcClientForEpHost.PushNetworkResourceStates(gsVpcState);
            } else {
                String topic = OneBoxConfig.HOST_ID_PREFIX + transitSwitch.getId();
                client.runProducer(topic, gsVpcState);
            }
        }

        ////////////////////////////////////////////////////////////////////////////
        // Step 2: Go to router hosts in current vpc, call update_substrate only
        ////////////////////////////////////////////////////////////////////////////
        final Goalstate.GoalState gsSubnetState = GoalStateUtil.CreateGoalState(
                Common.OperationType.CREATE_UPDATE_ROUTER,
                new SubnetState[]{customerSubnetState},
                transitSwitchHosts);

        for (HostInfo transitRouter : OneBoxConfig.transitRouterHosts) {
            if (isFastPath) {
                System.out.println("Send VPC id :" + subnetState.getVpcId() + " with fast path");
                GoalStateProvisionerClient gRpcClient = new GoalStateProvisionerClient(OneBoxConfig.gRPCServerIp, transitRouter.getGRPCServerPort());
                gRpcClient.PushNetworkResourceStates(gsSubnetState);
            } else {
                String topic = OneBoxConfig.HOST_ID_PREFIX + transitRouter.getId();
                client.runProducer(topic, gsSubnetState);
            }
        }
    }

    public static long[][] CreatePortGroup(PortStateGroup portStateGroup) {
        List<PortState> portStates = portStateGroup.getPortStates();
        int portCount = portStates.size();
        int epHostCount = OneBoxConfig.epHosts.size();
        int portCountPerHost = portCount / epHostCount > 0 ? portCount / epHostCount : 1;

        long[][] results = new long[epHostCount][];

        ExecutorService executor = Executors.newFixedThreadPool(THREADS_LIMIT);
        CompletionService<long[]> goalStateProgrammingService = new ExecutorCompletionService<long[]>(executor);

        for (int i = 0; i < epHostCount; i++) {

            if (OneBoxConfig.IS_PARALLEL) {
                final int nodeIndex = i;

                goalStateProgrammingService.submit(new Callable<long[]>() {
                    @Override
                    public long[] call() throws IllegalStateException {
                        String name = Thread.currentThread().getName();
                        System.out.println("Running on thread " + name);

                        return OneBoxUtil.CreatePorts(portStates, nodeIndex, nodeIndex * portCountPerHost, (nodeIndex + 1) * portCountPerHost);
                    }
                });
            } else {
                long[] times = OneBoxUtil.CreatePorts(portStates, i, i * portCountPerHost, (i + 1) * portCountPerHost);
                results[i] = times;
            }
        }

        int received = 0;
        boolean errors = false;

        while (OneBoxConfig.IS_PARALLEL && received < epHostCount && !errors) {
            try {
                Future<long[]> resultFuture = goalStateProgrammingService.take();
                long[] result = resultFuture.get();
                results[received] = result;
                received++;
            } catch (Exception e) {
                e.printStackTrace();
                errors = true;
            }
        }

        return results;
    }

    public static long[] CreatePorts(List<PortState> portStates, int hostIndex, int epStartIndex, int epEndIndex) {

        System.out.println("EP host index :" + hostIndex + "; EP start index: " + epStartIndex + "; end index: " + epEndIndex);
        long[] recordedTimeStamp = new long[3];
        boolean isFastPath = true; //portStates.get(0).isFastPath();

        SubnetState customerSubnetState = OneBoxConfig.customerSubnetState;
        HostInfo[] transitSwitchHostsForSubnet = OneBoxConfig.transitSwitchHosts;
        PortState[] customerPortStates = new PortState[epEndIndex - epStartIndex];
        HostInfo epHost = OneBoxConfig.epHosts.get(hostIndex);

        for (int i = 0; i < epEndIndex - epStartIndex; i++) {
            int epIndex = epStartIndex + i;
            PortState customerPortState = OneBoxUtil.GeneretePortState(epHost, epIndex);
            customerPortStates[i] = customerPortState;
        }

        GoalStateProvisionerClient gRpcClientForEpHost = new GoalStateProvisionerClient(OneBoxConfig.gRPCServerIp, epHost.getGRPCServerPort());
        MessageClient kafkaClient = new MessageClient(new GoalStateMessageConsumerFactory(), new GoalStateMessageProducerFactory());
        String topicForEndpoint = OneBoxConfig.HOST_ID_PREFIX + epHost.getId();

        ////////////////////////////////////////////////////////////////////////////
        // Step 1: Go to EP host, update_endpoint
        ////////////////////////////////////////////////////////////////////////////
        final Goalstate.GoalState gsPortState = GoalStateUtil.CreateGoalState(
                Common.OperationType.INFO,
                customerSubnetState,
                transitSwitchHostsForSubnet,
                Common.OperationType.CREATE,
                customerPortStates,
                epHost);

        if (isFastPath) {
            System.out.println("Sending " + customerPortStates.length + " ports with fast path");
            System.out.println("Sending: " + gsPortState);
            gRpcClientForEpHost.PushNetworkResourceStates(gsPortState);
        } else {
            kafkaClient.runProducer(topicForEndpoint, gsPortState);
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
                customerPortStates,
                epHost);

        for (HostInfo switchForSubnet : transitSwitchHostsForSubnet) {
            if (isFastPath) {
                System.out.println("Sending " + customerPortStates.length + " ports to transit switch with fast path");
                System.out.println("Sending: " + gsPortStateForSwitch);
                GoalStateProvisionerClient gRpcClientForSwitchHost = new GoalStateProvisionerClient(OneBoxConfig.gRPCServerIp, switchForSubnet.getGRPCServerPort());
                gRpcClientForSwitchHost.PushNetworkResourceStates(gsPortStateForSwitch);
            } else {
                String topicForSwitch = OneBoxConfig.HOST_ID_PREFIX + switchForSubnet.getId();
                kafkaClient.runProducer(topicForSwitch, gsPortStateForSwitch);
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
                customerPortStates,
                epHost);

        if (isFastPath) {
            System.out.println("Sending " + customerPortStates.length + " with fast path");
            gRpcClientForEpHost.PushNetworkResourceStates(gsFinalizedPortState);
        } else {
            kafkaClient.runProducer(topicForEndpoint, gsFinalizedPortState);
        }

        recordedTimeStamp[2] = System.nanoTime();

        return recordedTimeStamp;
    }

    public static long[] CreatePort(PortState portState) {

        boolean isFastPath = portState.isFastPath();
        PortState customerPortState;
        HostInfo epHost;
        SubnetState customerSubnetState;
        HostInfo[] transitSwitchHostsForSubnet;

        long[] recordedTimeStamp = new long[3];

        if (portState.getNetworkId().equalsIgnoreCase(OneBoxConfig.subnet1Id)) {
            customerSubnetState = OneBoxConfig.customerSubnetState1;
            transitSwitchHostsForSubnet = OneBoxConfig.transitSwitchHostsForSubnet1;
            isFastPath = true;
        } else if (portState.getNetworkId().equalsIgnoreCase(OneBoxConfig.subnet2Id)) {
            customerSubnetState = OneBoxConfig.customerSubnetState2;
            transitSwitchHostsForSubnet = OneBoxConfig.transitSwitchHostsForSubnet2;
        } else {
            customerSubnetState = OneBoxConfig.customerSubnetState;
            transitSwitchHostsForSubnet = OneBoxConfig.transitSwitchHosts;
            isFastPath = true;
        }

        if (portState.getId().equalsIgnoreCase(OneBoxConfig.ep1Id)) {
            System.out.println("check input id :" + portState.getId());
            customerPortState = OneBoxConfig.customerPortStateForSubnet1[0];
            System.out.println("check name :" + customerPortState.getName());
            epHost = OneBoxConfig.epHostForSubnet1[0];
            isFastPath = true;
        } else if (portState.getId().equalsIgnoreCase(OneBoxConfig.ep2Id)) {
            customerPortState = OneBoxConfig.customerPortStateForSubnet1[1];
            epHost = OneBoxConfig.epHostForSubnet1[1];
            isFastPath = true;
        } else if (portState.getId().equalsIgnoreCase(OneBoxConfig.ep3Id)) {
            customerPortState = OneBoxConfig.customerPortStateForSubnet1[2];
            epHost = OneBoxConfig.epHostForSubnet1[2];
            isFastPath = true;
        } else if (portState.getId().equalsIgnoreCase(OneBoxConfig.ep4Id)) {
            customerPortState = OneBoxConfig.customerPortStateForSubnet1[3];
            epHost = OneBoxConfig.epHostForSubnet1[3];
            isFastPath = true;
        } else if (portState.getId().equalsIgnoreCase(OneBoxConfig.ep5Id)) {
            customerPortState = OneBoxConfig.customerPortStateForSubnet2[0];
            epHost = OneBoxConfig.epHostForSubnet2[0];
        } else if (portState.getId().equalsIgnoreCase(OneBoxConfig.ep6Id)) {
            customerPortState = OneBoxConfig.customerPortStateForSubnet2[1];
            epHost = OneBoxConfig.epHostForSubnet2[1];
        } else if (portState.getId().equalsIgnoreCase(OneBoxConfig.ep7Id)) {
            customerPortState = OneBoxConfig.customerPortStateForSubnet2[2];
            epHost = OneBoxConfig.epHostForSubnet2[2];
        } else if (portState.getId().equalsIgnoreCase(OneBoxConfig.ep8Id)) {
            customerPortState = OneBoxConfig.customerPortStateForSubnet2[3];
            epHost = OneBoxConfig.epHostForSubnet2[3];
        } else {
            System.out.println("EP host counter :" + OneBoxConfig.epHostCounter + "| ep counter: " + OneBoxConfig.epCounter);

            epHost = OneBoxConfig.epHosts.get(OneBoxConfig.epHostCounter);
            customerPortState = OneBoxUtil.GeneretePortState(epHost, OneBoxConfig.epCounter);

            OneBoxConfig.epCounter++;
            if (OneBoxConfig.epCounter % OneBoxConfig.EP_PER_HOST == 0) {
                OneBoxConfig.epHostCounter++;
            }
        }

        System.out.println("EP :" + customerPortState.getId() + " name " + customerPortState.getName());

        GoalStateProvisionerClient gRpcClientForEpHost = new GoalStateProvisionerClient(OneBoxConfig.gRPCServerIp, epHost.getGRPCServerPort());
        MessageClient kafkaClient = new MessageClient(new GoalStateMessageConsumerFactory(), new GoalStateMessageProducerFactory());
        String topicForEndpoint = OneBoxConfig.HOST_ID_PREFIX + epHost.getId();

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
            System.out.println("Send port id :" + portState.getId() + " with fast path");
            gRpcClientForEpHost.PushNetworkResourceStates(gsPortState);
        } else {
            kafkaClient.runProducer(topicForEndpoint, gsPortState);
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
                System.out.println("Send port id :" + portState.getId() + " to transit switch with fast path");
                GoalStateProvisionerClient gRpcClientForSwitchHost = new GoalStateProvisionerClient(OneBoxConfig.gRPCServerIp, switchForSubnet.getGRPCServerPort());
                gRpcClientForSwitchHost.PushNetworkResourceStates(gsPortStateForSwitch);
            } else {
                String topicForSwitch = OneBoxConfig.HOST_ID_PREFIX + switchForSubnet.getId();
                kafkaClient.runProducer(topicForSwitch, gsPortStateForSwitch);
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
            System.out.println("Send port id :" + portState.getId() + " with fast path");
            gRpcClientForEpHost.PushNetworkResourceStates(gsFinalizedPortState);
        } else {
            kafkaClient.runProducer(topicForEndpoint, gsFinalizedPortState);
        }

        recordedTimeStamp[2] = System.nanoTime();

        return recordedTimeStamp;
    }

    public static List<HostInfo> AssignNodes(List<HostInfo> hosts) {
        List<HostInfo> hostInfoList = new ArrayList<>(hosts);
        for (int i = 0; i < hostInfoList.size(); i++) {
            HostInfo host = hostInfoList.get(i);
            host.setGRPCServerPort(OneBoxConfig.GRPC_SERVER_PORT + i);
        }

        return hostInfoList;
    }

    // This function generates port state solely based on the container host
    public static PortState GeneretePortState(HostInfo hostInfo, int epIndex) {
        return new PortState(OneBoxConfig.projectId,
                OneBoxConfig.subnetId,
                epIndex + "_" + hostInfo.getId(),
                epIndex + "_" + hostInfo.getId(),
                GenereateMacAddress(epIndex),
                OneBoxConfig.VETH_NAME,
                new String[]{GenereateIpAddress(epIndex)});
    }

    private static String GenereateMacAddress(int index) {
        return "0e:73:ae:c8:" + Integer.toHexString((index + 6) / 256) + ":" + Integer.toHexString((index + 6) % 256);
    }

    private static String GenereateIpAddress(int index) {
        return "10.0." + (index + 6) / 256 + "." + (index + 6) % 256;
    }

    public static void CreateSubnetLegacy(SubnetState subnetState, VpcState vpcState) {

        //TODO: Algorithm to allocate transit switches and routers
        HostInfo[] transitSwitches = {
                new HostInfo(OneBoxConfig.TRANSIT_SWTICH_1_HOST_ID, "transit switch host1", OneBoxConfig.TRANSIT_SWITCH_1_IP, OneBoxConfig.TRANSIT_SWITCH_1_MAC),
                new HostInfo(OneBoxConfig.TRANSIT_SWTICH_3_HOST_ID, "transit switch host2", OneBoxConfig.TRANSIT_SWITCH_3_IP, OneBoxConfig.TRANSIT_SWITCH_3_MAC)
        };
        HostInfo[] transitRouters = {
                new HostInfo(OneBoxConfig.TRANSIT_ROUTER_1_HOST_ID, "transit router host1", OneBoxConfig.TRANSIT_ROUTER_1_IP, OneBoxConfig.TRANSIT_ROUTER_1_MAC),
                new HostInfo(OneBoxConfig.TRANSIT_ROUTER_2_HOST_ID, "transit router host2", OneBoxConfig.TRANSIT_ROUTER_2_IP, OneBoxConfig.TRANSIT_ROUTER_2_MAC)
        };

        // Generate subnet goal states and send them to all transit routers
        MessageClient client = new MessageClient(new GoalStateMessageConsumerFactory(), new GoalStateMessageProducerFactory());
        Goalstate.GoalState subnetGoalState = GoalStateUtil.CreateGoalState(
                Common.OperationType.CREATE_UPDATE_ROUTER,
                new SubnetState[]{subnetState},
                new HostInfo[][]{transitSwitches});
        for (HostInfo transitRouter : transitRouters) {
            String topic = MessageClient.getGoalStateTopic(transitRouter.getId());
            client.runProducer(topic, subnetGoalState);
        }

        // Generate vpc goal states and send them to all transit switches
        Goalstate.GoalState vpcGoalstate = GoalStateUtil.CreateGoalState(
                Common.OperationType.CREATE_UPDATE_SWITCH,
                vpcState,
                transitRouters);
        for (HostInfo transitSwitch : transitSwitches) {
            String topic = MessageClient.getGoalStateTopic(transitSwitch.getId());
            client.runProducer(topic, vpcGoalstate);
        }

    }
}
