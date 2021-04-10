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
package com.futurewei.alcor.web.entity.route;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.common.entity.CustomerResource;
import lombok.Data;

import java.util.List;

@Data
public class RouterWebRequestObject extends CustomerResource {

    @JsonProperty("tenant_id")
    private String tenantId;

    @JsonProperty("admin_state_up")
    private boolean adminStateUp;

    @JsonProperty("external_gateway_info")
    private ExternalGateway external_gateway_info;

    @JsonProperty("distributed")
    private boolean distributed;

    @JsonProperty("ha")
    private boolean ha;

    @JsonProperty("availability_zone_hints")
    private List<String> availabilityZoneHints;

    @JsonProperty("service_type_id")
    private String serviceTypeId;

    @JsonProperty("flavor_id")
    private String flavorId;

    public RouterWebRequestObject () {}

    public RouterWebRequestObject(String projectId, String id, String name, String description) {
        super(projectId, id, name, description);
    }

    public RouterWebRequestObject(String projectId, String id, String name, String description, String tenantId, boolean adminStateUp, ExternalGateway external_gateway_info, boolean distributed, boolean ha, List<String> availabilityZoneHints, String serviceTypeId, String flavorId) {
        super(projectId, id, name, description);
        this.tenantId = tenantId;
        this.adminStateUp = adminStateUp;
        this.external_gateway_info = external_gateway_info;
        this.distributed = distributed;
        this.ha = ha;
        this.availabilityZoneHints = availabilityZoneHints;
        this.serviceTypeId = serviceTypeId;
        this.flavorId = flavorId;
    }
}
