package com.futurewei.alcor.web.entity.gateway;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class VpcInfo {

    @JsonProperty("vpc_id")
    private String vpcId;

    @JsonProperty("vni")
    private String vpcVni;

    @JsonProperty("owner")
    private String owner;

    public VpcInfo() {
    }

    public VpcInfo(String vpcId, String vpcVni, String owner) {
        this.vpcId = vpcId;
        this.vpcVni = vpcVni;
        this.owner = owner;
    }
}
