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

package com.futurewei.alcor.web.entity.quota;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.common.entity.CustomerResource;

public class QuotaDetailEntity extends CustomerResource {

    @JsonProperty("floating_ip")
    private QuotaUsageEntity floatingIp;

    private QuotaUsageEntity router;

    @JsonProperty("rbac_policy")
    private QuotaUsageEntity rbacPolicy;

    private QuotaUsageEntity subnetPool;

    @JsonProperty("security_group_rule")
    private QuotaUsageEntity securityGroupRule;

    @JsonProperty("security_group")
    private QuotaUsageEntity securityGroup;

    private QuotaUsageEntity subnet;

    private QuotaUsageEntity port;

    private QuotaUsageEntity network;

    public QuotaDetailEntity() {
    }

    public QuotaUsageEntity getFloatingIp() {
        return floatingIp;
    }

    public void setFloatingIp(QuotaUsageEntity floatingIp) {
        this.floatingIp = floatingIp;
    }

    public QuotaUsageEntity getRouter() {
        return router;
    }

    public void setRouter(QuotaUsageEntity router) {
        this.router = router;
    }

    public QuotaUsageEntity getRbacPolicy() {
        return rbacPolicy;
    }

    public void setRbacPolicy(QuotaUsageEntity rbacPolicy) {
        this.rbacPolicy = rbacPolicy;
    }

    public QuotaUsageEntity getSubnetPool() {
        return subnetPool;
    }

    public void setSubnetPool(QuotaUsageEntity subnetPool) {
        this.subnetPool = subnetPool;
    }

    public QuotaUsageEntity getSecurityGroupRule() {
        return securityGroupRule;
    }

    public void setSecurityGroupRule(QuotaUsageEntity securityGroupRule) {
        this.securityGroupRule = securityGroupRule;
    }

    public QuotaUsageEntity getSecurityGroup() {
        return securityGroup;
    }

    public void setSecurityGroup(QuotaUsageEntity securityGroup) {
        this.securityGroup = securityGroup;
    }

    public QuotaUsageEntity getSubnet() {
        return subnet;
    }

    public void setSubnet(QuotaUsageEntity subnet) {
        this.subnet = subnet;
    }

    public QuotaUsageEntity getPort() {
        return port;
    }

    public void setPort(QuotaUsageEntity port) {
        this.port = port;
    }

    public QuotaUsageEntity getNetwork() {
        return network;
    }

    public void setNetwork(QuotaUsageEntity network) {
        this.network = network;
    }
}
