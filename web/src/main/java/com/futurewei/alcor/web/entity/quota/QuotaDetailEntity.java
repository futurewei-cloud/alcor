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
