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
package com.futurewei.alcor.portmanager.config;

public class UnitTestConfig {
    public static String portId = "3d53801c-32ce-4e97-9572-bb966f4aa53e";
    public static String portId2 = "3d53801c-32ce-4e97-9572-bb966f4625ba";
    public static String projectId = "3d53801c-32ce-4e97-9572-bb966f4de79c";
    public static int ipv4Version = 4;
    public static int ipv6Version = 6;
    public static String vpcId = "3d53801c-32ce-4e97-9572-bb966f4d175e";
    public static String tenantId = "3d53801c-32ce-4e97-9572-bb966f476ec";
    public static String subnetId = "3d53801c-32ce-4e97-9572-bb966f4056b";
    public static String rangeId = "3d53801c-32ce-4e97-9572-bb966f6ba29";
    public static String vpcCidr = "11.11.1.0/24";
    public static String ip1 = "11.11.11.100";
    public static String ip2 = "11.11.11.101";
    public static String ipv6Address = "2001:3CA1:310F:201A:121B:4231:2345:1010";
    public static String mac1 = "00:01:6C:06:A6:29";
    public static String mac2 = "00:01:6C:06:A6:30";
    public static String nodeId = "3d53801c-32ce-4e97-9572-bb966f77ea5";
    public static String nodeId2 = "3d53801c-32ce-4e97-9572-bb966f55e31";
    public static String securityGroupId = "3d53801c-32ce-4e97-9572-bb966f4d45ca";
    public static String securityGroupName = "securitygroup1";
    public static String securityGroupRuleId = "3d53801c-32ce-4e97-9572-bb966f4dcca8";
    public static String remoteIpPrefix = "192.168.1.0/24";
    public static String direction = "ingress";
    public static String direction2 = "egress";
    public static String protocolTcp = "tcp";
    public static String protocolUdp = "udp";
    public static String protocolIcmp = "icmp";
    public static int portRangeMin = 1;
    public static int portRangeMax = 65535;
    public static int portRangeMinIcmp = 0;
    public static int portRangeMaxIcmp = 255;
    public static String etherType = "IPv4";
    public static String routeDestination = "192.168.1.0/24";
    public static String routeTarget = "Local";
    public static String portEntityWithFixedIps = "{\n" +
            "    \"port\": {\n" +
            "        \"id\":\"" + portId + "\",\n" +
            "        \"network_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"binding:host_id\":\"" + nodeId + "\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"" + subnetId + "\", \"ip_address\":\"" + ip1 + "\"}],\n" +
            "        \"security_groups\": [\""+ securityGroupId +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip2 + "\", \"mac_address\":\"" + mac1 + "\"}]\n" +
            "    }\n" +
            "}";
    public static String portEntityWithoutFixedIps = "{\n" +
            "    \"port\": {\n" +
            "        \"id\":\"" + portId + "\",\n" +
            "        \"network_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"binding:host_id\":\"" + nodeId + "\",\n" +
            "        \"security_groups\": [\""+ securityGroupId +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip2 + "\", \"mac_address\":\"" + mac1 + "\"}]\n" +
            "    }\n" +
            "}";
    public static String portEntityWithMacAddress = "{\n" +
            "    \"port\": {\n" +
            "        \"id\":\"" + portId + "\",\n" +
            "        \"network_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"binding:host_id\":\"" + nodeId + "\",\n" +
            "        \"mac_address\":\"" + mac1 + "\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"" + subnetId + "\", \"ip_address\":\"" + ip1 + "\"}],\n" +
            "        \"security_groups\": [\""+ securityGroupId +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip2 + "\", \"mac_address\":\"" + mac1 + "\"}]\n" +
            "    }\n" +
            "}";
    public static String portEntityWithoutMacAddress = "{\n" +
            "    \"port\": {\n" +
            "        \"id\":\"" + portId + "\",\n" +
            "        \"network_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"binding:host_id\":\"" + nodeId + "\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"" + subnetId + "\", \"ip_address\":\"" + ip1 + "\"}],\n" +
            "        \"security_groups\": [\""+ securityGroupId +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip2 + "\", \"mac_address\":\"" + mac1 + "\"}]\n" +
            "    }\n" +
            "}";

    public static String portEntityWithSecurityGroup = "{\n" +
            "    \"port\": {\n" +
            "        \"id\":\"" + portId + "\",\n" +
            "        \"network_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"binding:host_id\":\"" + nodeId + "\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"" + subnetId + "\", \"ip_address\":\"" + ip1 + "\"}],\n" +
            "        \"security_groups\": [\""+ securityGroupId +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip2 + "\", \"mac_address\":\"" + mac1 + "\"}]\n" +
            "    }\n" +
            "}";

    public static String portEntityWithoutSecurityGroup = "{\n" +
            "    \"port\": {\n" +
            "        \"id\":\"" + portId + "\",\n" +
            "        \"network_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"binding:host_id\":\"" + nodeId + "\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"" + subnetId + "\", \"ip_address\":\"" + ip1 + "\"}],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip2 + "\", \"mac_address\":\"" + mac1 + "\"}]\n" +
            "    }\n" +
            "}";

    public static String updateFixedIps = "{\n" +
            "    \"port\": {\n" +
            "        \"id\":\"" + portId + "\",\n" +
            "        \"network_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"binding:host_id\":\"" + nodeId + "\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"" + subnetId + "\", \"ip_address\":\"" + ip2 + "\"}],\n" +
            "        \"security_groups\": [\""+ securityGroupId +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip2 + "\", \"mac_address\":\"" + mac1 + "\"}]\n" +
            "    }\n" +
            "}";
    public static String createPortBulk = "{\n" +
            "    \"ports\": [{\n" +
            "        \"id\":\"" + portId + "\",\n" +
            "        \"network_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"binding:host_id\":\"" + nodeId + "\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"" + subnetId + "\", \"ip_address\":\"" + ip1 + "\"}],\n" +
            "        \"security_groups\": [\""+ securityGroupId +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip2 + "\", \"mac_address\":\"" + mac1 + "\"}]\n" +
            "    },{\n" +
            "        \"id\":\"" + portId2 + "\",\n" +
            "        \"network_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"binding:host_id\":\"" + nodeId + "\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"" + subnetId + "\", \"ip_address\":\"" + ip2 + "\"}],\n" +
            "        \"security_groups\": [\""+ securityGroupId +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip2 + "\", \"mac_address\":\"" + mac2 + "\"}]\n" +
            "    }]\n" +
            "}";
    public static String updatePortBulk = "{\n" +
            "    \"ports\": [{\n" +
            "        \"id\":\"" + portId + "\",\n" +
            "        \"network_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"binding:host_id\":\"" + nodeId + "\",\n" +
            "        \"mac_address\":\"" + mac2 + "\",\n" +
            "        \"binding:host_id\":\"" + nodeId2 + "\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"" + subnetId + "\", \"ip_address\":\"" + ip2 + "\"}],\n" +
            "        \"security_groups\": [\""+ securityGroupId +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip2 + "\", \"mac_address\":\"" + mac1 + "\"}]\n" +
            "    },{\n" +
            "        \"id\":\"" + portId2 + "\",\n" +
            "        \"network_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"binding:host_id\":\"" + nodeId + "\",\n" +
            "        \"mac_address\":\"" + mac2 + "\",\n" +
            "        \"binding:host_id\":\"" + nodeId2 + "\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"" + subnetId + "\", \"ip_address\":\"" + ip2 + "\"}],\n" +
            "        \"security_groups\": [\""+ securityGroupId +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip2 + "\", \"mac_address\":\"" + mac2 + "\"}]\n" +
            "    }]\n" +
            "}";
}
