package com.futurewei.alcor.dataplane.entity;

import java.util.List;

public class InternalPorts {
    private String subnetId;
    private String securityGroupId;
    private List<String> portIds;

    public InternalPorts() { };

    public InternalPorts(String subnetId, String securityGroupId, List<String> portIds) {
        this.subnetId = subnetId;
        this.securityGroupId = securityGroupId;
        this.portIds = portIds;
    }

    public void setSubnetId(String subnetId) { this.subnetId = subnetId; }

    public void setSecurityGroupId(String securityGroupId) {
        this.securityGroupId = securityGroupId;
    }

    public void setPortIds(List<String> portIds) {
        this.portIds = portIds;
    }

    public String getSubnetId() {
        return this.subnetId;
    }

    public String getSecurityGroupId() {
        return this.securityGroupId;
    }

    public List<String>getPortIds() {
        return this.portIds;
    }
}
