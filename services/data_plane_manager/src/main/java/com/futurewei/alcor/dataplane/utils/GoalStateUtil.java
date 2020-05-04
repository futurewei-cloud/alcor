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

import com.futurewei.alcor.dataplane.config.env.AppConfig;
import com.futurewei.alcor.dataplane.entity.*;
import com.futurewei.alcor.dataplane.service.NodeManager;
import com.futurewei.alcor.dataplane.service.impl.PortGoalStateServiceImpl;
import com.futurewei.alcor.dataplane.service.impl.SubnetGoalStateServiceImpl;
import com.futurewei.alcor.dataplane.utils.logging.Logger;
import com.futurewei.alcor.dataplane.utils.logging.LoggerFactory;
import com.futurewei.alcor.schema.Goalstate;
import com.futurewei.alcor.schema.Port;
import com.futurewei.alcor.schema.Subnet;
import com.futurewei.alcor.schema.Vpc;

import java.util.logging.Level;

import com.futurewei.alcor.common.constants.Common;

public class GoalStateUtil {
    public static Goalstate.GoalState CreateGoalState(
            com.futurewei.alcor.schema.Common.OperationType option,
            VpcState customerVpcState,
            HostInfo[] transitRouterHosts) {
        final Vpc.VpcState vpcState = GoalStateUtil.CreateGSVpcState(
                option,
                customerVpcState,
                transitRouterHosts);

        Goalstate.GoalState goalstate = Goalstate.GoalState.newBuilder()
                .addVpcStates(vpcState)
                .build();

        return goalstate;
    }

    public static Goalstate.GoalState CreateGoalState(
            com.futurewei.alcor.schema.Common.OperationType option,
            SubnetState[] customerSubnetStates,
            HostInfo[][] transitSwitchHosts) {
        Goalstate.GoalState.Builder goalstate = Goalstate.GoalState.newBuilder();

        for (int i = 0; i < customerSubnetStates.length; i++) {
            final Subnet.SubnetState gsSubnetState = GoalStateUtil.CreateGSSubnetState(
                    option,
                    customerSubnetStates[i],
                    transitSwitchHosts[i]);

            goalstate.addSubnetStates(gsSubnetState);
        }

        return goalstate.build();
    }

    public static Goalstate.GoalState CreateGoalState(
            com.futurewei.alcor.schema.Common.OperationType vpcOption,
            VpcState customerVpcState,
            HostInfo[] transitRouterHosts,
            com.futurewei.alcor.schema.Common.OperationType subnetOption,
            SubnetState[] customerSubnetStates,
            HostInfo[][] transitSwitchHosts) {
        final Vpc.VpcState vpcState = GoalStateUtil.CreateGSVpcState(
                vpcOption,
                customerVpcState,
                transitRouterHosts);

        Goalstate.GoalState.Builder goalState = Goalstate.GoalState.newBuilder().addVpcStates(vpcState);

        for (int i = 0; i < customerSubnetStates.length; i++) {
            final Subnet.SubnetState gsSubnetState = GoalStateUtil.CreateGSSubnetState(
                    subnetOption,
                    customerSubnetStates[i],
                    transitSwitchHosts[i]);

            goalState.addSubnetStates(gsSubnetState);
        }

        return goalState.build();
    }

    public static Goalstate.GoalState CreateGoalState(
            com.futurewei.alcor.schema.Common.OperationType subnetOption,
            SubnetState customerSubnetState,
            HostInfo[] transitSwitchHosts,
            com.futurewei.alcor.schema.Common.OperationType portOption,
            PortState[] customerPortStates,
            HostInfo[] portHosts) {
        final Subnet.SubnetState gsSubnetState = GoalStateUtil.CreateGSSubnetState(
                subnetOption,
                customerSubnetState,
                transitSwitchHosts);

        Goalstate.GoalState.Builder goalstate = Goalstate.GoalState.newBuilder().addSubnetStates(gsSubnetState);

        for (int i = 0; i < customerPortStates.length; i++) {
            final Port.PortState gsPortState = GoalStateUtil.CreateGSPortState(
                    portOption,
                    customerPortStates[i],
                    portHosts[i]);

            goalstate.addPortStates(gsPortState);
        }

        return goalstate.build();
    }

    public static Goalstate.GoalState CreateGoalState(
            com.futurewei.alcor.schema.Common.OperationType subnetOption,
            SubnetState customerSubnetState,
            HostInfo[] transitSwitchHosts,
            com.futurewei.alcor.schema.Common.OperationType portOption,
            PortState[] customerPortStates,
            HostInfo portHost) {
        final Subnet.SubnetState gsSubnetState = GoalStateUtil.CreateGSSubnetState(
                subnetOption,
                customerSubnetState,
                transitSwitchHosts);

        Goalstate.GoalState.Builder goalstate = Goalstate.GoalState.newBuilder().addSubnetStates(gsSubnetState);

        for (int i = 0; i < customerPortStates.length; i++) {
            final Port.PortState gsPortState = GoalStateUtil.CreateGSPortState(
                    portOption,
                    customerPortStates[i],
                    portHost);

            goalstate.addPortStates(gsPortState);
        }

        return goalstate.build();
    }

    public static Goalstate.GoalState CreateGoalState(
            com.futurewei.alcor.schema.Common.OperationType subnetOption,
            SubnetState customerSubnetState,
            HostInfo[] transitSwitchHosts,
            com.futurewei.alcor.schema.Common.OperationType portOption,
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

        Goalstate.GoalState goalstate = Goalstate.GoalState.newBuilder()
                .addSubnetStates(gsSubnetState)
                .addPortStates(gsPortState)
                .build();

        return goalstate;
    }

    public static Vpc.VpcState CreateGSVpcState(

            com.futurewei.alcor.schema.Common.OperationType option,
            String project_id,
            String vpc_id,
            String vpc_name,
            String cidr) {
        return Vpc.VpcState.newBuilder()
                .setOperationType(option)
                .setConfiguration(Vpc.VpcConfiguration.newBuilder()
                        .setProjectId(project_id)
                        .setId(vpc_id)
                        .setName(vpc_name)
                        .setCidr(cidr)
                        .setTunnelId(AppConfig.Tunnel_Id))
                .build();
    }

    public static Vpc.VpcState CreateGSVpcState(
            com.futurewei.alcor.schema.Common.OperationType option,
            VpcState customerVpcState,
            HostInfo[] transitRouterHosts) {
        String vpcId = customerVpcState.getId();
        Vpc.VpcConfiguration.Builder vpcConfiguration = Vpc.VpcConfiguration.newBuilder();

        vpcConfiguration.setProjectId(customerVpcState.getProjectId())
                .setId(vpcId)
                .setName(customerVpcState.getName())
                .setCidr(customerVpcState.getCidr())
                .setTunnelId(AppConfig.Tunnel_Id);

        for (HostInfo routerHost : transitRouterHosts) {
            vpcConfiguration.addTransitRouters(
                    Vpc.VpcConfiguration.TransitRouter.newBuilder()
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
            com.futurewei.alcor.schema.Common.OperationType option,
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
                .setTunnelId(AppConfig.Tunnel_Id);

        subnetConfiguration.setGateway(
                Subnet.SubnetConfiguration.Gateway.newBuilder()
                        .setVpcId(vpcId)
                        .setSubnetId(subnetId)
                        .setIpAddress(customerSubnetState.getGatewayIp())
                        .setMacAddress(AppConfig.GATEWAY_MAC_ADDRESS));

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
            com.futurewei.alcor.schema.Common.OperationType option,
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

    public static PortState CreatePort(PortState portState) {
        Logger logger = LoggerFactory.getLogger();

        PortState customerPortState = AssignVipMacToPort(portState, ControllerConfig.epCounter);
        HostInfo epHost = NodeManager.nodeManager.getHostInfoById(portState.getBindingHostId());
        SubnetState customerSubnetState = ControllerConfig.customerSubnetState;
        HostInfo[] transitSwitchHostsForSubnet = ControllerConfig.transitSwitchHosts;

        logger.log(Level.INFO, "EP counter: " + ControllerConfig.epCounter);
        ControllerConfig.epCounter++;
        logger.log(Level.INFO, "EP :" + customerPortState);
        logger.log(Level.INFO, "Host :" + epHost);

        PortProgramInfo portProgramInfo = new PortProgramInfo(customerPortState, epHost, customerSubnetState, transitSwitchHostsForSubnet);
        PortGoalStateServiceImpl gsProgrammer = new PortGoalStateServiceImpl(portProgramInfo);
        gsProgrammer.SendGoalStateToHosts();

        return customerPortState;
    }

    public static void CreateSubnet(SubnetState subnetState, VpcState vpcState) {

        SubnetState customerSubnetState = new SubnetState(subnetState);
        HostInfo[] transitSwitchHosts = ControllerConfig.transitSwitchHosts;
        VpcState customerVpcState = vpcState;
        HostInfo[] transitRouterHosts = ControllerConfig.transitRouterHosts;

        SubnetProgramInfo subnetProgramInfo = new SubnetProgramInfo(customerSubnetState, transitSwitchHosts, customerVpcState, transitRouterHosts);
        SubnetGoalStateServiceImpl gsProgrammer = new SubnetGoalStateServiceImpl(subnetProgramInfo);
        gsProgrammer.SendGoalStateToHosts();
    }

    public static PortState AssignVipMacToPort(PortState portState, int epIndex) {

        PortState state = new PortState(portState);

        state.setMacAddress(GenereateMacAddress(epIndex));
        String[] vpcIps = new String[]{GenereateIpAddress(epIndex)};
        state.setFixedIps(PortState.convertToFixedIps(vpcIps, portState.getNetworkId()));
        state.setStatus("UP");

        return state;
    }

    private static String GenereateMacAddress(int index) {
        return "0e:73:ae:c8:" + Integer.toHexString((index + 6) / 256) + ":" + Integer.toHexString((index + 6) % 256);
    }

    private static String GenereateIpAddress(int index) {
        return "10.0." + (index + 6) / 256 + "." + (index + 6) % 256;
    }
}
