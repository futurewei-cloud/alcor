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

package com.futurewei.alcor.web.entity.vpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.common.entity.CustomerResource;
import com.futurewei.alcor.web.entity.route.RouteEntity;
import com.futurewei.alcor.web.entity.SegmentInfoInVpc;
import lombok.Data;

import java.util.List;

@Data
public class VpcWebRequestObject extends CustomerResource {

    @JsonProperty("cidr")
    private String cidr;

    @JsonProperty("routes")
    private List<RouteEntity> routeEntities;

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
    private List availabilityZoneHints;

    @JsonProperty("revision_number")
    private Integer revisionNumber;

    @JsonProperty("status")
    private String status;

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

    @JsonProperty("sort_dir")
    private String sortDir;

    @JsonProperty("sort_key")
    private String sortKey;

    public VpcWebRequestObject() {
    }

    public VpcWebRequestObject(String projectId, String id, String name, List<RouteEntity> routeEntityList) {
        this(projectId, id, name, null, routeEntityList);
    }

    public VpcWebRequestObject(VpcWebRequestObject state) {
        this(state.getProjectId(), state.getId(), state.getName(), state.getDescription(), state.getRouteEntities());
    }

    public VpcWebRequestObject(String projectId, String id, String name, String description, List<RouteEntity> routeEntityList) {

        super(projectId, id, name, description);
        this.routeEntities = routeEntityList;
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

    @Override
    public String getTenantId() {
        return tenantId;
    }

    @Override
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
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

    public List getAvailabilityZoneHints() {
        return availabilityZoneHints;
    }

    public void setAvailabilityZoneHints(List availabilityZoneHints) {
        this.availabilityZoneHints = availabilityZoneHints;
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
}


