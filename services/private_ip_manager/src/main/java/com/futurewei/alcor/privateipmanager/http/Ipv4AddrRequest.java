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

public class Ipv4AddrRequest {
    @JsonProperty("subnet_id")
    private String subnetId;

    @JsonProperty("ipv4_addr")
    private String ipv4Addr;

    @JsonProperty("state")
    private String state;

    public Ipv4AddrRequest() {}

    public Ipv4AddrRequest(String subnetId, String ipv4Addr, String state) {
        this.subnetId = subnetId;
        this.ipv4Addr = ipv4Addr;
        this.state = state;
    }

    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    public String getIpv4Addr() {
        return ipv4Addr;
    }

    public void setIpv4Addr(String ipv4Addr) {
        this.ipv4Addr = ipv4Addr;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
