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
package com.futurewei.alcor.web.entity.vpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class NetworkIPAvailabilityEntity {

    @JsonProperty("network_id")
    private String vpcId;

    @JsonProperty("network_name")
    private String vpcName;

    @JsonProperty("tenant_id")
    private String tenantId;

    @JsonProperty("project_id")
    private String projectId;

    @JsonProperty("total_ips")
    private Integer totalIps;

    @JsonProperty("used_ips")
    private Integer usedIps;

    @JsonProperty("subnet_ip_availability")
    private List<SubnetIPAvailabilityEntity> subnetIpAvailability;

    public NetworkIPAvailabilityEntity () {}

    public NetworkIPAvailabilityEntity (String vpcId, String vpcName, String tenantId, String projectId, Integer totalIps, Integer usedIps, List<SubnetIPAvailabilityEntity> subnetIpAvailabilityEntity) {
        this.vpcId = vpcId;
        this.vpcName = vpcName;
        this.tenantId = tenantId;
        this.projectId = projectId;
        this.totalIps = totalIps;
        this.usedIps = usedIps;
        this.subnetIpAvailability = subnetIpAvailabilityEntity;
    }
}
