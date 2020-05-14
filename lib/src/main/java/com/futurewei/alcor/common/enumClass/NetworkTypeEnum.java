package com.futurewei.alcor.common.enumClass;

import org.springframework.core.env.Environment;

public enum NetworkTypeEnum {

    FLAT("flat"),
    VLAN("vlan"),
    VXLAN("vxlan"),
    GRE("gre");

    private String networkType;

    NetworkTypeEnum (String env) {
        this.networkType = env;
    }

    public String getNetworkType () {
        return networkType;
    }
}
