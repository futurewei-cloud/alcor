package com.futurewei.alioth.controller.comm.message;

import com.futurewei.alioth.controller.app.DemoConfig;
import com.futurewei.alioth.controller.model.HostInfo;
import com.futurewei.alioth.controller.model.PortState;
import com.futurewei.alioth.controller.model.SubnetState;
import com.futurewei.alioth.controller.model.VpcState;
import com.futurewei.alioth.controller.schema.*;
import com.futurewei.alioth.controller.schema.Goalstate.GoalState;
import com.futurewei.alioth.controller.utilities.GoalStateUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class MessageClientTest {

    @Test
    public void runConsumer() {
    }

    @Test
    public void runProducer() {
    }

    @Test
    public void vpcCreateUpdateE2EVerification() {
        final Vpc.VpcState vpc_state = GoalStateUtil.CreateGSVpcState(Common.OperationType.CREATE,
                "dbf72700-5106-4a7a-918f-a016853911f8",
                "99d9d709-8478-4b46-9f3f-2206b1023fd3",
                "SuperVpc",
                "192.168.0.0/24");

        final Vpc.VpcState vpc_state2 = GoalStateUtil.CreateGSVpcState(Common.OperationType.UPDATE,
                "92ced20a-7b7f-47f0-818d-69a296144c52",
                "92ced20a-7b7f-47f0-818d-69a296144c52",
                "MiniVpc",
                "192.168.1.0/29");

        GoalState goalstate = GoalState.newBuilder()
                .addVpcStates(vpc_state).addVpcStates(vpc_state2)
                .build();

        MessageClient client = new MessageClient(new GoalStateMessageConsumerFactory(), new GoalStateMessageProducerFactory());
        String topic = "hostid-controller_vpc_test";
        client.runProducer(topic, goalstate);
        List goalStateList = client.runConsumer(topic, true);

        Assert.assertEquals("invalid message count", 1, goalStateList.size());
        GoalState receivedGoalState = (GoalState) goalStateList.get(0);

        Assert.assertEquals("invalid vpc state count", 2, receivedGoalState.getVpcStatesCount());
        Assert.assertEquals("invalid subnet state count", 0, receivedGoalState.getSubnetStatesCount());
        Assert.assertEquals("invalid port state count", 0, receivedGoalState.getPortStatesCount());
        Assert.assertEquals("invalid security group state count", 0, receivedGoalState.getSecurityGroupStatesCount());

        TestUtil.AssertVpcStates(vpc_state, receivedGoalState.getVpcStates(0));
        TestUtil.AssertVpcStates(vpc_state2, receivedGoalState.getVpcStates(1));

        try {

            TestUtil.AssertVpcStates(vpc_state, receivedGoalState.getVpcStates(1));
            Assert.assertTrue(false);
        } catch (AssertionError assertionError){
            //catch expected exception
            Assert.assertTrue(true);
        }
    }

    @Test
    public void subnetCreateUpdateE2EVerification() {
        SubnetState customerSubnetState = new SubnetState("dbf72700-5106-4a7a-918f-a016853911f8",
                "99d9d709-8478-4b46-9f3f-2206b1023fd3",
                "d973934b-93e8-42fa-ac91-bf0cdb84fffc",
                "Subnet1",
                "10.0.0.0/24");

        HostInfo[] transitSwitches = {
                new HostInfo("subnet1-ts1", "transit switch host1", new byte[]{10,0,0,1}, "fa:16:3e:d7:f1:04"),
                new HostInfo("subnet1-ts2", "transit switch host2", new byte[]{10,0,0,2}, "fa:16:3e:d7:f1:05")
        };

        final Subnet.SubnetState subnetState1 = GoalStateUtil.CreateGSSubnetState(Common.OperationType.CREATE,
                customerSubnetState,
                transitSwitches);

        SubnetState customerSubnetState2 = new SubnetState("dbf72700-5106-4a7a-918f-a016853911f8",
                "99d9d709-8478-4b46-9f3f-2206b1023fd3",
                "8cb94df3-05bd-45d1-95c0-1ad75f929810",
                "Subnet2",
                "10.0.1.0/24");

        HostInfo[] transitSwitches2 = {
                new HostInfo("subnet2-ts1", "transit switch host1", new byte[]{10,0,1,1}, "fa:16:3e:d7:f1:06"),
                new HostInfo("subnet2-ts2", "transit switch host2", new byte[]{10,0,1,2}, "fa:16:3e:d7:f1:07")
        };

        final Subnet.SubnetState subnetState2 = GoalStateUtil.CreateGSSubnetState(Common.OperationType.CREATE,
                customerSubnetState2,
                transitSwitches2);

        GoalState goalstate = GoalState.newBuilder()
                .addSubnetStates(subnetState1).addSubnetStates(subnetState2)
                .build();

        MessageClient client = new MessageClient(new GoalStateMessageConsumerFactory(), new GoalStateMessageProducerFactory());
        String topic = "hostid-controller_subnet_test";
        client.runProducer(topic, goalstate);
        List goalStateList = client.runConsumer(topic, true);

        Assert.assertEquals("invalid message count", 1, goalStateList.size());
        GoalState receivedGoalState = (GoalState) goalStateList.get(0);

        Assert.assertEquals("invalid vpc state count", 0, receivedGoalState.getVpcStatesCount());
        Assert.assertEquals("invalid subnet state count", 2, receivedGoalState.getSubnetStatesCount());
        Assert.assertEquals("invalid port state count", 0, receivedGoalState.getPortStatesCount());
        Assert.assertEquals("invalid security group state count", 0, receivedGoalState.getSecurityGroupStatesCount());

        TestUtil.AssertSubnetStates(subnetState1, receivedGoalState.getSubnetStates(0));
        TestUtil.AssertSubnetStates(subnetState2, receivedGoalState.getSubnetStates(1));

        try {

            TestUtil.AssertSubnetStates(subnetState1, receivedGoalState.getSubnetStates(1));
            Assert.assertTrue(false);
        } catch (AssertionError assertionError){
            //catch expected exception
            Assert.assertTrue(true);
        }
    }

    @Test
    public void createOneVpcTwoSubnetsFourPortsE2EVerification() {

        ////////////////////////////////////////////////////////////////////////////
        // Scenario: Create one VPC and two subnets, Subnet1 and Subnet2
        //           Subnet1 has one transit switch S1 and two endpoints EP1 and EP2
        //           Subnet2 has one transit switch S2 and two endpoints EP3 and EP4
        ////////////////////////////////////////////////////////////////////////////
        String projectId = "dbf72700-5106-4a7a-918f-a016853911f8";
        String vpcId = "99d9d709-8478-4b46-9f3f-2206b1023fd3";
        String subnet1Id = "d973934b-93e8-42fa-ac91-bf0cdb84fffc";
        String subnet2Id = "8cb94df3-05bd-45d1-95c0-1ad75f929810";
        String ep1Id = "89e72582-b4fc-4e4e-b46a-6eee650e03f5";
        String ep2Id = "34bf0cec-0969-4635-b9a9-dd32611f35a4";
        String ep3Id = "364d2bbd-2def-4c70-9965-9ffd2165f43a";
        String ep4Id = "c60fe503-88a2-4198-a3be-85c197acd9db";
        HostInfo[] transitRouterHosts = {
                new HostInfo("vpc1-transit-router1", "transit router1 host", DemoConfig.TRANSIT_ROUTER_1_IP, DemoConfig.TRANSIT_ROUTER_1_MAC)
        };

        HostInfo[] transitSwitchHostsForSubnet1 = {
                new HostInfo("subnet1-transit-switch1","transit switch1 host for subnet1", DemoConfig.TRANSIT_SWITCH_1_IP, DemoConfig.TRANSIT_SWITCH_1_MAC),
        };
        HostInfo[] transitSwitchHostsForSubnet2 = {
                new HostInfo("subnet2-transit-switch1","transit switch1 host for subnet2", DemoConfig.TRANSIT_SWITCH_2_IP, DemoConfig.TRANSIT_SWITCH_2_MAC)
        };

        // This is the combination of the two arrays above
        HostInfo[] transitSwitchHosts = {
                new HostInfo("subnet1-transit-switch1","transit switch1 host for subnet1", DemoConfig.TRANSIT_SWITCH_1_IP, DemoConfig.TRANSIT_SWITCH_1_MAC),
                new HostInfo("subnet2-transit-switch1","transit switch1 host for subnet2", DemoConfig.TRANSIT_SWITCH_2_IP, DemoConfig.TRANSIT_SWITCH_2_MAC)
        };

        MessageClient client = new MessageClient(new GoalStateMessageConsumerFactory(), new GoalStateMessageProducerFactory());

        ////////////////////////////////////////////////////////////////////////////
        // Step 1: Go to S1 host and S2 host, call update_vpc and update_substrate
        ////////////////////////////////////////////////////////////////////////////
        VpcState customerVpcState =
                new VpcState(projectId, vpcId,
                        "SuperVpc",
                        "10.0.0.0/24");

        final GoalState gsVpcState = GoalStateUtil.CreateGoalState(Common.OperationType.CREATE_UPDATE_SWITCH,
                customerVpcState,
                transitRouterHosts);

        for(HostInfo transitSwitch : transitSwitchHosts)
        {
            String topic = DemoConfig.HOST_ID_PREFIX + transitSwitch.getId();
            client.runProducer(topic, gsVpcState);
            List goalStateList = client.runConsumer(topic, true);

            Assert.assertEquals("invalid message count", 1, goalStateList.size());
            GoalState receivedGoalState = (GoalState) goalStateList.get(0);

            Assert.assertEquals("invalid vpc state count", 1, receivedGoalState.getVpcStatesCount());
            Assert.assertEquals("invalid subnet state count", 0, receivedGoalState.getSubnetStatesCount());
            Assert.assertEquals("invalid port state count", 0, receivedGoalState.getPortStatesCount());
            Assert.assertEquals("invalid security group state count", 0, receivedGoalState.getSecurityGroupStatesCount());
            TestUtil.AssertVpcStates(gsVpcState.getVpcStates(0), receivedGoalState.getVpcStates(0));
        }

        ////////////////////////////////////////////////////////////////////////////
        // Step 2: Go to R1 host, call update_substrate only
        ////////////////////////////////////////////////////////////////////////////
        SubnetState customerSubnetState1 = new SubnetState(projectId, vpcId, subnet1Id,
                "Subnet1",
                "10.0.0.0/24");
        SubnetState customerSubnetState2 = new SubnetState(projectId, vpcId, subnet2Id,
                "Subnet2",
                "10.0.1.0/24");

        final GoalState gsSubnetState = GoalStateUtil.CreateGoalState(
                Common.OperationType.CREATE_UPDATE_ROUTER,
                new SubnetState[]{customerSubnetState1, customerSubnetState2},
                transitSwitchHosts);

        for(HostInfo transitRouter : transitRouterHosts){
            String topic = DemoConfig.HOST_ID_PREFIX + transitRouter.getId();
            client.runProducer(topic, gsSubnetState);
            List goalStateList = client.runConsumer(topic, true);

            Assert.assertEquals("invalid message count", 1, goalStateList.size());
            GoalState receivedGoalState = (GoalState) goalStateList.get(0);

            Assert.assertEquals("invalid vpc state count", 0, receivedGoalState.getVpcStatesCount());
            Assert.assertEquals("invalid subnet state count", 2, receivedGoalState.getSubnetStatesCount());
            Assert.assertEquals("invalid port state count", 0, receivedGoalState.getPortStatesCount());
            Assert.assertEquals("invalid security group state count", 0, receivedGoalState.getSecurityGroupStatesCount());

            TestUtil.AssertSubnetStates(gsSubnetState.getSubnetStates(0), receivedGoalState.getSubnetStates(0));
            TestUtil.AssertSubnetStates(gsSubnetState.getSubnetStates(1), receivedGoalState.getSubnetStates(1));

            try {
                TestUtil.AssertSubnetStates(gsSubnetState.getSubnetStates(0), receivedGoalState.getSubnetStates(1));
                Assert.assertTrue(false);
            } catch (AssertionError assertionError){
                //catch expected exception
                Assert.assertTrue(true);
            }
        }

        ////////////////////////////////////////////////////////////////////////////
        // Step 3: Go to EP1 host and EP2 host, update_endpoint
        //         Go to EP3 host and EP4 host, update_endpoint
        ////////////////////////////////////////////////////////////////////////////
        PortState[] customerPortStateForSubnet1 = {
                new PortState(projectId, subnet1Id, ep1Id,
                        DemoConfig.EP1_ID,
                        "fa:16:3e:d7:f0:00",
                        DemoConfig.VNET_NAME,
                        new String[]{"10.0.0.1"}),
                new PortState(projectId, subnet1Id, ep2Id,
                        DemoConfig.EP2_ID,
                        "fa:16:3e:d7:f0:01",
                        DemoConfig.VNET_NAME,
                        new String[]{"10.0.0.2"})
        };

        HostInfo[] epHostForSubnet1 = {
                new HostInfo("subnet1-ep1", "ep1 host", DemoConfig.EP1_HOST_IP, DemoConfig.EP1_HOST_MAC),
                new HostInfo("subnet1-ep2", "ep2 host", DemoConfig.EP2_HOST_IP, DemoConfig.EP2_HOST_MAC)
        };

        for (int i=0; i<customerPortStateForSubnet1.length; i++){
            final GoalState gsPortState = GoalStateUtil.CreateGoalState(
                    Common.OperationType.INFO,
                    customerSubnetState1,
                    transitSwitchHostsForSubnet1,
                    Common.OperationType.CREATE,
                    customerPortStateForSubnet1[i],
                    epHostForSubnet1[i]);

            String topic = DemoConfig.HOST_ID_PREFIX + epHostForSubnet1[i].getId();
            client.runProducer(topic, gsPortState);
            List goalStateList = client.runConsumer(topic, true);

            Assert.assertEquals("invalid message count", 1, goalStateList.size());
            GoalState receivedGoalState = (GoalState) goalStateList.get(0);

            Assert.assertEquals("invalid vpc state count", 0, receivedGoalState.getVpcStatesCount());
            Assert.assertEquals("invalid subnet state count", 1, receivedGoalState.getSubnetStatesCount());
            Assert.assertEquals("invalid port state count", 1, receivedGoalState.getPortStatesCount());
            Assert.assertEquals("invalid security group state count", 0, receivedGoalState.getSecurityGroupStatesCount());

            TestUtil.AssertSubnetStates(gsPortState.getSubnetStates(0), receivedGoalState.getSubnetStates(0));
            TestUtil.AssertPortStates(gsPortState.getPortStates(0), receivedGoalState.getPortStates(0));
        }

        PortState[] customerPortStateForSubnet2 = {
                new PortState(projectId, subnet2Id, ep3Id,
                        DemoConfig.EP3_ID,
                        "fa:16:3e:d7:f0:02",
                        DemoConfig.VNET_NAME,
                        new String[]{"10.0.1.1"}),
                new PortState(projectId, subnet2Id, ep4Id,
                        DemoConfig.EP4_ID,
                        "fa:16:3e:d7:f0:03",
                        DemoConfig.VNET_NAME,
                        new String[]{"10.0.1.2"})
        };

        HostInfo[] epHostForSubnet2 = {
                new HostInfo("subnet2-ep1", "ep3 host", DemoConfig.EP3_HOST_IP, DemoConfig.EP3_HOST_MAC),
                new HostInfo("subnet2-ep2", "ep4 host", DemoConfig.EP4_HOST_IP, DemoConfig.EP4_HOST_MAC)
        };

        for (int i=0; i<customerPortStateForSubnet2.length; i++){
            final GoalState gsPortState = GoalStateUtil.CreateGoalState(
                    Common.OperationType.INFO,
                    customerSubnetState2,
                    transitSwitchHostsForSubnet2,
                    Common.OperationType.CREATE,
                    customerPortStateForSubnet2[i],
                    epHostForSubnet2[i]);

            String topic = DemoConfig.HOST_ID_PREFIX + epHostForSubnet2[i].getId();
            client.runProducer(topic, gsPortState);
            List goalStateList = client.runConsumer(topic, true);

            Assert.assertEquals("invalid message count", 1, goalStateList.size());
            GoalState receivedGoalState = (GoalState) goalStateList.get(0);

            Assert.assertEquals("invalid vpc state count", 0, receivedGoalState.getVpcStatesCount());
            Assert.assertEquals("invalid subnet state count", 1, receivedGoalState.getSubnetStatesCount());
            Assert.assertEquals("invalid port state count", 1, receivedGoalState.getPortStatesCount());
            Assert.assertEquals("invalid security group state count", 0, receivedGoalState.getSecurityGroupStatesCount());

            TestUtil.AssertSubnetStates(gsPortState.getSubnetStates(0), receivedGoalState.getSubnetStates(0));
            TestUtil.AssertPortStates(gsPortState.getPortStates(0), receivedGoalState.getPortStates(0));
        }


        ////////////////////////////////////////////////////////////////////////////
        // Step 4: Go to S1 host and S2 host, update_ep and update_substrate
        ////////////////////////////////////////////////////////////////////////////
        final GoalState gsPortStateForSubnet1 = GoalStateUtil.CreateGoalState(
                Common.OperationType.INFO,
                customerSubnetState1,
                transitSwitchHostsForSubnet1,
                Common.OperationType.CREATE_UPDATE_SWITCH,
                customerPortStateForSubnet1,
                epHostForSubnet1);

        for (HostInfo switchForSubnet1 : transitSwitchHostsForSubnet1){
            String topic = DemoConfig.HOST_ID_PREFIX + switchForSubnet1.getId();
            client.runProducer(topic, gsPortStateForSubnet1);
            List goalStateList = client.runConsumer(topic, true);

            Assert.assertEquals("invalid message count", 1, goalStateList.size());
            GoalState receivedGoalState = (GoalState) goalStateList.get(0);

            Assert.assertEquals("invalid vpc state count", 0, receivedGoalState.getVpcStatesCount());
            Assert.assertEquals("invalid subnet state count", 1, receivedGoalState.getSubnetStatesCount());
            Assert.assertEquals("invalid port state count", 2, receivedGoalState.getPortStatesCount());
            Assert.assertEquals("invalid security group state count", 0, receivedGoalState.getSecurityGroupStatesCount());

            TestUtil.AssertSubnetStates(gsPortStateForSubnet1.getSubnetStates(0), receivedGoalState.getSubnetStates(0));
            TestUtil.AssertPortStates(gsPortStateForSubnet1.getPortStates(0), receivedGoalState.getPortStates(0));
            TestUtil.AssertPortStates(gsPortStateForSubnet1.getPortStates(1), receivedGoalState.getPortStates(1));
        }

        final GoalState gsPortStateForSubnet2 = GoalStateUtil.CreateGoalState(
                Common.OperationType.INFO,
                customerSubnetState2,
                transitSwitchHostsForSubnet2,
                Common.OperationType.CREATE_UPDATE_SWITCH,
                customerPortStateForSubnet2,
                epHostForSubnet2);

        for (HostInfo switchForSubnet2 : transitSwitchHostsForSubnet2){
            String topic = DemoConfig.HOST_ID_PREFIX + switchForSubnet2.getId();
            client.runProducer(topic, gsPortStateForSubnet2);
            List goalStateList = client.runConsumer(topic, true);

            Assert.assertEquals("invalid message count", 1, goalStateList.size());
            GoalState receivedGoalState = (GoalState) goalStateList.get(0);

            Assert.assertEquals("invalid vpc state count", 0, receivedGoalState.getVpcStatesCount());
            Assert.assertEquals("invalid subnet state count", 1, receivedGoalState.getSubnetStatesCount());
            Assert.assertEquals("invalid port state count", 2, receivedGoalState.getPortStatesCount());
            Assert.assertEquals("invalid security group state count", 0, receivedGoalState.getSecurityGroupStatesCount());

            TestUtil.AssertSubnetStates(gsPortStateForSubnet2.getSubnetStates(0), receivedGoalState.getSubnetStates(0));
            TestUtil.AssertPortStates(gsPortStateForSubnet2.getPortStates(0), receivedGoalState.getPortStates(0));
            TestUtil.AssertPortStates(gsPortStateForSubnet2.getPortStates(1), receivedGoalState.getPortStates(1));
        }


        ////////////////////////////////////////////////////////////////////////////
        // Step 5: Go to EP1 host and EP2 host, update_agent_md and update_agent_ep
        //         Go to EP3 host and EP4 host, update_agent_md and update_agent_ep
        ////////////////////////////////////////////////////////////////////////////

        for (int i=0; i<customerPortStateForSubnet1.length; i++){
            final GoalState gsPortState = GoalStateUtil.CreateGoalState(
                    Common.OperationType.INFO,
                    customerSubnetState1,
                    transitSwitchHostsForSubnet1,
                    Common.OperationType.FINALIZE,
                    customerPortStateForSubnet1[i],
                    epHostForSubnet1[i]);

            String topic = DemoConfig.HOST_ID_PREFIX + epHostForSubnet1[i].getId();
            client.runProducer(topic, gsPortState);
            List goalStateList = client.runConsumer(topic, true);

            Assert.assertEquals("invalid message count", 1, goalStateList.size());
            GoalState receivedGoalState = (GoalState) goalStateList.get(0);

            Assert.assertEquals("invalid vpc state count", 0, receivedGoalState.getVpcStatesCount());
            Assert.assertEquals("invalid subnet state count", 1, receivedGoalState.getSubnetStatesCount());
            Assert.assertEquals("invalid port state count", 1, receivedGoalState.getPortStatesCount());
            Assert.assertEquals("invalid security group state count", 0, receivedGoalState.getSecurityGroupStatesCount());

            TestUtil.AssertSubnetStates(gsPortState.getSubnetStates(0), receivedGoalState.getSubnetStates(0));
            TestUtil.AssertPortStates(gsPortState.getPortStates(0), receivedGoalState.getPortStates(0));
        }

        for (int i=0; i<customerPortStateForSubnet2.length; i++){
            final GoalState gsPortState = GoalStateUtil.CreateGoalState(
                    Common.OperationType.INFO,
                    customerSubnetState2,
                    transitSwitchHostsForSubnet2,
                    Common.OperationType.FINALIZE,
                    customerPortStateForSubnet2[i],
                    epHostForSubnet2[i]);

            String topic = DemoConfig.HOST_ID_PREFIX + epHostForSubnet2[i].getId();
            client.runProducer(topic, gsPortState);
            List goalStateList = client.runConsumer(topic, true);

            Assert.assertEquals("invalid message count", 1, goalStateList.size());
            GoalState receivedGoalState = (GoalState) goalStateList.get(0);

            Assert.assertEquals("invalid vpc state count", 0, receivedGoalState.getVpcStatesCount());
            Assert.assertEquals("invalid subnet state count", 1, receivedGoalState.getSubnetStatesCount());
            Assert.assertEquals("invalid port state count", 1, receivedGoalState.getPortStatesCount());
            Assert.assertEquals("invalid security group state count", 0, receivedGoalState.getSecurityGroupStatesCount());

            TestUtil.AssertSubnetStates(gsPortState.getSubnetStates(0), receivedGoalState.getSubnetStates(0));
            TestUtil.AssertPortStates(gsPortState.getPortStates(0), receivedGoalState.getPortStates(0));
        }

    }

}