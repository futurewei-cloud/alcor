package com.futurewei.alioth.controller.schema;

import org.junit.Assert;

public class TestUtil {
    public static Vpc.VpcState CreateVpcState(
            Common.OperationType option,
            String project_id,
            String vpc_id,
            String vpc_name,
            String cidr){
        return Vpc.VpcState.newBuilder()
                .setOperationType(option)
                .setConfiguration(Vpc.VpcConfiguration.newBuilder()
                        .setProjectId(project_id)
                        .setId(vpc_id)
                        .setName(vpc_name)
                        .setCidr(cidr))
                .build();
    }

    public static void AssertVpcStates(Vpc.VpcState expected, Vpc.VpcState result){
        Assert.assertEquals("operation type mismatched", expected.getOperationType(), result.getOperationType());
        Assert.assertEquals("project id mismatched", expected.getConfiguration().getProjectId(), result.getConfiguration().getProjectId());
        Assert.assertEquals("vpc id mismatched", expected.getConfiguration().getId(), result.getConfiguration().getId());
        Assert.assertEquals("vpc name mismatched", expected.getConfiguration().getName(), result.getConfiguration().getName());
        Assert.assertEquals("cidr mismatched", expected.getConfiguration().getCidr(), result.getConfiguration().getCidr());
    }
}
