package com.futurewei.alcor.web.entity.gateway;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class VpcInfoSub {

    @JsonProperty("vpc_id")
    private String vpcId;

    @JsonProperty("vpc_vni")
    private Integer vpcVni;

    public VpcInfoSub(String vpcId, Integer vpcVni) {
        this.vpcId = vpcId;
        this.vpcVni = vpcVni;
    }
}
