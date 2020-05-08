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

import com.futurewei.alcor.common.entity.CustomerResource;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SubnetState extends CustomerResource {

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

    @JsonProperty("mac_address")
    private String gatewayMacAddress;

    @JsonProperty("dns_list")
    private List<String> dnsList;

    @JsonProperty("ipv4_range_id")
    private String ipV4RangeId;

    @JsonProperty("ipv6_range_id")
    private String ipV6RangeId;

    public SubnetState() {
    }

    public SubnetState(String projectId, String vpcId, String id, String name, String cidr) {
        this(projectId, vpcId, id, name, cidr, null, null, null, false, null, null, null, null, null, null, null);
    }

    public SubnetState(String projectId, String vpcId, String id, String name, String cidr, String gatewayIp, String gatewayMacAddress) {
        this(projectId, vpcId, id, name, cidr, null, null, gatewayIp, false, null, null, null, null, gatewayMacAddress, null, null);
    }

    public SubnetState(String projectId, String vpcId, String id, String name, String cidr, List<RouteWebObject> routes) {
        this(projectId, vpcId, id, name, cidr, null, null, null, false, null, null, routes, null, null, null, null);
    }

    public SubnetState(String projectId, String vpcId, String id, String name, String cidr, String gatewayIp) {
        this(projectId, vpcId, id, name, cidr, null, null, gatewayIp, false, null, null, null, null, null, null, null);
    }

    public SubnetState(SubnetState state) {
        this(state.getProjectId(), state.getVpcId(), state.getId(), state.getName(), state.getCidr(), state.getDescription(),
                state.getAvailabilityZone(), state.getGatewayIp(), state.getDhcpEnable(), state.getPrimaryDns(), state.getSecondaryDns(), state.getRoutes(), state.getDnsList(), state.getGatewayMacAddress(), state.getIpV4RangeId(), state.getIpV6RangeId());
    }

    public SubnetState(String projectId, String vpcId, String id, String name, String cidr, String description, String availabilityZone,
                       String gatewayIp, Boolean dhcpEnable, String primaryDns, String secondaryDns, List<RouteWebObject> routes, List<String> dnsList, String gatewayMacAddress, String ipV4RangeId, String ipV6RangeId) {

        super(projectId, id, name, description);

        this.vpcId = vpcId;
        this.cidr = cidr;
        this.availabilityZone = availabilityZone;
        this.gatewayIp = gatewayIp;
        this.dhcpEnable = dhcpEnable;
        this.primaryDns = primaryDns;
        this.secondaryDns = secondaryDns;
        this.routes = routes;
        this.gatewayMacAddress = gatewayMacAddress;
        this.dnsList = (dnsList == null ? null : new ArrayList<>(dnsList));
        this.ipV4RangeId = ipV4RangeId;
        this.ipV6RangeId = ipV6RangeId;
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

    public String getGatewayMacAddress() {
        return gatewayMacAddress;
    }

    public void setGatewayMacAddress(String gatewayMacAddress) {
        this.gatewayMacAddress = gatewayMacAddress;
    }

    public String getIpV4RangeId() {
        return ipV4RangeId;
    }

    public void setIpV4RangeId(String ipV4RangeId) {
        this.ipV4RangeId = ipV4RangeId;
    }

    public String getIpV6RangeId() {
        return ipV6RangeId;
    }

    public void setIpV6RangeId(String ipV6RangeId) {
        this.ipV6RangeId = ipV6RangeId;
    }
}