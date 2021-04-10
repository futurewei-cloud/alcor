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
package com.futurewei.alcor.web.entity.gateway;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ZetaPortEntity implements Serializable {
    @JsonProperty("port_id")
    private String portId;

    @JsonProperty("vpc_id")
    private String vpcId;

    @JsonProperty("ips_port")
    private List<ZetaPortIP> portIps;

    @JsonProperty("mac_port")
    private String portMac;

    @JsonProperty("ip_node")
    private String nodeIp;

    @JsonProperty("mac_node")
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
