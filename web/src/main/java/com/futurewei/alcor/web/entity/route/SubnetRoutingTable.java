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
import java.util.List;

public class SubnetRoutingTable {
    @JsonProperty("subnet_id")
    private String subnet_id;

    @JsonProperty("routing_rules")
    private List<RoutingRule> routing_rules;

    public SubnetRoutingTable() {

    }

    public SubnetRoutingTable(String subnet_id, List<RoutingRule> routing_rules) {
        this.subnet_id = subnet_id;
        this.routing_rules = routing_rules;
    }

    public String getSubnetId() { return this.subnet_id; }
    public void setSubnetId(String subnet_id) { this.subnet_id = subnet_id; }

    public List<RoutingRule> getRoutingRules() { return this.routing_rules; }
    public void setRoutingRules(List<RoutingRule> routing_rules) {
        this.routing_rules = routing_rules;
    }
}
