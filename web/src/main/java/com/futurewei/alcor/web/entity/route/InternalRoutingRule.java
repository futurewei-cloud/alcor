/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
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
