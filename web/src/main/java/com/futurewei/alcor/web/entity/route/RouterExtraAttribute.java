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

package com.futurewei.alcor.web.entity.route;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.common.entity.CustomerResource;
import lombok.Data;

import java.util.List;

@Data
public class RouterExtraAttribute extends CustomerResource {

    @JsonProperty("external_gateway_info")
    private ExternalGateway external_gateway_info;

    @JsonProperty("revision_number")
    private Integer revisionNumber;

    @JsonProperty("distributed")
    private boolean distributed;

    @JsonProperty("ha")
    private boolean ha;

    @JsonProperty("availability_zone_hints")
    private List<String> availabilityZoneHints;

    @JsonProperty("availability_zones")
    private List<String> availabilityZones;

    @JsonProperty("service_type_id")
    private String serviceTypeId;

    @JsonProperty("flavor_id")
    private String flavorId;

    @JsonProperty("tags")
    private List<String> tags;

    @JsonProperty("conntrack_helpers")
    private List<String> conntrackHelpers;

    public RouterExtraAttribute () {}

    public RouterExtraAttribute(String projectId, String id, String name, String description, ExternalGateway external_gateway_info, Integer revisionNumber, boolean distributed, boolean ha, List<String> availabilityZoneHints, List<String> availabilityZones, String serviceTypeId, String flavorId, List<String> tags, List<String> conntrackHelpers) {
        super(projectId, id, name, description);
        this.external_gateway_info = external_gateway_info;
        this.revisionNumber = revisionNumber;
        this.distributed = distributed;
        this.ha = ha;
        this.availabilityZoneHints = availabilityZoneHints;
        this.availabilityZones = availabilityZones;
        this.serviceTypeId = serviceTypeId;
        this.flavorId = flavorId;
        this.tags = tags;
        this.conntrackHelpers = conntrackHelpers;
    }
}
