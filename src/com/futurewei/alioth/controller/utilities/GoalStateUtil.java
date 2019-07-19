package com.futurewei.alioth.controller.utilities;

import com.futurewei.alioth.controller.schema.Common;
import com.futurewei.alioth.controller.schema.Vpc;

public class GoalStateUtil {
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
}
