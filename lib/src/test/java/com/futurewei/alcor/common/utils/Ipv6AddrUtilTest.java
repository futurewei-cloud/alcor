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
package com.futurewei.alcor.common.utils;

import org.junit.Assert;
import org.junit.Test;

public class Ipv6AddrUtilTest {
    @Test
    public void ipv6AddrCheckTest() {
        //Happy cases
        Assert.assertTrue(Ipv6AddrUtil.formatCheck("::1"));
        Assert.assertTrue(Ipv6AddrUtil.formatCheck("2001::"));
        Assert.assertTrue(Ipv6AddrUtil.formatCheck("2001:250:6000::1"));
        Assert.assertTrue(Ipv6AddrUtil.formatCheck("2001:600:500:400:8:800:200C:417A"));
        Assert.assertTrue(Ipv6AddrUtil.formatCheck("0:0:0:0:0:0:0:1"));
        Assert.assertTrue(Ipv6AddrUtil.formatCheck("FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF"));

        //Unhappy cases
        Assert.assertFalse(Ipv6AddrUtil.formatCheck("0::0::1"));
        Assert.assertFalse(Ipv6AddrUtil.formatCheck("2001::2001::"));
        Assert.assertFalse(Ipv6AddrUtil.formatCheck("0:0:0:0:0:0:1"));
        Assert.assertFalse(Ipv6AddrUtil.formatCheck("0:0:0:0:0:0:0:0:1"));
        Assert.assertFalse(Ipv6AddrUtil.formatCheck("FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFH"));
    }


    @Test
    public void ipv6PrefixCheckTest() {
        //Happy cases
        Assert.assertTrue(Ipv6AddrUtil.ipv6PrefixCheck("::/0"));
        Assert.assertTrue(Ipv6AddrUtil.ipv6PrefixCheck("::/128"));
        Assert.assertTrue(Ipv6AddrUtil.ipv6PrefixCheck("::1/128"));
        Assert.assertTrue(Ipv6AddrUtil.ipv6PrefixCheck("2001::/16"));
        Assert.assertTrue(Ipv6AddrUtil.ipv6PrefixCheck("2001:250:6000::/48"));
        Assert.assertTrue(Ipv6AddrUtil.ipv6PrefixCheck("2001::8:800:200C:417A/128"));
        Assert.assertTrue(Ipv6AddrUtil.ipv6PrefixCheck("2001:600:500:400:8:800:200C:417A/128"));
        Assert.assertTrue(Ipv6AddrUtil.ipv6PrefixCheck("2001:006:005:004:8:800:200C:417A/128"));

        //Unhappy cases
        Assert.assertFalse(Ipv6AddrUtil.ipv6PrefixCheck("2001::8:800:200C:417H"));
        Assert.assertFalse(Ipv6AddrUtil.ipv6PrefixCheck("2001::8:800:200C"));
        Assert.assertFalse(Ipv6AddrUtil.ipv6PrefixCheck("FF01:101/128"));
        Assert.assertFalse(Ipv6AddrUtil.ipv6PrefixCheck("FF01::101/129"));
        Assert.assertFalse(Ipv6AddrUtil.ipv6PrefixCheck("2001:00006:005:004:8:800:200C:417A/128"));
        Assert.assertFalse(Ipv6AddrUtil.ipv6PrefixCheck("2001:006:005:004:8:800:200C/112"));
        Assert.assertTrue(Ipv4AddrUtil.ipv4PrefixCheck("192.168.1.0/24"));
    }
}
