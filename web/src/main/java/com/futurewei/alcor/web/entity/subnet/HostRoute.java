package com.futurewei.alcor.web.entity.subnet;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class HostRoute {

    @JsonProperty("destination")
    private String destination;

    @JsonProperty("nexthop")
    private String nexthop;

    public HostRoute () {}

    public HostRoute(String destination, String nexthop) {
        this.destination = destination;
        this.nexthop = nexthop;
    }

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
