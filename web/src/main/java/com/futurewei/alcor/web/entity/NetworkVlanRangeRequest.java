package com.futurewei.alcor.web.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class NetworkVlanRangeRequest {

    @JsonProperty("id")
    private String id;

    @JsonProperty("segment_id")
    private String segmentId;

    @JsonProperty("network_type")
    private String networkType;

    @JsonProperty("first_key")
    private int firstKey;

    @JsonProperty("last_key")
    private int lastKey;

    @JsonProperty("used_keys")
    private int usedKeys;

    @JsonProperty("total_keys")
    private int totalKeys;

    public NetworkVlanRangeRequest () {}

    public NetworkVlanRangeRequest(String id, String segmentId, String networkType, int firstKey, int lastKey) {
        this.id = id;
        this.segmentId = segmentId;
        this.networkType = networkType;
        this.firstKey = firstKey;
        this.lastKey = lastKey;
    }
}
