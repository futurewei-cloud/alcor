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
package com.futurewei.alcor.web.entity.route;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.common.entity.CustomerResource;
import com.futurewei.alcor.common.enumClass.RouteTableType;
import lombok.Data;

import java.util.List;

@Data
public class RouteTable extends CustomerResource {

    @JsonProperty("routes")
    private List<RouteEntry> routeEntities;

    @JsonProperty("route_table_type")
    private RouteTableType routeTableType;

    // store subnet_id
    @JsonProperty("owner")
    private String owner;

    public RouteTable () {}

    public RouteTable(String projectId, String id, String name, String description, List<RouteEntry> routeEntities, RouteTableType routeTableType, String owner) {
        super(projectId, id, name, description);
        this.routeEntities = routeEntities;
        this.routeTableType = routeTableType;
        this.owner = owner;
    }
}
