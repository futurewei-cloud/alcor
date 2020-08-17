/*
 *
 * Copyright 2019 The Alcor Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *         you may not use this file except in compliance with the License.
 *         You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *         See the License for the specific language governing permissions and
 *         limitations under the License.
 * /
 */

package com.futurewei.alcor.common.cache.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.common.entity.CustomerResource;

public class TestEntity extends CustomerResource {

    private boolean shared;

    private int count;

    @JsonProperty("revision_number")
    private int revisionNumber;

    @JsonProperty("egress_firewall_policy_id")
    private String egressFirewallPolicyId;

    @JsonProperty("enable_dhcp")
    private Boolean enableDhcp;

    @JsonProperty("ip_version")
    private Integer ipVersion;

    public boolean isShared() {
        return shared;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getRevisionNumber() {
        return revisionNumber;
    }

    public void setRevisionNumber(int revisionNumber) {
        this.revisionNumber = revisionNumber;
    }

    public String getEgressFirewallPolicyId() {
        return egressFirewallPolicyId;
    }

    public void setEgressFirewallPolicyId(String egressFirewallPolicyId) {
        this.egressFirewallPolicyId = egressFirewallPolicyId;
    }

    public Boolean getEnableDhcp() {
        return enableDhcp;
    }

    public void setEnableDhcp(Boolean enableDhcp) {
        this.enableDhcp = enableDhcp;
    }

    public Integer getIpVersion() {
        return ipVersion;
    }

    public void setIpVersion(Integer ipVersion) {
        this.ipVersion = ipVersion;
    }


}
