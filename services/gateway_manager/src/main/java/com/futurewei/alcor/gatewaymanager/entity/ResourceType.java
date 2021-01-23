package com.futurewei.alcor.gatewaymanager.entity;

public enum ResourceType {

    VPC("vpc"),
    VPN("vpn"),
    VPCPEERING("vpc-peering");

    private final String resourceType;

    ResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceType() {
        return resourceType;
    }
}
