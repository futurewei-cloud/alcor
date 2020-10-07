package com.futurewei.alcor.web.entity.networkacl;

public enum Protocol {
    TCP("tcp"),
    UDP("udp"),
    ICMP("icmp"),
    ICMPV6("icmpv6"),
    ALL("all");

    private String protocol;

    Protocol(String protocol) {
        this.protocol = protocol;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public boolean equals(Protocol protocol) {
        return this.protocol.equals(protocol.getProtocol());
    }

    @Override
    public String toString() {
        return "Protocol{" +
                "protocol='" + protocol + '\'' +
                '}';
    }
}
