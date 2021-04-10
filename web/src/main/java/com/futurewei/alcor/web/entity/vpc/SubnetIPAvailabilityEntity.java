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
import lombok.Data;

@Data
public class SubnetIPAvailabilityEntity {

    @JsonProperty("total_ips")
    private Integer totalIps;

    @JsonProperty("used_ips")
    private Integer usedIps;

    @JsonProperty("subnet_id")
    private String subnetId;

    @JsonProperty("subnet_name")
    private String subnetName;

    @JsonProperty("ip_version")
    private Integer ipVersion;

    @JsonProperty("cidr")
    private String cidr;

    public SubnetIPAvailabilityEntity() {}

    public SubnetIPAvailabilityEntity(Integer totalIps, Integer usedIps, String subnetId, String subnetName, Integer ipVersion, String cidr) {
        this.totalIps = totalIps;
        this.usedIps = usedIps;
        this.subnetId = subnetId;
        this.subnetName = subnetName;
        this.ipVersion = ipVersion;
        this.cidr = cidr;
    }
}
