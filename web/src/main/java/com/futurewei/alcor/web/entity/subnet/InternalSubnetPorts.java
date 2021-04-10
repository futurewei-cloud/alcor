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
