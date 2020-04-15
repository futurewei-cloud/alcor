package com.futurewei.alcor.ipmanager.http;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Ipv4AddrRequest {
    @JsonProperty("subnet_id")
    private String subnetId;

    @JsonProperty("ipv4_addr")
    private String ipv4Addr;

    @JsonProperty("state")
    private String state;

    public Ipv4AddrRequest() {}

    public Ipv4AddrRequest(String subnetId, String ipv4Addr, String state) {
        this.subnetId = subnetId;
        this.ipv4Addr = ipv4Addr;
        this.state = state;
    }

    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    public String getIpv4Addr() {
        return ipv4Addr;
    }

    public void setIpv4Addr(String ipv4Addr) {
        this.ipv4Addr = ipv4Addr;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
