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

import com.futurewei.alcor.common.entity.CustomerResource;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class RouteEntry extends CustomerResource {

    @NotNull
    private String destination;

    @NotNull
    private String target;

    @NotNull
    private Integer priority;

    @NotNull
    private String routeTableId;

    @NotNull
    private String nexthop;

    public RouteEntry() {

    }

    public RouteEntry(String projectId, String Id, String name, String description,
                       String destination, String target, Integer priority, String routeTableId, String nexthop) {
        super(projectId, Id, name, description);
        this.destination = destination;
        this.target = target;
        this.priority = priority;
        this.routeTableId = routeTableId;
        this.nexthop = nexthop;
    }
}
