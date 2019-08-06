package com.futurewei.alioth.controller.utilities;

import com.futurewei.alioth.controller.app.DemoConfig;
import com.futurewei.alioth.controller.model.SubnetState;
import com.futurewei.alioth.controller.model.VpcState;
import com.futurewei.alioth.controller.schema.Common;
import com.futurewei.alioth.controller.schema.Goalstate.*;
import com.futurewei.alioth.controller.schema.Port;
import com.futurewei.alioth.controller.schema.Subnet;
import com.futurewei.alioth.controller.schema.Subnet.*;
import com.futurewei.alioth.controller.schema.Vpc;
import com.futurewei.alioth.controller.schema.Vpc.*;
import com.futurewei.alioth.controller.model.*;

public class GoalStateUtil {
    public static  GoalState CreateGoalState(
            Common.OperationType option,
            VpcState customerVpcState,
            HostInfo[] transitRouterHosts)
    {
        final Vpc.VpcState vpcState = GoalStateUtil.CreateGSVpcState(
                option,
                customerVpcState,
                transitRouterHosts);

        GoalState goalstate = GoalState.newBuilder()
                .addVpcStates(vpcState)
                .build();

        return goalstate;
    }

    public static GoalState CreateGoalState(
            Common.OperationType option,
            SubnetState[] customerSubnetStates,
            HostInfo[][] transitSwitchHosts)
    {
        GoalState.Builder goalstate = GoalState.newBuilder();

        for(int i = 0; i < customerSubnetStates.length; i++){
            final Subnet.SubnetState gsSubnetState = GoalStateUtil.CreateGSSubnetState(
                    option,
                    customerSubnetStates[i],
                    transitSwitchHosts[i]);

            goalstate.addSubnetStates(gsSubnetState);
        }

        return goalstate.build();
    }

    public static GoalState CreateGoalState(
            Common.OperationType subnetOption,
            SubnetState customerSubnetState,
            HostInfo[] transitSwitchHosts,
            Common.OperationType portOption,
            PortState[] customerPortStates,
            HostInfo[] portHosts)
    {
        final Subnet.SubnetState gsSubnetState = GoalStateUtil.CreateGSSubnetState(
                subnetOption,
                customerSubnetState,
                transitSwitchHosts);

        GoalState.Builder goalstate = GoalState.newBuilder().addSubnetStates(gsSubnetState);

        for (int i = 0; i < customerPortStates.length; i++) {
            final Port.PortState gsPortState = GoalStateUtil.CreateGSPortState(
                    portOption,
                    customerPortStates[i],
                    portHosts[i]);

            goalstate.addPortStates(gsPortState);
        }

        return goalstate.build();
    }

    public static GoalState CreateGoalState(
            Common.OperationType subnetOption,
            SubnetState customerSubnetState,
            HostInfo[] transitSwitchHosts,
            Common.OperationType portOption,
            PortState customerPortState,
            HostInfo portHost)
    {
        final Subnet.SubnetState gsSubnetState = GoalStateUtil.CreateGSSubnetState(
                subnetOption,
                customerSubnetState,
                transitSwitchHosts);

        final Port.PortState gsPortState = GoalStateUtil.CreateGSPortState(
                portOption,
                customerPortState,
                portHost);

        GoalState goalstate = GoalState.newBuilder()
                .addSubnetStates(gsSubnetState)
                .addPortStates(gsPortState)
                .build();

        return goalstate;
    }

    public static Vpc.VpcState CreateGSVpcState(
            Common.OperationType option,
            String project_id,
            String vpc_id,
            String vpc_name,
            String cidr)
    {
        return Vpc.VpcState.newBuilder()
                .setOperationType(option)
                .setConfiguration(VpcConfiguration.newBuilder()
                        .setProjectId(project_id)
                        .setId(vpc_id)
                        .setName(vpc_name)
                        .setCidr(cidr))
                .build();
    }

    public static Vpc.VpcState CreateGSVpcState(
            Common.OperationType option,
            VpcState customerVpcState,
            HostInfo[] transitRouterHosts)
    {
        String vpcId = customerVpcState.getId();
        VpcConfiguration.Builder vpcConfiguration = VpcConfiguration.newBuilder();

        vpcConfiguration.setProjectId(customerVpcState.getProjectId())
                .setId(vpcId)
                .setName(customerVpcState.getName())
                .setCidr(customerVpcState.getCidr());

        for (HostInfo routerHost : transitRouterHosts){
            vpcConfiguration.addTransitRouters(
                    VpcConfiguration.TransitRouter.newBuilder()
                        .setVpcId(vpcId)
                        .setIpAddress(routerHost.getHostIpAddress())
                        .setMacAddress(routerHost.getHostMacAddress()));
        }

        vpcConfiguration.build();

        return Vpc.VpcState.newBuilder()
                .setOperationType(option)
                .setConfiguration(vpcConfiguration)
                .build();
    }

    public static Subnet.SubnetState CreateGSSubnetState(
            Common.OperationType option,
            SubnetState customerSubnetState,
            HostInfo[] transitSwitchHosts)
    {
        String vpcId = customerSubnetState.getVpcId();
        String subnetId = customerSubnetState.getId();
        SubnetConfiguration.Builder subnetConfiguration = SubnetConfiguration.newBuilder();

        subnetConfiguration.setProjectId(customerSubnetState.getProjectId())
                .setVpcId(vpcId)
                .setId(subnetId)
                .setName(customerSubnetState.getName())
                .setCidr(customerSubnetState.getCidr());

        for(HostInfo switchHost : transitSwitchHosts){
            subnetConfiguration.addTransitSwitches(
                    SubnetConfiguration.TransitSwitch.newBuilder()
                            .setVpcId(vpcId)
                            .setSubnetId(subnetId)
                            .setIpAddress(switchHost.getHostIpAddress())
                            .setMacAddress(switchHost.getHostMacAddress()));
        }

        subnetConfiguration.build();

        return Subnet.SubnetState.newBuilder()
                .setOperationType(option)
                .setConfiguration(subnetConfiguration)
                .build();
    }

    public static Port.PortState CreateGSPortState(
            Common.OperationType option,
            PortState customerPortState,
            HostInfo portHost)
    {
        Port.PortConfiguration.Builder portConfiguration = Port.PortConfiguration.newBuilder();

        portConfiguration.setProjectId(customerPortState.getProjectId())
                .setNetworkId(customerPortState.getNetworkId())
                .setId(customerPortState.getId())
                .setName(customerPortState.getName())
                .setMacAddress(customerPortState.getMacAddress())
                .setVethName(customerPortState.getVethName())
                .setHostInfo(Port.PortConfiguration.HostInfo.newBuilder()
                                .setIpAddress(portHost.getHostIpAddress())
                                .setMacAddress(portHost.getHostMacAddress()));

        return Port.PortState.newBuilder()
                .setOperationType(option)
                .setConfiguration(portConfiguration)
                .build();
    }
}
