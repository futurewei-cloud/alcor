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
package com.futurewei.alcor.web.entity.dataplane;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

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

    public String getPortIp() {
        return portIp;
    }

    public void setPortIp(String portIp) {
        this.portIp = portIp;
    }

    @Override
    public int hashCode() {
       return 1;
    }

    @Override
    public boolean equals(Object obj) {
        NeighborInfo o=(NeighborInfo)obj;
        return this.hostId.equals(o.hostId)&&this.hostIp.equals(o.hostIp)&&this.portId.equals(o.portId);
    }
}
