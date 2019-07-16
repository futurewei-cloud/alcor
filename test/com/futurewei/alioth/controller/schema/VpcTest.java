package com.futurewei.alioth.controller.schema;

import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.Assert;
import org.junit.Test;

public class VpcTest {
    @Test
    public void basicSerializationVerification() {
        String project_id = "dbf72700-5106-4a7a-918f-a016853911f8";
        String vpc_id = "99d9d709-8478-4b46-9f3f-2206b1023fd3";
        String vpc_name = "SuperVpc";
        String cidr = "192.168.0.0/16";
        final Vpc.VpcConfiguration config = Vpc.VpcConfiguration.newBuilder()
                .setProjectId(project_id)
                .setId(vpc_id)
                .setName(vpc_name)
                .setCidr(cidr)
                .build();
        final Vpc.VpcState state = Vpc.VpcState.newBuilder()
                .setOperationType(Common.OperationType.CREATE)
                .setConfiguration(config)
                .build();
        final byte[] binaryState = state.toByteArray();

        try {
            final Vpc.VpcState deserializedObject = Vpc.VpcState.parseFrom(binaryState);

            Assert.assertEquals("operation type mismatched", Common.OperationType.CREATE, deserializedObject.getOperationType());

            Assert.assertEquals("project id mismatched", project_id, deserializedObject.getConfiguration().getProjectId());
            Assert.assertEquals("vpc id mismatched", vpc_id, deserializedObject.getConfiguration().getId());
            Assert.assertEquals("vpc name mismatched", vpc_name, deserializedObject.getConfiguration().getName());
            Assert.assertEquals("cidr mismatched", cidr, deserializedObject.getConfiguration().getCidr());
        } catch(InvalidProtocolBufferException bf_exp) {
            Assert.assertTrue(false);
        }
    }
}