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
    private String routeTableType;

    // store subnet_id / vpc_id
    @JsonProperty("owner")
    private String owner;

    public RouteTable () {}

    public RouteTable(String projectId, String id, String name, String description, List<RouteEntry> routeEntities, String routeTableType, String owner) {
        super(projectId, id, name, description);
        this.routeEntities = routeEntities;
        this.routeTableType = routeTableType;
        this.owner = owner;
    }

    public RouteTable(RouteTable routeTable) {
        super(routeTable.getProjectId(), routeTable.getId(), routeTable.getName(), routeTable.getDescription());
        this.routeEntities = routeTable.getRouteEntities();
        this.routeTableType = routeTable.getRouteTableType();
        this.owner = routeTable.getOwner();
    }
}
