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

public class IpAddrRequest {
    @JsonProperty("ip_version")
    private int ipVersion;

    @JsonProperty("range_id")
    private String rangeId;

    @JsonProperty("ip_addr")
    private String ipAddr;

    @JsonProperty("state")
    private String state;

    public IpAddrRequest() {}

    public IpAddrRequest(int ipVersion, String rangeId, String ipAddr, String state) {
        this.ipVersion = ipVersion;
        this.rangeId = rangeId;
        this.ipAddr = ipAddr;
        this.state = state;
    }

    public int getIpVersion() {
        return ipVersion;
    }

    public void setIpVersion(int ipVersion) {
        this.ipVersion = ipVersion;
    }

    public String getRangeId() {
        return rangeId;
    }

    public void setRangeId(String rangeId) {
        this.rangeId = rangeId;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
