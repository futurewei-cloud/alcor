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

package com.futurewei.alcor.controller.schema;

import com.futurewei.alcor.controller.app.onebox.DemoConfig;
import com.futurewei.alcor.controller.model.HostInfo;
import com.futurewei.alcor.controller.model.VpcState;
import com.futurewei.alcor.controller.utilities.GoalStateUtil;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.Assert;
import org.junit.Test;

public class VpcTest {

    @Test
    public void basicSerializationVerification() {
        final Vpc.VpcState state = GoalStateUtil.CreateGSVpcState(Common.OperationType.CREATE,
                "dbf72700-5106-4a7a-918f-a016853911f8",
                "99d9d709-8478-4b46-9f3f-2206b1023fd3",
                "SuperVpc",
                "192.168.0.0/24");

        final byte[] binaryState = state.toByteArray();

        try {
            final Vpc.VpcState deserializedObject = Vpc.VpcState.parseFrom(binaryState);

            TestUtil.AssertVpcStates(state, deserializedObject);
        } catch (InvalidProtocolBufferException bf_exp) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void stringSerializationVerification() {
        final Vpc.VpcState state = GoalStateUtil.CreateGSVpcState(Common.OperationType.DELETE,
                "dbf72700-5106-4a7a-918f-a016853911f8",
                "99d9d709-8478-4b46-9f3f-2206b1023fd3",
                "SuperVpc",
                "192.168.0.0/24");

        final ByteString byteStringState = state.toByteString();

        try {
            final Vpc.VpcState deserializedObject = Vpc.VpcState.parseFrom(byteStringState);

            TestUtil.AssertVpcStates(state, deserializedObject);
        } catch (InvalidProtocolBufferException bf_exp) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void serializationVerificationWithTransitRouterIps() {
        VpcState customerVpcState =
                new VpcState("dbf72700-5106-4a7a-918f-a016853911f8",
                        "99d9d709-8478-4b46-9f3f-2206b1023fd3",
                        "SuperVpc",
                        "10.0.0.0/24");
        HostInfo[] transitRouterHosts = {
                new HostInfo(DemoConfig.TRANSIT_ROUTER_1_HOST_ID, "transit router host1", DemoConfig.TRANSIT_ROUTER_1_IP, DemoConfig.TRANSIT_ROUTER_1_MAC),
                new HostInfo(DemoConfig.TRANSIT_ROUTER_2_HOST_ID, "transit router host2", DemoConfig.TRANSIT_ROUTER_2_IP, DemoConfig.TRANSIT_ROUTER_2_MAC)
        };

        final Vpc.VpcState state = GoalStateUtil.CreateGSVpcState(Common.OperationType.CREATE,
                customerVpcState,
                transitRouterHosts);

        final byte[] binaryState = state.toByteArray();

        try {
            final Vpc.VpcState deserializedObject = Vpc.VpcState.parseFrom(binaryState);

            TestUtil.AssertVpcStates(state, deserializedObject);
        } catch (InvalidProtocolBufferException bf_exp) {
            Assert.assertTrue(false);
        }
    }
}