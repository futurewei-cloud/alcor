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
package com.futurewei.alcor.web.entity.subnet;

import com.futurewei.alcor.web.entity.port.PortHostInfo;

import java.util.List;

public class InternalSubnetPorts {
    private String subnetId;
    private String gatewayPortId;
    private String gatewayPortIp;
    private String gatewayPortMac;
    private List<PortHostInfo> ports;

    public InternalSubnetPorts() {

    }

    public InternalSubnetPorts(String subnetId, String gatewayPortId, String gatewayPortIp, String gatewayPortMac, List<PortHostInfo> ports) {
        this.subnetId = subnetId;
        this.gatewayPortId = gatewayPortId;
        this.gatewayPortIp = gatewayPortIp;
        this.gatewayPortMac = gatewayPortMac;
        this.ports = ports;
    }

    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    public String getGatewayPortId() {
        return gatewayPortId;
    }

    public void setGatewayPortId(String gatewayPortId) {
        this.gatewayPortId = gatewayPortId;
    }

    public String getGatewayPortIp() {
        return gatewayPortIp;
    }

    public void setGatewayPortIp(String gatewayPortIp) {
        this.gatewayPortIp = gatewayPortIp;
    }

    public String getGatewayPortMac() {
        return gatewayPortMac;
    }

    public void setGatewayPortMac(String gatewayPortMac) {
        this.gatewayPortMac = gatewayPortMac;
    }

    public List<PortHostInfo> getPorts() {
        return ports;
    }

    public void setPorts(List<PortHostInfo> ports) {
        this.ports = ports;
    }
}
