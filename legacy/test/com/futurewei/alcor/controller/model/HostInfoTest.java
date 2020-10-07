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

package com.futurewei.alcor.controller.model;

import org.junit.Assert;
import org.junit.Test;

public class HostInfoTest {
    @Test
    public void basicTest() {
        HostInfo[] transitSwitches = {
                new HostInfo("ts-1", "transit switch host1", new byte[]{10, 0, 0, 11}, "3D:F2:C9:A6:B3:4F"),
                new HostInfo("ts-2", "transit switch host2", new byte[]{10, 0, 0, 12}, "3D:F2:C9:A6:B3:50"),
                new HostInfo("ts-3", "transit switch host3 with wrong mac", new byte[]{10, 0, 0, 13}, "3D:F2:C9:A6:B3:50A"),
                new HostInfo("ts-4", "transit switch host4", new byte[]{10, 0, 0, 14}, "fa:16:3e:d7:f1:04"),
                new HostInfo("ts-5", "transit switch host5", new byte[]{10, 0, 0, 14}, "fa-16-3e-d7-f1-04"),
                new HostInfo("ts-6", "transit switch host6", new byte[]{172 - 256, 0, 0, 11}, "3D:F2:C9:A6:B3:4F"),
        };

        Assert.assertEquals("invalid host1 id", "ts-1", transitSwitches[0].getId());
        Assert.assertEquals("invalid host2 id", "ts-2", transitSwitches[1].getId());

        Assert.assertEquals("invalid host1 name", "transit switch host1", transitSwitches[0].getHostName());
        Assert.assertEquals("invalid host2 name", "transit switch host2", transitSwitches[1].getHostName());

        Assert.assertEquals("invalid ip address", "10.0.0.11", transitSwitches[0].getHostIpAddress());
        Assert.assertEquals("invalid ip address", "10.0.0.12", transitSwitches[1].getHostIpAddress());
        Assert.assertEquals("invalid ip address", "172.0.0.11", transitSwitches[5].getHostIpAddress());

        Assert.assertEquals("invalid mac address", "3D:F2:C9:A6:B3:4F", transitSwitches[0].getHostMacAddress());
        Assert.assertEquals("invalid mac address", "3D:F2:C9:A6:B3:50", transitSwitches[1].getHostMacAddress());
        Assert.assertEquals("invalid mac address", null, transitSwitches[2].getHostMacAddress());
        Assert.assertEquals("invalid mac address", "fa:16:3e:d7:f1:04", transitSwitches[3].getHostMacAddress());
        Assert.assertEquals("invalid mac address", "fa-16-3e-d7-f1-04", transitSwitches[4].getHostMacAddress());
    }

}