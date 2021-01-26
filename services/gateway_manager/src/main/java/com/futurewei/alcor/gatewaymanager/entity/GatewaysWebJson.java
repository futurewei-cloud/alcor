package com.futurewei.alcor.gatewaymanager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.web.entity.gateway.GatewayEntity;
import lombok.Data;

import java.util.List;

@Data
public class GatewaysWebJson {

    @JsonProperty("gateways")
    private List<GatewayEntity> gatewayEntitys;

    public GatewaysWebJson(List<GatewayEntity> gatewayEntitys) {
        this.gatewayEntitys = gatewayEntitys;
    }
}
