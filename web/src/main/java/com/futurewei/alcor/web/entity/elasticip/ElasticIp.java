/*
Copyright 2020 The Alcor Authors.

Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
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
