package com.futurewei.alcor.web.entity.port;

public enum IpAllocation {
    DEFERRED("deferred"),
    IMMEDIATE("immediate"),
    NONE("none");

    private String ipAllocation;

    IpAllocation(String ipAllocation) {
        this.ipAllocation = ipAllocation;
    }

    public String getIpAllocation() {
        return ipAllocation;
    }

    public void setIpAllocation(String ipAllocation) {
        this.ipAllocation = ipAllocation;
    }
}
