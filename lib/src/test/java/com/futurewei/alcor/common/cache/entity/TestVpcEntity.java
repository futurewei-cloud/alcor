package com.futurewei.alcor.common.cache.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TestVpcEntity {

    private String name;

    private boolean shared;

    @JsonProperty("provider:physical_network")
    private String providerPhysicalNetwork;

    public TestVpcEntity(String name, boolean shared, String providerPhysicalNetwork) {
        this.name = name;
        this.shared = shared;
        this.providerPhysicalNetwork = providerPhysicalNetwork;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getShared() {
        return shared;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }

    public String getProviderPhysicalNetwork() {
        return providerPhysicalNetwork;
    }

    public void setProviderPhysicalNetwork(String providerPhysicalNetwork) {
        this.providerPhysicalNetwork = providerPhysicalNetwork;
    }
}
