package com.futurewei.alcor.gatewaymanager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class VpcInfoSub {

    @JsonProperty("vpc_id")
    private String vpcId;

    @JsonProperty("vni")
    private Integer vpcVni;

    public VpcInfoSub(String vpcId, Integer vpcVni) {
        this.vpcId = vpcId;
        this.vpcVni = vpcVni;
    }
}
