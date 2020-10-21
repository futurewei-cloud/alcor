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
package com.futurewei.alcor.web.entity.securitygroup;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.common.entity.CustomerResource;
import com.google.gson.annotations.SerializedName;

public class SecurityGroupRule extends CustomerResource {

    @JsonProperty("security_group_id")
    private String securityGroupId;

    @JsonProperty("remote_group_id")
    private String remoteGroupId;

    @JsonProperty("direction")
    private String direction;

    @JsonProperty("remote_ip_prefix")
    private String remoteIpPrefix;

    @JsonProperty("protocol")
    private String protocol;

    @JsonProperty("port_range_max")
    private Integer portRangeMax;

    @JsonProperty("port_range_min")
    private Integer portRangeMin;

    @JsonProperty("ethertype")
    private String etherType;

    public static enum EtherType {
        IPV4("IPv4"),
        IPV6("IPv6");

        private String type;

        EtherType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return "EtherType{" +
                    "type='" + type + '\'' +
                    '}';
        }
    }

    public static enum Direction {
        INGRESS("ingress"),
        EGRESS("egress");

        private String direction;

        Direction(String direction) {
            this.direction = direction;
        }

        public String getDirection() {
            return direction;
        }

        public void setDirection(String direction) {
            this.direction = direction;
        }

        @Override
        public String toString() {
            return "Direction{" +
                    "direction='" + direction + '\'' +
                    '}';
        }
    }

    public SecurityGroupRule() {
    }

    public SecurityGroupRule(String securityGroupId, String remoteGroupId, String direction, String remoteIpPrefix, String protocol, Integer portRangeMax, Integer portRangeMin, String etherType) {
        this.securityGroupId = securityGroupId;
        this.remoteGroupId = remoteGroupId;
        this.direction = direction;
        this.remoteIpPrefix = remoteIpPrefix;
        this.protocol = protocol;
        this.portRangeMax = portRangeMax;
        this.portRangeMin = portRangeMin;
        this.etherType = etherType;
    }

    public SecurityGroupRule(CustomerResource state, String securityGroupId, String remoteGroupId, String direction, String remoteIpPrefix, String protocol, Integer portRangeMax, Integer portRangeMin, String etherType) {
        super(state);
        this.securityGroupId = securityGroupId;
        this.remoteGroupId = remoteGroupId;
        this.direction = direction;
        this.remoteIpPrefix = remoteIpPrefix;
        this.protocol = protocol;
        this.portRangeMax = portRangeMax;
        this.portRangeMin = portRangeMin;
        this.etherType = etherType;
    }

    public SecurityGroupRule(String projectId, String id, String name, String description, String securityGroupId, String remoteGroupId, String direction, String remoteIpPrefix, String protocol, Integer portRangeMax, Integer portRangeMin, String etherType) {
        super(projectId, id, name, description);
        this.securityGroupId = securityGroupId;
        this.remoteGroupId = remoteGroupId;
        this.direction = direction;
        this.remoteIpPrefix = remoteIpPrefix;
        this.protocol = protocol;
        this.portRangeMax = portRangeMax;
        this.portRangeMin = portRangeMin;
        this.etherType = etherType;
    }

    public String getSecurityGroupId() {
        return securityGroupId;
    }

    public void setSecurityGroupId(String securityGroupId) {
        this.securityGroupId = securityGroupId;
    }

    public String getRemoteGroupId() {
        return remoteGroupId;
    }

    public void setRemoteGroupId(String remoteGroupId) {
        this.remoteGroupId = remoteGroupId;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getRemoteIpPrefix() {
        return remoteIpPrefix;
    }

    public void setRemoteIpPrefix(String remoteIpPrefix) {
        this.remoteIpPrefix = remoteIpPrefix;
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

    public String getEtherType() {
        return etherType;
    }

    public void setEtherType(String etherType) {
        this.etherType = etherType;
    }

//    @Override
//    public String toString() {
//        return "SecurityGroupRuleJson{" +
//                "securityGroupId='" + securityGroupId + '\'' +
//                ", remoteGroupId='" + remoteGroupId + '\'' +
//                ", direction='" + direction + '\'' +
//                ", remoteIpPrefix='" + remoteIpPrefix + '\'' +
//                ", protocol='" + protocol + '\'' +
//                ", portRangeMax=" + portRangeMax +
//                ", portRangeMin=" + portRangeMin +
//                ", etherType='" + etherType + '\'' +
//                '}';
//    }
}
