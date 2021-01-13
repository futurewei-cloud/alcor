package com.futurewei.alcor.web.entity.gateway;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class GatewayIpJson {

    @JsonProperty("vpc_id")
    private String vpcId;

    @JsonProperty("vni")
    private Integer vni;

    @JsonProperty("zgc_id")
    private String zgcId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("port_ibo")
    private String portIbo;

    @JsonProperty("gws")
    private List<GatewayIp> gatewayIps;
}
