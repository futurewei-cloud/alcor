package com.futurewei.alcor.dataplane.entity;

import org.ishugaliy.allgood.consistent.hash.annotation.Generated;
import org.ishugaliy.allgood.consistent.hash.node.Node;

import java.util.Objects;
import java.util.StringJoiner;

public class ArionWing implements Node {
    private String group;
    private String ip;
    private String mac;
    private int port;

    public ArionWing() {

    }

    public ArionWing(String group, String ip, String mac, int port) {
        this.group = group != null ? group : "";
        this.mac = mac;
        this.ip = ip != null ? ip : "";
        this.port = port;
    }

    public String getGroup() {
        return group;
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
        return getGroup();
    }

    @Override
    @Generated
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArionWing)) return false;
        ArionWing that = (ArionWing) o;
        return getPort() == that.getPort() &&
                Objects.equals(getGroup(), that.getGroup()) &&
                Objects.equals(getIp(), that.getIp()) &&
                Objects.equals(getMac(), that.getMac());
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(getGroup(), getIp(), getMac(), getPort());
    }

    @Override
    @Generated
    public String toString() {
        return new StringJoiner(", ", ArionWing.class.getSimpleName() + "[", "]")
                .add("dc='" + getGroup() + "'")
                .add("ip='" + getIp() + "'")
                .add("ip='" + getMac() + "'")
                .add("port=" + getPort())
                .toString();
    }
}
