package com.futurewei.alioth.controller.comm.message;

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
        final Subnet.SubnetState subnet_state1 = GoalStateUtil.CreateGSSubnetState(Common.OperationType.CREATE,
                "dbf72700-5106-4a7a-918f-a016853911f8",
                "99d9d709-8478-4b46-9f3f-2206b1023fd3",
                "d973934b-93e8-42fa-ac91-bf0cdb84fffc",
                "Subnet1",
                "192.168.0.0/24",
                "192.168.0.1",
                "192.168.0.2");

        final Subnet.SubnetState subnet_state2 = GoalStateUtil.CreateGSSubnetState(Common.OperationType.CREATE,
                "dbf72700-5106-4a7a-918f-a016853911f8",
                "99d9d709-8478-4b46-9f3f-2206b1023fd3",
                "8cb94df3-05bd-45d1-95c0-1ad75f929810",
                "Subnet2",
                "192.168.1.0/24",
                "192.168.1.1",
                "192.168.1.3");

        GoalState goalstate = GoalState.newBuilder()
                .addSubnetStates(subnet_state1).addSubnetStates(subnet_state2)
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

        TestUtil.AssertSubnetStates(subnet_state1, receivedGoalState.getSubnetStates(0));
        TestUtil.AssertSubnetStates(subnet_state2, receivedGoalState.getSubnetStates(1));

        try {

            TestUtil.AssertSubnetStates(subnet_state1, receivedGoalState.getSubnetStates(1));
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