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
package com.futurewei.alcor.privateipmanager.util;

import com.futurewei.alcor.privateipmanager.config.UnitTestConfig;
import com.futurewei.alcor.privateipmanager.entity.IpAddrAlloc;
import com.futurewei.alcor.privateipmanager.entity.IpAddrRange;
import com.futurewei.alcor.web.entity.ip.IpAddrRangeRequest;
import com.futurewei.alcor.web.entity.ip.IpAddrRequest;

public class IpAddressBuilder {
    public static IpAddrAlloc buildIpAddrAlloc() {
        IpAddrAlloc ipAddrAlloc = new IpAddrAlloc();
        ipAddrAlloc.setIpVersion(UnitTestConfig.ipv4);
        ipAddrAlloc.setState(UnitTestConfig.activated);
        ipAddrAlloc.setRangeId(UnitTestConfig.rangeId);
        ipAddrAlloc.setSubnetId(UnitTestConfig.subnetId);

        return ipAddrAlloc;
    }

    public static IpAddrRange buildIpAddrRange() {
        IpAddrRange ipAddrRange = new IpAddrRange(UnitTestConfig.rangeId,
                UnitTestConfig.vpcId,
                UnitTestConfig.subnetId,
                UnitTestConfig.ipv4,
                UnitTestConfig.firstIp,
                UnitTestConfig.lastIp);

        return ipAddrRange;
    }

    public static IpAddrRequest buildIpAddrRequest() {
        IpAddrRequest ipAddrRequest = new IpAddrRequest(
                UnitTestConfig.ipv4,
                UnitTestConfig.vpcId,
                UnitTestConfig.subnetId,
                UnitTestConfig.rangeId,
                UnitTestConfig.ip1,
                UnitTestConfig.activated);

        return ipAddrRequest;
    }

    public static IpAddrRangeRequest buildIpAddrRangeRequest() {
        IpAddrRangeRequest ipAddrRangeRequest = new IpAddrRangeRequest(
                UnitTestConfig.rangeId,
                UnitTestConfig.vpcId,
                UnitTestConfig.subnetId,
                UnitTestConfig.ipv4,
                UnitTestConfig.firstIp,
                UnitTestConfig.lastIp);
        return ipAddrRangeRequest;
    }
}
