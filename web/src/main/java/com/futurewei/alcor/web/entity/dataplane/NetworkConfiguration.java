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
