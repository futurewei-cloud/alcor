package com.futurewei.alcor.web.entity.gateway;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ZetaGatewayIpJson {

    @JsonProperty("vpc_id")
    private String vpcId;

    @JsonProperty("vni")
    private String vni;

    @JsonProperty("zgc_id")
    private String zgcId;

    @JsonProperty("id")
    private String id;

    @JsonProperty("port_ibo")
    private String portIbo;

    @JsonProperty("gws")
    private List<GatewayIp> gatewayIps;

    @JsonProperty("ports")
    private List<String> ports;

    public ZetaGatewayIpJson() { }

    public ZetaGatewayIpJson(List<GatewayIp> gws, String id, String portIbo, List<String> ports,
                             String vni, String vpcId, String zgcId) {
        this.gatewayIps = gws;
        this.id = id;
        this.portIbo = portIbo;
        this.ports = ports;
        this.vni = vni;
        this.vpcId = vpcId;
        this.zgcId = zgcId;
    }
}
