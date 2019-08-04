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

        Assert.assertEquals("transit router ips mismatched", expected.getConfiguration().getTransitRoutersCount(), result.getConfiguration().getTransitRoutersCount());
        TestUtil.AssertTransitRouters(expected.getConfiguration().getTransitRoutersList(), result.getConfiguration().getTransitRoutersList());
    }

    public static void AssertTransitRouters(List<VpcConfiguration.TransitRouter> expected, List<VpcConfiguration.TransitRouter> result){
        Assert.assertEquals(expected.size(), result.size());
        for (int i = 0; i < expected.size(); i++) {
            TestUtil.AssertTransitRouter(expected.get(i), result.get(i));
        }
    }

    private static void AssertTransitRouter(VpcConfiguration.TransitRouter expected, VpcConfiguration.TransitRouter result) {
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

        Assert.assertEquals("transit router ips mismatched", expected.getConfiguration().getTransitSwitchesCount(), result.getConfiguration().getTransitSwitchesCount());
        TestUtil.AssertTransitSwitches(expected.getConfiguration().getTransitSwitchesList(), result.getConfiguration().getTransitSwitchesList());
    }

    public static void AssertTransitSwitches(List<SubnetConfiguration.TransitSwitch> expected, List<SubnetConfiguration.TransitSwitch> result){
        Assert.assertEquals(expected.size(), result.size());
        for (int i = 0; i < expected.size(); i++) {
            TestUtil.AssertTransitSwitch(expected.get(i), result.get(i));
        }
    }

    private static void AssertTransitSwitch(SubnetConfiguration.TransitSwitch expected, SubnetConfiguration.TransitSwitch result) {
        Assert.assertEquals("vip id mismatched", expected.getVpcId(), result.getVpcId());
        Assert.assertEquals("subnet id mismatched", expected.getSubnetId(), result.getSubnetId());
        Assert.assertEquals("router ip mismatched", expected.getIpAddress(), result.getIpAddress());
    }
}
