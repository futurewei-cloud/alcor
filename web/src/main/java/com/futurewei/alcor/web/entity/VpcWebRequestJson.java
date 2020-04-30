package com.futurewei.alcor.web.entity;

import lombok.Data;

import java.util.UUID;

@Data
public class VpcWebRequestJson {

    private VpcWebRequestObject network;

    public VpcWebRequestJson() {

    }

    public VpcWebRequestJson(VpcWebRequestObject vpcObject) {
        this.network = vpcObject;
    }

    public VpcWebRequestJson(VpcWebRequestObject vpcObject, UUID genId) {
        this.network = vpcObject;
        this.network.setId(String.valueOf(genId));
    }

}
