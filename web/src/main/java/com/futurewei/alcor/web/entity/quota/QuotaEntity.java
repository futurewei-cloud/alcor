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
package com.futurewei.alcor.web.entity.quota;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.common.entity.CustomerResource;

public class QuotaEntity extends CustomerResource {

    @JsonProperty("floatingip")
    private int floatingIp;

    @JsonProperty("network")
    private int network;

    @JsonProperty("port")
    private int port;

    @JsonProperty("rbac_policy")
    private int rbacPolicy;

    @JsonProperty("router")
    private int router;

    @JsonProperty("security_group")
    private int securityGroup;

    @JsonProperty("security_group_rule")
    private int securityGroupRule;

    @JsonProperty("subnet")
    private long subnet;

    @JsonProperty("subnetpool")
    private long subnetPool;

    public QuotaEntity() {
    }

    public QuotaEntity(int floatingIp, int network, int port, int rbacPolicy, int router, int securityGroup,
                       int securityGroupRule, int subnet, int subnetPool) {
        this.floatingIp = floatingIp;
        this.network = network;
        this.port = port;
        this.rbacPolicy = rbacPolicy;
        this.router = router;
        this.securityGroup = securityGroup;
        this.securityGroupRule = securityGroupRule;
        this.subnet = subnet;
        this.subnetPool = subnetPool;
    }

    public int getFloatingIp() {
        return floatingIp;
    }

    public void setFloatingIp(int floatingIp) {
        this.floatingIp = floatingIp;
    }

    public int getNetwork() {
        return network;
    }

    public void setNetwork(int network) {
        this.network = network;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getRbacPolicy() {
        return rbacPolicy;
    }

    public void setRbacPolicy(int rbacPolicy) {
        this.rbacPolicy = rbacPolicy;
    }

    public int getRouter() {
        return router;
    }

    public void setRouter(int router) {
        this.router = router;
    }

    public int getSecurityGroup() {
        return securityGroup;
    }

    public void setSecurityGroup(int securityGroup) {
        this.securityGroup = securityGroup;
    }

    public int getSecurityGroupRule() {
        return securityGroupRule;
    }

    public void setSecurityGroupRule(int securityGroupRule) {
        this.securityGroupRule = securityGroupRule;
    }

    public long getSubnet() {
        return subnet;
    }

    public void setSubnet(long subnet) {
        this.subnet = subnet;
    }

    public long getSubnetPool() {
        return subnetPool;
    }

    public void setSubnetPool(long subnetPool) {
        this.subnetPool = subnetPool;
    }
}
