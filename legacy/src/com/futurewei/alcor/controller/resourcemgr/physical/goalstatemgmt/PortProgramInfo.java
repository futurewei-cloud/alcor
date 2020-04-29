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
package com.futurewei.alcor.controller.resourcemgr.physical.goalstatemgmt;

import com.futurewei.alcor.controller.model.HostInfo;
import com.futurewei.alcor.controller.model.PortState;
import com.futurewei.alcor.controller.model.SubnetState;
import lombok.Data;

@Data
public class PortProgramInfo {

    private PortState customerPortState;
    private HostInfo epHost;
    private SubnetState customerSubnetState;
    private HostInfo[] transitSwitchHosts;

    public PortProgramInfo(PortState portState, HostInfo epHost, SubnetState subnetState, HostInfo[] transitSwitchHostsForSubnet) {
        this.customerPortState = portState;
        this.epHost = epHost;
        this.customerSubnetState = subnetState;
        this.transitSwitchHosts = transitSwitchHostsForSubnet.clone();
    }
}
