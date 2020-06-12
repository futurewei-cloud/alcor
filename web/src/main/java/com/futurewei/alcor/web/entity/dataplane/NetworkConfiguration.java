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
import com.futurewei.alcor.schema.Common;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupEntity;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import lombok.Data;

import java.util.List;

@Data
public class NetworkConfiguration {

  private Common.ResourceType rsType;
  private Common.OperationType opType;
  boolean allOrNone = true;

  @JsonProperty("ports")
  private List<InternalPortEntity> portStates;

  @JsonProperty("vpcs")
  private List<VpcEntity> vpcs;

  @JsonProperty("subnets")
  private List<InternalSubnetEntity> subnets;

  @JsonProperty("security_groups")
  private List<SecurityGroupEntity> securityGroupEntities;

  public Common.ResourceType getRsType() {
    return rsType;
  }

  public void setRsType(Common.ResourceType rsType) {
    this.rsType = rsType;
  }

  public Common.OperationType getOpType() {
    return opType;
  }

  public void setOpType(Common.OperationType opType) {
    this.opType = opType;
  }

  public boolean isAllOrNone() {
    return allOrNone;
  }

  public void setAllOrNone(boolean allOrNone) {
    this.allOrNone = allOrNone;
  }

  public List<InternalPortEntity> getPortStates() {
    return portStates;
  }

  public void setPortStates(List<InternalPortEntity> portStates) {
    this.portStates = portStates;
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

  public List<SecurityGroupEntity> getSecurityGroupEntities() {
    return securityGroupEntities;
  }

  public void setSecurityGroupEntities(List<SecurityGroupEntity> securityGroupEntities) {
    this.securityGroupEntities = securityGroupEntities;
  }

  public NetworkConfiguration(Common.ResourceType rsType,
                              Common.OperationType opType, boolean allOrNone,
                              List<InternalPortEntity> portStates,
                              List<VpcEntity> vpcs,
                              List<InternalSubnetEntity> subnets,
                              List<SecurityGroupEntity> securityGroupEntities) {
    this.rsType = rsType;
    this.opType = opType;
    this.allOrNone = allOrNone;
    this.portStates = portStates;
    this.vpcs = vpcs;
    this.subnets = subnets;
    this.securityGroupEntities = securityGroupEntities;
  }

  public NetworkConfiguration(List<InternalPortEntity> portStates,
                              List<VpcEntity> vpcs,
                              List<InternalSubnetEntity> subnets,
                              List<SecurityGroupEntity> securityGroupEntities) {
    this.portStates = portStates;
    this.vpcs = vpcs;
    this.subnets = subnets;
    this.securityGroupEntities = securityGroupEntities;
  }
}
