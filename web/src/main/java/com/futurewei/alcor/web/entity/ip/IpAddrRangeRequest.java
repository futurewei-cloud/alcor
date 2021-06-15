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

package com.futurewei.alcor.web.entity.ip;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IpAddrRangeRequest {
    @JsonProperty("id")
    private String id;

    @JsonProperty("vpc_id")
    private String vpcId;

    @JsonProperty("subnet_id")
    private String subnetId;

    @JsonProperty("ip_version")
    private int ipVersion;

    @JsonProperty("first_ip")
    private String firstIp;

    @JsonProperty("last_ip")
    private String lastIp;

    @JsonProperty("used_ips")
    private long usedIps;

    @JsonProperty("total_ips")
    private long totalIps;

    public IpAddrRangeRequest() {}

    public IpAddrRangeRequest(String id, String vpcId, String subnetId, int ipVersion, String firstIp, String lastIp) {
        this.id = id;
        this.vpcId = vpcId;
        this.subnetId = subnetId;
        this.ipVersion = ipVersion;
        this.firstIp = firstIp;
        this.lastIp = lastIp;
    }

    public int getIpVersion() {
        return ipVersion;
    }

    public void setIpVersion(int ipVersion) {
        this.ipVersion = ipVersion;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    public String getFirstIp() {
        return firstIp;
    }

    public void setFirstIp(String firstIp) {
        this.firstIp = firstIp;
    }

    public String getLastIp() {
        return lastIp;
    }

    public void setLastIp(String lastIp) {
        this.lastIp = lastIp;
    }

    public long getUsedIps() {
        return usedIps;
    }

    public void setUsedIps(long usedIps) {
        this.usedIps = usedIps;
    }

    public long getTotalIps() {
        return totalIps;
    }

    public void setTotalIps(long totalIps) {
        this.totalIps = totalIps;
    }

    @Override
    public String toString() {
        return "IpAddrRangeRequest{" +
                "id='" + id + '\'' +
                ", vpcId='" + vpcId + '\'' +
                ", subnetId='" + subnetId + '\'' +
                ", ipVersion=" + ipVersion +
                ", firstIp='" + firstIp + '\'' +
                ", lastIp='" + lastIp + '\'' +
                ", usedIps=" + usedIps +
                ", totalIps=" + totalIps +
                '}';
    }
}
