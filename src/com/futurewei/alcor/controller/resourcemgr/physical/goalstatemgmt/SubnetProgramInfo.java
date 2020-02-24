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

import com.futurewei.alcor.controller.app.onebox.OneBoxConfig;
import com.futurewei.alcor.controller.model.HostInfo;
import com.futurewei.alcor.controller.model.SubnetState;
import com.futurewei.alcor.controller.model.VpcState;
import lombok.Data;

@Data
public class SubnetProgramInfo {

    private SubnetState customerSubnetState;
    private HostInfo[] transitSwitchHosts;
    private VpcState customerVpcState;
    private HostInfo[] transitRouterHosts;

    public SubnetProgramInfo(SubnetState customerSubnetState, HostInfo[] transitSwitchHosts, VpcState customerVpcState, HostInfo[] transitRouterHosts){
        this.customerSubnetState = customerSubnetState;
        this.transitSwitchHosts = transitSwitchHosts.clone();
        this.customerVpcState = customerVpcState;
        this.transitRouterHosts = transitRouterHosts.clone();
    }
}
