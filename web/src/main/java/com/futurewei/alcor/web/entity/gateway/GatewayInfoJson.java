package com.futurewei.alcor.web.entity.gateway;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class GatewayInfoJson implements Serializable {

    @JsonProperty("gatewayinfo")
    private GatewayInfo gatewayInfo;

    public GatewayInfoJson() {
    }

    public GatewayInfoJson(GatewayInfo gatewayInfo) {
        this.gatewayInfo = gatewayInfo;
    }
}
