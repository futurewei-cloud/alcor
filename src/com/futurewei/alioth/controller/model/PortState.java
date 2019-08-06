package com.futurewei.alioth.controller.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PortState extends CustomerResource {

    private String networkId;
    private String tenantId;
    private boolean adminStateUp;
    private String macAddress;
    private String vethName;

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

    @Data
    public class FixedIp {
        private String subnetId;
        private String ipAddress;

        public FixedIp(String subnetId, String vpcIp) {
            this.subnetId = subnetId;
            this.ipAddress = vpcIp;
        }
    }

    @Data
    public class AllowAddressPair {
        private String ipAddress;
        private String macAddress;
    }

    @Data
    public class ExtraDhcpOpt {
        private String optName;
        private String optValue;
    }

    @Data
    public class SecurityGroupId {
        private String id;
    }

    @Data
    public class DnsRecord {
        private String hostName;
        private String ipAddress;
        private String fqdn;
    }

    public PortState() {}

    public PortState(String projectId, String subnetId, String id, String name, String macAddress, String vethName, String[] vpcIps) {
        super(projectId, id, name, "");
        this.networkId = subnetId;
        this.macAddress = macAddress;
        this.vethName = vethName;

        this.fixedIps = new ArrayList<>();
        for(String vpcIp : vpcIps){
            this.fixedIps.add(new FixedIp(subnetId, vpcIp));
        }
    }
}
