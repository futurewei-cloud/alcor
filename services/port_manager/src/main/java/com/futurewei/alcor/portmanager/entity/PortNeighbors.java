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
package com.futurewei.alcor.portmanager.entity;

import com.futurewei.alcor.web.entity.dataplane.NeighborInfo;

import java.util.Map;

public class PortNeighbors {
    private String vpcId;
    private Map<String, NeighborInfo> neighbors;

    public PortNeighbors() {

    }

    public PortNeighbors(String vpcId, Map<String, NeighborInfo> neighbors) {
        this.vpcId = vpcId;
        this.neighbors = neighbors;
    }

    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    public Map<String, NeighborInfo> getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(Map<String, NeighborInfo> neighbors) {
        this.neighbors = neighbors;
    }
}
