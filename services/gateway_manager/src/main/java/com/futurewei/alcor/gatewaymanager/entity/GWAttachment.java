package com.futurewei.alcor.gatewaymanager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class GWAttachment {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("type")
    private ResourceType type;

    @JsonProperty("resource_id")
    private String resourceId; // (VPC | VPN)

    @JsonProperty("gateway_id")
    private String gatewayId;

    @JsonProperty("options")
    private Map<String, String> options;

    @JsonProperty("status")
    private String state; // (available | pending | associating)

    @JsonProperty("association_state")
    private String associationState; // (associated | associating)

    @JsonProperty("associated_routetable")
    private String associatedRouteTable;

    @JsonProperty("subnets")
    private List<String> subnets; //for VPC only

    @JsonProperty("source_vni")
    private String source_vni;

    @JsonProperty("destination_encap")
    private String destinationEncap;

    @JsonProperty("tags")
    private List<String> tags;

    public GWAttachment(String name, ResourceType type, String resourceId, String gatewayId, String state, String source_vni) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.type = type;
        this.resourceId = resourceId;
        this.gatewayId = gatewayId;
        this.state = state;
        this.source_vni = source_vni;
    }
}
