package com.futurewei.alcor.web.entity.dataplane;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class NeighborInfo {

  @JsonProperty("host_ip")
  private String hostIp;

  @JsonProperty("host_id")
  private String hostId;

  @JsonProperty("port_id")
  private String portId;
}
