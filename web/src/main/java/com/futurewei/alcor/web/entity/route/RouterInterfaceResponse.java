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
import lombok.Data;

import java.util.List;

@Data
public class RouterInterfaceResponse {

    @JsonProperty("id")
    private String routerId;

    @JsonProperty("network_id")
    private String vpcId;

    @JsonProperty("port_id")
    private String portId;

    @JsonProperty("subnet_id")
    private String subnetId;

    @JsonProperty("subnet_ids")
    private List<String> subnetIds;

    @JsonProperty("project_id")
    private String projectId;

    @JsonProperty("tenant_id")
    private String tenantId;

    @JsonProperty("tags")
    private List<String> tags;

    public RouterInterfaceResponse () {}

    public RouterInterfaceResponse(String routerId, String vpcId, String portId, String subnetId, List<String> subnetIds, String projectId, String tenantId, List<String> tags) {
        this.routerId = routerId;
        this.vpcId = vpcId;
        this.portId = portId;
        this.subnetId = subnetId;
        this.subnetIds = subnetIds;
        this.projectId = projectId;
        this.tenantId = tenantId;
        this.tags = tags;
    }
}
