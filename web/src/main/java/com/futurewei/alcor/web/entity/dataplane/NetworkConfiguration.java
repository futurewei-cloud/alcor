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

@Data
public class NetworkConfiguration {

  private Common.ResourceType rsType;
  private Common.OperationType opType;
  boolean allOrNone = true;

  @JsonProperty("ports")
  private InternalPortEntityNB[] portStates;

  @JsonProperty("vpcs")
  private VpcEntity[] vpcs;

  @JsonProperty("subnets")
  private SubnetEntityNB[] subnets;

  @JsonProperty("security_groups")
  private SecurityGroupEntity[] securityGroupEntities;
}
