package com.futurewei.alcor.ipmanager.http;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Ipv4AddrRangeRequest {
    @JsonProperty("subnet_id")
    private String subnetId;

    @JsonProperty("first_addr")
    private String firstAddr;

    @JsonProperty("last_addr")
    private String lastAddr;

    public Ipv4AddrRangeRequest() {}

    public Ipv4AddrRangeRequest(String subnetId, String firstAddr, String lastAddr) {
        this.subnetId = subnetId;
        this.firstAddr = firstAddr;
        this.lastAddr = lastAddr;
    }

    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    public String getFirstAddr() {
        return firstAddr;
    }

    public void setFirstAddr(String firstAddr) {
        this.firstAddr = firstAddr;
    }

    public String getLastAddr() {
        return lastAddr;
    }

    public void setLastAddr(String lastAddr) {
        this.lastAddr = lastAddr;
    }
}
