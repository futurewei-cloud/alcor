package com.futurewei.alcor.web.entity;

import lombok.Data;

@Data
public class KeyAlloc {

    private Long key;
    private String rangeId;
    private String networkType;

    public KeyAlloc() {}

    public KeyAlloc(Long key, String rangeId, String networkType) {
        this.key = key;
        this.rangeId = rangeId;
        this.networkType = networkType;
    }
}
