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
package com.futurewei.alcor.web.entity.dataplane;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.route.RouteEntity;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class InternalPortEntity extends PortEntity {

    @JsonProperty("routes")
    private List<RouteEntity> routes;

    @JsonProperty("l2_neighbor_ids")
    private List<String> l2NeighborIds;

    @JsonProperty("l3_neighbor_ids")
    private List<String> l3NeighborIds;

    @JsonProperty("binding_host_ip")
    private String bindingHostIP;

    public List<RouteEntity> getRoutes() {
        return routes;
    }

    public void setRoutes(List<RouteEntity> routes) {
        this.routes = routes;
    }

    public List<String> getL2NeighborIds() {
        return l2NeighborIds;
    }

    public void setL2NeighborIds(List<String> l2NeighborIds) {
        this.l2NeighborIds = l2NeighborIds;
    }

    public List<String> getL3NeighborIds() {
        return l3NeighborIds;
    }

    public void setL3NeighborIds(List<String> l3NeighborIds) {
        this.l3NeighborIds = l3NeighborIds;
    }

    public String getBindingHostIP() {
        return bindingHostIP;
    }

    public void setBindingHostIP(String bindingHostIP) {
        this.bindingHostIP = bindingHostIP;
    }

    public Set<NeighborInfo> getInternalNeighborInfo1() {
        return internalNeighborInfo1;
    }

    public void setInternalNeighborInfo1(Set<NeighborInfo> internalNeighborInfo1) {
        this.internalNeighborInfo1 = internalNeighborInfo1;
    }

    public Set<InternalSubnetEntity> getSubnetEntities() {
        return subnetEntities;
    }

    public void setSubnetEntities(Set<InternalSubnetEntity> subnetEntities) {
        this.subnetEntities = subnetEntities;
    }

    public Set<VpcEntity> getVpcEntities() {
        return vpcEntities;
    }

    public void setVpcEntities(Set<VpcEntity> vpcEntities) {
        this.vpcEntities = vpcEntities;
    }

    private Set<NeighborInfo> internalNeighborInfo1;
    private Set<InternalSubnetEntity> subnetEntities;
    private Set<VpcEntity> vpcEntities;

    public InternalPortEntity() {}

    public InternalPortEntity(
            PortEntity portEntity,
            List<RouteEntity> routeEntities,
            List<String> l2NeighborIds,
            List<String> l3NeighborIds,
            String bindingHostIP) {
        super(portEntity);
        this.routes = routeEntities;
        this.l2NeighborIds = l2NeighborIds;
        this.l3NeighborIds = l3NeighborIds;
        this.bindingHostIP = bindingHostIP;
    }
}