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
package com.futurewei.alcor.controller.web.util;

import com.futurewei.alcor.controller.model.HostInfo;
import com.futurewei.alcor.controller.model.PortState;
import com.futurewei.alcor.controller.model.SubnetState;
import com.futurewei.alcor.controller.model.VpcState;
import com.futurewei.alcor.controller.resourcemgr.physical.goalstatemgmt.PortGoalStateProgrammer;
import com.futurewei.alcor.controller.resourcemgr.physical.goalstatemgmt.PortProgramInfo;
import com.futurewei.alcor.controller.resourcemgr.physical.goalstatemgmt.SubnetGoalStateProgrammer;
import com.futurewei.alcor.controller.resourcemgr.physical.goalstatemgmt.SubnetProgramInfo;
import com.futurewei.alcor.controller.resourcemgr.physical.nodemgmt.DataCenterConfig;

public class ControllerUtil {

    public static PortState CreatePort(PortState portState) {

        PortState customerPortState = ControllerUtil.AssignVipMacToPort(portState, ControllerConfig.epCounter);
        HostInfo epHost = DataCenterConfig.nodeManager.getHostInfoById(portState.getBindingHostId());
        SubnetState customerSubnetState = ControllerConfig.customerSubnetState;
        HostInfo[] transitSwitchHostsForSubnet = ControllerConfig.transitSwitchHosts;

        System.out.println("EP counter: " + ControllerConfig.epCounter);
        ControllerConfig.epCounter++;
        System.out.println("EP :" + customerPortState);
        System.out.println("Host :" + epHost);

        PortProgramInfo portProgramInfo = new PortProgramInfo(customerPortState, epHost, customerSubnetState, transitSwitchHostsForSubnet);
        PortGoalStateProgrammer gsProgrammer = new PortGoalStateProgrammer(portProgramInfo);
        gsProgrammer.SendGoalStateToHosts();

        return customerPortState;
    }

    public static void CreateSubnet(SubnetState subnetState, VpcState vpcState) {

        SubnetState customerSubnetState = new SubnetState(subnetState);
        HostInfo[] transitSwitchHosts = ControllerConfig.transitSwitchHosts;
        VpcState customerVpcState = vpcState;
        HostInfo[] transitRouterHosts = ControllerConfig.transitRouterHosts;

        SubnetProgramInfo subnetProgramInfo = new SubnetProgramInfo(customerSubnetState, transitSwitchHosts, customerVpcState, transitRouterHosts);
        SubnetGoalStateProgrammer gsProgrammer = new SubnetGoalStateProgrammer(subnetProgramInfo);
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
