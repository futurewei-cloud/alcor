package com.futurewei.alcor.common.enumClass;

public enum Ipv6AddressModeEnum {

    SLAAC("slaac"),
    STATEFUL("dhcpv6-stateful"),
    STATELESS("dhcpv6-stateless"),
    NULL("null");

    private String mode;

    Ipv6AddressModeEnum (String env) {
        this.mode = env;
    }

    public String getMode () {
        return mode;
    }
}
