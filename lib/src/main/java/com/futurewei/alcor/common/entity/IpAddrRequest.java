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

package com.futurewei.alcor.common.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IpAddrRequest {
    @JsonProperty("ip_version")
    private int ipVersion;

    @JsonProperty("subnet_id")
    private String subnetId;

    @JsonProperty("range_id")
    private String rangeId;

    @JsonProperty("ip")
    private String ip;

    @JsonProperty("state")
    private String state;

    public IpAddrRequest() {}

    public IpAddrRequest(int ipVersion, String subnetId, String rangeId, String ip, String state) {
        this.ipVersion = ipVersion;
        this.subnetId = subnetId;
        this.rangeId = rangeId;
        this.ip = ip;
        this.state = state;
    }

    public int getIpVersion() {
        return ipVersion;
    }

    public void setIpVersion(int ipVersion) {
        this.ipVersion = ipVersion;
    }

    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    public String getRangeId() {
        return rangeId;
    }

    public void setRangeId(String rangeId) {
        this.rangeId = rangeId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "IpAddrRequest{" +
                "ipVersion=" + ipVersion +
                ", rangeId='" + rangeId + '\'' +
                ", ip='" + ip + '\'' +
                ", state='" + state + '\'' +
                '}';
    }
}
