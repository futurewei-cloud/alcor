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