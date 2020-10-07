package com.futurewei.alcor.vpcmanager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class VlanKeyRequest {

    @JsonProperty("key")
    private Long key;

    @JsonProperty("range_id")
    private String rangeId;

    @JsonProperty("network_type")
    private String networkType;

    public VlanKeyRequest () {}

    public VlanKeyRequest(Long key, String rangeId, String networkType) {
        this.key = key;
        this.rangeId = rangeId;
        this.networkType = networkType;
    }

}
