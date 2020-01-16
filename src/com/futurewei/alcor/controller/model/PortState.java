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

package com.futurewei.alcor.controller.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PortState extends CustomerResource {

    @Data
    public static class FixedIp {

        @JsonProperty("subnet_id")
        private String subnetId;

        @JsonProperty("ip_address")
        private String ipAddress;

        public FixedIp() {

        }

        public FixedIp(String subnetId, String ipAddress) {
            this.subnetId = subnetId;
            this.ipAddress = ipAddress;
        }
    }

    @Data
    public static class AllowAddressPair {

        @JsonProperty("ip_address")
        private String ipAddress;

        @JsonProperty("mac_address")
        private String macAddress;
    }

    @Data
    public static class ExtraDhcpOpt {

        @JsonProperty("opt_name")
        private String optName;

        @JsonProperty("opt_value")
        private String optValue;
    }

    @Data
    public static class SecurityGroupId {
        private String id;
    }

    @Data
    public static class DnsRecord {

        @JsonProperty("host_name")
        private String hostName;

        @JsonProperty("ip_address")
        private String ipAddress;

        @JsonProperty("fqdn")
        private String fqdn;
    }

    @JsonProperty("network_id")
    private String networkId;

    @JsonProperty("tenant_id")
    private String tenantId;

    @JsonProperty("admin_state_up")
    private boolean adminStateUp;

    @JsonProperty("mac_address")
    private String macAddress;

    @JsonProperty("veth_name")
    private String vethName;

    @JsonProperty("fast_path")
    private boolean isFastPath;

    @JsonProperty("device_id")
    private String deviceId;

    @JsonProperty("device_owner")
    private String deviceOwner;

    @JsonProperty("status")
    private String status;

    @JsonProperty("fixed_ips")
    private List<FixedIp> fixedIps;

    @JsonProperty("allowed_address_pairs")
    private List<AllowAddressPair> allowedAddressPairs;

    @JsonProperty("extra_dhcp_opts")
    private List<ExtraDhcpOpt> extraDhcpOpts;

    @JsonProperty("security_groups")
    private List<SecurityGroupId> securityGroups;

    @JsonProperty("binding:host_id")
    private String bindingHostId;

    @JsonProperty("binding:profile")
    private String bindingProfile;

    @JsonProperty("binding:vnic_type")
    private String bindingVnicType;

    @JsonProperty("network_ns")
    private String networkNamespace;

    @JsonProperty("dns_name")
    private String dnsName;

    @JsonProperty("dns_assignment")
    private List<DnsRecord> dnsAssignment;

    public PortState() {
    }

    public PortState(String projectId, String subnetId, String id, String name, String macAddress, String vethName, String[] vpcIps) {
        this(projectId, subnetId, id, name, macAddress, vethName, vpcIps, false);
    }

    public PortState(String projectId, String subnetId, String id, String name, String macAddress, String vethName,
                     String[] vpcIps, boolean isFastPath) {
        super(projectId, id, name, "");
        this.networkId = subnetId;
        this.macAddress = macAddress;
        this.vethName = vethName;
        this.fixedIps = PortState.convertToFixedIps(vpcIps, subnetId);
        this.isFastPath = isFastPath;
    }

    public PortState(String projectId, String subnetId, String id, String name,
                     String tenantId, boolean adminStateUp, String macAddress, String vethName,
                     boolean isFastPath, String deviceId, String deviceOwner, String status,
                     List<FixedIp> fixedIps, List<AllowAddressPair> allowedAddressPairs, List<ExtraDhcpOpt> extraDhcpOpts,
                     List<SecurityGroupId> securityGroups, String bindingHostId, String bindingProfile, String bindingVnicType,
                     String networkNamespace, String dnsName, List<DnsRecord> dnsAssignment) {
        super(projectId, id, name, "");
        this.networkId = subnetId;
        this.tenantId = tenantId;
        this.adminStateUp = adminStateUp;
        this.macAddress = macAddress;
        this.vethName = vethName;
        this.isFastPath = isFastPath;
        this.deviceId = deviceId;
        this.deviceOwner = deviceOwner;
        this.status = status;
        this.fixedIps = (fixedIps == null ? null : new ArrayList<>(fixedIps));
        this.allowedAddressPairs = (allowedAddressPairs == null ? null : new ArrayList<>(allowedAddressPairs));
        this.extraDhcpOpts = (extraDhcpOpts == null ? null : new ArrayList<>(extraDhcpOpts));
        this.securityGroups = (securityGroups == null ? null : new ArrayList<>(securityGroups));
        this.bindingHostId = bindingHostId;
        this.bindingProfile = bindingProfile;
        this.bindingVnicType = bindingVnicType;
        this.networkNamespace = (Strings.isNullOrEmpty(networkNamespace) ? "" : networkNamespace);
        this.dnsName = dnsName;
        this.dnsAssignment = (dnsAssignment == null ? null : new ArrayList<>(dnsAssignment));
    }

    public PortState(PortState state) {
        this(state.getProjectId(), state.getNetworkId(), state.getId(), state.getName(),
                state.getTenantId(), state.isAdminStateUp(), state.getMacAddress(), state.getVethName(),
                state.isFastPath(), state.getDeviceId(), state.getDeviceOwner(), state.getStatus(),
                state.getFixedIps(), state.getAllowedAddressPairs(), state.getExtraDhcpOpts(),
                state.getSecurityGroups(), state.getBindingHostId(), state.getBindingProfile(), state.getBindingVnicType(),
                state.getNetworkNamespace(), state.getDnsName(), state.getDnsAssignment());
    }

    public static List<FixedIp> convertToFixedIps(String[] vpcIps, String subnetId) {

        List<FixedIp> fixedIps = new ArrayList<>();
        if (vpcIps != null) {
            for (String vpcIp : vpcIps) {
                fixedIps.add(new FixedIp(subnetId, vpcIp));
            }
        }

        return fixedIps;
    }
}

