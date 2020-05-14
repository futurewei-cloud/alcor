package com.futurewei.alcor.common.enumClass;

public enum Ipv6RaModeEnum {

    SLAAC("slaac"),
    STATEFUL("dhcpv6-stateful"),
    STATELESS("dhcpv6-stateless"),
    NULL("null");

    private String mode;

    Ipv6RaModeEnum (String env) {
        this.mode = env;
    }

    public String getMode () {
        return mode;
    }
}
