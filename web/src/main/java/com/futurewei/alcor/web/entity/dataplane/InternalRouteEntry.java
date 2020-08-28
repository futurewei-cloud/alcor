package com.futurewei.alcor.web.entity.dataplane;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.web.entity.subnet.HostRoute;
import lombok.Data;

@Data
public class InternalRouteEntry extends HostRoute {

  @JsonProperty("destination")
  private String destination;

  @JsonProperty("nexthop")
  private String nexthop;

  @JsonProperty("priority")
  private Integer priority;

  public InternalRouteEntry() {}

  public String getDestination() {
    return destination;
  }

  public void setDestination(String destination) {
    this.destination = destination;
  }

  public String getNexthop() {
    return nexthop;
  }

  public void setNexthop(String nexthop) {
    this.nexthop = nexthop;
  }
}
