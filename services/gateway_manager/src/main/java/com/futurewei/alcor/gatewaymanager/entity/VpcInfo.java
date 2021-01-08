package com.futurewei.alcor.gatewaymanager.entity;

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
}
