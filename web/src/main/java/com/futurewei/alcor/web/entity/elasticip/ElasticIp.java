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


package com.futurewei.alcor.web.entity.elasticip;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.common.entity.CustomerResource;


public class ElasticIp extends CustomerResource {

    @JsonIgnore
    private String rangeId;

    @JsonProperty("elastic_ip_version")
    private Integer elasticIpVersion;

    @JsonProperty("elastic_ip")
    private String elasticIp;

    @JsonProperty("port_id")
    private String portId;

    @JsonProperty("private_ip")
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private String privateIp;

    @JsonProperty("dns_name")
    private String dnsName;

    @JsonProperty("dns_domain")
    private String dnsDomain;

    public ElasticIp() {
    }

    public ElasticIp(ElasticIp eip) {
        super(eip.getProjectId(), eip.getId(), eip.getName(), eip.getDescription());
        this.rangeId = eip.getRangeId();
        this.elasticIpVersion = eip.getElasticIpVersion();
        this.elasticIp = eip.getElasticIp();
        this.portId = eip.getPortId();
        this.privateIp = eip.getPrivateIp();
        this.dnsName = eip.getDnsName();
        this.dnsDomain = eip.getDnsDomain();
    }

    public ElasticIp(String projectId, String id, String name, String description,
                     String rangeId, Integer elasticIpVersion, String elasticIp, String portId,
                     String privateIp, String dnsName, String dnsDomain) {
        super(projectId, id, name, description);
        this.rangeId = rangeId;
        this.elasticIpVersion = elasticIpVersion;
        this.elasticIp = elasticIp;
        this.portId = portId;
        this.privateIp = privateIp;
        this.dnsName = dnsName;
        this.dnsDomain = dnsDomain;
    }

    public String getRangeId() {
        return rangeId;
    }

    public void setRangeId(String rangeId) {
        this.rangeId = rangeId;
    }

    public Integer getElasticIpVersion() {
        return elasticIpVersion;
    }

    public void setElasticIpVersion(Integer elasticIpVersion) {
        this.elasticIpVersion = elasticIpVersion;
    }

    public String getElasticIp() {
        return elasticIp;
    }

    public void setElasticIp(String elasticIp) {
        this.elasticIp = elasticIp;
    }

    public String getPortId() {
        return portId;
    }

    public void setPortId(String portId) {
        this.portId = portId;
    }

    public String getPrivateIp() {
        return privateIp;
    }

    public void setPrivateIp(String privateIp) {
        this.privateIp = privateIp;
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

    @Override
    public String toString() {
        return "ElasticIp{" +
                "rangeId='" + rangeId + '\'' +
                ", elasticIpVersion=" + elasticIpVersion +
                ", elasticIp='" + elasticIp + '\'' +
                ", portId='" + portId + '\'' +
                ", privateIp='" + privateIp + '\'' +
                ", dnsName='" + dnsName + '\'' +
                ", dnsDomain='" + dnsDomain + '\'' +
                "} " + super.toString();
    }
}
