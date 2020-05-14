package com.futurewei.alcor.common.enumClass;

public enum NetworkStatusEnum {

    ACTIVE("ACTIVE"),
    DOWN("DOWN"),
    BUILD("BUILD"),
    ERROR("ERROR");

    private String networkStatus;

    NetworkStatusEnum (String env) {
        this.networkStatus = env;
    }

    public String getNetworkStatus () {
        return networkStatus;
    }
}
