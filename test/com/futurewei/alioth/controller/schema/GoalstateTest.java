package com.futurewei.alioth.controller.schema;

import com.futurewei.alioth.controller.schema.Vpc.VpcState;
import com.futurewei.alioth.controller.schema.Goalstate.GoalState;
import com.futurewei.alioth.controller.utilities.GoalStateUtil;
import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.Assert;
import org.junit.Test;

public class GoalstateTest {
    @Test
    public void basicSerializationVerification() {
        final VpcState vpc_state = GoalStateUtil.CreateGSVpcState(Common.OperationType.CREATE,
                "dbf72700-5106-4a7a-918f-a016853911f8",
                "99d9d709-8478-4b46-9f3f-2206b1023fd3",
                "SuperVpc",
                "192.168.0.0/24");

        final VpcState vpc_state2 = GoalStateUtil.CreateGSVpcState(Common.OperationType.UPDATE,
                "92ced20a-7b7f-47f0-818d-69a296144c52",
                "92ced20a-7b7f-47f0-818d-69a296144c52",
                "MiniVpc",
                "192.168.1.0/28");

        GoalState goalstate = GoalState.newBuilder()
                .addVpcStates(vpc_state).addVpcStates(vpc_state2)
                .build();

        final byte[] binaryState = goalstate.toByteArray();

        try {
            final GoalState deserializedObject = GoalState.parseFrom(binaryState);

            Assert.assertEquals("invalid vpc state count", 2, deserializedObject.getVpcStatesCount());
            Assert.assertEquals("invalid subnet state count", 0, deserializedObject.getSubnetStatesCount());
            Assert.assertEquals("invalid port state count", 0, deserializedObject.getPortStatesCount());
            Assert.assertEquals("invalid security group state count", 0, deserializedObject.getSecurityGroupStatesCount());

            TestUtil.AssertVpcStates(vpc_state, deserializedObject.getVpcStates(0));
            TestUtil.AssertVpcStates(vpc_state2, deserializedObject.getVpcStates(1));

            TestUtil.AssertVpcStates(vpc_state, deserializedObject.getVpcStates(1));
            Assert.assertTrue(false);
        } catch(InvalidProtocolBufferException bf_exp) {
            Assert.assertTrue(false);
        } catch (AssertionError assertionError){
            //catch expected exception
            Assert.assertTrue(true);
        }
    }

}