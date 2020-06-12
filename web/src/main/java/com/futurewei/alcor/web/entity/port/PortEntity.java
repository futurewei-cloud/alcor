/*
Copyright 2019 The Alcor Authors.

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
package com.futurewei.alcor.web.entity.port;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PortEntity {
    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("project_id")
    private String projectId;

    @JsonProperty("description")
    private String description;

    @JsonProperty("network_id")
    private String vpcId;

    @JsonProperty("tenant_id")
    private String tenantId;

    @JsonProperty("admin_state_up")
    private boolean adminStateUp;

    @JsonProperty("mac_address")
    private String macAddress;

    @JsonProperty("veth_name")
    private String vethName;

    @JsonProperty("fast_path")
    private boolean fastPath;

    @JsonProperty("device_id")
    private String deviceId;

    @JsonProperty("device_owner")
    private String deviceOwner;    //compute:nova, network:dhcp, network:router_interface

    @JsonProperty("status")
    private String status;

    @JsonProperty("fixed_ips")
    private List<FixedIp> fixedIps;

    @JsonProperty("allowed_address_pairs")
    private List<AllowAddressPair> allowedAddressPairs;

    @JsonProperty("extra_dhcp_opts")
    private List<ExtraDhcpOpt> extraDhcpOpts;

    @JsonProperty("security_groups")
    private List<String> securityGroups;

    @JsonProperty("binding:host_id")
    private String bindingHostId;

    @JsonProperty("binding:profile")
    private String bindingProfile;

    @JsonProperty("binding:vif_details")
    private String bindingVifDetails; //port_filter, ovs_hybrid_plug

    @JsonProperty("binding:vif_type")
    private String bindingVifType;  //ovs, bridge, macvtap, hw_veb, hostdev_physical, vhostuser, distributed, other

    @JsonProperty("binding:vnic_type")
    private String bindingVnicType;  //normal, macvtap, direct, baremetal, direct-physical

    @JsonProperty("network_ns")
    private String networkNamespace;

    @JsonProperty("dns_name")
    private String dnsName;

    @JsonProperty("dns_domain")
    private String dnsDomain;

    @JsonProperty("dns_assignment")
    private List<DnsRecord> dnsAssignment;

    @JsonProperty("create_at")
    private String createAt;

    @JsonProperty("update_at")
    private String updateAt;

    @JsonProperty("ip_allocation")
    private String ipAllocation;

    @JsonProperty("port_security_enabled")
    private boolean portSecurityEnabled;

    @JsonProperty("qos_network_policy_id")
    private String qosNetworkPolicyId;

    @JsonProperty("qos_policy_id")
    private String qosPolicyId;

    @JsonProperty("revision_number")
    private int revisionNumber;

    @JsonProperty("resource_request")
    private int resourceRequest;

    @JsonProperty("tags")
    private List<String> tags;

    @JsonProperty("uplink_status_propagation")
    private boolean uplinkStatusPropagation;

    @JsonProperty("mac_learning_enabled")
    private boolean macLearningEnabled;

    public PortEntity() {

    }

    public PortEntity(String id, String projectId, String name, String description, String vpcId, String tenantId, boolean adminStateUp, String macAddress, String vethName, boolean fastPath, String deviceId, String deviceOwner, String status, List<FixedIp> fixedIps, List<AllowAddressPair> allowedAddressPairs, List<ExtraDhcpOpt> extraDhcpOpts, List<String> securityGroups, String bindingHostId, String bindingProfile, String bindingVifDetails, String bindingVifType, String bindingVnicType, String networkNamespace, String dnsName, String dnsDomain, List<DnsRecord> dnsAssignment, String createAt, String updateAt, String ipAllocation, boolean portSecurityEnabled, String qosNetworkPolicyId, String qosPolicyId, int revisionNumber, int resourceRequest, List<String> tags, boolean uplinkStatusPropagation, boolean macLearningEnabled) {
        this.id = id;
        this.projectId = projectId;
        this.name = name;
        this.description = description;
        this.vpcId = vpcId;
        this.tenantId = tenantId;
        this.adminStateUp = adminStateUp;
        this.macAddress = macAddress;
        this.vethName = vethName;
        this.fastPath = fastPath;
        this.deviceId = deviceId;
        this.deviceOwner = deviceOwner;
        this.status = status;
        this.fixedIps = fixedIps;
        this.allowedAddressPairs = allowedAddressPairs;
        this.extraDhcpOpts = extraDhcpOpts;
        this.securityGroups = securityGroups;
        this.bindingHostId = bindingHostId;
        this.bindingProfile = bindingProfile;
        this.bindingVifDetails = bindingVifDetails;
        this.bindingVifType = bindingVifType;
        this.bindingVnicType = bindingVnicType;
        this.networkNamespace = networkNamespace;
        this.dnsName = dnsName;
        this.dnsDomain = dnsDomain;
        this.dnsAssignment = dnsAssignment;
        this.createAt = createAt;
        this.updateAt = updateAt;
        this.ipAllocation = ipAllocation;
        this.portSecurityEnabled = portSecurityEnabled;
        this.qosNetworkPolicyId = qosNetworkPolicyId;
        this.qosPolicyId = qosPolicyId;
        this.revisionNumber = revisionNumber;
        this.resourceRequest = resourceRequest;
        this.tags = tags;
        this.uplinkStatusPropagation = uplinkStatusPropagation;
        this.macLearningEnabled = macLearningEnabled;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public boolean isAdminStateUp() {
        return adminStateUp;
    }

    public void setAdminStateUp(boolean adminStateUp) {
        this.adminStateUp = adminStateUp;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getVethName() {
        return vethName;
    }

    public void setVethName(String vethName) {
        this.vethName = vethName;
    }

    public boolean isFastPath() {
        return fastPath;
    }

    public void setFastPath(boolean fastPath) {
        this.fastPath = fastPath;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceOwner() {
        return deviceOwner;
    }

    public void setDeviceOwner(String deviceOwner) {
        this.deviceOwner = deviceOwner;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<FixedIp> getFixedIps() {
        return fixedIps;
    }

    public void setFixedIps(List<FixedIp> fixedIps) {
        this.fixedIps = fixedIps;
    }

    public List<AllowAddressPair> getAllowedAddressPairs() {
        return allowedAddressPairs;
    }

    public void setAllowedAddressPairs(List<AllowAddressPair> allowedAddressPairs) {
        this.allowedAddressPairs = allowedAddressPairs;
    }

    public List<ExtraDhcpOpt> getExtraDhcpOpts() {
        return extraDhcpOpts;
    }

    public void setExtraDhcpOpts(List<ExtraDhcpOpt> extraDhcpOpts) {
        this.extraDhcpOpts = extraDhcpOpts;
    }

    public List<String> getSecurityGroups() {
        return securityGroups;
    }

    public void setSecurityGroups(List<String> securityGroups) {
        this.securityGroups = securityGroups;
    }

    public String getBindingHostId() {
        return bindingHostId;
    }

    public void setBindingHostId(String bindingHostId) {
        this.bindingHostId = bindingHostId;
    }

    public String getBindingProfile() {
        return bindingProfile;
    }

    public void setBindingProfile(String bindingProfile) {
        this.bindingProfile = bindingProfile;
    }

    public String getBindingVifDetails() {
        return bindingVifDetails;
    }

    public void setBindingVifDetails(String bindingVifDetails) {
        this.bindingVifDetails = bindingVifDetails;
    }

    public String getBindingVifType() {
        return bindingVifType;
    }

    public void setBindingVifType(String bindingVifType) {
        this.bindingVifType = bindingVifType;
    }

    public String getBindingVnicType() {
        return bindingVnicType;
    }

    public void setBindingVnicType(String bindingVnicType) {
        this.bindingVnicType = bindingVnicType;
    }

    public String getNetworkNamespace() {
        return networkNamespace;
    }

    public void setNetworkNamespace(String networkNamespace) {
        this.networkNamespace = networkNamespace;
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

    public List<DnsRecord> getDnsAssignment() {
        return dnsAssignment;
    }

    public void setDnsAssignment(List<DnsRecord> dnsAssignment) {
        this.dnsAssignment = dnsAssignment;
    }

    public String getCreateAt() {
        return createAt;
    }

    public void setCreateAt(String createAt) {
        this.createAt = createAt;
    }

    public String getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(String updateAt) {
        this.updateAt = updateAt;
    }

    public String getIpAllocation() {
        return ipAllocation;
    }

    public void setIpAllocation(String ipAllocation) {
        this.ipAllocation = ipAllocation;
    }

    public boolean isPortSecurityEnabled() {
        return portSecurityEnabled;
    }

    public void setPortSecurityEnabled(boolean portSecurityEnabled) {
        this.portSecurityEnabled = portSecurityEnabled;
    }

    public String getQosNetworkPolicyId() {
        return qosNetworkPolicyId;
    }

    public void setQosNetworkPolicyId(String qosNetworkPolicyId) {
        this.qosNetworkPolicyId = qosNetworkPolicyId;
    }

    public String getQosPolicyId() {
        return qosPolicyId;
    }

    public void setQosPolicyId(String qosPolicyId) {
        this.qosPolicyId = qosPolicyId;
    }

    public int getRevisionNumber() {
        return revisionNumber;
    }

    public void setRevisionNumber(int revisionNumber) {
        this.revisionNumber = revisionNumber;
    }

    public int getResourceRequest() {
        return resourceRequest;
    }

    public void setResourceRequest(int resourceRequest) {
        this.resourceRequest = resourceRequest;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public boolean isUplinkStatusPropagation() {
        return uplinkStatusPropagation;
    }

    public void setUplinkStatusPropagation(boolean uplinkStatusPropagation) {
        this.uplinkStatusPropagation = uplinkStatusPropagation;
    }

    public boolean isMacLearningEnabled() {
        return macLearningEnabled;
    }

    public void setMacLearningEnabled(boolean macLearningEnabled) {
        this.macLearningEnabled = macLearningEnabled;
    }
}
