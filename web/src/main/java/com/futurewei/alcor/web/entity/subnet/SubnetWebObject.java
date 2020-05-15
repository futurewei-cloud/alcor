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

package com.futurewei.alcor.web.entity.subnet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.common.entity.CustomerResource;
import com.futurewei.alcor.web.entity.AllocationPool;
import com.futurewei.alcor.web.entity.HostRoute;
import com.futurewei.alcor.web.entity.route.RouteWebObject;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.ArrayList;
import java.util.List;

@Data
public class SubnetWebObject extends CustomerResource {

    @JsonProperty("network_id")
    private String vpcId;

    @JsonProperty("cidr")
    private String cidr;

    @JsonIgnore
    private String availabilityZone;

    @JsonProperty("gateway_ip")
    private String gatewayIp;

    @JsonProperty("dhcp_enable")
    private Boolean dhcpEnable;

    @JsonIgnore
    private String primaryDns;

    @JsonIgnore
    private String secondaryDns;

    @JsonProperty("routes")
    private List<RouteWebObject> routes;

    @JsonIgnore
    private String gatewayMacAddress;

    @JsonIgnore
    private List<String> dnsList;

    @JsonProperty("ip_version")
    private Integer ipVersion;

    @JsonIgnore
    private String ipV4RangeId;

    @JsonIgnore
    private String ipV6RangeId;

    @JsonProperty("ipv6_address_mode")
    private String ipv6AddressMode;

    @JsonProperty("ipv6_ra_mode")
    private String ipv6RaMode;

    @JsonProperty("revision_number")
    private Integer revisionNumber;

    @JsonProperty("segment_id")
    private String segmentId;

    @JsonIgnore
    private Boolean shared;

    @JsonIgnore
    private String sortDir;

    @JsonIgnore
    private String sortKey;

    @JsonProperty("tenant_id")
    private String tenantId;

    @JsonProperty("subnetpool_id")
    private String subnetpoolId;

    @JsonProperty("dns_publish_fixed_ip")
    private boolean dnsPublishFixedIp;

    @JsonProperty("tags")
    private List<String> tags;

    @JsonIgnore
    private String tagsAny;

    @JsonIgnore
    private String notTags;

    @JsonIgnore
    private String notTagsAny;

    @JsonIgnore
    private String fields;

    @JsonProperty("dns_nameservers")
    private List<String> dnsNameservers;

    @JsonProperty("allocation_pools")
    private List<AllocationPool> allocationPools;

    @JsonProperty("host_routes")
    private List<HostRoute> hostRoutes;

    @JsonIgnore
    private Integer prefixlen;

    @JsonIgnore
    private boolean useDefaultSubnetpool;

    @JsonProperty("service_types")
    private List<String> serviceTypes;

    @CreatedDate
    @JsonProperty("created_at")
    private String created_at;

    @LastModifiedDate
    @JsonProperty("updated_at")
    private String updated_at;

    public SubnetWebObject() {
    }

    public SubnetWebObject(String projectId, String vpcId, String id, String name, String cidr) {
        this(projectId, vpcId, id, name, cidr, null, null, null, false, null, null, null, null, null, null, null);
    }

    public SubnetWebObject(String projectId, String vpcId, String id, String name, String cidr, String gatewayIp, String gatewayMacAddress) {
        this(projectId, vpcId, id, name, cidr, null, null, gatewayIp, false, null, null, null, null, gatewayMacAddress, null, null);
    }

    public SubnetWebObject(String projectId, String vpcId, String id, String name, String cidr, List<RouteWebObject> routes) {
        this(projectId, vpcId, id, name, cidr, null, null, null, false, null, null, routes, null, null, null, null);
    }

    public SubnetWebObject(String projectId, String vpcId, String id, String name, String cidr, String gatewayIp) {
        this(projectId, vpcId, id, name, cidr, null, null, gatewayIp, false, null, null, null, null, null, null, null);
    }

    public SubnetWebObject(SubnetWebObject state) {
        this(state.getProjectId(), state.getVpcId(), state.getId(), state.getName(), state.getCidr(), state.getDescription(),
                state.getAvailabilityZone(), state.getGatewayIp(), state.getDhcpEnable(), state.getPrimaryDns(), state.getSecondaryDns(), state.getRoutes(), state.getDnsList(), state.getGatewayMacAddress(), state.getIpV4RangeId(), state.getIpV6RangeId());
    }

    public SubnetWebObject(String projectId, String vpcId, String id, String name, String cidr, String description, String availabilityZone,
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
}
