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

package com.futurewei.alcor.web.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.common.entity.CustomerResource;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SubnetWebObject extends CustomerResource {

    @JsonProperty("vpc_id")
    private String vpcId;

    @JsonProperty("cidr")
    private String cidr;

    @JsonProperty("availability_zone")
    private String availabilityZone;

    @JsonProperty("gateway_ip")
    private String gatewayIp;

    @JsonProperty("dhcp_enable")
    private Boolean dhcpEnable;

    @JsonProperty("primary_dns")
    private String primaryDns;

    @JsonProperty("secondary_dns")
    private String secondaryDns;

    @JsonProperty("routes")
    private List<RouteWebObject> routes;

    @JsonProperty("dns_list")
    private List<String> dnsList;

    public SubnetWebObject() {
    }

    public SubnetWebObject(String projectId, String vpcId, String id, String name, String cidr) {
        this(projectId, vpcId, id, name, cidr, null, null, null, false, null, null, null, null);
    }

    public SubnetWebObject(String projectId, String vpcId, String id, String name, String cidr, List<RouteWebObject> routes) {
        this(projectId, vpcId, id, name, cidr, null, null, null, false, null, null, routes, null);
    }

    public SubnetWebObject(String projectId, String vpcId, String id, String name, String cidr, String gatewayIp) {
        this(projectId, vpcId, id, name, cidr, null, null, gatewayIp, false, null, null, null, null);
    }

    public SubnetWebObject(SubnetWebObject state) {
        this(state.getProjectId(), state.getVpcId(), state.getId(), state.getName(), state.getCidr(), state.getDescription(),
                state.getAvailabilityZone(), state.getGatewayIp(), state.getDhcpEnable(), state.getPrimaryDns(), state.getSecondaryDns(), state.getRoutes(), state.getDnsList());
    }

    public SubnetWebObject(String projectId, String vpcId, String id, String name, String cidr, String description, String availabilityZone,
                           String gatewayIp, Boolean dhcpEnable, String primaryDns, String secondaryDns, List<RouteWebObject> routes, List<String> dnsList) {

        super(projectId, id, name, description);

        this.vpcId = vpcId;
        this.cidr = cidr;
        this.availabilityZone = availabilityZone;
        this.gatewayIp = gatewayIp;
        this.dhcpEnable = dhcpEnable;
        this.primaryDns = primaryDns;
        this.secondaryDns = secondaryDns;
        this.routes = routes;
        this.dnsList = (dnsList == null ? null : new ArrayList<>(dnsList));
    }

    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    public String getCidr() {
        return cidr;
    }

    public void setCidr(String cidr) {
        this.cidr = cidr;
    }

    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    public String getGatewayIp() {
        return gatewayIp;
    }

    public void setGatewayIp(String gatewayIp) {
        this.gatewayIp = gatewayIp;
    }

    public Boolean getDhcpEnable() {
        return dhcpEnable;
    }

    public void setDhcpEnable(Boolean dhcpEnable) {
        this.dhcpEnable = dhcpEnable;
    }

    public String getPrimaryDns() {
        return primaryDns;
    }

    public void setPrimaryDns(String primaryDns) {
        this.primaryDns = primaryDns;
    }

    public String getSecondaryDns() {
        return secondaryDns;
    }

    public void setSecondaryDns(String secondaryDns) {
        this.secondaryDns = secondaryDns;
    }

    public List<String> getDnsList() {
        return dnsList;
    }

    public void setDnsList(List<String> dnsList) {
        this.dnsList = dnsList;
    }

    public List<RouteWebObject> getRoutes() {
        return routes;
    }

    public void setRoutes(List<RouteWebObject> routes) {
        this.routes = routes;
    }
}
