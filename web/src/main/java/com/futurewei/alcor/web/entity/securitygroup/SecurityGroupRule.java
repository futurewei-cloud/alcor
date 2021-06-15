/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.futurewei.alcor.web.entity.securitygroup;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.common.entity.CustomerResource;

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
