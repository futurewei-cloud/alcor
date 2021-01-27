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
import com.futurewei.alcor.web.entity.port.PortEntity;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.List;

@Data
public class SubnetEntity extends CustomerResource {

    @JsonProperty("network_id")
    private String vpcId;

    @JsonProperty("cidr")
    private String cidr;

    @JsonProperty("availability_zone")
    private String availabilityZone;

    // TODO: considering to put into port
    @JsonProperty("gateway_ip")
    private String gatewayIp = "";

    // subnet_gateway_port_id
    @JsonProperty("gatewayPortId")
    private String gatewayPortId;

    @JsonProperty("gateway_port_detail")
    private GatewayPortDetail gatewayPortDetail;

    @JsonProperty("attached_router_id")
    private String attachedRouterId;

    @JsonProperty("port_detail")
    private PortEntity port;

    @JsonProperty("enable_dhcp")
    private Boolean dhcpEnable;

    @JsonProperty("primary_dns")
    private String primaryDns;

    @JsonProperty("secondary_dns")
    private String secondaryDns;

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

    public SubnetEntity() {
    }

    public SubnetEntity(String projectId, String vpcId, String id, String name, String cidr) {
        super(projectId, id, name, null);
        this.vpcId = vpcId;
        this.cidr = cidr;
    }

    public SubnetEntity(String projectId, String id, String name, String description, String vpcId,
                        String cidr, String availabilityZone, String gatewayIp, Boolean dhcpEnable, String primaryDns,
                        String secondaryDns, GatewayPortDetail gatewayPortDetail, List<String> dnsList,
                        Integer ipVersion, String ipV4RangeId, String ipV6RangeId, String ipv6AddressMode, String ipv6RaMode,
                        Integer revisionNumber, String segmentId, Boolean shared, String sortDir, String sortKey,
                        String subnetpoolId, boolean dnsPublishFixedIp, List<String> tags, String tagsAny,
                        String notTags, String notTagsAny, String fields, List<String> dnsNameservers, List<AllocationPool> allocationPools,
                        List<HostRoute> hostRoutes, Integer prefixlen, boolean useDefaultSubnetpool, List<String> serviceTypes, String created_at,
                        String updated_at) {
        super(projectId, id, name, description);
        this.vpcId = vpcId;
        this.cidr = cidr;
        this.availabilityZone = availabilityZone;
        this.gatewayIp = gatewayIp;
        this.dhcpEnable = dhcpEnable;
        this.primaryDns = primaryDns;
        this.secondaryDns = secondaryDns;
        this.gatewayPortDetail = gatewayPortDetail;
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

    public SubnetEntity(SubnetEntity subnetEntity) {
        this(subnetEntity.getProjectId(), subnetEntity.getId(), subnetEntity.getName(), subnetEntity.getDescription(), subnetEntity.getVpcId(),
                subnetEntity.getCidr(), subnetEntity.getAvailabilityZone(), subnetEntity.getGatewayIp(), subnetEntity.getDhcpEnable(), subnetEntity.getPrimaryDns(),
                subnetEntity.getSecondaryDns(), subnetEntity.getGatewayPortDetail(), subnetEntity.getDnsList(),
                subnetEntity.getIpVersion(), subnetEntity.getIpV4RangeId(), subnetEntity.getIpV6RangeId(), subnetEntity.getIpv6AddressMode(), subnetEntity.getIpv6RaMode(),
                subnetEntity.getRevisionNumber(), subnetEntity.getSegmentId(), subnetEntity.getShared(), subnetEntity.getSortDir(), subnetEntity.getSortKey(),
                subnetEntity.getSubnetpoolId(), subnetEntity.dnsPublishFixedIp, subnetEntity.getTags(), subnetEntity.getTagsAny(),
                subnetEntity.getNotTags(), subnetEntity.getNotTagsAny(), subnetEntity.getFields(), subnetEntity.getDnsNameservers(), subnetEntity.getAllocationPools(),
                subnetEntity.getHostRoutes(), subnetEntity.getPrefixlen(), subnetEntity.useDefaultSubnetpool, subnetEntity.getServiceTypes(), subnetEntity.getCreated_at(),
                subnetEntity.getUpdated_at());
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

    public GatewayPortDetail getGatewayPortDetail() {
        return gatewayPortDetail;
    }

    public void setGatewayPortDetail(GatewayPortDetail gatewayPortDetail) {
        this.gatewayPortDetail = gatewayPortDetail;
    }

    public String getAttachedRouterId() {
        return attachedRouterId;
    }

    public void setAttachedRouterId(String attachedRouterId) {
        this.attachedRouterId = attachedRouterId;
    }

    public PortEntity getPort() {
        return port;
    }

    public void setPort(PortEntity port) {
        this.port = port;
    }

    public List<String> getDnsList() {
        return dnsList;
    }

    public void setDnsList(List<String> dnsList) {
        this.dnsList = dnsList;
    }

    public Integer getIpVersion() {
        return ipVersion;
    }

    public void setIpVersion(Integer ipVersion) {
        this.ipVersion = ipVersion;
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

    public String getIpv6AddressMode() {
        return ipv6AddressMode;
    }

    public void setIpv6AddressMode(String ipv6AddressMode) {
        this.ipv6AddressMode = ipv6AddressMode;
    }

    public String getIpv6RaMode() {
        return ipv6RaMode;
    }

    public void setIpv6RaMode(String ipv6RaMode) {
        this.ipv6RaMode = ipv6RaMode;
    }

    public Integer getRevisionNumber() {
        return revisionNumber;
    }

    public void setRevisionNumber(Integer revisionNumber) {
        this.revisionNumber = revisionNumber;
    }

    public String getSegmentId() {
        return segmentId;
    }

    public void setSegmentId(String segmentId) {
        this.segmentId = segmentId;
    }

    public Boolean getShared() {
        return shared;
    }

    public void setShared(Boolean shared) {
        this.shared = shared;
    }

    public String getSortDir() {
        return sortDir;
    }

    public void setSortDir(String sortDir) {
        this.sortDir = sortDir;
    }

    public String getSortKey() {
        return sortKey;
    }

    public void setSortKey(String sortKey) {
        this.sortKey = sortKey;
    }

    public String getSubnetpoolId() {
        return subnetpoolId;
    }

    public void setSubnetpoolId(String subnetpoolId) {
        this.subnetpoolId = subnetpoolId;
    }

    public boolean isDnsPublishFixedIp() {
        return dnsPublishFixedIp;
    }

    public void setDnsPublishFixedIp(boolean dnsPublishFixedIp) {
        this.dnsPublishFixedIp = dnsPublishFixedIp;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getTagsAny() {
        return tagsAny;
    }

    public void setTagsAny(String tagsAny) {
        this.tagsAny = tagsAny;
    }

    public String getNotTags() {
        return notTags;
    }

    public void setNotTags(String notTags) {
        this.notTags = notTags;
    }

    public String getNotTagsAny() {
        return notTagsAny;
    }

    public void setNotTagsAny(String notTagsAny) {
        this.notTagsAny = notTagsAny;
    }

    public String getFields() {
        return fields;
    }

    public void setFields(String fields) {
        this.fields = fields;
    }

    public List<String> getDnsNameservers() {
        return dnsNameservers;
    }

    public void setDnsNameservers(List<String> dnsNameservers) {
        this.dnsNameservers = dnsNameservers;
    }

    public List<AllocationPool> getAllocationPools() {
        return allocationPools;
    }

    public void setAllocationPools(List<AllocationPool> allocationPools) {
        this.allocationPools = allocationPools;
    }

    public List<HostRoute> getHostRoutes() {
        return hostRoutes;
    }

    public void setHostRoutes(List<HostRoute> hostRoutes) {
        this.hostRoutes = hostRoutes;
    }

    public Integer getPrefixlen() {
        return prefixlen;
    }

    public void setPrefixlen(Integer prefixlen) {
        this.prefixlen = prefixlen;
    }

    public boolean isUseDefaultSubnetpool() {
        return useDefaultSubnetpool;
    }

    public void setUseDefaultSubnetpool(boolean useDefaultSubnetpool) {
        this.useDefaultSubnetpool = useDefaultSubnetpool;
    }

    public List<String> getServiceTypes() {
        return serviceTypes;
    }

    public void setServiceTypes(List<String> serviceTypes) {
        this.serviceTypes = serviceTypes;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }
}
