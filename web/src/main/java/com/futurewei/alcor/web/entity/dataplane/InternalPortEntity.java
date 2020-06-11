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
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class InternalPortEntity extends PortEntity {

    @JsonProperty("routes")
    private List<RouteEntity> routes;

    @JsonProperty("neighbor_info")
    private List<NeighborInfo> neighborInfos;

    @JsonProperty("binding_host_ip")
    private String bindingHostIP;

    public InternalPortEntity(PortEntity portEntity, List<RouteEntity> routeEntities, List<NeighborInfo> neighborInfos, String bindingHostIP) {
        super(portEntity);
        this.routes = new ArrayList<>(routeEntities);
        this.neighborInfos = new ArrayList<>(neighborInfos);
        this.bindingHostIP = bindingHostIP;
    }
}
