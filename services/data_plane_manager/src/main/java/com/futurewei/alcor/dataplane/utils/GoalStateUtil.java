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

import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.dataplane.config.Config;
import com.futurewei.alcor.dataplane.entity.PortProgramInfo;
import com.futurewei.alcor.dataplane.entity.SubnetProgramInfo;
import com.futurewei.alcor.dataplane.service.NodeManager;
import com.futurewei.alcor.dataplane.service.impl.PortGoalStateServiceImpl;
import com.futurewei.alcor.dataplane.service.impl.SubnetGoalStateServiceImpl;
import com.futurewei.alcor.schema.Goalstate;
import com.futurewei.alcor.schema.Port;
import com.futurewei.alcor.schema.Port.PortConfiguration.HostInfo;
import com.futurewei.alcor.schema.Subnet;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static com.futurewei.alcor.schema.Common.OperationType;
import static com.futurewei.alcor.schema.Port.PortConfiguration.FixedIp;
import static com.futurewei.alcor.schema.Port.PortState;
import static com.futurewei.alcor.schema.Subnet.SubnetState;
import static com.futurewei.alcor.schema.Vpc.VpcConfiguration;
import static com.futurewei.alcor.schema.Vpc.VpcState;


public class GoalStateUtil {

    public static Goalstate.GoalState CreateGoalState(OperationType option,
                                                      Subnet.SubnetState[] customerSubnetStates, HostInfo[][] transitSwitchHosts) {
        Goalstate.GoalState.Builder goalstate =
                Goalstate.GoalState.newBuilder();

        for (int i = 0; i < customerSubnetStates.length; i++) {
            final Subnet.SubnetState gsSubnetState =
                    GoalStateUtil.CreateGSSubnetState(option,
                            customerSubnetStates[i], transitSwitchHosts[i]);

            goalstate.addSubnetStates(gsSubnetState);
        }

        return goalstate.build();
    }

    public static Goalstate.GoalState CreateGoalState(OperationType vpcOption
            , VpcState customerVpcState, HostInfo[] transitRouterHosts,
                                                      OperationType subnetOption, Subnet.SubnetState[] customerSubnetStates, HostInfo[][] transitSwitchHosts) {
        final VpcState vpcState = GoalStateUtil.CreateGSVpcState(vpcOption,
                customerVpcState, transitRouterHosts);

        Goalstate.GoalState.Builder goalState =
                Goalstate.GoalState.newBuilder().addVpcStates(vpcState);

        for (int i = 0; i < customerSubnetStates.length; i++) {
            final Subnet.SubnetState gsSubnetState =
                    GoalStateUtil.CreateGSSubnetState(subnetOption,
                            customerSubnetStates[i], transitSwitchHosts[i]);

            goalState.addSubnetStates(gsSubnetState);
        }

        return goalState.build();
    }

    public static Goalstate.GoalState CreateGoalState(OperationType subnetOption, Subnet.SubnetState customerSubnetState, HostInfo[] transitSwitchHosts, OperationType portOption, PortState[] customerPortStates, HostInfo[] portHosts) {
        final Subnet.SubnetState gsSubnetState =
                GoalStateUtil.CreateGSSubnetState(subnetOption,
                        customerSubnetState, transitSwitchHosts);

        Goalstate.GoalState.Builder goalstate =
                Goalstate.GoalState.newBuilder().addSubnetStates(gsSubnetState);

        for (int i = 0; i < customerPortStates.length; i++) {
            final PortState gsPortState =
                    GoalStateUtil.CreateGSPortState(portOption,
                            customerPortStates[i], portHosts[i]);

            goalstate.addPortStates(gsPortState);
        }

        return goalstate.build();
    }

    public static Goalstate.GoalState CreateGoalState(OperationType subnetOption, Subnet.SubnetState customerSubnetState, HostInfo[] transitSwitchHosts, OperationType portOption, PortState[] customerPortStates, HostInfo portHost) {
        final Subnet.SubnetState gsSubnetState =
                GoalStateUtil.CreateGSSubnetState(subnetOption,
                        customerSubnetState, transitSwitchHosts);

        Goalstate.GoalState.Builder goalstate =
                Goalstate.GoalState.newBuilder().addSubnetStates(gsSubnetState);

        for (int i = 0; i < customerPortStates.length; i++) {
            final PortState gsPortState =
                    GoalStateUtil.CreateGSPortState(portOption,
                            customerPortStates[i], portHost);

            goalstate.addPortStates(gsPortState);
        }

        return goalstate.build();
    }

    public static Goalstate.GoalState CreateGoalState(OperationType subnetOption, Subnet.SubnetState customerSubnetState, HostInfo[] transitSwitchHosts, OperationType portOption, PortState customerPortState, HostInfo portHost) {
        final Subnet.SubnetState gsSubnetState =
                GoalStateUtil.CreateGSSubnetState(subnetOption,
                        customerSubnetState, transitSwitchHosts);

        final PortState gsPortState =
                GoalStateUtil.CreateGSPortState(portOption, customerPortState
                        , portHost);

        Goalstate.GoalState goalstate =
                Goalstate.GoalState.newBuilder().addSubnetStates(gsSubnetState).addPortStates(gsPortState).build();

        return goalstate;
    }


    public static VpcState CreateGSVpcState(OperationType option,
                                            VpcState customerVpcState,
                                            HostInfo[] transitRouterHosts) {
        String vpcId = customerVpcState.getConfiguration().getId();
        VpcConfiguration.Builder vpcConfiguration =
                VpcConfiguration.newBuilder();

        vpcConfiguration.setProjectId(customerVpcState.getConfiguration().getProjectId()).setId(vpcId).setName(customerVpcState.getConfiguration().getName()).setCidr(customerVpcState.getConfiguration().getCidr()).setTunnelId(Config.Tunnel_Id);

        for (HostInfo routerHost : transitRouterHosts) {
            vpcConfiguration.addTransitRouters(VpcConfiguration.TransitRouter.newBuilder().setVpcId(vpcId).setIpAddress(routerHost.getIpAddress()).setMacAddress(routerHost.getMacAddress()));
        }

        vpcConfiguration.build();

        return VpcState.newBuilder().setOperationType(option).setConfiguration(vpcConfiguration).build();
    }

    public static Subnet.SubnetState CreateGSSubnetState(OperationType option
            , Subnet.SubnetState customerSubnetState, HostInfo[] transitSwitchHosts) {
        String vpcId = customerSubnetState.getConfiguration().getVpcId();
        String subnetId = customerSubnetState.getConfiguration().getId();
        Subnet.SubnetConfiguration.Builder subnetConfiguration =
                Subnet.SubnetConfiguration.newBuilder();

        subnetConfiguration.setProjectId(customerSubnetState.getConfiguration().getProjectId()).setVpcId(vpcId).setId(subnetId).setName(customerSubnetState.getConfiguration().getName()).setCidr(customerSubnetState.getConfiguration().getCidr()).setTunnelId(Config.Tunnel_Id);

        subnetConfiguration.setGateway(Subnet.SubnetConfiguration.Gateway.newBuilder().setVpcId(vpcId).setSubnetId(subnetId).setIpAddress(customerSubnetState.getConfiguration().getGateway().getIpAddress()).setMacAddress(Config.GATEWAY_MAC_ADDRESS));

        for (HostInfo switchHost : transitSwitchHosts) {
            subnetConfiguration.addTransitSwitches(Subnet.SubnetConfiguration.TransitSwitch.newBuilder().setVpcId(vpcId).setSubnetId(subnetId).setIpAddress(switchHost.getIpAddress()).setMacAddress(switchHost.getMacAddress()));
        }

        subnetConfiguration.build();

        return Subnet.SubnetState.newBuilder().setOperationType(option).setConfiguration(subnetConfiguration).build();
    }

    public static PortState CreateGSPortState(OperationType option,
                                              PortState customerPortState,
                                              HostInfo portHost) {
        Port.PortConfiguration.Builder portConfiguration =
                Port.PortConfiguration.newBuilder();

        portConfiguration.setProjectId(customerPortState.getConfiguration().getProjectId()).setNetworkId(customerPortState.getConfiguration().getNetworkId()).setId(customerPortState.getConfiguration().getId()).setName(customerPortState.getConfiguration().getName()).setMacAddress(customerPortState.getConfiguration().getMacAddress()).setVethName(customerPortState.getConfiguration().getVethName()).setNetworkNs(customerPortState.getConfiguration().getNetworkNs()).setHostInfo(HostInfo.newBuilder().setIpAddress(portHost.getIpAddress()).setMacAddress(portHost.getMacAddress()));

        for (Port.PortConfiguration.FixedIp fixedIp :
                customerPortState.getConfiguration().getFixedIpsList()) {
            portConfiguration.addFixedIps(Port.PortConfiguration.FixedIp.newBuilder().setIpAddress(fixedIp.getIpAddress()).setSubnetId(fixedIp.getSubnetId()));
        }

        return PortState.newBuilder().setOperationType(option).setConfiguration(portConfiguration).build();
    }

    public static PortState CreatePort(PortState portState) {
        Logger logger = LoggerFactory.getLogger();

        PortState customerPortState = AssignVipMacToPort(portState,
                Config.epCounter);
        HostInfo epHost =
                NodeManager.nodeManager.getHostInfoById(portState.getConfiguration().getId()); // not sure
        SubnetState customerSubnetState = Config.customerSubnetState;
        HostInfo[] transitSwitchHostsForSubnet = Config.transitSwitchHosts;

        logger.log(Level.INFO, "EP counter: " + Config.epCounter);
        Config.epCounter++;
        logger.log(Level.INFO, "EP :" + customerPortState);
        logger.log(Level.INFO, "Host :" + epHost);

        PortProgramInfo portProgramInfo =
                new PortProgramInfo(customerPortState, epHost,
                        customerSubnetState, transitSwitchHostsForSubnet);
        PortGoalStateServiceImpl gsProgrammer =
                new PortGoalStateServiceImpl(portProgramInfo);
        gsProgrammer.SendGoalStateToHosts();

        return customerPortState;
    }

    public static void CreateSubnet(SubnetState subnetState,
                                    VpcState vpcState) {

        SubnetState customerSubnetState = subnetState;
        HostInfo[] transitSwitchHosts = Config.transitSwitchHosts;
        VpcState customerVpcState = vpcState;
        HostInfo[] transitRouterHosts = Config.transitRouterHosts;

        SubnetProgramInfo subnetProgramInfo =
                new SubnetProgramInfo(customerSubnetState, transitSwitchHosts
                        , customerVpcState, transitRouterHosts);
        SubnetGoalStateServiceImpl gsProgrammer =
                new SubnetGoalStateServiceImpl(subnetProgramInfo);
        gsProgrammer.SendGoalStateToHosts();
    }

    public static PortState AssignVipMacToPort(PortState portState,
                                               int epIndex) {

        Port.PortConfiguration.Builder conf =
                portState.getConfiguration().newBuilder();
        conf.setMacAddress(GenereateMacAddress(epIndex));
        String[] vpcIps = new String[]{GenereateIpAddress(epIndex)};
        try {

            List<FixedIp> fixedIps = convertToFixedIps(vpcIps,
                    portState.getConfiguration().getNetworkId());
            for (FixedIp fip : fixedIps) {
                conf.addFixedIps(fip);
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
//        conf.setStatus("UP"); // not sure
        PortState.Builder builder = portState.newBuilderForType();
        builder.setConfiguration(conf);
        return builder.build();
    }

    public static List<FixedIp> convertToFixedIps(String[] vpcIps,
                                                  String subnetId) throws InvalidProtocolBufferException {

        List<FixedIp> fixedIps = new ArrayList<>();
        if (vpcIps != null) {
            for (String vpcIp : vpcIps) {
                FixedIp.Builder builder = FixedIp.newBuilder();
                FixedIp build =
                        builder.setSubnetId(subnetId).setIpAddress(vpcIp).build();
                fixedIps.add(build);
            }
        }

        return fixedIps;
    }

    private static String GenereateMacAddress(int index) {
        return "0e:73:ae:c8:" + Integer.toHexString((index + 6) / 256) + ":" + Integer.toHexString((index + 6) % 256);
    }

    private static String GenereateIpAddress(int index) {
        return "10.0." + (index + 6) / 256 + "." + (index + 6) % 256;
    }
}
