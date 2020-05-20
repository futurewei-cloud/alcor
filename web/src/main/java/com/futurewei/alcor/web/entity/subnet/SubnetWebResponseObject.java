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
import com.futurewei.alcor.web.entity.route.RouteWebObject;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.ArrayList;
import java.util.List;

@Data
public class SubnetWebResponseObject extends CustomerResource {

    @JsonProperty("network_id")
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

    @JsonProperty("gateway_macAddress")
    private String gatewayMacAddress;

    @JsonProperty("dns_list")
    private List<String> dnsList;

    @JsonProperty("ip_version")
    private Integer ipVersion;

    @JsonProperty("ipV4_rangeId")
    private String ipV4RangeId;

    @JsonProperty("ipV6_rangeId")
    private String ipV6RangeId;

    @JsonProperty("ipv6_address_mode")
    private String ipv6AddressMode;

    @JsonProperty("ipv6_ra_mode")
    private String ipv6RaMode;

    @JsonProperty("revision_number")
    private Integer revisionNumber;

    @JsonProperty("segment_id")
    private String segmentId;

    @JsonProperty("shared")
    private Boolean shared;

    @JsonProperty("sort_dir")
    private String sortDir;

    @JsonProperty("sort_key")
    private String sortKey;

    @JsonProperty("tenant_id")
    private String tenantId;

    @JsonProperty("subnetpool_id")
    private String subnetpoolId;

    @JsonProperty("dns_publish_fixed_ip")
    private boolean dnsPublishFixedIp;

    @JsonProperty("tags")
    private List<String> tags;

    @JsonProperty("tags-any")
    private String tagsAny;

    @JsonProperty("not-tags")
    private String notTags;

    @JsonProperty("not-tags-any")
    private String notTagsAny;

    @JsonProperty("fields")
    private String fields;

    @JsonProperty("dns_nameservers")
    private List<String> dnsNameservers;

    @JsonProperty("allocation_pools")
    private List<AllocationPool> allocationPools;

    @JsonProperty("host_routes")
    private List<HostRoute> hostRoutes;

    @JsonProperty("prefixlen")
    private Integer prefixlen;

    @JsonProperty("use_default_subnet_pool")
    private boolean useDefaultSubnetpool;

    @JsonProperty("service_types")
    private List<String> serviceTypes;

    @CreatedDate
    @JsonProperty("created_at")
    private String created_at;

    @LastModifiedDate
    @JsonProperty("updated_at")
    private String updated_at;

    public SubnetWebResponseObject() {
    }

    public SubnetWebResponseObject(String projectId, String vpcId, String id, String name, String cidr) {
        super(projectId, id, name, null);
        this.vpcId = vpcId;
        this.cidr = cidr;
    }

    public SubnetWebResponseObject(String projectId, String vpcId, String id, String name, String cidr, List<RouteWebObject> routes) {
        super(projectId, id, name, null);
        this.vpcId = vpcId;
        this.cidr = cidr;
        this.routes = routes;
    }

    public SubnetWebResponseObject(String projectId, String id, String name, String description, String vpcId, String cidr, String availabilityZone, String gatewayIp, Boolean dhcpEnable, String primaryDns, String secondaryDns, List<RouteWebObject> routes, String gatewayMacAddress, List<String> dnsList, Integer ipVersion, String ipV4RangeId, String ipV6RangeId, String ipv6AddressMode, String ipv6RaMode, Integer revisionNumber, String segmentId, Boolean shared, String sortDir, String sortKey, String tenantId, String subnetpoolId, boolean dnsPublishFixedIp, List<String> tags, String tagsAny, String notTags, String notTagsAny, String fields, List<String> dnsNameservers, List<AllocationPool> allocationPools, List<HostRoute> hostRoutes, Integer prefixlen, boolean useDefaultSubnetpool, List<String> serviceTypes, String created_at, String updated_at) {
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
        this.dnsList = dnsList;
        this.ipVersion = ipVersion;
        this.ipV4RangeId = ipV4RangeId;
        this.ipV6RangeId = ipV6RangeId;
        this.ipv6AddressMode = ipv6AddressMode;
        this.ipv6RaMode = ipv6RaMode;
        this.revisionNumber = revisionNumber;
        this.segmentId = segmentId;
        this.shared = shared;
        this.sortDir = sortDir;
        this.sortKey = sortKey;
        this.tenantId = tenantId;
        this.subnetpoolId = subnetpoolId;
        this.dnsPublishFixedIp = dnsPublishFixedIp;
        this.tags = tags;
        this.tagsAny = tagsAny;
        this.notTags = notTags;
        this.notTagsAny = notTagsAny;
        this.fields = fields;
        this.dnsNameservers = dnsNameservers;
        this.allocationPools = allocationPools;
        this.hostRoutes = hostRoutes;
        this.prefixlen = prefixlen;
        this.useDefaultSubnetpool = useDefaultSubnetpool;
        this.serviceTypes = serviceTypes;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

}
