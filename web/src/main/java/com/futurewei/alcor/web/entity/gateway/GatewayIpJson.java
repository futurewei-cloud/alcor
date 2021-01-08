package com.futurewei.alcor.web.entity.gateway;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class GatewayIpJson {
    @JsonProperty("gws")
    private List<GatewayIp> gatewayIps;
}
