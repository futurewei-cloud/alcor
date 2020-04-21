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

package com.futurewei.alcor.privateipmanager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IpAddrRangeRequest {
    @JsonProperty("id")
    private String id;

    @JsonProperty("ip_version")
    private int ipVersion;

    @JsonProperty("first_addr")
    private String firstAddr;

    @JsonProperty("last_addr")
    private String lastAddr;

    public IpAddrRangeRequest() {}

    public IpAddrRangeRequest(String id, int ipVersion, String firstAddr, String lastAddr) {
        this.id = id;
        this.ipVersion = ipVersion;
        this.firstAddr = firstAddr;
        this.lastAddr = lastAddr;
    }

    public int getIpVersion() {
        return ipVersion;
    }

    public void setIpVersion(int ipVersion) {
        this.ipVersion = ipVersion;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
