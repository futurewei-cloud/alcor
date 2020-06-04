package com.futurewei.alcor.web.entity.dataplane;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.web.entity.port.PortEntity;

import java.util.List;

public class InternalPortEntityNB extends PortEntity {

    @JsonProperty("neighbor_info")
    private List<HostInfoNB> neighborInfo;

    public List<HostInfoNB> getNeighborInfo() {
        return neighborInfo;
    }

    public void setNeighborInfo(List<HostInfoNB> neighborInfo) {
        this.neighborInfo = neighborInfo;
    }
}
