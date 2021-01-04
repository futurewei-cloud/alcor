package com.futurewei.alcor.gatewaymanager.entity;

public enum GatewayType {

    ZETA("zata"),
    IGW("igw"),
    NGW("ngw"),
    TGW("tgw");

    private final String gatewayType;

    GatewayType(String gatewayType) {
        this.gatewayType = gatewayType;
    }

    public String getGatewayType() {
        return gatewayType;
    }
}
