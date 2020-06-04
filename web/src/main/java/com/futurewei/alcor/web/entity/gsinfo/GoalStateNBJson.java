package com.futurewei.alcor.web.entity.gsinfo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.common.constants.Common;
import com.futurewei.alcor.common.entity.CustomerResource;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupEntity;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;

public class GoalStateNBJson extends CustomerResource {

  private Common.ResourceType rsType;
  private Common.OperationType opType;
  boolean allOrNone = true;

  @JsonProperty("ports")
  private PortEntity[] portStates;

  @JsonProperty("vpcs")
  private VpcEntity[] vpcs;

  @JsonProperty("subnets")
  private SubnetEntity[] subnets;

  @JsonProperty("host_infos")
  private HostInfoNB[] hostInfoNBs;

  @JsonProperty("security_groups")
  private SecurityGroupEntity[] securityGroupEntities;

  public HostInfoNB[] getHostInfoNBs() {
    return hostInfoNBs;
  }

  public void setHostInfoNBs(HostInfoNB[] hostInfoNBs) {
    this.hostInfoNBs = hostInfoNBs;
  }

  public SecurityGroupEntity[] getSecurityGroupEntities() {
    return securityGroupEntities;
  }

  public void setSecurityGroupEntities(SecurityGroupEntity[] securityGroupEntities) {
    this.securityGroupEntities = securityGroupEntities;
  }

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

  public VpcEntity[] getVpcs() {
    return vpcs;
  }

  public void setVpcs(VpcEntity[] vpcs) {
    this.vpcs = vpcs;
  }

  public SubnetEntity[] getSubnets() {
    return subnets;
  }

  public void setSubnets(SubnetEntity[] subnets) {
    this.subnets = subnets;
  }

  public PortEntity[] getPortStates() {
    return portStates;
  }

  public void setPortStates(PortEntity[] portState) {
    this.portStates = portState;
  }
}
