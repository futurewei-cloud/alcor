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
import com.futurewei.alcor.web.entity.elasticip.ElasticIpPortDetails;

import java.util.ArrayList;
import java.util.List;

public class FloatingIpResponse extends CustomerResource {

    @JsonProperty("router_id")
    private String routeId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("dns_domain")
    private String dnsDomain;

    @JsonProperty("dns_name")
    private String dnsName;

    @JsonProperty("port_details")
    private ElasticIpPortDetails portDetails;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String UpdatedAt;

    @JsonProperty("revision_number")
    private String revisionNumber;

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
        this.setProjectId(elasticIpInfo.getProjectId());
        this.setTenantId(elasticIpInfo.getTenantId());
        this.dnsDomain = elasticIpInfo.getDnsDomain();
        this.dnsName = elasticIpInfo.getDnsName();
        this.setDescription(elasticIpInfo.getDescription());
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
