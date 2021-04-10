/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/


package com.futurewei.alcor.web.entity.vpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.common.entity.CustomerResource;
import com.futurewei.alcor.web.entity.route.RouteEntity;
import com.futurewei.alcor.web.entity.vpc.SegmentInfoInVpc;
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
    private boolean portSecurityEnabled = true;

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
}


