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
package com.futurewei.alcor.web.entity.subnet;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GatewayPortDetail {

    @JsonProperty("gateway_macAddress")
    private String gatewayMacAddress;

    // subnet_gateway_port_id
    @JsonProperty("gateway_port_id")
    private String gatewayPortId;

    public GatewayPortDetail() {}

    public GatewayPortDetail(String gatewayMacAddress, String gatewayPortId) {
        this.gatewayMacAddress = gatewayMacAddress;
        this.gatewayPortId = gatewayPortId;
    }

    public String getGatewayMacAddress() {
        return gatewayMacAddress;
    }

    public void setGatewayMacAddress(String gatewayMacAddress) {
        this.gatewayMacAddress = gatewayMacAddress;
    }

    public String getGatewayPortId() {
        return gatewayPortId;
    }

    public void setGatewayPortId(String gatewayPortId) {
        this.gatewayPortId = gatewayPortId;
    }
}
