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
package com.futurewei.alcor.dataplane.entity;

import com.futurewei.alcor.web.entity.port.PortEntity;

import java.util.List;

/**
 * Ports created info per Host, not include the ports exist in host already, host and port mapping
 */
public class UTPortWithSubnetAndIPMapping {

    private String portId;

    private String portName;

    private String portMacAddress;

    private String vethName;

    private String bindingHostId;

    private List<PortEntity.FixedIp> fixedIps;

    public UTPortWithSubnetAndIPMapping() {
    }

    public UTPortWithSubnetAndIPMapping(String portId, List<PortEntity.FixedIp> fixedIps) {
        this.portId = portId;
        this.fixedIps = fixedIps;
    }

    public String getPortId() {
        return portId;
    }

    public void setPortId(String portId) {
        this.portId = portId;
    }

    public List<PortEntity.FixedIp> getFixedIps() {
        return fixedIps;
    }

    public void setFixedIps(List<PortEntity.FixedIp> fixedIps) {
        this.fixedIps = fixedIps;
    }

    public String getPortName() {
        return portName;
    }

    public void setPortName(String portName) {
        this.portName = portName;
    }

    public String getPortMacAddress() {
        return portMacAddress;
    }

    public void setPortMacAddress(String portMacAddress) {
        this.portMacAddress = portMacAddress;
    }

    public String getVethName() {
        return vethName;
    }

    public void setVethName(String vethName) {
        this.vethName = vethName;
    }

    public String getBindingHostId() {
        return bindingHostId;
    }

    public void setBindingHostId(String bindingHostId) {
        this.bindingHostId = bindingHostId;
    }
}
