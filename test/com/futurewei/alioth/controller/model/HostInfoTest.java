package com.futurewei.alioth.controller.model;

import org.junit.Assert;
import org.junit.Test;

public class HostInfoTest {
    @Test
    public void basicTest(){
        HostInfo[] transitSwitches = {
                new HostInfo("ts-1", "transit switch host1", new byte[]{10,0,0,11}),
                new HostInfo("ts-2", "transit switch host2", new byte[]{10,0,0,12})
        };

        Assert.assertEquals("invalid host1 id", "ts-1", transitSwitches[0].getId());
        Assert.assertEquals("invalid host2 id", "ts-2", transitSwitches[1].getId());

        Assert.assertEquals("invalid host1 name", "transit switch host1", transitSwitches[0].getHostName());
        Assert.assertEquals("invalid host2 name", "transit switch host2", transitSwitches[1].getHostName());

        Assert.assertEquals("invalid ip address", "10.0.0.11", transitSwitches[0].getHostIpAddress());
        Assert.assertEquals("invalid ip address", "10.0.0.12", transitSwitches[1].getHostIpAddress());
    }

}