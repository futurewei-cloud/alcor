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
