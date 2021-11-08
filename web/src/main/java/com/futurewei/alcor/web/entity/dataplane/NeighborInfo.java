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
package com.futurewei.alcor.web.entity.dataplane;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.ignite.cache.query.annotations.QuerySqlField;

@Data
public class NeighborInfo {
    @JsonProperty("host_ip")
    private String hostIp;

    @JsonProperty("host_id")
    private String hostId;

    @JsonProperty("port_id")
    private String portId;

    @JsonProperty("port_mac")
    private String portMac;

    @JsonProperty("port_ip")
    private String portIp;

    @JsonProperty("vpc_id")
    @QuerySqlField(index = true)
    private String vpcId;

    @JsonProperty("subnet_id")
    private String subnetId;

    public NeighborInfo() {
    }

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getPortId() {
        return portId;
    }

    public void setPortId(String portId) {
        this.portId = portId;
    }

    public String getPortMac() {
        return portMac;
    }

    public void setPortMac(String portMac) {
        this.portMac = portMac;
    }

    public NeighborInfo(String hostIp, String hostId, String portId, String portMac) {
        this.hostIp = hostIp;
        this.hostId = hostId;
        this.portId = portId;
        this.portMac = portMac;
    }

    public NeighborInfo(String hostIp, String hostId, String portId,
                        String portMac, String portIp) {
        this.hostIp = hostIp;
        this.hostId = hostId;
        this.portId = portId;
        this.portMac = portMac;
        this.portIp = portIp;
    }

    public NeighborInfo(String hostIp, String hostId, String portId, String portMac, String portIp, String vpcId, String subnetId) {
        this.hostIp = hostIp;
        this.hostId = hostId;
        this.portId = portId;
        this.portMac = portMac;
        this.portIp = portIp;
        this.vpcId = vpcId;
        this.subnetId = subnetId;
    }

    public String getPortIp() {
        return portIp;
    }

    public void setPortIp(String portIp) {
        this.portIp = portIp;
    }

    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    @Override
    public int hashCode() {
        return (getHostId() + getHostIp() + getPortId() + getPortIp() + getPortMac()).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NeighborInfo))
           return false;
        NeighborInfo o=(NeighborInfo)obj;
        return this.hostId.equals(o.hostId)
                &&this.hostIp.equals(o.hostIp)
                &&this.portId.equals(o.portId)
                &&this.portIp.equals(o.portIp)
                &&this.portMac.equals(o.portMac);
    }
}