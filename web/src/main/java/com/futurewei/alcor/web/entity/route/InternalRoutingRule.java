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
import com.futurewei.alcor.common.enumClass.OperationType;

public class InternalRoutingRule {
    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("destination")
    private String destination;

    @JsonProperty("next_hop_ip")
    private String next_hop_ip;

    @JsonProperty("priority")
    private Integer priority;

    @JsonProperty("operation_type")
    private OperationType operation_type;

    @JsonProperty("routing_rule_extra_info")
    private InternalRoutingRuleExtraInfo routing_rule_extra_info;

    public InternalRoutingRule() {

    }

    public InternalRoutingRule(String id,
                               String name,
                               String destination,
                               String next_hop_ip,
                               Integer priority,
                               OperationType operation_type,
                               InternalRoutingRuleExtraInfo routing_rule_extra_info) {
        this.id = id;
        this.name = name;
        this.destination = destination;
        this.next_hop_ip = next_hop_ip;
        this.priority = priority;
        this.operation_type = operation_type;
        this.routing_rule_extra_info = routing_rule_extra_info;
    }

    public String getId() { return this.id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }

    public String getDestination() { return this.destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getNextHopIp() { return this.next_hop_ip; }
    public void setNextHopIp(String next_hop_ip) { this.next_hop_ip = next_hop_ip; }

    public Integer getPriority() { return this.priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public OperationType getOperationType() { return this.operation_type; }
    public void setOperationType(OperationType operation_type) { this.operation_type = operation_type; }

    public InternalRoutingRuleExtraInfo getRoutingRuleExtraInfo() { return this.routing_rule_extra_info; }
    public void setRoutingRuleExtraInfo(InternalRoutingRuleExtraInfo routing_rule_extra_info) {
        this.routing_rule_extra_info = routing_rule_extra_info;
    }
}
