package com.futurewei.alcor.web.entity;

import lombok.Data;

@Data
public class VlanKeyAlloc {

    private Long key;
    private String rangeId;
    private String networkType;

    public VlanKeyAlloc () {}

    public VlanKeyAlloc(Long key, String rangeId, String networkType) {
        this.key = key;
        this.rangeId = rangeId;
        this.networkType = networkType;
    }
}
