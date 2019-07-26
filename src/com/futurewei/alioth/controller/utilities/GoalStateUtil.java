package com.futurewei.alioth.controller.utilities;

import com.futurewei.alioth.controller.schema.Common;
import com.futurewei.alioth.controller.schema.Subnet.*;
import com.futurewei.alioth.controller.schema.Vpc.VpcConfiguration;
import com.futurewei.alioth.controller.schema.Vpc.VpcState;

public class GoalStateUtil {
    public static VpcState CreateVpcState(
            Common.OperationType option,
            String project_id,
            String vpc_id,
            String vpc_name,
            String cidr)
    {
        return VpcState.newBuilder()
                .setOperationType(option)
                .setConfiguration(VpcConfiguration.newBuilder()
                        .setProjectId(project_id)
                        .setId(vpc_id)
                        .setName(vpc_name)
                        .setCidr(cidr))
                .build();
    }

    public static VpcState CreateVpcState(
            Common.OperationType option,
            String project_id,
            String vpc_id,
            String vpc_name,
            String cidr,
            String transit_router_ip,
            String transit_router_ip2)
    {
        VpcConfiguration vpcConfiguration = VpcConfiguration.newBuilder()
                .setProjectId(project_id)
                .setId(vpc_id)
                .setName(vpc_name)
                .setCidr(cidr)
                .addTransitRouterIps(VpcConfiguration.TransitRouterIp.newBuilder()
                        .setVpcId(vpc_id)
                        .setIpAddress(transit_router_ip))
                .addTransitRouterIps(VpcConfiguration.TransitRouterIp.newBuilder()
                        .setVpcId(vpc_id)
                        .setIpAddress(transit_router_ip2))
                .build();

        return VpcState.newBuilder()
                .setOperationType(option)
                .setConfiguration(vpcConfiguration)
                .build();
    }

    public static SubnetState CreateSubnetState(
            Common.OperationType option,
            String project_id,
            String vpc_id,
            String subnet_id,
            String subnet_name,
            String cidr,
            String transit_switch_ip,
            String transit_switch_ip2)
    {
        SubnetConfiguration subnetConfiguration = SubnetConfiguration.newBuilder()
                .setProjectId(project_id)
                .setVpcId(vpc_id)
                .setId(subnet_id)
                .setName(subnet_name)
                .setCidr(cidr)
                .addTransitSwitchIps(SubnetConfiguration.TransitSwitchIp.newBuilder()
                        .setVpcId(vpc_id)
                        .setSubnetId(subnet_id)
                        .setIpAddress(transit_switch_ip))
                .addTransitSwitchIps(SubnetConfiguration.TransitSwitchIp.newBuilder()
                        .setVpcId(vpc_id)
                        .setSubnetId(subnet_id)
                        .setIpAddress(transit_switch_ip2))
                .build();

        return SubnetState.newBuilder()
                .setOperationType(option)
                .setConfiguration(subnetConfiguration)
                .build();
    }
}
