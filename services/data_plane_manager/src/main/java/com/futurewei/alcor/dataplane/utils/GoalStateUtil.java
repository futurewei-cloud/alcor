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

package com.futurewei.alcor.dataplane.utils;

import com.futurewei.alcor.dataplane.app.onebox.OneBoxConfig;
import com.futurewei.alcor.dataplane.model.HostInfo;
import com.futurewei.alcor.dataplane.model.PortState;
import com.futurewei.alcor.dataplane.model.SubnetState;
import com.futurewei.alcor.dataplane.model.VpcState;
import com.futurewei.alcor.schema.Common;
import com.futurewei.alcor.schema.Goalstate.GoalState;
import com.futurewei.alcor.schema.Port;
import com.futurewei.alcor.schema.Subnet;
import com.futurewei.alcor.schema.Vpc;
import com.futurewei.alcor.schema.Vpc.VpcConfiguration;

public class GoalStateUtil {
    public static GoalState CreateGoalState(
            Common.OperationType option,
            VpcState customerVpcState,
            HostInfo[] transitRouterHosts) {
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
            HostInfo[][] transitSwitchHosts) {
        GoalState.Builder goalstate = GoalState.newBuilder();

        for (int i = 0; i < customerSubnetStates.length; i++) {
            final Subnet.SubnetState gsSubnetState = GoalStateUtil.CreateGSSubnetState(
                    option,
                    customerSubnetStates[i],
                    transitSwitchHosts[i]);

            goalstate.addSubnetStates(gsSubnetState);
        }

        return goalstate.build();
    }

    public static GoalState CreateGoalState(
            Common.OperationType vpcOption,
            VpcState customerVpcState,
            HostInfo[] transitRouterHosts,
            Common.OperationType subnetOption,
            SubnetState[] customerSubnetStates,
            HostInfo[][] transitSwitchHosts) {
        final Vpc.VpcState vpcState = GoalStateUtil.CreateGSVpcState(
                vpcOption,
                customerVpcState,
                transitRouterHosts);

        GoalState.Builder goalState = GoalState.newBuilder().addVpcStates(vpcState);

        for (int i = 0; i < customerSubnetStates.length; i++) {
            final Subnet.SubnetState gsSubnetState = GoalStateUtil.CreateGSSubnetState(
                    subnetOption,
                    customerSubnetStates[i],
                    transitSwitchHosts[i]);

            goalState.addSubnetStates(gsSubnetState);
        }

        return goalState.build();
    }

    public static GoalState CreateGoalState(
            Common.OperationType subnetOption,
            SubnetState customerSubnetState,
            HostInfo[] transitSwitchHosts,
            Common.OperationType portOption,
            PortState[] customerPortStates,
            HostInfo[] portHosts) {
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
            PortState[] customerPortStates,
            HostInfo portHost) {
        final Subnet.SubnetState gsSubnetState = GoalStateUtil.CreateGSSubnetState(
                subnetOption,
                customerSubnetState,
                transitSwitchHosts);

        GoalState.Builder goalstate = GoalState.newBuilder().addSubnetStates(gsSubnetState);

        for (int i = 0; i < customerPortStates.length; i++) {
            final Port.PortState gsPortState = GoalStateUtil.CreateGSPortState(
                    portOption,
                    customerPortStates[i],
                    portHost);

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
            HostInfo portHost) {
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
            String cidr) {
        return Vpc.VpcState.newBuilder()
                .setOperationType(option)
                .setConfiguration(VpcConfiguration.newBuilder()
                        .setProjectId(project_id)
                        .setId(vpc_id)
                        .setName(vpc_name)
                        .setCidr(cidr)
                        .setTunnelId(OneBoxConfig.Tunnel_Id))
                .build();
    }

    public static Vpc.VpcState CreateGSVpcState(
            Common.OperationType option,
            VpcState customerVpcState,
            HostInfo[] transitRouterHosts) {
        String vpcId = customerVpcState.getId();
        VpcConfiguration.Builder vpcConfiguration = VpcConfiguration.newBuilder();

        vpcConfiguration.setProjectId(customerVpcState.getProjectId())
                .setId(vpcId)
                .setName(customerVpcState.getName())
                .setCidr(customerVpcState.getCidr())
                .setTunnelId(OneBoxConfig.Tunnel_Id);

        for (HostInfo routerHost : transitRouterHosts) {
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
            HostInfo[] transitSwitchHosts) {
        String vpcId = customerSubnetState.getVpcId();
        String subnetId = customerSubnetState.getId();
        Subnet.SubnetConfiguration.Builder subnetConfiguration = Subnet.SubnetConfiguration.newBuilder();

        subnetConfiguration.setProjectId(customerSubnetState.getProjectId())
                .setVpcId(vpcId)
                .setId(subnetId)
                .setName(customerSubnetState.getName())
                .setCidr(customerSubnetState.getCidr())
                .setTunnelId(OneBoxConfig.Tunnel_Id);

        subnetConfiguration.setGateway(
                Subnet.SubnetConfiguration.Gateway.newBuilder()
                        .setVpcId(vpcId)
                        .setSubnetId(subnetId)
                        .setIpAddress(customerSubnetState.getGatewayIp())
                        .setMacAddress(OneBoxConfig.GATEWAY_MAC_ADDRESS));

        for (HostInfo switchHost : transitSwitchHosts) {
            subnetConfiguration.addTransitSwitches(
                    Subnet.SubnetConfiguration.TransitSwitch.newBuilder()
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
            HostInfo portHost) {
        Port.PortConfiguration.Builder portConfiguration = Port.PortConfiguration.newBuilder();

        portConfiguration.setProjectId(customerPortState.getProjectId())
                .setNetworkId(customerPortState.getNetworkId())
                .setId(customerPortState.getId())
                .setName(customerPortState.getName())
                .setMacAddress(customerPortState.getMacAddress())
                .setVethName(customerPortState.getVethName())
                .setNetworkNs(customerPortState.getNetworkNamespace())
                .setHostInfo(Port.PortConfiguration.HostInfo.newBuilder()
                        .setIpAddress(portHost.getHostIpAddress())
                        .setMacAddress(portHost.getHostMacAddress()));

        for (PortState.FixedIp fixedIp : customerPortState.getFixedIps()) {
            portConfiguration.addFixedIps(
                    Port.PortConfiguration.FixedIp.newBuilder()
                            .setIpAddress(fixedIp.getIpAddress())
                            .setSubnetId(fixedIp.getSubnetId()));
        }

        return Port.PortState.newBuilder()
                .setOperationType(option)
                .setConfiguration(portConfiguration)
                .build();
    }
}
