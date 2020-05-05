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

import static com.futurewei.alcor.schema.Subnet.SubnetState;
import static com.futurewei.alcor.schema.Vpc.VpcState;

@Data
public class SubnetProgramInfo {

    private SubnetState customerSubnetState;
    private HostInfo[] transitSwitchHosts;
    private VpcState customerVpcState;
    private HostInfo[] transitRouterHosts;

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

    public VpcState getCustomerVpcState() {
        return customerVpcState;
    }

    public void setCustomerVpcState(VpcState customerVpcState) {
        this.customerVpcState = customerVpcState;
    }

    public HostInfo[] getTransitRouterHosts() {
        return transitRouterHosts;
    }

    public void setTransitRouterHosts(HostInfo[] transitRouterHosts) {
        this.transitRouterHosts = transitRouterHosts;
    }

    public SubnetProgramInfo(SubnetState customerSubnetState, HostInfo[] transitSwitchHosts, VpcState customerVpcState, HostInfo[] transitRouterHosts) {
        this.customerSubnetState = customerSubnetState;
        this.transitSwitchHosts = transitSwitchHosts.clone();
        this.customerVpcState = customerVpcState;
        this.transitRouterHosts = transitRouterHosts.clone();
    }
}
