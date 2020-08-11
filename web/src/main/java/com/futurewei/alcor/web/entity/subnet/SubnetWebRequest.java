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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.common.entity.CustomerResource;
import com.futurewei.alcor.web.entity.route.RouteEntity;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.List;

@Data
public class SubnetWebRequest extends CustomerResource {

    @JsonProperty("network_id")
    private String vpcId;

    @JsonProperty("cidr")
    private String cidr;

    @JsonProperty("gateway_ip")
    private String gatewayIp;

    @JsonProperty("enable_dhcp")
    private Boolean dhcpEnable;

    @JsonProperty("routes")
    private List<RouteEntity> routeEntities;

    @JsonProperty("ip_version")
    private Integer ipVersion;

    @JsonProperty("ipv6_address_mode")
    private String ipv6AddressMode;

    @JsonProperty("ipv6_ra_mode")
    private String ipv6RaMode;

    @JsonProperty("revision_number")
    private Integer revisionNumber;

    @JsonProperty("segment_id")
    private String segmentId;

    @JsonProperty("tenant_id")
    private String tenantId;

    @JsonProperty("subnetpool_id")
    private String subnetpoolId;

    @JsonProperty("dns_publish_fixed_ip")
    private boolean dnsPublishFixedIp;

    @JsonProperty("tags")
    private List<String> tags;

    @JsonProperty("dns_nameservers")
    private List<String> dnsNameservers;

    @JsonProperty("allocation_pools")
    private List<AllocationPool> allocationPools;

    @JsonProperty("host_routes")
    private List<HostRoute> hostRoutes;

    @JsonProperty("service_types")
    private List<String> serviceTypes;

    @CreatedDate
    @JsonProperty("created_at")
    private String created_at;

    @LastModifiedDate
    @JsonProperty("updated_at")
    private String updated_at;

    public SubnetWebRequest(String projectId, String id, String name, String description, String vpcId, String cidr, String gatewayIp, Boolean dhcpEnable, List<RouteEntity> routeEntities, Integer ipVersion, String ipv6AddressMode, String ipv6RaMode, Integer revisionNumber, String segmentId, String tenantId, String subnetpoolId, boolean dnsPublishFixedIp, List<String> tags, List<String> dnsNameservers, List<AllocationPool> allocationPools, List<HostRoute> hostRoutes, List<String> serviceTypes, String created_at, String updated_at) {
        super(projectId, id, name, description);
        this.vpcId = vpcId;
        this.cidr = cidr;
        this.gatewayIp = gatewayIp;
        this.dhcpEnable = dhcpEnable;
        this.routeEntities = routeEntities;
        this.ipVersion = ipVersion;
        this.ipv6AddressMode = ipv6AddressMode;
        this.ipv6RaMode = ipv6RaMode;
        this.revisionNumber = revisionNumber;
        this.segmentId = segmentId;
        this.tenantId = tenantId;
        this.subnetpoolId = subnetpoolId;
        this.dnsPublishFixedIp = dnsPublishFixedIp;
        this.tags = tags;
        this.dnsNameservers = dnsNameservers;
        this.allocationPools = allocationPools;
        this.hostRoutes = hostRoutes;
        this.serviceTypes = serviceTypes;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }
}
