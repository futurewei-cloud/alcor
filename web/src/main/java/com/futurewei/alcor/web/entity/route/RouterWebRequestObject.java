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
public class RouterWebRequestObject extends CustomerResource {

    @JsonProperty("tenant_id")
    private String tenantId;

    @JsonProperty("admin_state_up")
    private boolean adminStateUp;

    @JsonProperty("status")
    private ExternalGateway external_gateway_info;

    @JsonProperty("distributed")
    private boolean distributed;

    @JsonProperty("ha")
    private boolean ha;

    @JsonProperty("availability_zone_hints")
    private List<String> availabilityZoneHints;

    @JsonProperty("service_type_id")
    private String serviceTypeId;

    @JsonProperty("flavor_id")
    private String flavorId;

    public RouterWebRequestObject () {}

    public RouterWebRequestObject(String projectId, String id, String name, String description) {
        super(projectId, id, name, description);
    }

    public RouterWebRequestObject(String projectId, String id, String name, String description, String tenantId, boolean adminStateUp, ExternalGateway external_gateway_info, boolean distributed, boolean ha, List<String> availabilityZoneHints, String serviceTypeId, String flavorId) {
        super(projectId, id, name, description);
        this.tenantId = tenantId;
        this.adminStateUp = adminStateUp;
        this.external_gateway_info = external_gateway_info;
        this.distributed = distributed;
        this.ha = ha;
        this.availabilityZoneHints = availabilityZoneHints;
        this.serviceTypeId = serviceTypeId;
        this.flavorId = flavorId;
    }
}
