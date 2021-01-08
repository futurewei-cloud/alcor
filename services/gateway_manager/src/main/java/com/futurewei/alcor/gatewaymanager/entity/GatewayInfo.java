package com.futurewei.alcor.gatewaymanager.entity;

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
}
