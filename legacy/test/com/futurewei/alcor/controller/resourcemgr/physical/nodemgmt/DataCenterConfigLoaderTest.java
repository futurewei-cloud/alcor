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

package com.futurewei.alcor.controller.resourcemgr.physical.nodemgmt;

import com.futurewei.alcor.controller.model.HostInfo;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class DataCenterConfigLoaderTest {

    @Test
    public void machineConfigFileLoadTest() {
        List<HostInfo> hostNodeList = new DataCenterConfigLoader().loadAndGetHostNodeList("./test/com/futurewei/alcor/controller/resourcemgr/physical/nodemgmt/machine.json");

        Assert.assertEquals("incorrect number of nodes", 200, hostNodeList.size());

        // Check the first node
        Assert.assertEquals("incorrect host id", "ephost_0", hostNodeList.get(0).getId());
        Assert.assertEquals("incorrect host ip", "172.17.0.2", hostNodeList.get(0).getHostIpAddress());
        Assert.assertEquals("incorrect host mac", "02:42:ac:11:00:02", hostNodeList.get(0).getHostMacAddress());

        // Check the last node
        Assert.assertEquals("incorrect host id", "ephost_199", hostNodeList.get(199).getId());
        Assert.assertEquals("incorrect host ip", "172.17.0.201", hostNodeList.get(199).getHostIpAddress());
        Assert.assertEquals("incorrect host mac", "02:42:ac:11:00:c9", hostNodeList.get(199).getHostMacAddress());
    }
}