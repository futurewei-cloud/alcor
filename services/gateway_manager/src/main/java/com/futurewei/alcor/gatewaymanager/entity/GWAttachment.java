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
