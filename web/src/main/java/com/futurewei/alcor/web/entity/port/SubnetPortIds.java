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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

public class SubnetPortIds {
    @JsonProperty("subnet_id")
    private String subnetId;

    @JsonProperty("port_ids")
    private Set<String> portIds;

    public SubnetPortIds() {

    }

    public SubnetPortIds(String subnetId, Set<String> portIds) {
        this.subnetId = subnetId;
        this.portIds = portIds;
    }

    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    public Set<String> getPortIds() {
        return portIds;
    }

    public void setPortIds(Set<String> portIds) {
        this.portIds = portIds;
    }
}
