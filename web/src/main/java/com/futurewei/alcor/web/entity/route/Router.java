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
import lombok.Data;
import org.apache.ignite.cache.query.annotations.QuerySqlField;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.List;

@Data
public class Router extends CustomerResource {

    @JsonProperty("neutron_router_routetable")
    private RouteTable neutronRouteTable;

    @JsonProperty("neutron_subnet_routetables")
    private List<RouteTable> neutronSubnetRouteTables;

    @JsonProperty("subnet_Ids")
    private List<String> subnetIds;

    @JsonProperty("routetables")
    private List<RouteTable> vpcRouteTables;

    // store vpc_default_route_table_id
    @JsonProperty("vpc_default_route_table_id")
    private String vpcDefaultRouteTableId;

    // store vpc_id
    @JsonProperty("owner")
    @QuerySqlField(index = true)
    private String owner;

    // store vpc_id
    @JsonProperty("router_extra_attribute_id")
    private String routerExtraAttributeId;

    // store subnet_gateway_port_id
    @JsonProperty("gateway_ports")
    private List<String> gatewayPorts;

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

    public Router() {
    }

    public Router(String projectId, String Id, String name, String description, RouteTable neutronRouteTable) {
        super(projectId, Id, name, description);
        this.neutronRouteTable = neutronRouteTable;
    }

    public Router(String projectId, String Id, String name, String description, List<RouteTable> vpcRouteTables) {
        super(projectId, Id, name, description);
        this.vpcRouteTables = vpcRouteTables;
    }

    public Router(String projectId, String id, String name, String description,
                  RouteTable neutronRouteTable, List<RouteTable> vpcRouteTables, String owner, List<String> gatewayPorts,
                  String tenantId, boolean adminStateUp, String status, String routerExtraAttributeId,
                  String vpcDefaultRouteTableId) {
        super(projectId, id, name, description);
        this.neutronRouteTable = neutronRouteTable;
        this.vpcRouteTables = vpcRouteTables;
        this.owner = owner;
        this.gatewayPorts = gatewayPorts;
        this.tenantId = tenantId;
        this.adminStateUp = adminStateUp;
        this.status = status;
        this.routerExtraAttributeId = routerExtraAttributeId;
        this.vpcDefaultRouteTableId = vpcDefaultRouteTableId;
    }

    public Router(Router r) {
        this(r.getProjectId(), r.getId(), r.getName(), r.getDescription(),
                r.getNeutronRouteTable(), r.getVpcRouteTables(), r.getOwner(), r.getGatewayPorts(),
                r.getTenantId(), true, r.getStatus(), r.getRouterExtraAttributeId(),
                r.getVpcDefaultRouteTableId());
    }
}