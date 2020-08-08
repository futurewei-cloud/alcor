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
import com.futurewei.alcor.web.entity.elasticip.ElasticIpInfo;
import com.futurewei.alcor.web.entity.elasticip.ElasticIpPortDetails;

import java.util.ArrayList;
import java.util.List;

public class FloatingIpResponse {

    @JsonProperty("router_id")
    private String routeId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("description")
    private String description;

    @JsonProperty("dns_domain")
    private String dnsDomain;

    @JsonProperty("dns_name")
    private String dnsName;

    @JsonProperty("port_details")
    private ElasticIpPortDetails portDetails;

    @JsonProperty("tenant_id")
    private String tenantId;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String UpdatedAt;

    @JsonProperty("revision_number")
    private String revisionNumber;

    @JsonProperty("project_id")
    private String projectId;

    @JsonProperty("floating_network_id")
    private String floatingNetworkId;

    @JsonProperty("fixed_ip_address")
    private String fixedIpAddress;

    @JsonProperty("floating_ip_address")
    private String floatingIpAddress;

    @JsonProperty("port_id")
    private String portId;

    @JsonProperty("id")
    private String id;

    @JsonProperty("tags")
    private List<String> tags;

    @JsonProperty("port_forwardings")
    private String portForwardings;

    public FloatingIpResponse() {
    }

    public FloatingIpResponse(ElasticIpInfo elasticIpInfo) {
        this.projectId = elasticIpInfo.getProjectId();
        this.tenantId = elasticIpInfo.getTenantId();
        this.dnsDomain = elasticIpInfo.getDnsDomain();
        this.dnsName = elasticIpInfo.getDnsName();
        this.description = elasticIpInfo.getDescription();
        this.floatingIpAddress = elasticIpInfo.getElasticIp();
        this.portId = elasticIpInfo.getPortId();
        this.fixedIpAddress = elasticIpInfo.getPrivateIp();
        this.id = elasticIpInfo.getId();
        this.status = elasticIpInfo.getState();
        if (null != elasticIpInfo.getPortDetails()) {
            this.portDetails = elasticIpInfo.getPortDetails();
        } else {
            this.portDetails = new ElasticIpPortDetails();
        }
        this.tags = new ArrayList<>();
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDnsDomain() {
        return dnsDomain;
    }

    public void setDnsDomain(String dnsDomain) {
        this.dnsDomain = dnsDomain;
    }

    public String getDnsName() {
        return dnsName;
    }

    public void setDnsName(String dnsName) {
        this.dnsName = dnsName;
    }

    public ElasticIpPortDetails getPortDetails() {
        return portDetails;
    }

    public void setPortDetails(ElasticIpPortDetails portDetails) {
        this.portDetails = portDetails;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return UpdatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        UpdatedAt = updatedAt;
    }

    public String getRevisionNumber() {
        return revisionNumber;
    }

    public void setRevisionNumber(String revisionNumber) {
        this.revisionNumber = revisionNumber;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getPortForwardings() {
        return portForwardings;
    }

    public void setPortForwardings(String portForwardings) {
        this.portForwardings = portForwardings;
    }

}
