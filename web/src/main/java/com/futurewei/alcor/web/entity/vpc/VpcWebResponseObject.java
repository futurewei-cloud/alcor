package com.futurewei.alcor.web.entity.vpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.common.entity.CustomerResource;
import com.futurewei.alcor.web.entity.route.RouteWebObject;
import com.futurewei.alcor.web.entity.SegmentInfoInVpc;
import com.futurewei.alcor.web.entity.subnet.SubnetWebObject;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.List;

@Data
public class VpcWebResponseObject extends CustomerResource {

    @JsonProperty("cidr")
    private String cidr;

    @JsonProperty("routes")
    private List<RouteWebObject> routes;

    @JsonProperty("admin_state_up")
    private boolean adminStateUp;

    @JsonProperty("dns_domain")
    private String dnsDomain;

    @JsonProperty("mtu")
    private Integer mtu;

    @JsonProperty("port_security_enabled")
    private boolean portSecurityEnabled;

    @JsonProperty("provider:network_type")
    private String networkType;

    @JsonProperty("provider:physical_network")
    private String physicalNetwork;

    @JsonProperty("provider:segmentation_id")
    private Integer segmentationId;

    @JsonProperty("router:external")
    private boolean routerExternal;

    @JsonProperty("segments")
    private List<SegmentInfoInVpc> segments;

    @JsonProperty("shared")
    private boolean shared;

    @JsonProperty("tenant_id")
    private String tenantId;

    @JsonProperty("vlan_transparent")
    private boolean vlanTransparent;

    @JsonProperty("is_default")
    private boolean isDefault;

    @JsonProperty("availability_zone_hints")
    private List<String> availabilityZoneHints;

    @JsonProperty("availability_zones")
    private List<String> availabilityZones;

    @JsonProperty("qos_policy_id")
    private List qosPolicyId;

    @JsonProperty("revision_number")
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
    private String ipv4AddressScope;

    @JsonProperty("ipv6_address_scope")
    private String ipv6AddressScope;

    @JsonProperty("l2_adjacency")
    private String l2Adjacency;

    @JsonProperty("subnets")
    private List<SubnetWebObject> subnets;

    public VpcWebResponseObject () {}

    public VpcWebResponseObject (String projectId, String id, String name, String cidr, List<RouteWebObject> routes) {
        super(projectId, id, name, cidr);
        this.routes = routes;
    }

    public VpcWebResponseObject(String projectId, String id, String name, String description, List<RouteWebObject> routes, boolean adminStateUp, String dnsDomain, Integer mtu, boolean portSecurityEnabled, String networkType, String physicalNetwork, Integer segmentationId, boolean routerExternal, List<SegmentInfoInVpc> segments, boolean shared, String tenantId, boolean vlanTransparent, boolean isDefault, List availabilityZoneHints, List availabilityZones, List qosPolicyId, Integer revisionNumber, String status, List<String> tags, String created_at, String updated_at, String ipv4AddressScope, String ipv6AddressScope, String l2Adjacency, List<SubnetWebObject> subnets, String cidr) {
        super(projectId, id, name, description);
        this.routes = routes;
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
        this.tenantId = tenantId;
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
}
