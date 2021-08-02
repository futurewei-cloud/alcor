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
package com.futurewei.alcor.web.entity.port;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.futurewei.alcor.common.entity.CustomerResource;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class PortEntity extends CustomerResource {
    @JsonProperty("network_id")
    private String vpcId;

    @JsonProperty("admin_state_up")
    private Boolean adminStateUp;

    @JsonProperty("mac_address")
    private String macAddress;

    @JsonProperty("veth_name")
    private String vethName;

    @JsonProperty("fast_path")
    private Boolean fastPath;

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
    private BindingProfile bindingProfile;

    @JsonProperty("binding:vif_details")
    private BindingVifDetails bindingVifDetails; //port_filter, ovs_hybrid_plug

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
    private DnsRecord dnsAssignment;

    @JsonProperty("create_at")
    private String createAt;

    @JsonProperty("update_at")
    private String updateAt;

    @JsonProperty("ip_allocation")
    private String ipAllocation;

    @JsonProperty("port_security_enabled")
    private Boolean portSecurityEnabled = true;

    @JsonProperty("qos_network_policy_id")
    private String qosNetworkPolicyId;

    @JsonProperty("qos_policy_id")
    private String qosPolicyId;

    @JsonProperty("revision_number")
    private Integer revisionNumber;

    @JsonProperty("resource_request")
    private ResourceRequest resourceRequest;

    @JsonProperty("tags")
    private List<String> tags;

    @JsonProperty("uplink_status_propagation")
    private Boolean uplinkStatusPropagation;

    @JsonProperty("mac_learning_enabled")
    private Boolean macLearningEnabled;

    public PortEntity() {
    }

    public PortEntity(String vpcId, Boolean adminStateUp, String macAddress, String vethName,
                      Boolean fastPath, String deviceId, String deviceOwner, String status, List<FixedIp> fixedIps,
                      List<AllowAddressPair> allowedAddressPairs, List<ExtraDhcpOpt> extraDhcpOpts, List<String> securityGroups,
                      String bindingHostId, BindingProfile bindingProfile, BindingVifDetails bindingVifDetails, String bindingVifType,
                      String bindingVnicType, String networkNamespace, String dnsName, String dnsDomain,
                      DnsRecord dnsAssignment, String createAt, String updateAt, String ipAllocation,
                      Boolean portSecurityEnabled, String qosNetworkPolicyId, String qosPolicyId, Integer revisionNumber,
                      ResourceRequest resourceRequest, List<String> tags, Boolean uplinkStatusPropagation, Boolean macLearningEnabled) {
        this.vpcId = vpcId;
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

    public PortEntity(CustomerResource state, String vpcId, Boolean adminStateUp, String macAddress, String vethName, Boolean fastPath, String deviceId, String deviceOwner, String status, List<FixedIp> fixedIps, List<AllowAddressPair> allowedAddressPairs, List<ExtraDhcpOpt> extraDhcpOpts, List<String> securityGroups, String bindingHostId, BindingProfile bindingProfile, BindingVifDetails bindingVifDetails, String bindingVifType, String bindingVnicType, String networkNamespace, String dnsName, String dnsDomain, DnsRecord dnsAssignment, String createAt, String updateAt, String ipAllocation, Boolean portSecurityEnabled, String qosNetworkPolicyId, String qosPolicyId, Integer revisionNumber, ResourceRequest resourceRequest, List<String> tags, Boolean uplinkStatusPropagation, Boolean macLearningEnabled) {
        super(state);
        this.vpcId = vpcId;
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

    public PortEntity(String projectId, String id, String name, String description, String vpcId, Boolean adminStateUp, String macAddress, String vethName, Boolean fastPath, String deviceId, String deviceOwner, String status, List<FixedIp> fixedIps, List<AllowAddressPair> allowedAddressPairs, List<ExtraDhcpOpt> extraDhcpOpts, List<String> securityGroups, String bindingHostId, BindingProfile bindingProfile, BindingVifDetails bindingVifDetails, String bindingVifType, String bindingVnicType, String networkNamespace, String dnsName, String dnsDomain, DnsRecord dnsAssignment, String createAt, String updateAt, String ipAllocation, Boolean portSecurityEnabled, String qosNetworkPolicyId, String qosPolicyId, Integer revisionNumber, ResourceRequest resourceRequest, List<String> tags, Boolean uplinkStatusPropagation, Boolean macLearningEnabled) {
        super(projectId, id, name, description);
        this.vpcId = vpcId;
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

    public PortEntity(PortEntity portEntity) {
        this(portEntity.getProjectId(), portEntity.getId(), portEntity.getName(), portEntity.getDescription(), portEntity.getVpcId(),
                portEntity.adminStateUp, portEntity.getMacAddress(), portEntity.getVethName(), portEntity.fastPath, portEntity.getDeviceId(),
                portEntity.getDeviceOwner(), portEntity.getStatus(), portEntity.getFixedIps(), portEntity.getAllowedAddressPairs(),
                portEntity.getExtraDhcpOpts(), portEntity.getSecurityGroups(), portEntity.getBindingHostId(), portEntity.getBindingProfile(),
                portEntity.getBindingVifDetails(), portEntity.getBindingVifType(), portEntity.getBindingVnicType(), portEntity.getNetworkNamespace(),
                portEntity.getDnsName(), portEntity.getDnsDomain(), portEntity.getDnsAssignment(), portEntity.getCreateAt(), portEntity.getUpdateAt(),
                portEntity.getIpAllocation(), portEntity.portSecurityEnabled, portEntity.getQosNetworkPolicyId(), portEntity.getQosPolicyId(),
                portEntity.getRevisionNumber(), portEntity.getResourceRequest(), portEntity.getTags(), portEntity.uplinkStatusPropagation,
                portEntity.macLearningEnabled);
    }

    public static class FixedIp {

        @JsonProperty("subnet_id")
        private String subnetId;

        @JsonProperty(value = "ipV4_rangeId",access = JsonProperty.Access.WRITE_ONLY)
        private String ipV4RangeId;

        @JsonProperty("ip_address")
        private String ipAddress;

        public FixedIp() {

        }

        public FixedIp(String subnetId, String ipAddress) {
            this.subnetId = subnetId;
            this.ipAddress = ipAddress;
        }

        public FixedIp(String subnetId, String ipV4RangeId, String ipAddress) {
            this.subnetId = subnetId;
            this.ipV4RangeId = ipV4RangeId;
            this.ipAddress = ipAddress;
        }

        public String getSubnetId() {
            return subnetId;
        }

        public void setSubnetId(String subnetId) {
            this.subnetId = subnetId;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }

        public String getIpV4RangeId() {
            return ipV4RangeId;
        }

        public void setIpV4RangeId(String ipV4RangeId) {
            this.ipV4RangeId = ipV4RangeId;
        }

        @Override
        public String toString() {
            return "FixedIp{" +
                    "subnetId='" + subnetId + '\'' +
                    ", ipAddress='" + ipAddress + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object object) {
            if (object == this) {
                return true;
            }

            if (!(object instanceof FixedIp)) {
                return false;
            }

            FixedIp fixedIp = (FixedIp)object;

            if(this.subnetId == null || this.ipAddress == null){
                return false;
            }

            return this.subnetId.equals(fixedIp.getSubnetId()) && this.ipAddress.equals(fixedIp.getIpAddress());
        }
    }

    @NoArgsConstructor
    public static class AllowAddressPair {

        @JsonProperty("ip_address")
        private String ipAddress;

        @JsonProperty("mac_address")
        private String macAddress;

        public AllowAddressPair(String ipAddress, String macAddress) {
            this.ipAddress = ipAddress;
            this.macAddress = macAddress;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }

        public String getMacAddress() {
            return macAddress;
        }

        public void setMacAddress(String macAddress) {
            this.macAddress = macAddress;
        }
    }

    @NoArgsConstructor
    public static class ExtraDhcpOpt {
        @JsonProperty("ip_version")
        private String ipVersion;

        @JsonProperty("opt_name")
        private String optName;

        @JsonProperty("opt_value")
        private String optValue;

        public ExtraDhcpOpt(String ipVersion, String optName, String optValue) {
            this.ipVersion = ipVersion;
            this.optName = optName;
            this.optValue = optValue;
        }

        public String getOptName() {
            return optName;
        }

        public void setOptName(String optName) {
            this.optName = optName;
        }

        public String getOptValue() {
            return optValue;
        }

        public void setOptValue(String optValue) {
            this.optValue = optValue;
        }

        public String getIpVersion() {
            return ipVersion;
        }

        public void setIpVersion(String ipVersion) {
            this.ipVersion = ipVersion;
        }

        @Override
        public String toString() {
            return "ExtraDhcpOpt{" +
                    "ipVersion='" + ipVersion + '\'' +
                    ", optName='" + optName + '\'' +
                    ", optValue='" + optValue + '\'' +
                    '}';
        }
    }

    public static class SecurityGroupId {
        private String id;

        public SecurityGroupId(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return "SecurityGroupId{" +
                    "id='" + id + '\'' +
                    '}';
        }
    }

    public static class DnsRecord {

        @JsonProperty("host_name")
        private String hostName;

        @JsonProperty("ip_address")
        private String ipAddress;

        @JsonProperty("fqdn")
        private String fqdn;

        public DnsRecord() {}

        public DnsRecord(String hostName, String ipAddress, String fqdn) {
            this.hostName = hostName;
            this.ipAddress = ipAddress;
            this.fqdn = fqdn;
        }

        @Override
        public String toString() {
            return "DnsRecord{" +
                    "hostName='" + hostName + '\'' +
                    ", ipAddress='" + ipAddress + '\'' +
                    ", fqdn='" + fqdn + '\'' +
                    '}';
        }
    }

    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    public Boolean getAdminStateUp() {
        return adminStateUp;
    }

    public void setAdminStateUp(Boolean adminStateUp) {
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

    public Boolean getFastPath() {
        return fastPath;
    }

    public void setFastPath(Boolean fastPath) {
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
        if (bindingHostId == null) {
            this.bindingHostId = "";
        } else {
            this.bindingHostId = bindingHostId;
        }
    }

    public BindingProfile getBindingProfile() {
        return bindingProfile;
    }

    public void setBindingProfile(BindingProfile bindingProfile) {
        this.bindingProfile = bindingProfile;
    }

    public BindingVifDetails getBindingVifDetails() {
        return bindingVifDetails;
    }

    public void setBindingVifDetails(BindingVifDetails bindingVifDetails) {
        this.bindingVifDetails = bindingVifDetails;
    }

    public String getBindingVifType() {
        return bindingVifType;
    }

    public void setBindingVifType(String bindingVifType) {
        if (bindingVifType == null) {
            this.bindingVifType = "";
        } else {
            this.bindingVifType = bindingVifType;
        }
    }

    public String getBindingVnicType() {
        return bindingVnicType;
    }

    public void setBindingVnicType(String bindingVnicType) {
        if (bindingVnicType == null) {
            this.bindingVnicType = "";
        } else {
            this.bindingVnicType = bindingVnicType;
        }
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

    public DnsRecord getDnsAssignment() {
        return dnsAssignment;
    }

    public void setDnsAssignment(DnsRecord dnsAssignment) {
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

    public Boolean getPortSecurityEnabled() {
        return portSecurityEnabled;
    }

    public void setPortSecurityEnabled(Boolean portSecurityEnabled) {
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

    public Integer getRevisionNumber() {
        return revisionNumber;
    }

    public void setRevisionNumber(Integer revisionNumber) {
        this.revisionNumber = revisionNumber;
    }

    public ResourceRequest getResourceRequest() {
        return resourceRequest;
    }

    public void setResourceRequest(ResourceRequest resourceRequest) {
        this.resourceRequest = resourceRequest;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Boolean getUplinkStatusPropagation() {
        return uplinkStatusPropagation;
    }

    public void setUplinkStatusPropagation(Boolean uplinkStatusPropagation) {
        this.uplinkStatusPropagation = uplinkStatusPropagation;
    }

    public Boolean getMacLearningEnabled() {
        return macLearningEnabled;
    }

    public void setMacLearningEnabled(Boolean macLearningEnabled) {
        this.macLearningEnabled = macLearningEnabled;
    }
}
