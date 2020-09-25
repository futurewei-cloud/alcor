package com.futurewei.alcor.web.entity.networkacl;

public enum EtherType {
    IPV4("ipv4"),
    IPV6("ipv6");

    private String etherType;

    EtherType(String etherType) {
        this.etherType = etherType;
    }

    public String getEtherType() {
        return etherType;
    }

    public void setEtherType(String etherType) {
        this.etherType = etherType;
    }

    @Override
    public String toString() {
        return "EtherType{" +
                "etherType='" + etherType + '\'' +
                '}';
    }
}
