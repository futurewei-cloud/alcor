/*
Copyright 2019 The Alcor Authors.

Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/
package com.futurewei.alcor.web.entity.route;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.common.entity.CustomerResource;
import lombok.Data;

import java.util.List;

@Data
public class RouterExtraAttribute extends CustomerResource {

    @JsonProperty("external_gateway_info")
    private ExternalGateway external_gateway_info;

    @JsonProperty("revision_number")
    private Integer revisionNumber;

    @JsonProperty("distributed")
    private boolean distributed;

    @JsonProperty("ha")
    private boolean ha;

    @JsonProperty("availability_zone_hints")
    private List<String> availabilityZoneHints;

    @JsonProperty("availability_zones")
    private List<String> availabilityZones;

    @JsonProperty("service_type_id")
    private String serviceTypeId;

    @JsonProperty("flavor_id")
    private String flavorId;

    @JsonProperty("tags")
    private List<String> tags;

    @JsonProperty("conntrack_helpers")
    private List<String> conntrackHelpers;

    public RouterExtraAttribute () {}

    public RouterExtraAttribute(String projectId, String id, String name, String description, ExternalGateway external_gateway_info, Integer revisionNumber, boolean distributed, boolean ha, List<String> availabilityZoneHints, List<String> availabilityZones, String serviceTypeId, String flavorId, List<String> tags, List<String> conntrackHelpers) {
        super(projectId, id, name, description);
        this.external_gateway_info = external_gateway_info;
        this.revisionNumber = revisionNumber;
        this.distributed = distributed;
        this.ha = ha;
        this.availabilityZoneHints = availabilityZoneHints;
        this.availabilityZones = availabilityZones;
        this.serviceTypeId = serviceTypeId;
        this.flavorId = flavorId;
        this.tags = tags;
        this.conntrackHelpers = conntrackHelpers;
    }
}
