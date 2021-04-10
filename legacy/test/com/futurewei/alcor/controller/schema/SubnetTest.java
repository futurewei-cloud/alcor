/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package com.futurewei.alcor.controller.schema;

import com.futurewei.alcor.controller.model.SubnetState;
import com.futurewei.alcor.controller.app.onebox.OneBoxConfig;
import com.futurewei.alcor.controller.model.HostInfo;
import com.futurewei.alcor.controller.utilities.GoalStateUtil;
import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.Assert;
import org.junit.Test;

public class SubnetTest {
    @Test
    public void basicSerializationVerification() {
        SubnetState customerSubnetState = new SubnetState("dbf72700-5106-4a7a-918f-a016853911f8",
                "99d9d709-8478-4b46-9f3f-2206b1023fd3",
                "d973934b-93e8-42fa-ac91-bf0cdb84fffc",
                "Subnet1",
                "192.168.0.0/28",
                "192.168.0.5");

        HostInfo[] transitSwitches = {
                new HostInfo(OneBoxConfig.TRANSIT_SWTICH_1_HOST_ID, "transit switch host1", OneBoxConfig.TRANSIT_SWITCH_1_IP, OneBoxConfig.TRANSIT_SWITCH_1_MAC),
                new HostInfo(OneBoxConfig.TRANSIT_SWTICH_3_HOST_ID, "transit switch host2", OneBoxConfig.TRANSIT_SWITCH_3_IP, OneBoxConfig.TRANSIT_SWITCH_3_MAC)
        };

        final Subnet.SubnetState state = GoalStateUtil.CreateGSSubnetState(Common.OperationType.CREATE,
                customerSubnetState,
                transitSwitches);

        final byte[] binaryState = state.toByteArray();

        try {
            final Subnet.SubnetState deserializedObject = Subnet.SubnetState.parseFrom(binaryState);

            TestUtil.AssertSubnetStates(state, deserializedObject);
        } catch (InvalidProtocolBufferException bf_exp) {
            Assert.assertTrue(false);
        }
    }
}
