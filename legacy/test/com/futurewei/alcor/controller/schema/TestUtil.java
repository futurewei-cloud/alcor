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


package com.futurewei.alcor.controller.schema;

import com.futurewei.alcor.controller.schema.Port.*;
import com.futurewei.alcor.controller.schema.Port.PortConfiguration.*;
import com.futurewei.alcor.controller.schema.Vpc.*;
import org.junit.Assert;

import java.util.List;

public class TestUtil {
    public static void AssertVpcStates(VpcState expected, VpcState result) {
        Assert.assertEquals("operation type mismatched", expected.getOperationType(), result.getOperationType());
        Assert.assertEquals("project id mismatched", expected.getConfiguration().getProjectId(), result.getConfiguration().getProjectId());
        Assert.assertEquals("vpc id mismatched", expected.getConfiguration().getId(), result.getConfiguration().getId());
        Assert.assertEquals("vpc name mismatched", expected.getConfiguration().getName(), result.getConfiguration().getName());
        Assert.assertEquals("cidr mismatched", expected.getConfiguration().getCidr(), result.getConfiguration().getCidr());

        Assert.assertEquals("subnet ids mismatched", expected.getConfiguration().getSubnetIdsCount(), result.getConfiguration().getSubnetIdsCount());

        Assert.assertEquals("routes mismatched", expected.getConfiguration().getRoutesCount(), result.getConfiguration().getRoutesCount());

        Assert.assertEquals("transit router ips mismatched", expected.getConfiguration().getTransitRoutersCount(), result.getConfiguration().getTransitRoutersCount());
        TestUtil.AssertTransitRouters(expected.getConfiguration().getTransitRoutersList(), result.getConfiguration().getTransitRoutersList());
    }

    public static void AssertTransitRouters(List<VpcConfiguration.TransitRouter> expected, List<VpcConfiguration.TransitRouter> result) {
        Assert.assertEquals(expected.size(), result.size());
        for (int i = 0; i < expected.size(); i++) {
            TestUtil.AssertTransitRouter(expected.get(i), result.get(i));
        }
    }

    private static void AssertTransitRouter(VpcConfiguration.TransitRouter expected, VpcConfiguration.TransitRouter result) {
        Assert.assertEquals("vip id mismatched", expected.getVpcId(), result.getVpcId());
        Assert.assertEquals("router ip mismatched", expected.getIpAddress(), result.getIpAddress());
    }

    public static void AssertSubnetStates(Subnet.SubnetState expected, Subnet.SubnetState result) {
        Assert.assertEquals("operation type mismatched", expected.getOperationType(), result.getOperationType());
        Assert.assertEquals("project id mismatched", expected.getConfiguration().getProjectId(), result.getConfiguration().getProjectId());
        Assert.assertEquals("vpc id mismatched", expected.getConfiguration().getVpcId(), result.getConfiguration().getVpcId());
        Assert.assertEquals("subnet id mismatched", expected.getConfiguration().getId(), result.getConfiguration().getId());
        Assert.assertEquals("subnet name mismatched", expected.getConfiguration().getName(), result.getConfiguration().getName());
        Assert.assertEquals("cidr mismatched", expected.getConfiguration().getCidr(), result.getConfiguration().getCidr());

        Assert.assertEquals("transit router ips mismatched", expected.getConfiguration().getTransitSwitchesCount(), result.getConfiguration().getTransitSwitchesCount());
        TestUtil.AssertTransitSwitches(expected.getConfiguration().getTransitSwitchesList(), result.getConfiguration().getTransitSwitchesList());
    }

    public static void AssertTransitSwitches(List<Subnet.SubnetConfiguration.TransitSwitch> expected, List<Subnet.SubnetConfiguration.TransitSwitch> result) {
        Assert.assertEquals(expected.size(), result.size());
        for (int i = 0; i < expected.size(); i++) {
            TestUtil.AssertTransitSwitch(expected.get(i), result.get(i));
        }
    }

    private static void AssertTransitSwitch(Subnet.SubnetConfiguration.TransitSwitch expected, Subnet.SubnetConfiguration.TransitSwitch result) {
        Assert.assertEquals("vip id mismatched", expected.getVpcId(), result.getVpcId());
        Assert.assertEquals("subnet id mismatched", expected.getSubnetId(), result.getSubnetId());
        Assert.assertEquals("router ip mismatched", expected.getIpAddress(), result.getIpAddress());
    }

    public static void AssertPortStates(PortState expected, PortState result) {
        Assert.assertEquals("operation type mismatched", expected.getOperationType(), result.getOperationType());
        Assert.assertEquals("project id mismatched", expected.getConfiguration().getProjectId(), result.getConfiguration().getProjectId());
        Assert.assertEquals("network id mismatched", expected.getConfiguration().getNetworkId(), result.getConfiguration().getNetworkId());
        Assert.assertEquals("id mismatched", expected.getConfiguration().getId(), result.getConfiguration().getId());
        Assert.assertEquals("name mismatched", expected.getConfiguration().getName(), result.getConfiguration().getName());
        Assert.assertEquals("admin state up mismatched", expected.getConfiguration().getNetworkNs(), result.getConfiguration().getNetworkNs());
        Assert.assertEquals("mac address mismatched", expected.getConfiguration().getMacAddress(), result.getConfiguration().getMacAddress());
        Assert.assertEquals("veth name mismatched", expected.getConfiguration().getVethName(), result.getConfiguration().getVethName());

        TestUtil.AssertHostInfo(expected.getConfiguration().getHostInfo(), result.getConfiguration().getHostInfo());

        Assert.assertEquals("fixed ip count mismatched", expected.getConfiguration().getFixedIpsCount(), result.getConfiguration().getFixedIpsCount());
        TestUtil.AssertFixedIps(expected.getConfiguration().getFixedIpsList(), result.getConfiguration().getFixedIpsList());

        //TODO: compare the remaining list
        Assert.assertEquals("security group count mismatched", expected.getConfiguration().getSecurityGroupIdsCount(), result.getConfiguration().getSecurityGroupIdsCount());
        Assert.assertEquals("allowed address pair count mismatched", expected.getConfiguration().getAllowAddressPairsCount(), result.getConfiguration().getAllowAddressPairsCount());
        Assert.assertEquals("extra dhcp option count mismatched", expected.getConfiguration().getExtraDhcpOptionsCount(), result.getConfiguration().getExtraDhcpOptionsCount());
    }

    private static void AssertHostInfo(HostInfo expected, HostInfo result) {
        Assert.assertEquals("host ip address mismatched", expected.getIpAddress(), result.getIpAddress());
        Assert.assertEquals("host mac address mismatched", expected.getMacAddress(), result.getMacAddress());
    }

    private static void AssertFixedIps(List<FixedIp> expected, List<FixedIp> result) {
        Assert.assertEquals(expected.size(), result.size());
        for (int i = 0; i < expected.size(); i++) {
            TestUtil.AssertFixedIp(expected.get(i), result.get(i));
        }
    }

    private static void AssertFixedIp(FixedIp expected, FixedIp result) {
        Assert.assertEquals("subnet id mismatched", expected.getSubnetId(), result.getSubnetId());
        Assert.assertEquals("ip address mismatched", expected.getIpAddress(), result.getIpAddress());
    }
}
