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

package com.futurewei.alcor.macmanager.entity;

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

    @JsonProperty("active")
    private String active;

    public MacState() {

    }

    public MacState(MacState state) {
        this(state.macAddress, state.projectId, state.vpcId, state.portId, state.active);
    }

    public MacState(String macAddress, String projectId, String vpcId, String portId, String active) {
        this.macAddress = macAddress;
        this.projectId = projectId;
        this.vpcId = vpcId;
        this.portId = portId;
        this.active = active;
    }
}

