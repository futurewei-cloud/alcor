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
package com.futurewei.alcor.web.entity.vpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class NetworkIPAvailabilityEntity {

    @JsonProperty("network_id")
    private String vpcId;

    @JsonProperty("network_name")
    private String vpcName;

    @JsonProperty("tenant_id")
    private String tenantId;

    @JsonProperty("project_id")
    private String projectId;

    @JsonProperty("total_ips")
    private Integer totalIps;

    @JsonProperty("used_ips")
    private Integer usedIps;

    @JsonProperty("subnet_ip_availability")
    private List<SubnetIPAvailabilityEntity> subnetIpAvailability;

    public NetworkIPAvailabilityEntity () {}

    public NetworkIPAvailabilityEntity (String vpcId, String vpcName, String tenantId, String projectId, Integer totalIps, Integer usedIps, List<SubnetIPAvailabilityEntity> subnetIpAvailabilityEntity) {
        this.vpcId = vpcId;
        this.vpcName = vpcName;
        this.tenantId = tenantId;
        this.projectId = projectId;
        this.totalIps = totalIps;
        this.usedIps = usedIps;
        this.subnetIpAvailability = subnetIpAvailabilityEntity;
    }
}
