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

