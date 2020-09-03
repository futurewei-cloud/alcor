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
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.List;

@Data
public class Router extends CustomerResource {

    @JsonProperty("routetable")
    private RouteTable neutronRouteTable;

    @JsonProperty("routetables")
    private List<RouteTable> vpcRouteTable;

    // store vpc_id
    @JsonProperty("owner")
    private String owner;

    // store vpc_id
    @JsonProperty("router_extra_attribute_id")
    private String routerExtraAttributeId;

    // store subnet_gateway_port_id
    @JsonProperty("ports")
    private List<String> ports;

    @JsonProperty("tenant_id")
    private String tenantId;

    @JsonProperty("admin_state_up")
    private boolean adminStateUp;

    @JsonProperty("status")
    private String status;

    @CreatedDate
    @JsonProperty("created_at")
    private String created_at;

    @LastModifiedDate
    @JsonProperty("updated_at")
    private String updated_at;

    public Router() {}

    public Router(String projectId, String Id, String name, String description, RouteTable neutronRouteTable) {
        super(projectId, Id, name, description);
        this.neutronRouteTable = neutronRouteTable;
    }

    public Router(String projectId, String Id, String name, String description, List<RouteTable> vpcRouteTable) {
        super(projectId, Id, name, description);
        this.vpcRouteTable = vpcRouteTable;
    }

    public Router(String projectId, String id, String name, String description, RouteTable neutronRouteTable, List<RouteTable> vpcRouteTable, String owner, List<String> ports, String tenantId, boolean adminStateUp, String status, String routerExtraAttributeId) {
        super(projectId, id, name, description);
        this.neutronRouteTable = neutronRouteTable;
        this.vpcRouteTable = vpcRouteTable;
        this.owner = owner;
        this.ports = ports;
        this.tenantId = tenantId;
        this.adminStateUp = adminStateUp;
        this.status = status;
        this.routerExtraAttributeId = routerExtraAttributeId;
    }
}
