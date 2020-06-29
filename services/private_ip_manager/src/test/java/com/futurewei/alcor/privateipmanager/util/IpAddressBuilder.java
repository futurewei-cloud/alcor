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
