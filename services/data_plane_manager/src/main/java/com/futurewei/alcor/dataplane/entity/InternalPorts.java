package com.futurewei.alcor.dataplane.entity;

import java.util.List;

public class InternalPorts {
    private String subnetId;
    private String securityGroupId;
    private List<String> ports;

    public InternalPorts() { };

    public InternalPorts(String subnetId, String securityGroupId, List<String> ports) {
        this.subnetId = subnetId;
        this.securityGroupId = securityGroupId;
        this.ports = ports;
    }

    public void setSubnetId(String subnetId) { this.subnetId = subnetId; }

    public void setSecurityGroupId(String securityGroupId) {
        this.securityGroupId = securityGroupId;
    }

    public void setPorts(List<String> ports) {
        this.ports = ports;
    }

    public String getSubnetId() {
        return this.subnetId;
    }

    public String getSecurityGroupId() {
        return this.securityGroupId;
    }

    public List<String>getPorts() {
        return this.ports;
    }
}
