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
package com.futurewei.alcor.web.entity.gateway;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ZetaPortEntity {
    @JsonProperty("port_id")
    private String portId;

    @JsonProperty("vpc_id")
    private String vpcId;

    @JsonProperty("ips_port")
    private List<ZetaPortIP> portIps;

    @JsonProperty("mac_port")
    private String portMac;

    @JsonProperty("node_ip")
    private String nodeIp;

    @JsonProperty("node_mac")
    private String nodeMac;

    public ZetaPortEntity() {

    }

    public ZetaPortEntity(String portId, String vpcId, List<ZetaPortIP> portIps, String portMac, String nodeIp, String nodeMac) {
        this.portId = portId;
        this.vpcId = vpcId;
        this.portIps = portIps;
        this.portMac = portMac;
        this.nodeIp = nodeIp;
        this.nodeMac = nodeMac;
    }

    public String getPortId() {
        return this.portId;
    }
    public void setPortId(String portId) {
        this.portId = portId;
    }

    public String getVpcId() { return this.vpcId; }
    public void setVpcId(String vpcId) { this.vpcId = vpcId; }

    public List<ZetaPortIP> getPortIps() { return this.portIps; }
    public void setPortIps(List<ZetaPortIP> portIps) { this.portIps = portIps; }

    public String getPortMac() { return this.portMac; }
    public void setPortMac(String portMac) { this.portMac = portMac; }

    public String getNodeIp() { return this.nodeIp; }
    public void setNodeIp(String nodeIp) { this.nodeIp = nodeIp; }

    public String getNodeMac() { return this.nodeMac; }
    public void setNodeMac(String nodeMac) { this.nodeMac = nodeMac; }
}
