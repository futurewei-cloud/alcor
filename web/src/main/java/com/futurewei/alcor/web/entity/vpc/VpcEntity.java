package com.futurewei.alcor.web.entity.vpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.common.entity.CustomerResource;
import com.futurewei.alcor.web.entity.route.RouteEntity;
import com.futurewei.alcor.web.entity.route.Router;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.List;

@Data
public class VpcEntity extends CustomerResource {

    @JsonProperty("cidr")
    private String cidr;

    @JsonProperty("routes")
    private List<RouteEntity> routeEntities;

    @JsonProperty("router")
    private Router router;

    @JsonProperty("admin_state_up")
    @SerializedName("admin_state_up")
    private boolean adminStateUp;

    @JsonProperty("dns_domain")
    @SerializedName("dns_domain")
    private String dnsDomain;

    @JsonProperty("mtu")
    private Integer mtu;

    @JsonProperty("port_security_enabled")
    @SerializedName("port_security_enabled")
    private boolean portSecurityEnabled;

    @JsonProperty("provider:network_type")
    @SerializedName("provider:network_type")
    private String networkType;

    @JsonProperty("provider:physical_network")
    @SerializedName("provider:physical_network")
    private String physicalNetwork;

    @JsonProperty("provider:segmentation_id")
    @SerializedName("provider:segmentation_id")
    private Integer segmentationId;

    @JsonProperty("router:external")
    @SerializedName("router:external")
    private boolean routerExternal;

    @JsonProperty("segments")
    private List<SegmentInfoInVpc> segments;

    @JsonProperty("shared")
    private boolean shared;

    @JsonProperty("vlan_transparent")
    @SerializedName("vlan_transparent")
    private boolean vlanTransparent;

    @JsonProperty("is_default")
    @SerializedName("is_default")
    private boolean isDefault;

    @JsonProperty("availability_zone_hints")
    @SerializedName("availability_zone_hints")
    private List<String> availabilityZoneHints;

    @JsonProperty("availability_zones")
    @SerializedName("availability_zones")
    private List<String> availabilityZones;

    @JsonProperty("qos_policy_id")
    @SerializedName("qos_policy_id")
    private List qosPolicyId;

    @JsonProperty("revision_number")
    @SerializedName("revision_number")
    private Integer revisionNumber;

    @JsonProperty("status")
    private String status;

    @JsonProperty("tags")
    private List<String> tags;

    @CreatedDate
    @JsonProperty("created_at")
    private String created_at;

    @LastModifiedDate
    @JsonProperty("updated_at")
    private String updated_at;

    @JsonProperty("ipv4_address_scope")
    @SerializedName("ipv4_address_scope")
    private String ipv4AddressScope;

    @JsonProperty("ipv6_address_scope")
    @SerializedName("ipv6_address_scope")
    private String ipv6AddressScope;

    @JsonProperty("l2_adjacency")
    @SerializedName("l2_adjacency")
    private String l2Adjacency;

    @JsonProperty("subnets")
    private List<String> subnets;

    public VpcEntity() {}

    public VpcEntity(String projectId, String id, String name, String cidr, List<RouteEntity> routeEntities) {
        super(projectId, id, name, cidr);
        this.routeEntities = routeEntities;
    }

    public VpcEntity(String projectId, String id, String name, String description, List<RouteEntity> routeEntities, boolean adminStateUp, String dnsDomain, Integer mtu, boolean portSecurityEnabled, String networkType, String physicalNetwork, Integer segmentationId, boolean routerExternal, List<SegmentInfoInVpc> segments, boolean shared, boolean vlanTransparent, boolean isDefault, List availabilityZoneHints, List availabilityZones, List qosPolicyId, Integer revisionNumber, String status, List<String> tags, String created_at, String updated_at, String ipv4AddressScope, String ipv6AddressScope, String l2Adjacency, List<String> subnets, String cidr) {
        super(projectId, id, name, description);
        this.routeEntities = routeEntities;
        this.adminStateUp = adminStateUp;
        this.dnsDomain = dnsDomain;
        this.mtu = mtu;
        this.portSecurityEnabled = portSecurityEnabled;
        this.networkType = networkType;
        this.physicalNetwork = physicalNetwork;
        this.segmentationId = segmentationId;
        this.routerExternal = routerExternal;
        this.segments = segments;
        this.shared = shared;
        this.vlanTransparent = vlanTransparent;
        this.isDefault = isDefault;
        this.availabilityZoneHints = availabilityZoneHints;
        this.availabilityZones = availabilityZones;
        this.qosPolicyId = qosPolicyId;
        this.revisionNumber = revisionNumber;
        this.status = status;
        this.tags = tags;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.ipv4AddressScope = ipv4AddressScope;
        this.ipv6AddressScope = ipv6AddressScope;
        this.l2Adjacency = l2Adjacency;
        this.subnets = subnets;
        this.cidr = cidr;
    }

    public String getCidr() {
        return cidr;
    }

    public void setCidr(String cidr) {
        this.cidr = cidr;
    }

    public List<RouteEntity> getRouteEntities() {
        return routeEntities;
    }

    public void setRouteEntities(List<RouteEntity> routeEntities) {
        this.routeEntities = routeEntities;
    }

    public boolean isAdminStateUp() {
        return adminStateUp;
    }

    public void setAdminStateUp(boolean adminStateUp) {
        this.adminStateUp = adminStateUp;
    }

    public String getDnsDomain() {
        return dnsDomain;
    }

    public void setDnsDomain(String dnsDomain) {
        this.dnsDomain = dnsDomain;
    }

    public Integer getMtu() {
        return mtu;
    }

    public void setMtu(Integer mtu) {
        this.mtu = mtu;
    }

    public boolean isPortSecurityEnabled() {
        return portSecurityEnabled;
    }

    public void setPortSecurityEnabled(boolean portSecurityEnabled) {
        this.portSecurityEnabled = portSecurityEnabled;
    }

    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public String getPhysicalNetwork() {
        return physicalNetwork;
    }

    public void setPhysicalNetwork(String physicalNetwork) {
        this.physicalNetwork = physicalNetwork;
    }

    public Integer getSegmentationId() {
        return segmentationId;
    }

    public void setSegmentationId(Integer segmentationId) {
        this.segmentationId = segmentationId;
    }

    public boolean isRouterExternal() {
        return routerExternal;
    }

    public void setRouterExternal(boolean routerExternal) {
        this.routerExternal = routerExternal;
    }

    public List<SegmentInfoInVpc> getSegments() {
        return segments;
    }

    public void setSegments(List<SegmentInfoInVpc> segments) {
        this.segments = segments;
    }

    public boolean isShared() {
        return shared;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }

    public boolean isVlanTransparent() {
        return vlanTransparent;
    }

    public void setVlanTransparent(boolean vlanTransparent) {
        this.vlanTransparent = vlanTransparent;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public List<String> getAvailabilityZoneHints() {
        return availabilityZoneHints;
    }

    public void setAvailabilityZoneHints(List<String> availabilityZoneHints) {
        this.availabilityZoneHints = availabilityZoneHints;
    }

    public List<String> getAvailabilityZones() {
        return availabilityZones;
    }

    public void setAvailabilityZones(List<String> availabilityZones) {
        this.availabilityZones = availabilityZones;
    }

    public List getQosPolicyId() {
        return qosPolicyId;
    }

    public void setQosPolicyId(List qosPolicyId) {
        this.qosPolicyId = qosPolicyId;
    }

    public Integer getRevisionNumber() {
        return revisionNumber;
    }

    public void setRevisionNumber(Integer revisionNumber) {
        this.revisionNumber = revisionNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
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

    public String getIpv4AddressScope() {
        return ipv4AddressScope;
    }

    public void setIpv4AddressScope(String ipv4AddressScope) {
        this.ipv4AddressScope = ipv4AddressScope;
    }

    public String getIpv6AddressScope() {
        return ipv6AddressScope;
    }

    public void setIpv6AddressScope(String ipv6AddressScope) {
        this.ipv6AddressScope = ipv6AddressScope;
    }

    public String getL2Adjacency() {
        return l2Adjacency;
    }

    public void setL2Adjacency(String l2Adjacency) {
        this.l2Adjacency = l2Adjacency;
    }

    public List<String> getSubnets() {
        return subnets;
    }

    public void setSubnets(List<String> subnets) {
        this.subnets = subnets;
    }
}
