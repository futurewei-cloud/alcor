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
