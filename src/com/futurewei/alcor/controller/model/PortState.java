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

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PortState extends CustomerResource {

    @Data
    public static class FixedIp {
        private String subnetId;
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
        private String ipAddress;
        private String macAddress;
    }

    @Data
    public static class ExtraDhcpOpt {
        private String optName;
        private String optValue;
    }

    @Data
    public static class SecurityGroupId {
        private String id;
    }

    @Data
    public static class DnsRecord {
        private String hostName;
        private String ipAddress;
        private String fqdn;
    }

    private String networkId;
    private String tenantId;
    private boolean adminStateUp;
    private String macAddress;
    private String vethName;

    private boolean isFastPath;

    private String deviceId;
    private String deviceOwner;
    private String status;

    private List<FixedIp> fixedIps;
    private List<AllowAddressPair> allowedAddressPairs;
    private List<ExtraDhcpOpt> extraDhcpOpts;
    private List<SecurityGroupId> securityGroups;

    private String bindingVnicType;
    private String dnsName;
    private List<DnsRecord> dnsAssignment;

    public PortState() {
    }

    public PortState(String projectId, String subnetId, String id, String name, String macAddress, String vethName, String[] vpcIps) {
        super(projectId, id, name, "");
        this.networkId = subnetId;
        this.macAddress = macAddress;
        this.vethName = vethName;

        this.fixedIps = new ArrayList<>();
        for (String vpcIp : vpcIps) {
            this.fixedIps.add(new FixedIp(subnetId, vpcIp));
        }
    }

    public PortState(String projectId, String subnetId, String id, String name, String macAddress, String vethName,
                     List<FixedIp> fixedIps, boolean isFastPath) {
        super(projectId, id, name, "");
        this.networkId = subnetId;
        this.macAddress = macAddress;
        this.vethName = vethName;
        this.fixedIps = fixedIps == null ? null : new ArrayList<FixedIp>(fixedIps);
        this.isFastPath = isFastPath;
    }

    public PortState(PortState state) {
        this(state.getProjectId(), state.getNetworkId(), state.getId(), state.getName(), state.getMacAddress(), state.getVethName(), state.getFixedIps(), state.isFastPath());
    }
}

