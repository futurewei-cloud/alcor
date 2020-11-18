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
package com.futurewei.alcor.web.entity.route;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.web.entity.subnet.HostRoute;
import lombok.Data;

import java.util.List;

@Data
public class UpdateRoutingRuleResponse {

    @JsonProperty("internalRouterInfo")
    private InternalRouterInfo internalRouterInfo;

    @JsonProperty("hostRouteToSubnet")
    private List<HostRoute> hostRouteToSubnet;

    public UpdateRoutingRuleResponse() {}

    public UpdateRoutingRuleResponse(InternalRouterInfo internalRouterInfo, List<HostRoute> hostRouteToSubnet) {
        this.internalRouterInfo = internalRouterInfo;
        this.hostRouteToSubnet = hostRouteToSubnet;
    }

    public InternalRouterInfo getInternalRouterInfo() {
        return internalRouterInfo;
    }

    public void setInternalRouterInfo(InternalRouterInfo internalRouterInfo) {
        this.internalRouterInfo = internalRouterInfo;
    }

    public List<HostRoute> getHostRouteToSubnet() {
        return hostRouteToSubnet;
    }

    public void setHostRouteToSubnet(List<HostRoute> hostRouteToSubnet) {
        this.hostRouteToSubnet = hostRouteToSubnet;
    }
}
