package com.futurewei.alioth.controller.schema;

import com.futurewei.alioth.controller.app.DemoConfig;
import com.futurewei.alioth.controller.model.HostInfo;
import com.futurewei.alioth.controller.schema.Vpc.VpcState;
import com.futurewei.alioth.controller.utilities.GoalStateUtil;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.Assert;
import org.junit.Test;

public class VpcTest {

    @Test
    public void basicSerializationVerification() {
        final VpcState state = GoalStateUtil.CreateGSVpcState(Common.OperationType.CREATE,
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
        final VpcState state = GoalStateUtil.CreateGSVpcState(Common.OperationType.DELETE,
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
        com.futurewei.alioth.controller.model.VpcState customerVpcState =
                new com.futurewei.alioth.controller.model.VpcState("dbf72700-5106-4a7a-918f-a016853911f8",
                "99d9d709-8478-4b46-9f3f-2206b1023fd3",
                "SuperVpc",
                "10.0.0.0/24");
        HostInfo[] transitRouterHosts = {
                new HostInfo(DemoConfig.TRANSIT_ROUTER_1_HOST_ID, "transit router host1", DemoConfig.TRANSIT_ROUTER_1_IP, DemoConfig.TRANSIT_ROUTER_1_MAC),
                new HostInfo(DemoConfig.TRANSIT_ROUTER_2_HOST_ID, "transit router host2", DemoConfig.TRANSIT_ROUTER_2_IP, DemoConfig.TRANSIT_ROUTER_2_MAC)
        };

        final VpcState state = GoalStateUtil.CreateGSVpcState(Common.OperationType.CREATE,
                customerVpcState,
                transitRouterHosts);

        final byte[] binaryState = state.toByteArray();

        try {
            final VpcState deserializedObject = VpcState.parseFrom(binaryState);

            TestUtil.AssertVpcStates(state, deserializedObject);
        } catch(InvalidProtocolBufferException bf_exp) {
            Assert.assertTrue(false);
        }
    }
}