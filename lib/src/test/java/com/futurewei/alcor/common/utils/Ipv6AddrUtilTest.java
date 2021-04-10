/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
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
