package com.futurewei.alcor.web.entity.gateway;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class VpcInfoSub {

    @JsonProperty("vpc_id")
    private String vpcId;

    @JsonProperty("vni")
    private String vpcVni;

    public VpcInfoSub(String vpcId, String vpcVni) {
        this.vpcId = vpcId;
        this.vpcVni = vpcVni;
    }
}
