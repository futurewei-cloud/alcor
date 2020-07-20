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

package com.futurewei.alcor.web.entity.elasticip.openstack;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.common.utils.Ipv4AddrUtil;
import com.futurewei.alcor.web.entity.elasticip.ElasticIpInfo;
import com.futurewei.alcor.web.entity.elasticip.ElasticIpInfoWrapper;
import com.futurewei.alcor.web.entity.ip.IpVersion;

public class FloatingIpRequest {

    @JsonProperty("project_id")
    private String projectId;

    @JsonProperty("tenant_id")
    private String tenantId;

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

    @JsonProperty("description")
    private String description;

    @JsonProperty("dns_name")
    private String dnsName;

    @JsonProperty("dns_domain")
    private String dnsDomain;

    public FloatingIpRequest() {
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
