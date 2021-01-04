package com.futurewei.alcor.gatewaymanager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GatewayWebJson {

    @JsonProperty("gateways")
    private GatewayEntity gatewayEntity;
}
