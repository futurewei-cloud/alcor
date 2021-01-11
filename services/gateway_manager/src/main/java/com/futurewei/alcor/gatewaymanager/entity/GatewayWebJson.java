package com.futurewei.alcor.gatewaymanager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.web.entity.gateway.GatewayEntity;
import lombok.Data;

import java.util.List;

@Data
public class GatewayWebJson {

    @JsonProperty("gateways")
    private List<GatewayEntity> gatewayEntitys;

    public GatewayWebJson(List<GatewayEntity> gatewayEntitys) {
        this.gatewayEntitys = gatewayEntitys;
    }
}
