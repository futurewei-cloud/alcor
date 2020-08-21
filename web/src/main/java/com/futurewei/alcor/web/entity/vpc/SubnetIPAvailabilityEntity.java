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

@Data
public class SubnetIPAvailabilityEntity {

    @JsonProperty("total_ips")
    private Integer totalIps;

    @JsonProperty("used_ips")
    private Integer usedIps;

    @JsonProperty("subnet_id")
    private String subnetId;

    @JsonProperty("subnet_name")
    private String subnetName;

    @JsonProperty("ip_version")
    private Integer ipVersion;

    @JsonProperty("cidr")
    private String cidr;

    public SubnetIPAvailabilityEntity() {}

    public SubnetIPAvailabilityEntity(Integer totalIps, Integer usedIps, String subnetId, String subnetName, Integer ipVersion, String cidr) {
        this.totalIps = totalIps;
        this.usedIps = usedIps;
        this.subnetId = subnetId;
        this.subnetName = subnetName;
        this.ipVersion = ipVersion;
        this.cidr = cidr;
    }
}
