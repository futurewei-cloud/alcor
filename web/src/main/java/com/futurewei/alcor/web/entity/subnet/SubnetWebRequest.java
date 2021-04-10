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
package com.futurewei.alcor.web.entity.subnet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.common.entity.CustomerResource;
import com.futurewei.alcor.web.entity.port.PortEntity;
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

    @JsonProperty("attached_router_id")
    private String attachedRouterId;

    @JsonProperty("port_detail")
    private PortEntity port;

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

    public SubnetWebRequest(String projectId, String id, String name, String description, String vpcId, String cidr, String gatewayIp, String attachedRouterId, PortEntity port, Boolean dhcpEnable, List<RouteEntity> routeEntities, Integer ipVersion, String ipv6AddressMode, String ipv6RaMode, Integer revisionNumber, String segmentId, String tenantId, String subnetpoolId, boolean dnsPublishFixedIp, List<String> tags, List<String> dnsNameservers, List<AllocationPool> allocationPools, List<HostRoute> hostRoutes, List<String> serviceTypes, String created_at, String updated_at) {
        super(projectId, id, name, description);
        this.vpcId = vpcId;
        this.cidr = cidr;
        this.gatewayIp = gatewayIp;
        this.attachedRouterId = attachedRouterId;
        this.port = port;
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
