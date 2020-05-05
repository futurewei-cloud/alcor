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
package com.futurewei.alcor.dataplane.entity;

import com.futurewei.alcor.schema.Port.PortConfiguration.HostInfo;
import lombok.Data;

import static com.futurewei.alcor.schema.Port.PortState;
import static com.futurewei.alcor.schema.Subnet.SubnetState;

@Data
public class PortProgramInfo {
    public PortState getCustomerPortState() {
        return customerPortState;
    }

    public void setCustomerPortState(PortState customerPortState) {
        this.customerPortState = customerPortState;
    }

    public HostInfo getEpHost() {
        return epHost;
    }

    public void setEpHost(HostInfo epHost) {
        this.epHost = epHost;
    }

    public SubnetState getCustomerSubnetState() {
        return customerSubnetState;
    }

    public void setCustomerSubnetState(SubnetState customerSubnetState) {
        this.customerSubnetState = customerSubnetState;
    }

    public HostInfo[] getTransitSwitchHosts() {
        return transitSwitchHosts;
    }

    public void setTransitSwitchHosts(HostInfo[] transitSwitchHosts) {
        this.transitSwitchHosts = transitSwitchHosts;
    }

    public boolean isFastPath() {
        return isFastPath;
    }

    public void setFastPath(boolean fastPath) {
        isFastPath = fastPath;
    }

    private boolean isFastPath=false;
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
