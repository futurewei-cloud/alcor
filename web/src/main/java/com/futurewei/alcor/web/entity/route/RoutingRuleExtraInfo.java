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
import com.futurewei.alcor.common.enumClass.VpcRouteTarget;

public class RoutingRuleExtraInfo {
    @JsonProperty("next_hop_mac")
    private String next_hop_mac;

    @JsonProperty("destination_type")
    private VpcRouteTarget destination_type;

    public RoutingRuleExtraInfo() {

    }

    public RoutingRuleExtraInfo(String next_hop_mac, VpcRouteTarget destination_type) {
        this.next_hop_mac = next_hop_mac;
        this.destination_type = destination_type;
    }

    public String getNextHopMac() { return this.next_hop_mac; }
    public void setNextHopMac(String next_hop_mac) { this.next_hop_mac = next_hop_mac; }

    public VpcRouteTarget getDestinationType() { return this.destination_type; }
    public void setDestinationType(VpcRouteTarget destination_type) {
        this.destination_type = destination_type;
    }
}
