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
package com.futurewei.alcor.web.entity.port;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DnsRecord {
    @JsonProperty("host_name")
    private String hostName;

    @JsonProperty("ip_address")
    private String ipAddress;

    @JsonProperty("fqdn")
    private String fqdn;

    public DnsRecord() {

    }

    public DnsRecord(String hostName, String ipAddress, String fqdn) {
        this.hostName = hostName;
        this.ipAddress = ipAddress;
        this.fqdn = fqdn;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getFqdn() {
        return fqdn;
    }

    public void setFqdn(String fqdn) {
        this.fqdn = fqdn;
    }
}
