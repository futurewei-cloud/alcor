package com.futurewei.alioth.controller.schema;

import com.futurewei.alioth.controller.utilities.GoalStateUtil;
import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.Assert;
import org.junit.Test;

public class SubnetTest {
    @Test
    public void basicSerializationVerification() {
        final Subnet.SubnetState state = GoalStateUtil.CreateSubnetState(Common.OperationType.CREATE,
                "dbf72700-5106-4a7a-918f-a016853911f8",
                "99d9d709-8478-4b46-9f3f-2206b1023fd3",
                "d973934b-93e8-42fa-ac91-bf0cdb84fffc",
                "Subnet1",
                "192.168.0.0/28",
                "192.168.0.1",
                "192.168.0.2");

        final byte[] binaryState = state.toByteArray();

        try {
            final Subnet.SubnetState deserializedObject = Subnet.SubnetState.parseFrom(binaryState);

            TestUtil.AssertSubnetStates(state, deserializedObject);
        } catch(InvalidProtocolBufferException bf_exp) {
            Assert.assertTrue(false);
        }
    }
}
