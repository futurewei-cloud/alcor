/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
