package com.futurewei.alcor.web.entity.gateway;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class VpcInfoJson implements Serializable {
    @JsonProperty("vpcinfo")
    private VpcInfo vpcInfo;

    public VpcInfoJson() {
    }

    public VpcInfoJson(VpcInfo vpcInfo) {
        this.vpcInfo = vpcInfo;
    }
}
