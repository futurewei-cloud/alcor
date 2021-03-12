package com.futurewei.alcor.dataplane.entity;

import java.util.List;

public class InternalSubnets {
    private String vpcId;
    private String routerId;
    private List<String> subnets;

    public InternalSubnets() { };

    public InternalSubnets(String vcpId, String routerId, List<String> subnets) {
        this.vpcId = vcpId;
        this.routerId = routerId;
        this.subnets = subnets;
    }

    public void setVpcId(String vpcId) { this.vpcId = vpcId; }

    public void setRouterId(String routerId) { this.routerId = routerId; }

    public void setSubnets(List<String> subnets) {
        this.subnets = subnets;
    }

    public String getVpcId() {
        return this.vpcId;
    }

    public String getRouterId() { return this.routerId; }

    public List<String>getSubnets() {
        return this.subnets;
    }
}
