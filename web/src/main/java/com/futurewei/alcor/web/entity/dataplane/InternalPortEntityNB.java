package com.futurewei.alcor.web.entity.dataplane;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class InternalPortEntityNB extends PortEntity {

  @JsonProperty("neighbor_info")
  private List<NeighborInfo> neighborIps;

  @JsonProperty("binding_host_ip")
  private String bindingHostIP;

  private Set<SubnetEntityNB> subnetEntities;
  private Set<VpcEntity> vpcEntities;
}
