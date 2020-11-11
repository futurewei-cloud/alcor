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
package com.futurewei.alcor.web.entity.port;

public class PortHostInfo {
    private String portId;
    private String portIp;
    private String portMac;
    private String hostId;
    private String hostIp;

    public PortHostInfo() {

    }

    public PortHostInfo(String portId, String portIp, String portMac, String hostId, String hostIp) {
        this.portId = portId;
        this.portIp = portIp;
        this.portMac = portMac;
        this.hostId = hostId;
        this.hostIp = hostIp;
    }

    public String getPortId() {
        return portId;
    }

    public void setPortId(String portId) {
        this.portId = portId;
    }

    public String getPortIp() {
        return portIp;
    }

    public void setPortIp(String portIp) {
        this.portIp = portIp;
    }

    public String getPortMac() {
        return portMac;
    }

    public void setPortMac(String portMac) {
        this.portMac = portMac;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }
}
