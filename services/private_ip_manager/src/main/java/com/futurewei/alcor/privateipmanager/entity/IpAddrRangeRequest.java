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

    @JsonProperty("subnet_id")
    private String subnetId;

    @JsonProperty("ip_version")
    private int ipVersion;

    @JsonProperty("first_ip")
    private String firstIp;

    @JsonProperty("last_ip")
    private String lastIp;

    @JsonProperty("used_ips")
    private long usedIps;

    @JsonProperty("total_ips")
    private long totalIps;

    public IpAddrRangeRequest() {}

    public IpAddrRangeRequest(String id, String subnetId, int ipVersion, String firstIp, String lastIp) {
        this.id = id;
        this.subnetId = subnetId;
        this.ipVersion = ipVersion;
        this.firstIp = firstIp;
        this.lastIp = lastIp;
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

    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    public String getFirstIp() {
        return firstIp;
    }

    public void setFirstIp(String firstIp) {
        this.firstIp = firstIp;
    }

    public String getLastIp() {
        return lastIp;
    }

    public void setLastIp(String lastIp) {
        this.lastIp = lastIp;
    }

    public long getUsedIps() {
        return usedIps;
    }

    public void setUsedIps(long usedIps) {
        this.usedIps = usedIps;
    }

    public long getTotalIps() {
        return totalIps;
    }

    public void setTotalIps(long totalIps) {
        this.totalIps = totalIps;
    }
}
