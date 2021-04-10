/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/


package com.futurewei.alcor.controller.schema;

import com.futurewei.alcor.controller.app.onebox.OneBoxConfig;
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
                new HostInfo(OneBoxConfig.TRANSIT_ROUTER_1_HOST_ID, "transit router host1", OneBoxConfig.TRANSIT_ROUTER_1_IP, OneBoxConfig.TRANSIT_ROUTER_1_MAC),
                new HostInfo(OneBoxConfig.TRANSIT_ROUTER_2_HOST_ID, "transit router host2", OneBoxConfig.TRANSIT_ROUTER_2_IP, OneBoxConfig.TRANSIT_ROUTER_2_MAC)
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