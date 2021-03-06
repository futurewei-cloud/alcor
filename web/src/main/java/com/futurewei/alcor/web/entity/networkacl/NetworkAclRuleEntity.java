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
package com.futurewei.alcor.web.entity.networkacl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.common.entity.CustomerResource;

public class NetworkAclRuleEntity extends CustomerResource {
    public static final int NUMBER_MAX_VALUE = 32767;
    public static final String DEFAULT_IPV4_PREFIX = "0.0.0.0/0";
    public static final String DEFAULT_IPV6_PREFIX = "::/0";

    @JsonProperty("number")
    private Integer number;

    @JsonProperty("network_acl_id")
    private String networkAclId;

    @JsonProperty("ip_prefix")
    private String ipPrefix;

    @JsonProperty("direction")
    private String direction;

    @JsonProperty("protocol")
    private String protocol;

    @JsonProperty("port_range_max")
    private Integer portRangeMax;

    @JsonProperty("port_range_min")
    private Integer portRangeMin;

    @JsonProperty("icmp_type")
    private Integer icmpType;

    @JsonProperty("icmp_code")
    private Integer icmpCode;

    @JsonProperty("ether_type")
    private String etherType;

    @JsonProperty("action")
    private String action;

    public NetworkAclRuleEntity() {

    }

    public NetworkAclRuleEntity(Integer number, String networkAclId, String ipPrefix, String direction, String protocol, Integer portRangeMax, Integer portRangeMin, Integer icmpType, Integer icmpCode, String etherType, String action) {
        this.number = number;
        this.networkAclId = networkAclId;
        this.ipPrefix = ipPrefix;
        this.direction = direction;
        this.protocol = protocol;
        this.portRangeMax = portRangeMax;
        this.portRangeMin = portRangeMin;
        this.icmpType = icmpType;
        this.icmpCode = icmpCode;
        this.etherType = etherType;
        this.action = action;
    }

    public NetworkAclRuleEntity(CustomerResource state, Integer number, String networkAclId, String ipPrefix, String direction, String protocol, Integer portRangeMax, Integer portRangeMin, Integer icmpType, Integer icmpCode, String etherType, String action) {
        super(state);
        this.number = number;
        this.networkAclId = networkAclId;
        this.ipPrefix = ipPrefix;
        this.direction = direction;
        this.protocol = protocol;
        this.portRangeMax = portRangeMax;
        this.portRangeMin = portRangeMin;
        this.icmpType = icmpType;
        this.icmpCode = icmpCode;
        this.etherType = etherType;
        this.action = action;
    }

    public NetworkAclRuleEntity(String projectId, String id, String name, String description, Integer number, String networkAclId, String ipPrefix, String direction, String protocol, Integer portRangeMax, Integer portRangeMin, Integer icmpType, Integer icmpCode, String etherType, String action) {
        super(projectId, id, name, description);
        this.number = number;
        this.networkAclId = networkAclId;
        this.ipPrefix = ipPrefix;
        this.direction = direction;
        this.protocol = protocol;
        this.portRangeMax = portRangeMax;
        this.portRangeMin = portRangeMin;
        this.icmpType = icmpType;
        this.icmpCode = icmpCode;
        this.etherType = etherType;
        this.action = action;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getNetworkAclId() {
        return networkAclId;
    }

    public void setNetworkAclId(String networkAclId) {
        this.networkAclId = networkAclId;
    }

    public String getIpPrefix() {
        return ipPrefix;
    }

    public void setIpPrefix(String ipPrefix) {
        this.ipPrefix = ipPrefix;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Integer getPortRangeMax() {
        return portRangeMax;
    }

    public void setPortRangeMax(Integer portRangeMax) {
        this.portRangeMax = portRangeMax;
    }

    public Integer getPortRangeMin() {
        return portRangeMin;
    }

    public void setPortRangeMin(Integer portRangeMin) {
        this.portRangeMin = portRangeMin;
    }

    public Integer getIcmpType() {
        return icmpType;
    }

    public void setIcmpType(Integer icmpType) {
        this.icmpType = icmpType;
    }

    public Integer getIcmpCode() {
        return icmpCode;
    }

    public void setIcmpCode(Integer icmpCode) {
        this.icmpCode = icmpCode;
    }

    public String getEtherType() {
        return etherType;
    }

    public void setEtherType(String etherType) {
        this.etherType = etherType;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
