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


package com.futurewei.alcor.web.entity.elasticip;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.web.entity.port.PortEntity;


public class ElasticIpInfo extends ElasticIp {

    @JsonProperty("state")
    private String state;

    @JsonProperty("port_details")
    private ElasticIpPortDetails portDetails;

    public ElasticIpInfo() {
    }

    public ElasticIpInfo(ElasticIp eip) {
        super(eip);
        if (eip.getPortId() != null) {
            this.state = ElasticIpState.ACTIVATED.getState();
        } else {
            this.state = ElasticIpState.DEACTIVATED.getState();
        }
    }

    public ElasticIpInfo(ElasticIp eip, PortEntity port) {
        this(eip);
        if (port != null) {
            ElasticIpPortDetails portDetails = new ElasticIpPortDetails();
            portDetails.setAdminStateUp(port.getAdminStateUp());
            portDetails.setDeviceId(port.getDeviceId());
            portDetails.setDeviceOwner(port.getDeviceOwner());
            portDetails.setMacAddress(port.getMacAddress());
            portDetails.setName(port.getName());
            portDetails.setNetworkId(port.getVpcId());
            portDetails.setStatus(port.getStatus());
            this.portDetails = portDetails;
        }
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public ElasticIpPortDetails getPortDetails() {
        return portDetails;
    }

    public void setPortDetails(ElasticIpPortDetails portDetails) {
        this.portDetails = portDetails;
    }

    @Override
    public String toString() {
        return "ElasticIpInfo{" +
                "state='" + state + '\'' +
                "} " + super.toString();
    }
}
