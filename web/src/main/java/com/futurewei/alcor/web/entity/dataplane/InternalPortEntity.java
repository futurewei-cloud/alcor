/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
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

    @JsonProperty("binding_host_ip")
    private String bindingHostIP;

    @JsonProperty("is_zeta_gateway_port")
    private Boolean isZetaGatewayPort;

    public List<RouteEntity> getRoutes() {
        return routes;
    }

    public void setRoutes(List<RouteEntity> routes) {
        this.routes = routes;
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
            String bindingHostIP) {
        super(portEntity);
        this.routes = routeEntities;
        this.bindingHostIP = bindingHostIP;
        this.isZetaGatewayPort = false;
    }

    public InternalPortEntity(
            PortEntity portEntity,
            List<RouteEntity> routeEntities,
            String bindingHostIP,
            Boolean isZetaGatewayPort) {
        super(portEntity);
        this.routes = routeEntities;
        this.bindingHostIP = bindingHostIP;
        this.isZetaGatewayPort = isZetaGatewayPort;
    }
}