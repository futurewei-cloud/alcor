package com.futurewei.alcor.web.entity.dataplane;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.route.RouteEntity;

import java.util.List;

public class InternalPortEntityNB extends PortEntity {

    @JsonProperty("neighbor_info")
    private List<HostInfoNB> neighborInfo;

    @JsonProperty("routes")
    private List<RouteEntity> routes;

    public List<HostInfoNB> getNeighborInfo() {
        return neighborInfo;
    }

    public void setNeighborInfo(List<HostInfoNB> neighborInfo) {
        this.neighborInfo = neighborInfo;
    }
}
