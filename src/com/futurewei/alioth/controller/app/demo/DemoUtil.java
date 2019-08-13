package com.futurewei.alioth.controller.app.demo;

import com.futurewei.alioth.controller.comm.message.GoalStateMessageConsumerFactory;
import com.futurewei.alioth.controller.comm.message.GoalStateMessageProducerFactory;
import com.futurewei.alioth.controller.comm.message.MessageClient;
import com.futurewei.alioth.controller.model.HostInfo;
import com.futurewei.alioth.controller.model.PortState;
import com.futurewei.alioth.controller.model.SubnetState;
import com.futurewei.alioth.controller.model.VpcState;
import com.futurewei.alioth.controller.schema.Common;
import com.futurewei.alioth.controller.schema.Goalstate;
import com.futurewei.alioth.controller.utilities.GoalStateUtil;

public class DemoUtil {

    private static String hostIdPrefix = "es2-";
    private static String projectId = "dbf72700-5106-4a7a-918f-a016853911f8";
    private static String vpcId = "99d9d709-8478-4b46-9f3f-2206b1023fd3";
    private static String subnet1Id = "d973934b-93e8-42fa-ac91-bf0cdb84fffc";
    private static String subnet2Id = "8cb94df3-05bd-45d1-95c0-1ad75f929810";
    private static String ep1Id = "89e72582-b4fc-4e4e-b46a-6eee650e03f5";
    private static String ep2Id = "34bf0cec-0969-4635-b9a9-dd32611f35a4";
    private static String ep3Id = "64353fd7-b60c-4108-93ff-ecaa6b63a6a3";
    private static String ep4Id = "cae2df90-4a50-437e-a3f2-e3b742c8fbf8";
    private static String ep5Id = "364d2bbd-2def-4c70-9965-9ffd2165f43a";
    private static String ep6Id = "c60fe503-88a2-4198-a3be-85c197acd9db";
    private static String ep7Id = "38e45f95-5ea7-4d0a-9027-886febc27bdc";
    private static String ep8Id = "b81abf49-87ab-4a58-b457-93dc5a0dabac";

    private static VpcState customerVpcState =
            new VpcState(projectId, vpcId,
                    "SuperVpc",
                    "10.0.0.0/16");

    private static SubnetState customerSubnetState1 = new SubnetState(projectId, vpcId, subnet1Id,
            "Subnet1",
            "10.0.0.0/24");
    private static SubnetState customerSubnetState2 = new SubnetState(projectId, vpcId, subnet2Id,
            "Subnet2",
            "10.0.1.0/24");

    private static PortState[] customerPortStateForSubnet1 = {
            new PortState(projectId, subnet1Id, ep1Id,
                    DemoConfig.EP1_ID,
                    "0e:73:ae:c8:87:00",
                    DemoConfig.VNET_NAME,
                    new String[]{"10.0.0.1"}),
            new PortState(projectId, subnet1Id, ep2Id,
                    DemoConfig.EP2_ID,
                    "0e:73:ae:c8:87:01",
                    DemoConfig.VNET_NAME,
                    new String[]{"10.0.0.2"}),
            new PortState(projectId, subnet1Id, ep3Id,
                    DemoConfig.EP3_ID,
                    "0e:73:ae:c8:87:02",
                    DemoConfig.VNET_NAME,
                    new String[]{"10.0.0.3"}),
            new PortState(projectId, subnet1Id, ep4Id,
                    DemoConfig.EP4_ID,
                    "0e:73:ae:c8:87:03",
                    DemoConfig.VNET_NAME,
                    new String[]{"10.0.0.4"})
    };

    private static PortState[] customerPortStateForSubnet2 = {
            new PortState(projectId, subnet2Id, ep5Id,
                    DemoConfig.EP5_ID,
                    "0e:73:ae:c8:87:04",
                    DemoConfig.VNET_NAME,
                    new String[]{"10.0.1.1"}),
            new PortState(projectId, subnet2Id, ep6Id,
                    DemoConfig.EP6_ID,
                    "0e:73:ae:c8:87:05",
                    DemoConfig.VNET_NAME,
                    new String[]{"10.0.1.2"}),
            new PortState(projectId, subnet2Id, ep7Id,
                    DemoConfig.EP7_ID,
                    "0e:73:ae:c8:87:06",
                    DemoConfig.VNET_NAME,
                    new String[]{"10.0.1.3"}),
            new PortState(projectId, subnet2Id, ep8Id,
                    DemoConfig.EP8_ID,
                    "0e:73:ae:c8:87:07",
                    DemoConfig.VNET_NAME,
                    new String[]{"10.0.1.4"})
    };

    private static HostInfo[] transitRouterHosts = {
            new HostInfo("vpc1-transit-router1", "transit router1 host", DemoConfig.TRANSIT_ROUTER_1_IP, DemoConfig.TRANSIT_ROUTER_1_MAC),
            new HostInfo("vpc1-transit-router2", "transit router2 host", DemoConfig.TRANSIT_ROUTER_2_IP, DemoConfig.TRANSIT_ROUTER_2_MAC)
    };

    private static HostInfo[] transitSwitchHostsForSubnet1 = {
            new HostInfo("subnet1-transit-switch1","transit switch1 host for subnet1", DemoConfig.TRANSIT_SWITCH_1_IP, DemoConfig.TRANSIT_SWITCH_1_MAC),
            new HostInfo("subnet1-transit-switch2","transit switch2 host for subnet1", DemoConfig.TRANSIT_SWITCH_2_IP, DemoConfig.TRANSIT_SWITCH_2_MAC)
    };

    private static HostInfo[] transitSwitchHostsForSubnet2 = {
            new HostInfo("subnet2-transit-switch1","transit switch1 host for subnet2", DemoConfig.TRANSIT_SWITCH_3_IP, DemoConfig.TRANSIT_SWITCH_3_MAC),
            new HostInfo("subnet2-transit-switch2","transit switch2 host for subnet2", DemoConfig.TRANSIT_SWITCH_4_IP, DemoConfig.TRANSIT_SWITCH_4_MAC)
    };

    private static HostInfo[] epHostForSubnet1 = {
            new HostInfo("subnet1-ep1", "ep1 host", DemoConfig.EP1_HOST_IP, DemoConfig.EP1_HOST_MAC),
            new HostInfo("subnet1-ep2", "ep2 host", DemoConfig.EP2_HOST_IP, DemoConfig.EP2_HOST_MAC),
            new HostInfo("subnet1-ep3", "ep3 host", DemoConfig.EP3_HOST_IP, DemoConfig.EP3_HOST_MAC),
            new HostInfo("subnet1-ep4", "ep4 host", DemoConfig.EP4_HOST_IP, DemoConfig.EP4_HOST_MAC),
    };

    private static HostInfo[] epHostForSubnet2 = {
            new HostInfo("subnet2-ep1", "ep5 host", DemoConfig.EP5_HOST_IP, DemoConfig.EP5_HOST_MAC),
            new HostInfo("subnet2-ep2", "ep6 host", DemoConfig.EP6_HOST_IP, DemoConfig.EP6_HOST_MAC),
            new HostInfo("subnet2-ep3", "ep7 host", DemoConfig.EP7_HOST_IP, DemoConfig.EP7_HOST_MAC),
            new HostInfo("subnet2-ep4", "ep8 host", DemoConfig.EP8_HOST_IP, DemoConfig.EP8_HOST_MAC),
    };

    public static void CreateSubnet(SubnetState subnetState){

        MessageClient client = new MessageClient(new GoalStateMessageConsumerFactory(), new GoalStateMessageProducerFactory());

        // This is the combination of all the transit switch hosts

        HostInfo[][] transitSwitchHosts;
        SubnetState customerSubnetState;

        if(subnetState.getId().equalsIgnoreCase(subnet1Id)){
            transitSwitchHosts = new HostInfo[][] {
                    transitSwitchHostsForSubnet1,
            };
            customerSubnetState = new SubnetState(customerSubnetState1);
        }
        else{
            transitSwitchHosts = new HostInfo[][] {
                    transitSwitchHostsForSubnet2
            };
            customerSubnetState = new SubnetState(customerSubnetState2);
        }

        ////////////////////////////////////////////////////////////////////////////
        // Step 1: Go to switch hosts in current subnet, call update_vpc and update_substrate
        ////////////////////////////////////////////////////////////////////////////
        final Goalstate.GoalState gsVpcState = GoalStateUtil.CreateGoalState(Common.OperationType.CREATE_UPDATE_SWITCH,
                customerVpcState,
                transitRouterHosts);

        for(HostInfo transitSwitch : transitSwitchHosts[0])
        {
            String topic = hostIdPrefix + transitSwitch.getId();
            client.runProducer(topic, gsVpcState);
        }

        ////////////////////////////////////////////////////////////////////////////
        // Step 2: Go to router hosts in current vpc, call update_substrate only
        ////////////////////////////////////////////////////////////////////////////
        final Goalstate.GoalState gsSubnetState = GoalStateUtil.CreateGoalState(
                Common.OperationType.CREATE_UPDATE_ROUTER,
                new SubnetState[]{customerSubnetState},
                transitSwitchHosts);

        for(HostInfo transitRouter : transitRouterHosts){
            String topic = hostIdPrefix + transitRouter.getId();
            client.runProducer(topic, gsSubnetState);
        }
    }

    public static void CreatePort(PortState portState){

        PortState customerPortState;
        HostInfo epHost;
        SubnetState customerSubnetState;
        HostInfo[] transitSwitchHostsForSubnet;

        if(portState.getNetworkId().equalsIgnoreCase(subnet1Id)){
            customerSubnetState = customerSubnetState1;
            transitSwitchHostsForSubnet = transitSwitchHostsForSubnet1;
        }
        else{
            customerSubnetState = customerSubnetState2;
            transitSwitchHostsForSubnet = transitSwitchHostsForSubnet2;
        }

        if(portState.getId().equalsIgnoreCase(ep1Id)){
            customerPortState = customerPortStateForSubnet1[0];
            epHost = epHostForSubnet1[0];
        }
        else if(portState.getId().equalsIgnoreCase(ep2Id)){
            customerPortState = customerPortStateForSubnet1[1];
            epHost = epHostForSubnet1[1];
        }
        else if(portState.getId().equalsIgnoreCase(ep3Id)){
            customerPortState = customerPortStateForSubnet1[2];
            epHost = epHostForSubnet1[2];
        }
        else if(portState.getId().equalsIgnoreCase(ep4Id)){
            customerPortState = customerPortStateForSubnet1[3];
            epHost = epHostForSubnet1[3];
        }
        else if(portState.getId().equalsIgnoreCase(ep5Id)){
            customerPortState = customerPortStateForSubnet2[0];
            epHost = epHostForSubnet2[0];
        }
        else if(portState.getId().equalsIgnoreCase(ep6Id)){
            customerPortState = customerPortStateForSubnet2[1];
            epHost = epHostForSubnet2[1];
        }
        else if(portState.getId().equalsIgnoreCase(ep7Id)){
            customerPortState = customerPortStateForSubnet2[2];
            epHost = epHostForSubnet2[2];
        }
        else{
            customerPortState = customerPortStateForSubnet2[3];
            epHost = epHostForSubnet2[3];
        }

        MessageClient client = new MessageClient(new GoalStateMessageConsumerFactory(), new GoalStateMessageProducerFactory());

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

        String topicForEndpoint = hostIdPrefix + epHost.getId();
        client.runProducer(topicForEndpoint, gsPortState);

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
            String topicForSwitch = hostIdPrefix + switchForSubnet.getId();
            client.runProducer(topicForSwitch, gsPortStateForSwitch);
        }

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

        client.runProducer(topicForEndpoint, gsFinalizedPortState);
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
