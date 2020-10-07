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
import lombok.Data;

import java.util.List;

@Data
public class RouterInterfaceResponse {

    @JsonProperty("id")
    private String routerId;

    @JsonProperty("network_id")
    private String vpcId;

    @JsonProperty("port_id")
    private String portId;

    @JsonProperty("subnet_id")
    private String subnetId;

    @JsonProperty("subnet_ids")
    private List<String> subnetIds;

    @JsonProperty("project_id")
    private String projectId;

    @JsonProperty("tenant_id")
    private String tenantId;

    @JsonProperty("tags")
    private List<String> tags;

    public RouterInterfaceResponse () {}

    public RouterInterfaceResponse(String routerId, String vpcId, String portId, String subnetId, List<String> subnetIds, String projectId, String tenantId, List<String> tags) {
        this.routerId = routerId;
        this.vpcId = vpcId;
        this.portId = portId;
        this.subnetId = subnetId;
        this.subnetIds = subnetIds;
        this.projectId = projectId;
        this.tenantId = tenantId;
        this.tags = tags;
    }
}
