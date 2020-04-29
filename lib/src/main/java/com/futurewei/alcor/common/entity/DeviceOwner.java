package com.futurewei.alcor.common.entity;

public enum DeviceOwner {
    NOVA("compute:nova"),
    ROUTER("network:router"),
    DHCP("network:dhcp");


    private String owner;

    DeviceOwner(String owner) {
        this.owner = owner;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
