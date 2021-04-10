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

package com.futurewei.alcor.web.entity.mac;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class MacState implements Serializable {
    @JsonProperty("mac_address")
    private String macAddress;

    @JsonProperty("project_id")
    private String projectId;

    @JsonProperty("vpc_id")
    private String vpcId;

    @JsonProperty("port_id")
    private String portId;

    @JsonProperty("state")
    private String state;

    @JsonIgnore
    private String rangeId;

    public MacState() {

    }

    public MacState(MacState state) {
        this(state.macAddress, state.projectId, state.vpcId, state.portId, state.state);
    }

    public MacState(String macAddress, String projectId, String vpcId, String portId, String state) {
        this.macAddress = macAddress;
        this.projectId = projectId;
        this.vpcId = vpcId;
        this.portId = portId;
        this.state = state;
    }

    public MacState(String macAddress, String projectId, String vpcId, String portId, String state, String rangeId) {
        this.macAddress = macAddress;
        this.projectId = projectId;
        this.vpcId = vpcId;
        this.portId = portId;
        this.state = state;
        this.rangeId = rangeId;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    public String getPortId() {
        return portId;
    }

    public void setPortId(String portId) {
        this.portId = portId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getRangeId() {
        return rangeId;
    }

    public void setRangeId(String rangeId) {
        this.rangeId = rangeId;
    }
}

