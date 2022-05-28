package com.futurewei.alcor.dataplane.entity;

import org.ishugaliy.allgood.consistent.hash.annotation.Generated;
import org.ishugaliy.allgood.consistent.hash.node.Node;

import java.util.Objects;
import java.util.StringJoiner;

public class ArionWing implements Node {
    private final String dc;
    private final String ip;
    private final String mac;
    private final int port;

    public ArionWing(String ip, String mac, int port) {
        this("", ip, mac, port);
    }

    public ArionWing(String dc, String ip, String mac, int port) {
        this.dc = dc != null ? dc : "";
        this.mac = mac;
        this.ip = ip != null ? ip : "";
        this.port = port;
    }

    public String getDc() {
        return dc;
    }

    public String getIp() {
        return ip;
    }

    public String getMac() {return mac; }

    public int getPort() {
        return port;
    }

    @Override
    public String getKey() {
        return String.format("%s:%s:%s", dc, ip, port);
    }

    @Override
    @Generated
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArionWing)) return false;
        ArionWing that = (ArionWing) o;
        return port == that.port &&
                Objects.equals(dc, that.dc) &&
                Objects.equals(ip, that.ip) &&
                Objects.equals(mac, that.mac);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(dc, ip, mac, port);
    }

    @Override
    @Generated
    public String toString() {
        return new StringJoiner(", ", ArionWing.class.getSimpleName() + "[", "]")
                .add("dc='" + dc + "'")
                .add("ip='" + ip + "'")
                .add("ip='" + mac + "'")
                .add("port=" + port)
                .toString();
    }
}
