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

public class Ipv4AddrUtilTest {

    @Test
    public void ipv4AddrCheckTest() {
        //Happy cases
        Assert.assertTrue(Ipv4AddrUtil.formatCheck("1.0.0.0"));
        Assert.assertTrue(Ipv4AddrUtil.formatCheck("192.168.1.1"));
        Assert.assertTrue(Ipv4AddrUtil.formatCheck("255.255.255.255"));

        //Unhappy cases
        Assert.assertFalse(Ipv4AddrUtil.formatCheck("0.0.0.0"));
        Assert.assertFalse(Ipv4AddrUtil.formatCheck("1.1.1"));
        Assert.assertFalse(Ipv4AddrUtil.formatCheck("256.1.1.1"));
        Assert.assertFalse(Ipv4AddrUtil.formatCheck("255.1.1.1.1"));
    }

    @Test
    public void ipv4PrefixCheckTest() {
        //Happy cases
        Assert.assertTrue(Ipv4AddrUtil.ipv4PrefixCheck("0.0.0.0/0"));
        Assert.assertTrue(Ipv4AddrUtil.ipv4PrefixCheck("192.168.1.0/24"));
        Assert.assertTrue(Ipv4AddrUtil.ipv4PrefixCheck("192.168.1.0/32"));

        //Unhappy cases
        Assert.assertFalse(Ipv4AddrUtil.ipv4PrefixCheck("192.168.1.0"));
        Assert.assertFalse(Ipv4AddrUtil.ipv4PrefixCheck("192.168.1.0/33"));
        Assert.assertFalse(Ipv4AddrUtil.ipv4PrefixCheck("192.168.1.0.0/24"));
        Assert.assertFalse(Ipv4AddrUtil.ipv4PrefixCheck("192.168.1.256/24"));
        Assert.assertFalse(Ipv4AddrUtil.ipv4PrefixCheck("256.168.1.0/24"));
        Assert.assertFalse(Ipv4AddrUtil.ipv4PrefixCheck("256.168.1/24"));
        Assert.assertTrue(Ipv6AddrUtil.ipv6PrefixCheck("2001::8:800:200C:417A/128"));
    }
}
