package com.futurewei.alcor.web.entity.gateway;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class VpcInfo {

    @JsonProperty("vpc_id")
    private String vpcId;

    @JsonProperty("vpc_vni")
    private Integer vpcVni;

    @JsonProperty("owner")
    private String owner;

    public VpcInfo() {
    }

    public VpcInfo(String vpcId, Integer vpcVni, String owner) {
        this.vpcId = vpcId;
        this.vpcVni = vpcVni;
        this.owner = owner;
    }
}
