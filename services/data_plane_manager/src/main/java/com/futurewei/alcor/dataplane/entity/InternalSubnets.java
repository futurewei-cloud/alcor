package com.futurewei.alcor.dataplane.entity;

import java.util.List;

public class InternalSubnets {
    private String vpcId;
    private String routerId;
    private List<String> subnetIds;

    public InternalSubnets() { };

    public InternalSubnets(String vcpId, String routerId, List<String> subnetIds) {
        this.vpcId = vcpId;
        this.routerId = routerId;
        this.subnetIds = subnetIds;
    }

    public void setVpcId(String vpcId) { this.vpcId = vpcId; }

    public void setRouterId(String routerId) { this.routerId = routerId; }

    public void setSubnetIds(List<String> subnetIds) {
        this.subnetIds = subnetIds;
    }

    public String getVpcId() {
        return this.vpcId;
    }

    public String getRouterId() { return this.routerId; }

    public List<String>getSubnetIds() {
        return this.subnetIds;
    }
}
