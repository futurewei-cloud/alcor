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
