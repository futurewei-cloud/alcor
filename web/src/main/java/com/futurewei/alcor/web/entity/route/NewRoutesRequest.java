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
import org.jetbrains.annotations.NotNull;

@Data
public class NewRoutesRequest {

    @JsonProperty("destination")
    private String destination;

    // gateway_ip_address
    @JsonProperty("nexthop")
    private String nexthop;

    public NewRoutesRequest() {}

    public NewRoutesRequest(String destination, String nexthop) {
        this.destination = destination;
        this.nexthop = nexthop;
    }
}
