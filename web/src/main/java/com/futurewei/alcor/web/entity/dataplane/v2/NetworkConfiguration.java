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
package com.futurewei.alcor.web.entity.dataplane.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.schema.Common.OperationType;
import com.futurewei.alcor.schema.Common.ResourceType;
import com.futurewei.alcor.web.entity.dataplane.InternalPortEntity;
import com.futurewei.alcor.web.entity.dataplane.InternalSubnetEntity;
import com.futurewei.alcor.web.entity.dataplane.NeighborEntry;
import com.futurewei.alcor.web.entity.dataplane.NeighborInfo;
import com.futurewei.alcor.web.entity.route.InternalRouterInfo;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroup;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
  private Map<String, NeighborInfo> neighborInfos;

  @JsonProperty("neighbor_table")
  private Map<String, List<NeighborEntry>> neighborTable;

  @JsonProperty("routers_internal")
  private List<InternalRouterInfo> internalRouterInfos;

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

  public Map<String, NeighborInfo> getNeighborInfos() {
    return neighborInfos;
  }

  public void setNeighborInfos(Map<String, NeighborInfo> neighborInfos) {
    this.neighborInfos = neighborInfos;
  }

  public Map<String, List<NeighborEntry>> getNeighborTable() {
    return neighborTable;
  }

  public void setNeighborTable(Map<String, List<NeighborEntry>> neighborTable) {
    this.neighborTable = neighborTable;
  }

  @Override
  public String toString() {
    return "NetworkConfiguration{" + "rsType=" + rsType + ", opType=" + opType + ", portEntities=" + portEntities + ", vpcs=" + vpcs + ", subnets=" + subnets + ", securityGroups=" + securityGroups + ", neighborInfos=" + neighborInfos + ", neighborTable=" + neighborTable + ", routerInfo=" + internalRouterInfos + '}';
  }
}
