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
import lombok.Data;

import java.util.List;

@Data
public class RouteTable extends CustomerResource {

    @JsonProperty("route_entries")
    private List<RouteEntry> routeEntities;

    @JsonProperty("route_table_type")
    private RouteTableType routeTableType;

    public RouteTable () {}

    public RouteTable(String projectId, String Id, String name, String description, List<RouteEntry> routeEntities, RouteTableType routeTableType) {
        super(projectId, Id, name, description);
        this.routeEntities = routeEntities;
        this.routeTableType = routeTableType;
    }

}
