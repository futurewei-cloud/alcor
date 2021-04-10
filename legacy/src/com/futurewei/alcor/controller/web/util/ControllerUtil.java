/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.futurewei.alcor.controller.web.util;

import com.futurewei.alcor.controller.logging.Logger;
import com.futurewei.alcor.controller.logging.LoggerFactory;
import com.futurewei.alcor.controller.model.HostInfo;
import com.futurewei.alcor.controller.model.PortState;
import com.futurewei.alcor.controller.model.SubnetState;
import com.futurewei.alcor.controller.model.VpcState;
import com.futurewei.alcor.controller.resourcemgr.physical.goalstatemgmt.PortGoalStateProgrammer;
import com.futurewei.alcor.controller.resourcemgr.physical.goalstatemgmt.PortProgramInfo;
import com.futurewei.alcor.controller.resourcemgr.physical.goalstatemgmt.SubnetGoalStateProgrammer;
import com.futurewei.alcor.controller.resourcemgr.physical.goalstatemgmt.SubnetProgramInfo;
import com.futurewei.alcor.controller.resourcemgr.physical.nodemgmt.DataCenterConfig;

import java.util.logging.Level;

public class ControllerUtil {

    public static PortState CreatePort(PortState portState) {
        Logger logger = LoggerFactory.getLogger();

        PortState customerPortState = ControllerUtil.AssignVipMacToPort(portState, ControllerConfig.epCounter);
        HostInfo epHost = DataCenterConfig.nodeManager.getHostInfoById(portState.getBindingHostId());
        SubnetState customerSubnetState = ControllerConfig.customerSubnetState;
        HostInfo[] transitSwitchHostsForSubnet = ControllerConfig.transitSwitchHosts;

        logger.log(Level.INFO, "EP counter: " + ControllerConfig.epCounter);
        ControllerConfig.epCounter++;
        logger.log(Level.INFO, "EP :" + customerPortState);
        logger.log(Level.INFO, "Host :" + epHost);

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
