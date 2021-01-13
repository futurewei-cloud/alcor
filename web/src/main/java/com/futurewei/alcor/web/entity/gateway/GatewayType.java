package com.futurewei.alcor.web.entity.gateway;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum GatewayType {

    ZETA("zeta"),
    IGW("igw"),
    NGW("ngw"),
    TGW("tgw"),
    DEFAULT(null);

    private final String gatewayType;

    @JsonCreator
    public static GatewayType forValue(String gatewayType) {
        GatewayType[] gatewayTypes = GatewayType.values();
        return Arrays.stream(gatewayTypes).filter(type -> type.getGatewayType().equals(gatewayType)).findFirst().orElse(DEFAULT);
    }

    GatewayType(String gatewayType) {
        this.gatewayType = gatewayType;
    }

    @JsonValue
    public String getGatewayType() {
        return gatewayType;
    }
}
