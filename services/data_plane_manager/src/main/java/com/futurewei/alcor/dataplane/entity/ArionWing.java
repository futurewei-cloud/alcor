/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
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
