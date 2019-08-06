package com.futurewei.alioth.controller.schema;

import com.futurewei.alioth.controller.app.DemoConfig;
import com.futurewei.alioth.controller.model.HostInfo;
import com.futurewei.alioth.controller.model.PortState;
import com.futurewei.alioth.controller.utilities.GoalStateUtil;
import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class PortTest {
    @Test
    public void basicSerializationVerification() {
        PortState customerPortState = new PortState("dbf72700-5106-4a7a-918f-a016853911f8",
                "d973934b-93e8-42fa-ac91-bf0cdb84fffc",
                "89e72582-b4fc-4e4e-b46a-6eee650e03f5",
                "ep1",
                "fa:16:3e:d7:f0:00",
                DemoConfig.VNET_NAME,
                new String[]{"10.0.0.1"});

        HostInfo epHost = new HostInfo("host0", "ep1 host", new byte[]{127,0,0,1}, "fa:16:3e:d7:f1:00");

        final Port.PortState state = GoalStateUtil.CreateGSPortState(Common.OperationType.CREATE,
                customerPortState,
                epHost);

        final byte[] binaryState = state.toByteArray();

        try {
            final Port.PortState deserializedObject = Port.PortState.parseFrom(binaryState);

            TestUtil.AssertPortStates(state, deserializedObject);
        } catch(InvalidProtocolBufferException bf_exp) {
            Assert.assertTrue(false);
        }
    }

}