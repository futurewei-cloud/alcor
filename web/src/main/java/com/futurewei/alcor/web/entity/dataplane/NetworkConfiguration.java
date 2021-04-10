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
package com.futurewei.alcor.web.entity.dataplane;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.schema.Common.OperationType;
import com.futurewei.alcor.schema.Common.ResourceType;
import com.futurewei.alcor.web.entity.route.InternalRouterInfo;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroup;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class NetworkConfiguration {

    private ResourceType rsType;
    private OperationType opType;

    @JsonProperty("ports_internal")
    private List<InternalPortEntity> portEntities;

    @JsonProperty("vpcs_internal")
    private List<VpcEntity> vpcs;

    @JsonProperty("subnets_internal")
    private List<InternalSubnetEntity> subnets;

    @JsonProperty("security_groups_internal")
    private List<SecurityGroup> securityGroups;

    @JsonProperty("neighbor_info")
    private List<NeighborInfo> neighborInfos;

    @JsonProperty("neighbor_table")
    private List<NeighborEntry> neighborTable;

    @JsonProperty("routers_internal")
    private List<InternalRouterInfo> internalRouterInfos;

    @Override
    public String toString() {
        return "NetworkConfiguration{" + "rsType=" + rsType + ", opType=" + opType + ", portEntities=" + portEntities + ", vpcs=" + vpcs + ", subnets=" + subnets + ", securityGroups=" + securityGroups + ", neighborInfos=" + neighborInfos + ", neighborTable=" + neighborTable + ", routerTable=" + internalRouterInfos + '}';
    }

    public List<InternalRouterInfo> getInternalRouterInfos() {
        return internalRouterInfos;
    }

    public void setInternalRouterInfos(List<InternalRouterInfo> internalRouterInfos) {
        this.internalRouterInfos = internalRouterInfos;
    }

    public void addPortEntity(InternalPortEntity portEntity) {
        if (this.portEntities == null) {
            this.portEntities = new ArrayList<>();
        }

        this.portEntities.add(portEntity);
    }

    public void addVpcEntity(VpcEntity vpcEntity) {
        if (this.vpcs == null) {
            this.vpcs = new ArrayList<>();
        }

        this.vpcs.add(vpcEntity);
    }

    public void addSubnetEntity(InternalSubnetEntity subnetEntity) {
        if (this.subnets == null) {
            this.subnets = new ArrayList<>();
        }

        this.subnets.add(subnetEntity);
    }

    public void addSecurityGroupEntity(SecurityGroup securityGroup) {
        if (this.securityGroups == null) {
            this.securityGroups = new ArrayList<>();
        }

        this.securityGroups.add(securityGroup);
    }

    public ResourceType getRsType() {
        return rsType;
    }

    public void setRsType(ResourceType rsType) {
        this.rsType = rsType;
    }

    public OperationType getOpType() {
        return opType;
    }

    public void setOpType(OperationType opType) {
        this.opType = opType;
    }

    public List<InternalPortEntity> getPortEntities() {
        return portEntities;
    }

    public void setPortEntities(List<InternalPortEntity> portEntities) {
        this.portEntities = portEntities;
    }

    public List<VpcEntity> getVpcs() {
        return vpcs;
    }

    public void setVpcs(List<VpcEntity> vpcs) {
        this.vpcs = vpcs;
    }

    public List<InternalSubnetEntity> getSubnets() {
        return subnets;
    }

    public void setSubnets(List<InternalSubnetEntity> subnets) {
        this.subnets = subnets;
    }

    public List<SecurityGroup> getSecurityGroups() {
        return securityGroups;
    }

    public void setSecurityGroups(List<SecurityGroup> securityGroups) {
        this.securityGroups = securityGroups;
    }

    public List<NeighborInfo> getNeighborInfos() {
        return neighborInfos;
    }

    public void setNeighborInfos(List<NeighborInfo> neighborInfos) {
        this.neighborInfos = neighborInfos;
    }

    public List<NeighborEntry> getNeighborTable() {
        return neighborTable;
    }

    public void setNeighborTable(List<NeighborEntry> neighborTable) {
        this.neighborTable = neighborTable;
    }
}
