package com.futurewei.alcor.vpcmanager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class NetworkRangeRequest {

    @JsonProperty("id")
    private String id;

    @JsonProperty("network_type")
    private String networkType;

    @JsonProperty("partition")
    private int partition;

    @JsonProperty("first_key")
    private int firstKey;

    @JsonProperty("last_key")
    private int lastKey;

    @JsonProperty("used_keys")
    private int usedKeys;

    @JsonProperty("total_keys")
    private int totalKeys;

    public NetworkRangeRequest() {}

    public NetworkRangeRequest(String id, String networkType,int partition, int firstKey, int lastKey) {
        this.id = id;
        this.networkType = networkType;
        this.partition = partition;
        this.firstKey = firstKey;
        this.lastKey = lastKey;
    }
}
