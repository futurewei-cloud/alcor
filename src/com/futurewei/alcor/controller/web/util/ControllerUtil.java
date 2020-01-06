/*
Copyright 2020 The Alcor Authors.

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

import com.futurewei.alcor.controller.app.onebox.OneBoxConfig;
import com.futurewei.alcor.controller.app.onebox.OneBoxUtil;
import com.futurewei.alcor.controller.model.HostInfo;
import com.futurewei.alcor.controller.model.PortState;
import com.futurewei.alcor.controller.model.SubnetState;
import com.futurewei.alcor.controller.resourcemgr.physical.goalstatemgmt.GoalStateWorker;
import com.futurewei.alcor.controller.resourcemgr.physical.goalstatemgmt.PortProgramInfo;

public class ControllerUtil {

    public static long[] CreatePort(PortState portState) {

        HostInfo epHost = OneBoxConfig.epHosts.get(OneBoxConfig.epHostCounter);
        PortState customerPortState = OneBoxUtil.GeneretePortState(epHost, OneBoxConfig.epCounter);
        SubnetState customerSubnetState = OneBoxConfig.customerSubnetState;
        HostInfo[] transitSwitchHostsForSubnet = OneBoxConfig.transitSwitchHosts;

        System.out.println("EP host counter :" + OneBoxConfig.epHostCounter + "| ep counter: " + OneBoxConfig.epCounter);

        OneBoxConfig.epCounter++;
        if (OneBoxConfig.epCounter % OneBoxConfig.EP_PER_HOST == 0) {
            OneBoxConfig.epHostCounter++;
        }

        System.out.println("EP :" + customerPortState.getId() + " name " + customerPortState.getName());

        PortProgramInfo portProgramInfo = new PortProgramInfo(customerPortState, epHost, customerSubnetState, transitSwitchHostsForSubnet);
        GoalStateWorker worker = new GoalStateWorker(portProgramInfo);
        return worker.SendGoalStateToHosts();
    }
}
