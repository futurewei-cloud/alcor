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
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroup;
import com.futurewei.alcor.schema.Common.ResourceType;
import com.futurewei.alcor.schema.Common.OperationType;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class NetworkConfiguration {

  private ResourceType rsType;
  private OperationType opType;
  boolean allOrNone = true;

  @JsonProperty("ports_internal")
  private List<InternalPortEntity> portEntities;

  @JsonProperty("vpcs_internal")
  private List<VpcEntity> vpcEntities;

  @JsonProperty("subnets_internal")
  private List<InternalSubnetEntity> subnetEntities;

  @JsonProperty("security_groups_internal")
  private List<SecurityGroupEntity> securityGroupEntities;

  public void addPortEntity(InternalPortEntity portEntity) {
    if (this.portEntities == null) {
      this.portEntities = new ArrayList<>();
    }

    this.portEntities.add(portEntity);
  }

  public void addVpcEntity(VpcEntity vpcEntity) {
    if (this.vpcEntities == null) {
      this.vpcEntities = new ArrayList<>();
    }

    this.vpcEntities.add(vpcEntity);
  }

  public void addSubnetEntity(InternalSubnetEntity subnetEntity) {
    if (this.subnetEntities == null) {
      this.subnetEntities = new ArrayList<>();
    }

    this.subnetEntities.add(subnetEntity);
  }

  public void addSecurityGroupEntity(SecurityGroupEntity securityGroupEntity) {
    if (this.securityGroupEntities == null) {
      this.securityGroupEntities = new ArrayList<>();
    }
  }

}
