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
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.List;
import java.util.Map;

@Data
public class VpcToRouteMapper {

    @JsonProperty("vpc_id")
    private String vpcId;

    @JsonProperty("route_ids")
    private List<String> routeIds;

    @CreatedDate
    @JsonProperty("created_at")
    private String created_at;

    @LastModifiedDate
    @JsonProperty("updated_at")
    private String updated_at;

    public VpcToRouteMapper() {}

    public VpcToRouteMapper(String vpcId, List<String> routeIds, String created_at, String updated_at) {
        this.vpcId = vpcId;
        this.routeIds = routeIds;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }
}

