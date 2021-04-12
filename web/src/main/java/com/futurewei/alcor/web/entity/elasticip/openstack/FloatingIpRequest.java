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


package com.futurewei.alcor.web.entity.elasticip.openstack;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.common.entity.CustomerResource;
import com.futurewei.alcor.web.entity.elasticip.ElasticIpInfo;


public class FloatingIpRequest extends CustomerResource {

    @JsonProperty("floating_network_id")
    private String floatingNetworkId;

    @JsonProperty("fixed_ip_address")
    private String fixedIpAddress;

    @JsonProperty("floating_ip_address")
    private String floatingIpAddress;

    @JsonProperty("port_id")
    private String portId;

    @JsonProperty("subnet_id")
    private String subnetId;

    @JsonProperty("dns_name")
    private String dnsName;

    @JsonProperty("dns_domain")
    private String dnsDomain;

    public FloatingIpRequest() {
    }

    public String getFloatingNetworkId() {
        return floatingNetworkId;
    }

    public void setFloatingNetworkId(String floatingNetworkId) {
        this.floatingNetworkId = floatingNetworkId;
    }

    public String getFixedIpAddress() {
        return fixedIpAddress;
    }

    public void setFixedIpAddress(String fixedIpAddress) {
        this.fixedIpAddress = fixedIpAddress;
    }

    public String getFloatingIpAddress() {
        return floatingIpAddress;
    }

    public void setFloatingIpAddress(String floatingIpAddress) {
        this.floatingIpAddress = floatingIpAddress;
    }

    public String getPortId() {
        return portId;
    }

    public void setPortId(String portId) {
        this.portId = portId;
    }

    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    public String getDnsName() {
        return dnsName;
    }

    public void setDnsName(String dnsName) {
        this.dnsName = dnsName;
    }

    public String getDnsDomain() {
        return dnsDomain;
    }

    public void setDnsDomain(String dnsDomain) {
        this.dnsDomain = dnsDomain;
    }

    public ElasticIpInfo getElasticIpInfo() {
        ElasticIpInfo elasticIpInfo = new ElasticIpInfo();

        elasticIpInfo.setProjectId(this.getProjectId());
        elasticIpInfo.setTenantId(this.getTenantId());
        elasticIpInfo.setPortId(this.getPortId());
        elasticIpInfo.setElasticIp(this.getFloatingIpAddress());
        elasticIpInfo.setPrivateIp(this.getFixedIpAddress());
        elasticIpInfo.setDnsDomain(this.getDnsDomain());
        elasticIpInfo.setDnsName(this.getDnsName());
        elasticIpInfo.setDescription(this.getDescription());

        return elasticIpInfo;
    }

}
