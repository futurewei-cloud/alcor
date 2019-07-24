package com.futurewei.alioth.controller.schema;

import com.futurewei.alioth.controller.schema.Vpc.VpcState;
import com.futurewei.alioth.controller.utilities.GoalStateUtil;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.Assert;
import org.junit.Test;

import javax.sql.rowset.spi.TransactionalWriter;

public class VpcTest {

    @Test
    public void basicSerializationVerification() {
        final VpcState state = GoalStateUtil.CreateVpcState(Common.OperationType.CREATE,
                "dbf72700-5106-4a7a-918f-a016853911f8",
                "99d9d709-8478-4b46-9f3f-2206b1023fd3",
                "SuperVpc",
                "192.168.0.0/24");

        final byte[] binaryState = state.toByteArray();

        try {
            final VpcState deserializedObject = VpcState.parseFrom(binaryState);

            TestUtil.AssertVpcStates(state, deserializedObject);
        } catch(InvalidProtocolBufferException bf_exp) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void stringSerializationVerification() {
        final VpcState state = GoalStateUtil.CreateVpcState(Common.OperationType.DELETE,
                "dbf72700-5106-4a7a-918f-a016853911f8",
                "99d9d709-8478-4b46-9f3f-2206b1023fd3",
                "SuperVpc",
                "192.168.0.0/24");

        final ByteString byteStringState = state.toByteString();

        try {
            final VpcState deserializedObject = VpcState.parseFrom(byteStringState);

            TestUtil.AssertVpcStates(state, deserializedObject);
        } catch (InvalidProtocolBufferException bf_exp) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void serializationVerificationWithTransitRouterIps() {
        final VpcState state = GoalStateUtil.CreateVpcState(Common.OperationType.CREATE,
                "dbf72700-5106-4a7a-918f-a016853911f8",
                "99d9d709-8478-4b46-9f3f-2206b1023fd3",
                "SuperVpc",
                "192.168.0.0/24",
                "10.0.0.1",
                "10.0.0.3");

        final byte[] binaryState = state.toByteArray();

        try {
            final VpcState deserializedObject = VpcState.parseFrom(binaryState);

            TestUtil.AssertVpcStates(state, deserializedObject);
        } catch(InvalidProtocolBufferException bf_exp) {
            Assert.assertTrue(false);
        }
    }
}