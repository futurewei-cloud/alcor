package com.futurewei.alcor.controller.schema;

import com.futurewei.alcor.controller.model.SubnetState;
import com.futurewei.alcor.controller.app.demo.DemoConfig;
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
                "192.168.0.0/28");

        HostInfo[] transitSwitches = {
                new HostInfo(DemoConfig.TRANSIT_SWTICH_1_HOST_ID, "transit switch host1", DemoConfig.TRANSIT_SWITCH_1_IP, DemoConfig.TRANSIT_SWITCH_1_MAC),
                new HostInfo(DemoConfig.TRANSIT_SWTICH_3_HOST_ID, "transit switch host2", DemoConfig.TRANSIT_SWITCH_3_IP, DemoConfig.TRANSIT_SWITCH_3_MAC)
        };

        final Subnet.SubnetState state = GoalStateUtil.CreateGSSubnetState(Common.OperationType.CREATE,
                customerSubnetState,
                transitSwitches);

        final byte[] binaryState = state.toByteArray();

        try {
            final Subnet.SubnetState deserializedObject = Subnet.SubnetState.parseFrom(binaryState);

            TestUtil.AssertSubnetStates(state, deserializedObject);
        } catch(InvalidProtocolBufferException bf_exp) {
            Assert.assertTrue(false);
        }
    }
}
