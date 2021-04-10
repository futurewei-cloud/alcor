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

import com.futurewei.alcor.controller.model.HostInfo;
import com.futurewei.alcor.controller.app.onebox.OneBoxConfig;
import com.futurewei.alcor.controller.model.PortState;
import com.futurewei.alcor.controller.utilities.GoalStateUtil;
import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.Assert;
import org.junit.Test;

public class PortTest {
    @Test
    public void basicSerializationVerification() {
        PortState customerPortState = new PortState("dbf72700-5106-4a7a-918f-a016853911f8",
                "d973934b-93e8-42fa-ac91-bf0cdb84fffc",
                "89e72582-b4fc-4e4e-b46a-6eee650e03f5",
                "ep1",
                "fa:16:3e:d7:f0:00",
                OneBoxConfig.VETH_NAME,
                new String[]{"10.0.0.1"});

        HostInfo epHost = new HostInfo("host0", "ep1 host", new byte[]{127, 0, 0, 1}, "fa:16:3e:d7:f1:00");

        final Port.PortState state = GoalStateUtil.CreateGSPortState(Common.OperationType.CREATE,
                customerPortState,
                epHost);

        final byte[] binaryState = state.toByteArray();

        try {
            final Port.PortState deserializedObject = Port.PortState.parseFrom(binaryState);

            TestUtil.AssertPortStates(state, deserializedObject);
        } catch (InvalidProtocolBufferException bf_exp) {
            Assert.assertTrue(false);
        }
    }

}