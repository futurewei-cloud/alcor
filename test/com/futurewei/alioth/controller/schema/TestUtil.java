package com.futurewei.alioth.controller.schema;

import com.futurewei.alioth.controller.schema.Vpc.VpcState;
import org.junit.Assert;

public class TestUtil {
    public static void AssertVpcStates(VpcState expected, VpcState result){
        Assert.assertEquals("operation type mismatched", expected.getOperationType(), result.getOperationType());
        Assert.assertEquals("project id mismatched", expected.getConfiguration().getProjectId(), result.getConfiguration().getProjectId());
        Assert.assertEquals("vpc id mismatched", expected.getConfiguration().getId(), result.getConfiguration().getId());
        Assert.assertEquals("vpc name mismatched", expected.getConfiguration().getName(), result.getConfiguration().getName());
        Assert.assertEquals("cidr mismatched", expected.getConfiguration().getCidr(), result.getConfiguration().getCidr());
    }
}
