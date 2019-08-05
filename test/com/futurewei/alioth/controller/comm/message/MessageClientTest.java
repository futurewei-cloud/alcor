package com.futurewei.alioth.controller.comm.message;

import com.futurewei.alioth.controller.comm.config.DemoConfig;
import com.futurewei.alioth.controller.model.HostInfo;
import com.futurewei.alioth.controller.model.SubnetState;
import com.futurewei.alioth.controller.schema.Common;
import com.futurewei.alioth.controller.schema.Goalstate.GoalState;
import com.futurewei.alioth.controller.schema.Subnet;
import com.futurewei.alioth.controller.schema.TestUtil;
import com.futurewei.alioth.controller.schema.Vpc;
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
        String topic = "hostid-bb009e95-3839-4a9d-abd9-9ad70b538112";
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

}