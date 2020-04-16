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

package com.futurewei.alcor.privateipmanager.http;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Ipv4AddrRangeRequest {
    @JsonProperty("subnet_id")
    private String subnetId;

    @JsonProperty("first_addr")
    private String firstAddr;

    @JsonProperty("last_addr")
    private String lastAddr;

    public Ipv4AddrRangeRequest() {}

    public Ipv4AddrRangeRequest(String subnetId, String firstAddr, String lastAddr) {
        this.subnetId = subnetId;
        this.firstAddr = firstAddr;
        this.lastAddr = lastAddr;
    }

    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    public String getFirstAddr() {
        return firstAddr;
    }

    public void setFirstAddr(String firstAddr) {
        this.firstAddr = firstAddr;
    }

    public String getLastAddr() {
        return lastAddr;
    }

    public void setLastAddr(String lastAddr) {
        this.lastAddr = lastAddr;
    }
}
