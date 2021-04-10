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

public class IpAddrRequest {
    @JsonProperty("ip_version")
    private int ipVersion;

    @JsonProperty("vpc_id")
    private String vpcId;

    @JsonProperty("subnet_id")
    private String subnetId;

    @JsonProperty("range_id")
    private String rangeId;

    @JsonProperty("ip")
    private String ip;

    @JsonProperty("state")
    private String state;

    public IpAddrRequest() {}

    public IpAddrRequest(int ipVersion, String vpcId, String subnetId,
                         String rangeId, String ip, String state) {
        this.ipVersion = ipVersion;
        this.vpcId = vpcId;
        this.subnetId = subnetId;
        this.rangeId = rangeId;
        this.ip = ip;
        this.state = state;
    }

    public int getIpVersion() {
        return ipVersion;
    }

    public void setIpVersion(int ipVersion) {
        this.ipVersion = ipVersion;
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

    public String getRangeId() {
        return rangeId;
    }

    public void setRangeId(String rangeId) {
        this.rangeId = rangeId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "IpAddrRequest{" +
                "ipVersion=" + ipVersion +
                ", vpcId='" + vpcId + '\'' +
                ", subnetId='" + subnetId + '\'' +
                ", rangeId='" + rangeId + '\'' +
                ", ip='" + ip + '\'' +
                ", state='" + state + '\'' +
                '}';
    }
}
