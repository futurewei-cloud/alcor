package com.futurewei.alcor.gatewaymanager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

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

    @JsonProperty("state")
    private String state; // (available | pending | associating)

    @JsonProperty("association_state")
    private String associationState; // (associated | associating)

    @JsonProperty("associated_routetable")
    private String associatedRouteTable;

    @JsonProperty("subnets")
    private List<String> subnets; //for VPC only

    @JsonProperty("tags")
    private List<String> tags;
}
