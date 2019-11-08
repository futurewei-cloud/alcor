package com.futurewei.alcor.controller.app.demo;

import com.futurewei.alcor.controller.comm.grpc.GoalStateProvisionerClient;
import com.futurewei.alcor.controller.comm.message.GoalStateMessageConsumerFactory;
import com.futurewei.alcor.controller.comm.message.GoalStateMessageProducerFactory;
import com.futurewei.alcor.controller.comm.message.MessageClient;
import com.futurewei.alcor.controller.model.*;
import com.futurewei.alcor.controller.schema.Common;
import com.futurewei.alcor.controller.schema.Goalstate;
import com.futurewei.alcor.controller.utilities.GoalStateUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.futurewei.alcor.controller.app.demo.DemoConfig.customerPortStates;

// NOTE: This file is only used for demo purpose.
//       Please don't use it in production
public class DemoUtil {

    private static final int THREADS_LIMIT = 50;
    private static final int TIMEOUT = 600;

    public static void CreateSubnet(SubnetState subnetState){

        boolean isFastPath = false;
        MessageClient client = new MessageClient(new GoalStateMessageConsumerFactory(), new GoalStateMessageProducerFactory());

        // This is the combination of all the transit switch hosts

        HostInfo[][] transitSwitchHosts;
        SubnetState customerSubnetState;

        if(subnetState.getId().equalsIgnoreCase(DemoConfig.subnet1Id)){
            transitSwitchHosts = new HostInfo[][] {
                    DemoConfig.transitSwitchHostsForSubnet1,
            };
            customerSubnetState = new SubnetState(DemoConfig.customerSubnetState1);

            isFastPath = true;
        }
        else if ((subnetState.getId().equalsIgnoreCase(DemoConfig.subnet2Id))){
            transitSwitchHosts = new HostInfo[][] {
                    DemoConfig.transitSwitchHostsForSubnet2
            };
            customerSubnetState = new SubnetState(DemoConfig.customerSubnetState2);
        }
        else{
            transitSwitchHosts = new HostInfo[][] {
                    DemoConfig.transitSwitchHosts,
            };
            customerSubnetState = new SubnetState(DemoConfig.customerSubnetState);

            isFastPath = true;
        }

        ////////////////////////////////////////////////////////////////////////////
        // Step 1: Go to switch hosts in current subnet, call update_vpc and update_substrate
        ////////////////////////////////////////////////////////////////////////////
        final Goalstate.GoalState gsVpcState = GoalStateUtil.CreateGoalState(
                Common.OperationType.CREATE_UPDATE_SWITCH,
                DemoConfig.customerVpcState,
                DemoConfig.transitRouterHosts,
                Common.OperationType.CREATE_UPDATE_GATEWAY,
                new SubnetState[]{customerSubnetState},
                transitSwitchHosts);

        for(HostInfo transitSwitch : transitSwitchHosts[0])
        {
            if(isFastPath){
                System.out.println("Send Subnet id :" + subnetState.getId() + " with fast path");
                System.out.println("GS: " + gsVpcState.toString());
                GoalStateProvisionerClient gRpcClientForEpHost = new GoalStateProvisionerClient(DemoConfig.gRPCServerIp, transitSwitch.getGRPCServerPort());
                gRpcClientForEpHost.PushNetworkResourceStates(gsVpcState);
            }
            else{
                String topic = DemoConfig.HOST_ID_PREFIX + transitSwitch.getId();
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

        for(HostInfo transitRouter : DemoConfig.transitRouterHosts){
            if(isFastPath){
                System.out.println("Send VPC id :" + subnetState.getVpcId() + " with fast path");
                GoalStateProvisionerClient gRpcClient = new GoalStateProvisionerClient(DemoConfig.gRPCServerIp, transitRouter.getGRPCServerPort());
                gRpcClient.PushNetworkResourceStates(gsSubnetState);
            }
            else{
                String topic = DemoConfig.HOST_ID_PREFIX + transitRouter.getId();
                client.runProducer(topic, gsSubnetState);
            }
        }
    }

    public static void CreatePortGroup(PortStateGroup portStateGroup){
        List<PortState> portStates = portStateGroup.getPortStates();
        int portCount = portStates.size();
        int epHostCount = DemoConfig.epHosts.size();
        int portCountPerHost = portCount/epHostCount > 0 ? portCount/epHostCount: 1;

        ExecutorService executor = Executors.newFixedThreadPool(THREADS_LIMIT);

        for (int i = 0; i < epHostCount ; i++) {

            if(DemoConfig.IS_PARALLEL){
                final int nodeIndex = i;
                Future<long[]> future = executor.submit(()-> {
                    try{
                        String name = Thread.currentThread().getName();
                        System.out.println("Running on thread " + name);

                        return DemoUtil.CreatePorts(portStates, nodeIndex, nodeIndex*portCountPerHost, (nodeIndex+1)*portCountPerHost);
                    }
                    catch(Exception e){
                        e.printStackTrace();
                        throw new IllegalStateException("programming task interrupted", e);
                    }
                });
            }
            else{
                DemoUtil.CreatePorts(portStates, i, i*portCountPerHost, (i+1)*portCountPerHost);
            }
        }
    }

    public static long[] CreatePorts(List<PortState> portStates, int hostIndex, int epStartIndex, int epEndIndex){

        System.out.println("EP host index :" + hostIndex + "; EP start index: " + epStartIndex + "; end index: " + epEndIndex);
        long[] recordedTimeStamp = new long[3];
        boolean isFastPath = true; //portStates.get(0).isFastPath();

        SubnetState customerSubnetState = DemoConfig.customerSubnetState;
        HostInfo[] transitSwitchHostsForSubnet = DemoConfig.transitSwitchHosts;
        PortState[] customerPortStates = new PortState[epEndIndex-epStartIndex];
        HostInfo epHost = DemoConfig.epHosts.get(hostIndex);

        for (int i = 0; i < epEndIndex-epStartIndex; i++) {
            int epIndex = epStartIndex + i;
            PortState customerPortState = DemoUtil.GeneretePortState(epHost, epIndex);
            customerPortStates[i] = customerPortState;
        }

        GoalStateProvisionerClient gRpcClientForEpHost = new GoalStateProvisionerClient(DemoConfig.gRPCServerIp, epHost.getGRPCServerPort());
        MessageClient kafkaClient = new MessageClient(new GoalStateMessageConsumerFactory(), new GoalStateMessageProducerFactory());
        String topicForEndpoint = DemoConfig.HOST_ID_PREFIX + epHost.getId();

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

        if(isFastPath){
            System.out.println("Sending " + customerPortStates.length + " ports with fast path");
            System.out.println("Sending: " + gsPortState);
            gRpcClientForEpHost.PushNetworkResourceStates(gsPortState);
        }
        else{
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

        for (HostInfo switchForSubnet : transitSwitchHostsForSubnet){
            if(isFastPath){
                System.out.println("Sending " + customerPortStates.length  + " ports to transit switch with fast path");
                System.out.println("Sending: " + gsPortStateForSwitch);
                GoalStateProvisionerClient gRpcClientForSwitchHost = new GoalStateProvisionerClient(DemoConfig.gRPCServerIp, switchForSubnet.getGRPCServerPort());
                gRpcClientForSwitchHost.PushNetworkResourceStates(gsPortStateForSwitch);
            }
            else{
                String topicForSwitch = DemoConfig.HOST_ID_PREFIX + switchForSubnet.getId();
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

        if(isFastPath){
            System.out.println("Sending " + customerPortStates.length  + " with fast path");
            gRpcClientForEpHost.PushNetworkResourceStates(gsFinalizedPortState);
        }
        else{
            kafkaClient.runProducer(topicForEndpoint, gsFinalizedPortState);
        }

        recordedTimeStamp[2] = System.nanoTime();

        return recordedTimeStamp;
    }

    public static long[] CreatePort(PortState portState){

        boolean isFastPath = portState.isFastPath();
        PortState customerPortState;
        HostInfo epHost;
        SubnetState customerSubnetState;
        HostInfo[] transitSwitchHostsForSubnet;

        long[] recordedTimeStamp = new long[3];

        if(portState.getNetworkId().equalsIgnoreCase(DemoConfig.subnet1Id)){
            customerSubnetState = DemoConfig.customerSubnetState1;
            transitSwitchHostsForSubnet = DemoConfig.transitSwitchHostsForSubnet1;
            isFastPath = true;
        }
        else if (portState.getNetworkId().equalsIgnoreCase(DemoConfig.subnet2Id)){
            customerSubnetState = DemoConfig.customerSubnetState2;
            transitSwitchHostsForSubnet = DemoConfig.transitSwitchHostsForSubnet2;
        }
        else{
            customerSubnetState = DemoConfig.customerSubnetState;
            transitSwitchHostsForSubnet = DemoConfig.transitSwitchHosts;
            isFastPath = true;
        }

        if(portState.getId().equalsIgnoreCase(DemoConfig.ep1Id)){
            customerPortState = DemoConfig.customerPortStateForSubnet1[0];
            epHost = DemoConfig.epHostForSubnet1[0];
            isFastPath = true;
        }
        else if(portState.getId().equalsIgnoreCase(DemoConfig.ep2Id)){
            customerPortState = DemoConfig.customerPortStateForSubnet1[1];
            epHost = DemoConfig.epHostForSubnet1[1];
            isFastPath = true;
        }
        else if(portState.getId().equalsIgnoreCase(DemoConfig.ep3Id)){
            customerPortState = DemoConfig.customerPortStateForSubnet1[2];
            epHost = DemoConfig.epHostForSubnet1[2];
            isFastPath = true;
        }
        else if(portState.getId().equalsIgnoreCase(DemoConfig.ep4Id)){
            customerPortState = DemoConfig.customerPortStateForSubnet1[3];
            epHost = DemoConfig.epHostForSubnet1[3];
            isFastPath = true;
        }
        else if(portState.getId().equalsIgnoreCase(DemoConfig.ep5Id)){
            customerPortState = DemoConfig.customerPortStateForSubnet2[0];
            epHost = DemoConfig.epHostForSubnet2[0];
        }
        else if(portState.getId().equalsIgnoreCase(DemoConfig.ep6Id)){
            customerPortState = DemoConfig.customerPortStateForSubnet2[1];
            epHost = DemoConfig.epHostForSubnet2[1];
        }
        else if(portState.getId().equalsIgnoreCase(DemoConfig.ep7Id)){
            customerPortState = DemoConfig.customerPortStateForSubnet2[2];
            epHost = DemoConfig.epHostForSubnet2[2];
        }
        else if(portState.getId().equalsIgnoreCase(DemoConfig.ep8Id)){
            customerPortState = DemoConfig.customerPortStateForSubnet2[3];
            epHost = DemoConfig.epHostForSubnet2[3];
        }
        else{
            System.out.println("EP host counter :" + DemoConfig.epHostCounter);

            epHost = DemoConfig.epHosts.get(DemoConfig.epHostCounter);
            customerPortState = DemoUtil.GeneretePortState(epHost, DemoConfig.epHostCounter);
            DemoConfig.epHostCounter++;
        }

        GoalStateProvisionerClient gRpcClientForEpHost = new GoalStateProvisionerClient(DemoConfig.gRPCServerIp, epHost.getGRPCServerPort());
        MessageClient kafkaClient = new MessageClient(new GoalStateMessageConsumerFactory(), new GoalStateMessageProducerFactory());
        String topicForEndpoint = DemoConfig.HOST_ID_PREFIX + epHost.getId();

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

        if(isFastPath){
            System.out.println("Send port id :" + portState.getId() + " with fast path");
            gRpcClientForEpHost.PushNetworkResourceStates(gsPortState);
        }
        else{
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

        for (HostInfo switchForSubnet : transitSwitchHostsForSubnet){
            if(isFastPath){
                System.out.println("Send port id :" + portState.getId() + " to transit switch with fast path");
                GoalStateProvisionerClient gRpcClientForSwitchHost = new GoalStateProvisionerClient(DemoConfig.gRPCServerIp, switchForSubnet.getGRPCServerPort());
                gRpcClientForSwitchHost.PushNetworkResourceStates(gsPortStateForSwitch);
            }
            else{
                String topicForSwitch = DemoConfig.HOST_ID_PREFIX + switchForSubnet.getId();
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

        if(isFastPath){
            System.out.println("Send port id :" + portState.getId() + " with fast path");
            gRpcClientForEpHost.PushNetworkResourceStates(gsFinalizedPortState);
        }
        else{
            kafkaClient.runProducer(topicForEndpoint, gsFinalizedPortState);
        }

        recordedTimeStamp[2] = System.nanoTime();

        return recordedTimeStamp;
    }

    public static void AssignNodes(List<HostInfo> hosts){
        for (int i = 0; i < hosts.size() ; i++) {
            hosts.get(i).setGRPCServerPort(50001+i);
        }

        DemoConfig.epHosts = new ArrayList<>(hosts);
    }

    // This function generates port state solely based on the container host
    public static PortState GeneretePortState(HostInfo hostInfo, int epIndex){
        return new PortState(DemoConfig.projectId,
                DemoConfig.subnetId,
                hostInfo.getId() + "_" + epIndex,
                hostInfo.getId() + "_" + epIndex,
                GenereateMacAddress(epIndex),
                DemoConfig.VETH_NAME,
                new String[]{GenereateIpAddress(epIndex)});
    }

    private static String GenereateMacAddress(int index){
        return "0e:73:ae:c8:" + Integer.toHexString((index+6)/250) + ":" + Integer.toHexString((index+6)%250);
    }

    private static String GenereateIpAddress(int index){
        return "10.0." + (index+6)/250 + "." + (index+6)%250;
    }

    public static void CreateSubnetLegacy(SubnetState subnetState, VpcState vpcState) {

        //TODO: Algorithm to allocate transit switches and routers
        HostInfo[] transitSwitches = {
                new HostInfo(DemoConfig.TRANSIT_SWTICH_1_HOST_ID, "transit switch host1", DemoConfig.TRANSIT_SWITCH_1_IP, DemoConfig.TRANSIT_SWITCH_1_MAC),
                new HostInfo(DemoConfig.TRANSIT_SWTICH_3_HOST_ID, "transit switch host2", DemoConfig.TRANSIT_SWITCH_3_IP, DemoConfig.TRANSIT_SWITCH_3_MAC)
        };
        HostInfo[] transitRouters = {
                new HostInfo(DemoConfig.TRANSIT_ROUTER_1_HOST_ID, "transit router host1", DemoConfig.TRANSIT_ROUTER_1_IP, DemoConfig.TRANSIT_ROUTER_1_MAC),
                new HostInfo(DemoConfig.TRANSIT_ROUTER_2_HOST_ID, "transit router host2", DemoConfig.TRANSIT_ROUTER_2_IP, DemoConfig.TRANSIT_ROUTER_2_MAC)
        };

        // Generate subnet goal states and send them to all transit routers
        MessageClient client = new MessageClient(new GoalStateMessageConsumerFactory(), new GoalStateMessageProducerFactory());
        Goalstate.GoalState subnetGoalState = GoalStateUtil.CreateGoalState(
                Common.OperationType.CREATE_UPDATE_ROUTER,
                new SubnetState[]{subnetState},
                new HostInfo[][]{transitSwitches});
        for(HostInfo transitRouter : transitRouters){
            String topic = MessageClient.getGoalStateTopic(transitRouter.getId());
            client.runProducer(topic, subnetGoalState);
        }

        // Generate vpc goal states and send them to all transit switches
        Goalstate.GoalState vpcGoalstate = GoalStateUtil.CreateGoalState(
                Common.OperationType.CREATE_UPDATE_SWITCH,
                vpcState,
                transitRouters);
        for(HostInfo transitSwitch : transitSwitches)
        {
            String topic = MessageClient.getGoalStateTopic(transitSwitch.getId());
            client.runProducer(topic, vpcGoalstate);
        }

    }
}
