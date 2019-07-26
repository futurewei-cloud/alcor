package com.futurewei.alioth.controller.schema;

import com.futurewei.alioth.controller.schema.Vpc.*;
import com.futurewei.alioth.controller.schema.Subnet.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestUtil {
    public static void AssertVpcStates(VpcState expected, VpcState result){
        Assert.assertEquals("operation type mismatched", expected.getOperationType(), result.getOperationType());
        Assert.assertEquals("project id mismatched", expected.getConfiguration().getProjectId(), result.getConfiguration().getProjectId());
        Assert.assertEquals("vpc id mismatched", expected.getConfiguration().getId(), result.getConfiguration().getId());
        Assert.assertEquals("vpc name mismatched", expected.getConfiguration().getName(), result.getConfiguration().getName());
        Assert.assertEquals("cidr mismatched", expected.getConfiguration().getCidr(), result.getConfiguration().getCidr());

        Assert.assertEquals("subnet ids mismatched", expected.getConfiguration().getSubnetIdsCount(), result.getConfiguration().getSubnetIdsCount());

        Assert.assertEquals("routes mismatched", expected.getConfiguration().getRoutesCount(), result.getConfiguration().getRoutesCount());

        Assert.assertEquals("transit router ips mismatched", expected.getConfiguration().getTransitRouterIpsCount(), result.getConfiguration().getTransitRouterIpsCount());
        TestUtil.AssertTransitRouterIps(expected.getConfiguration().getTransitRouterIpsList(), result.getConfiguration().getTransitRouterIpsList());
    }

    public static void AssertTransitRouterIps(List<VpcConfiguration.TransitRouterIp> expected, List<VpcConfiguration.TransitRouterIp> result){
        Assert.assertEquals(expected.size(), result.size());
        for (int i = 0; i < expected.size(); i++) {
            TestUtil.AssertTransitRouterIp(expected.get(i), result.get(i));
        }
    }

    private static void AssertTransitRouterIp(VpcConfiguration.TransitRouterIp expected, VpcConfiguration.TransitRouterIp result) {
        Assert.assertEquals("vip id mismatched", expected.getVpcId(), result.getVpcId());
        Assert.assertEquals("router ip mismatched", expected.getIpAddress(), result.getIpAddress());
    }

    public static void AssertSubnetStates(SubnetState expected, SubnetState result){
        Assert.assertEquals("operation type mismatched", expected.getOperationType(), result.getOperationType());
        Assert.assertEquals("project id mismatched", expected.getConfiguration().getProjectId(), result.getConfiguration().getProjectId());
        Assert.assertEquals("vpc id mismatched", expected.getConfiguration().getVpcId(), result.getConfiguration().getVpcId());
        Assert.assertEquals("subnet id mismatched", expected.getConfiguration().getId(), result.getConfiguration().getId());
        Assert.assertEquals("subnet name mismatched", expected.getConfiguration().getName(), result.getConfiguration().getName());
        Assert.assertEquals("cidr mismatched", expected.getConfiguration().getCidr(), result.getConfiguration().getCidr());

        Assert.assertEquals("transit router ips mismatched", expected.getConfiguration().getTransitSwitchIpsCount(), result.getConfiguration().getTransitSwitchIpsCount());
        TestUtil.AssertTransitSwitchIps(expected.getConfiguration().getTransitSwitchIpsList(), result.getConfiguration().getTransitSwitchIpsList());
    }

    public static void AssertTransitSwitchIps(List<SubnetConfiguration.TransitSwitchIp> expected, List<SubnetConfiguration.TransitSwitchIp> result){
        Assert.assertEquals(expected.size(), result.size());
        for (int i = 0; i < expected.size(); i++) {
            TestUtil.AssertTransitSwitchIp(expected.get(i), result.get(i));
        }
    }

    private static void AssertTransitSwitchIp(SubnetConfiguration.TransitSwitchIp expected, SubnetConfiguration.TransitSwitchIp result) {
        Assert.assertEquals("vip id mismatched", expected.getVpcId(), result.getVpcId());
        Assert.assertEquals("subnet id mismatched", expected.getSubnetId(), result.getSubnetId());
        Assert.assertEquals("router ip mismatched", expected.getIpAddress(), result.getIpAddress());
    }
}
