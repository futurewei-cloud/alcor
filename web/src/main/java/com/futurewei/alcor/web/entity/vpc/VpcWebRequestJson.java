package com.futurewei.alcor.web.entity.vpc;

import lombok.Data;

import java.util.UUID;

@Data
public class VpcWebRequestJson {

    private VpcWebRequest network;

    public VpcWebRequestJson() {

    }

    public VpcWebRequestJson(VpcWebRequest vpcObject) {
        this.network = vpcObject;
    }

    public VpcWebRequestJson(VpcWebRequest vpcObject, UUID genId) {
        this.network = vpcObject;
        this.network.setId(String.valueOf(genId));
    }

}
