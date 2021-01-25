package com.futurewei.alcor.web.entity.gateway;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class GatewayInfo {

    @JsonProperty("resource_id")
    private String resourceId;

    @JsonProperty("gateways")
    private List<GatewayEntity> gatewayEntities;

    @JsonProperty("routetables")
    private List<RoutingTable> routeTables;

    @JsonProperty("status")
    private String status;

    public GatewayInfo() {
    }

    public GatewayInfo(String resourceId, List<GatewayEntity> gatewayEntities, List<RoutingTable> routeTables, String status) {
        this.resourceId = resourceId;
        this.gatewayEntities = gatewayEntities;
        this.routeTables = routeTables;
        this.status = status;
    }
}
