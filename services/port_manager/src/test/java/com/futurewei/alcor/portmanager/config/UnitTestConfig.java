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
    public static String nodeId = "9192a4d4-ffff-4ece-b3f0-8d36e3d85002";
    public static String nodeName = "node2";
    public static String nodeLocalIp = "10.213.43.161";
    public static String nodeMacAddress = "90:17:ac:c1:34:64";
    public static String nodeVeth = "eth0";
    public static int nodeGRPCServerPort = 0;

    public static String portId1 = "3d53801c-32ce-4e97-9572-bb966f4aa53e";
    public static String portId2 = "3d53801c-32ce-4e97-9572-bb966f4625ba";
    public static String portName1 = "portName1";
    public static String portName2 = "portName2";
    public static String adminState1 = "false";
    public static String adminState2 = "true";
    public static String bindingProfile1 = "Profile1";
    public static String bindingProfile2 = "Profile2";
    public static String bindingVnicType1 = "normal";
    public static String bindingVnicType2 = "macvtap";
    public static String description1 = "description1";
    public static String description2 = "description2";
    public static String deviceId1 = "deviceId1";
    public static String deviceId2 = "deviceId2";
    public static String deviceOwner1 = "deviceOwner1";
    public static String deviceOwner2 = "deviceOwner2";
    public static String dnsDomain1 = "dnsDomain1";
    public static String dnsDomain2 = "dnsDomain2";
    public static String dnsName1 = "dnsName1";
    public static String dnsName2 = "dnsName2";
    public static String qosPolicyId1 = "qosPolicyId1";
    public static String qosPolicyId2 = "qosPolicyId2";
    public static String portSecurityEnabled1 = "false";
    public static String portSecurityEnabled2 = "true";
    public static String macLearningEnabled1 = "false";
    public static String macLearningEnabled2 = "true";
    public static String projectId = "3d53801c-32ce-4e97-9572-bb966f4de79c";
    public static int ipv4Version = 4;
    public static int ipv6Version = 6;
    public static String vpcId = "3d53801c-32ce-4e97-9572-bb966f4d175e";
    public static String tenantId = "3d53801c-32ce-4e97-9572-bb966f476ec";
    public static String subnetId = "3d53801c-32ce-4e97-9572-bb966f4056b";
    public static String subnetId2 = "3d53801c-32ce-4e97-9572-bb966fe82b1";
    public static String rangeId = "3d53801c-32ce-4e97-9572-bb966f6ba29";
    public static String vpcCidr = "11.11.1.0/24";
    public static String ip1 = "11.11.11.100";
    public static String ip2 = "11.11.11.101";
    public static String ipv6Address = "2001:3CA1:310F:201A:121B:4231:2345:1010";
    public static String mac1 = "00:01:6C:06:A6:29";
    public static String mac2 = "00:01:6C:06:A6:30";
    public static String nodeId1 = "3d53801c-32ce-4e97-9572-bb966f77ea5";
    public static String nodeId2 = "3d53801c-32ce-4e97-9572-bb966f55e31";
    public static String securityGroupId1 = "3d53801c-32ce-4e97-9572-bb966f4d45ca";
    public static String securityGroupId2 = "3d53801c-32ce-4e97-9572-bb966f4a7abd";
    public static String securityGroupName = "securitygroup1";
    public static String securityGroupRuleId = "3d53801c-32ce-4e97-9572-bb966f4dcca8";
    public static String remoteIpPrefix = "192.168.1.0/24";
    public static String direction1 = "ingress";
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
    public static String elasticIpId1 = "3d53801c-32ce-4e97-9572-bb966f4dc123";
    public static String elasticIpName1 = "elastic ip1";
    public static String elasticIpAddress1 = "200.10.1.10";
    public static String operationType = "add";
    public static String portEntityWithFixedIps = "{\n" +
            "    \"port\": {\n" +
            "        \"id\":\"" + portId1 + "\",\n" +
            "        \"network_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"binding:host_id\":\"" + nodeId1 + "\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"" + subnetId + "\", \"ip_address\":\"" + ip1 + "\"}],\n" +
            "        \"security_groups\": [\""+ securityGroupId1 +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip2 + "\", \"mac_address\":\"" + mac1 + "\"}]\n" +
            "    }\n" +
            "}";
    public static String portEntityWithoutFixedIps = "{\n" +
            "    \"port\": {\n" +
            "        \"id\":\"" + portId1 + "\",\n" +
            "        \"network_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"binding:host_id\":\"" + nodeId1 + "\",\n" +
            "        \"security_groups\": [\""+ securityGroupId1 +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip2 + "\", \"mac_address\":\"" + mac1 + "\"}]\n" +
            "    }\n" +
            "}";
    public static String portEntityWithMacAddress = "{\n" +
            "    \"port\": {\n" +
            "        \"id\":\"" + portId1 + "\",\n" +
            "        \"network_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"binding:host_id\":\"" + nodeId1 + "\",\n" +
            "        \"mac_address\":\"" + mac1 + "\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"" + subnetId + "\", \"ip_address\":\"" + ip1 + "\"}],\n" +
            "        \"security_groups\": [\""+ securityGroupId1 +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip2 + "\", \"mac_address\":\"" + mac1 + "\"}]\n" +
            "    }\n" +
            "}";
    public static String portEntityWithoutMacAddress = "{\n" +
            "    \"port\": {\n" +
            "        \"id\":\"" + portId1 + "\",\n" +
            "        \"network_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"binding:host_id\":\"" + nodeId1 + "\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"" + subnetId + "\", \"ip_address\":\"" + ip1 + "\"}],\n" +
            "        \"security_groups\": [\""+ securityGroupId1 +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip2 + "\", \"mac_address\":\"" + mac1 + "\"}]\n" +
            "    }\n" +
            "}";

    public static String portEntityWithSecurityGroup = "{\n" +
            "    \"port\": {\n" +
            "        \"id\":\"" + portId1 + "\",\n" +
            "        \"network_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"binding:host_id\":\"" + nodeId1 + "\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"" + subnetId + "\", \"ip_address\":\"" + ip1 + "\"}],\n" +
            "        \"security_groups\": [\""+ securityGroupId1 +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip2 + "\", \"mac_address\":\"" + mac1 + "\"}]\n" +
            "    }\n" +
            "}";

    public static String portEntityWithoutSecurityGroup = "{\n" +
            "    \"port\": {\n" +
            "        \"id\":\"" + portId1 + "\",\n" +
            "        \"network_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"binding:host_id\":\"" + nodeId1 + "\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"" + subnetId + "\", \"ip_address\":\"" + ip1 + "\"}],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip2 + "\", \"mac_address\":\"" + mac1 + "\"}]\n" +
            "    }\n" +
            "}";

    public static String updateFixedIps = "{\n" +
            "    \"port\": {\n" +
            "        \"id\":\"" + portId1 + "\",\n" +
            "        \"network_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"binding:host_id\":\"" + nodeId1 + "\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"" + subnetId + "\", \"ip_address\":\"" + ip2 + "\"}],\n" +
            "        \"security_groups\": [\""+ securityGroupId1 +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip2 + "\", \"mac_address\":\"" + mac1 + "\"}]\n" +
            "    }\n" +
            "}";
    public static String updateMacAddress = "{\n" +
            "    \"port\": {\n" +
            "        \"id\":\"" + portId1 + "\",\n" +
            "        \"network_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"binding:host_id\":\"" + nodeId1 + "\",\n" +
            "        \"mac_address\":\"" + mac2 + "\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"" + subnetId + "\", \"ip_address\":\"" + ip1 + "\"}],\n" +
            "        \"security_groups\": [\""+ securityGroupId1 +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip1 + "\", \"mac_address\":\"" + mac1 + "\"}]\n" +
            "    }\n" +
            "}";
    public static String updateSecurityGroups = "{\n" +
            "    \"port\": {\n" +
            "        \"id\":\"" + portId1 + "\",\n" +
            "        \"network_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"binding:host_id\":\"" + nodeId1 + "\",\n" +
            "        \"mac_address\":\"" + mac1 + "\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"" + subnetId + "\", \"ip_address\":\"" + ip1 + "\"}],\n" +
            "        \"security_groups\": [\""+ securityGroupId2 +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip1 + "\", \"mac_address\":\"" + mac1 + "\"}]\n" +
            "    }\n" +
            "}";
    public static String updateName = "{\n" +
            "    \"port\": {\n" +
            "        \"id\":\"" + portId1 + "\",\n" +
            "        \"name\":\"" + portName2 + "\",\n" +
            "        \"network_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"binding:host_id\":\"" + nodeId1 + "\",\n" +
            "        \"mac_address\":\"" + mac1 + "\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"" + subnetId + "\", \"ip_address\":\"" + ip1 + "\"}],\n" +
            "        \"security_groups\": [\""+ securityGroupId1 +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip1 + "\", \"mac_address\":\"" + mac1 + "\"}]\n" +
            "    }\n" +
            "}";
    public static String updateAdminState = "{\n" +
            "    \"port\": {\n" +
            "        \"id\":\"" + portId1 + "\",\n" +
            "        \"name\":\"" + portName1 + "\",\n" +
            "        \"admin_state_up\":\"" + adminState2 + "\",\n" +
            "        \"network_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"binding:host_id\":\"" + nodeId1 + "\",\n" +
            "        \"mac_address\":\"" + mac1 + "\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"" + subnetId + "\", \"ip_address\":\"" + ip1 + "\"}],\n" +
            "        \"security_groups\": [\""+ securityGroupId1 +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip1 + "\", \"mac_address\":\"" + mac1 + "\"}]\n" +
            "    }\n" +
            "}";
    public static String updateBindingHost = "{\n" +
            "    \"port\": {\n" +
            "        \"id\":\"" + portId1 + "\",\n" +
            "        \"name\":\"" + portName1 + "\",\n" +
            "        \"admin_state_up\":\"" + adminState2 + "\",\n" +
            "        \"network_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"binding:host_id\":\"" + nodeId2 + "\",\n" +
            "        \"mac_address\":\"" + mac1 + "\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"" + subnetId + "\", \"ip_address\":\"" + ip1 + "\"}],\n" +
            "        \"security_groups\": [\""+ securityGroupId1 +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip1 + "\", \"mac_address\":\"" + mac1 + "\"}]\n" +
            "    }\n" +
            "}";
    public static String updateBindingProfile = "{\n" +
            "    \"port\": {\n" +
            "        \"id\":\"" + portId1 + "\",\n" +
            "        \"name\":\"" + portName1 + "\",\n" +
            "        \"admin_state_up\":\"" + adminState1 + "\",\n" +
            "        \"network_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"binding:profile\":\"" + bindingProfile2 + "\",\n" +
            "        \"mac_address\":\"" + mac1 + "\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"" + subnetId + "\", \"ip_address\":\"" + ip1 + "\"}],\n" +
            "        \"security_groups\": [\""+ securityGroupId1 +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip1 + "\", \"mac_address\":\"" + mac1 + "\"}]\n" +
            "    }\n" +
            "}";

    public static String updateBindingVnicType = "{\n" +
            "    \"port\": {\n" +
            "        \"id\":\"" + portId1 + "\",\n" +
            "        \"name\":\"" + portName1 + "\",\n" +
            "        \"admin_state_up\":\"" + adminState1 + "\",\n" +
            "        \"network_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"binding:vnic_type\":\"" + bindingVnicType2 + "\",\n" +
            "        \"mac_address\":\"" + mac1 + "\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"" + subnetId + "\", \"ip_address\":\"" + ip1 + "\"}],\n" +
            "        \"security_groups\": [\""+ securityGroupId1 +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip1 + "\", \"mac_address\":\"" + mac1 + "\"}]\n" +
            "    }\n" +
            "}";

    public static String updateDescription = "{\n" +
            "    \"port\": {\n" +
            "        \"id\":\"" + portId1 + "\",\n" +
            "        \"name\":\"" + portName1 + "\",\n" +
            "        \"admin_state_up\":\"" + adminState1 + "\",\n" +
            "        \"network_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"description\":\"" + description2 + "\",\n" +
            "        \"mac_address\":\"" + mac1 + "\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"" + subnetId + "\", \"ip_address\":\"" + ip1 + "\"}],\n" +
            "        \"security_groups\": [\""+ securityGroupId1 +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip1 + "\", \"mac_address\":\"" + mac1 + "\"}]\n" +
            "    }\n" +
            "}";

    public static String updateDeviceId = "{\n" +
            "    \"port\": {\n" +
            "        \"id\":\"" + portId1 + "\",\n" +
            "        \"name\":\"" + portName1 + "\",\n" +
            "        \"admin_state_up\":\"" + adminState1 + "\",\n" +
            "        \"network_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"device_id\":\"" + deviceId2 + "\",\n" +
            "        \"mac_address\":\"" + mac1 + "\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"" + subnetId + "\", \"ip_address\":\"" + ip1 + "\"}],\n" +
            "        \"security_groups\": [\""+ securityGroupId1 +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip1 + "\", \"mac_address\":\"" + mac1 + "\"}]\n" +
            "    }\n" +
            "}";

    public static String updateDeviceOwner = "{\n" +
            "    \"port\": {\n" +
            "        \"id\":\"" + portId1 + "\",\n" +
            "        \"name\":\"" + portName1 + "\",\n" +
            "        \"admin_state_up\":\"" + adminState1 + "\",\n" +
            "        \"network_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"device_owner\":\"" + deviceOwner2 + "\",\n" +
            "        \"mac_address\":\"" + mac1 + "\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"" + subnetId + "\", \"ip_address\":\"" + ip1 + "\"}],\n" +
            "        \"security_groups\": [\""+ securityGroupId1 +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip1 + "\", \"mac_address\":\"" + mac1 + "\"}]\n" +
            "    }\n" +
            "}";
    public static String updateDnsDomain = "{\n" +
            "    \"port\": {\n" +
            "        \"id\":\"" + portId1 + "\",\n" +
            "        \"name\":\"" + portName1 + "\",\n" +
            "        \"admin_state_up\":\"" + adminState1 + "\",\n" +
            "        \"network_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"dns_domain\":\"" + dnsDomain2 + "\",\n" +
            "        \"mac_address\":\"" + mac1 + "\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"" + subnetId + "\", \"ip_address\":\"" + ip1 + "\"}],\n" +
            "        \"security_groups\": [\""+ securityGroupId1 +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip1 + "\", \"mac_address\":\"" + mac1 + "\"}]\n" +
            "    }\n" +
            "}";
    public static String updateDnsName = "{\n" +
            "    \"port\": {\n" +
            "        \"id\":\"" + portId1 + "\",\n" +
            "        \"name\":\"" + portName1 + "\",\n" +
            "        \"admin_state_up\":\"" + adminState1 + "\",\n" +
            "        \"network_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"dns_name\":\"" + dnsName2 + "\",\n" +
            "        \"mac_address\":\"" + mac1 + "\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"" + subnetId + "\", \"ip_address\":\"" + ip1 + "\"}],\n" +
            "        \"security_groups\": [\""+ securityGroupId1 +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip1 + "\", \"mac_address\":\"" + mac1 + "\"}]\n" +
            "    }\n" +
            "}";

    public static String updateQosPolicyId = "{\n" +
            "    \"port\": {\n" +
            "        \"id\":\"" + portId1 + "\",\n" +
            "        \"name\":\"" + portName1 + "\",\n" +
            "        \"admin_state_up\":\"" + adminState1 + "\",\n" +
            "        \"network_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"qos_policy_id\":\"" + qosPolicyId2 + "\",\n" +
            "        \"mac_address\":\"" + mac1 + "\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"" + subnetId + "\", \"ip_address\":\"" + ip1 + "\"}],\n" +
            "        \"security_groups\": [\""+ securityGroupId1 +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip1 + "\", \"mac_address\":\"" + mac1 + "\"}]\n" +
            "    }\n" +
            "}";
    public static String updatePortSecurityEnabled = "{\n" +
            "    \"port\": {\n" +
            "        \"id\":\"" + portId1 + "\",\n" +
            "        \"name\":\"" + portName1 + "\",\n" +
            "        \"admin_state_up\":\"" + adminState1 + "\",\n" +
            "        \"network_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"port_security_enabled\":\"" + portSecurityEnabled2 + "\",\n" +
            "        \"mac_address\":\"" + mac1 + "\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"" + subnetId + "\", \"ip_address\":\"" + ip1 + "\"}],\n" +
            "        \"security_groups\": [\""+ securityGroupId1 +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip1 + "\", \"mac_address\":\"" + mac1 + "\"}]\n" +
            "    }\n" +
            "}";
    public static String updateMacLearningEnabled = "{\n" +
            "    \"port\": {\n" +
            "        \"id\":\"" + portId1 + "\",\n" +
            "        \"name\":\"" + portName1 + "\",\n" +
            "        \"admin_state_up\":\"" + adminState1 + "\",\n" +
            "        \"network_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"mac_learning_enabled\":\"" + macLearningEnabled2 + "\",\n" +
            "        \"mac_address\":\"" + mac1 + "\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"" + subnetId + "\", \"ip_address\":\"" + ip1 + "\"}],\n" +
            "        \"security_groups\": [\""+ securityGroupId1 +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip1 + "\", \"mac_address\":\"" + mac1 + "\"}]\n" +
            "    }\n" +
            "}";
    public static String createPortBulk = "{\n" +
            "    \"ports\": [{\n" +
            "        \"id\":\"" + portId1 + "\",\n" +
            "        \"network_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"binding:host_id\":\"" + nodeId1 + "\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"" + subnetId + "\", \"ip_address\":\"" + ip1 + "\"}],\n" +
            "        \"security_groups\": [\""+ securityGroupId1 +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip2 + "\", \"mac_address\":\"" + mac1 + "\"}]\n" +
            "    },{\n" +
            "        \"id\":\"" + portId2 + "\",\n" +
            "        \"network_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"binding:host_id\":\"" + nodeId1 + "\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"" + subnetId + "\", \"ip_address\":\"" + ip2 + "\"}],\n" +
            "        \"security_groups\": [\""+ securityGroupId1 +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip2 + "\", \"mac_address\":\"" + mac2 + "\"}]\n" +
            "    }]\n" +
            "}";
    public static String updatePortBulk = "{\n" +
            "    \"ports\": [{\n" +
            "        \"id\":\"" + portId1 + "\",\n" +
            "        \"network_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"binding:host_id\":\"" + nodeId1 + "\",\n" +
            "        \"mac_address\":\"" + mac2 + "\",\n" +
            "        \"binding:host_id\":\"" + nodeId2 + "\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"" + subnetId + "\", \"ip_address\":\"" + ip2 + "\"}],\n" +
            "        \"security_groups\": [\""+ securityGroupId1 +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip2 + "\", \"mac_address\":\"" + mac1 + "\"}]\n" +
            "    },{\n" +
            "        \"id\":\"" + portId2 + "\",\n" +
            "        \"network_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"binding:host_id\":\"" + nodeId1 + "\",\n" +
            "        \"mac_address\":\"" + mac2 + "\",\n" +
            "        \"binding:host_id\":\"" + nodeId2 + "\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"" + subnetId + "\", \"ip_address\":\"" + ip2 + "\"}],\n" +
            "        \"security_groups\": [\""+ securityGroupId1 +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip2 + "\", \"mac_address\":\"" + mac2 + "\"}]\n" +
            "    }]\n" +
            "}";
    public static String updateElasticIp = "{\n" +
            "    \"elasticip\": {\n" +
            "        \"id\":\"" + elasticIpId1 + "\",\n" +
            "        \"name\":\"" + elasticIpName1 + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"elastic_ip_version\":\"" + 4 + "\",\n" +
            "        \"elastic_ip_address\":\"" + elasticIpAddress1 + "\",\n" +
            "        \"port_id\":\"" + "\",\n" +
            "        \"dns_domain\":\"" + "\",\n" +
            "        \"dns_name\":\"" + "\"\n" +
            "    }\n" +
            "}";
    public static String updateL3Neighbor = "{\n" +
            "        \"vpc_id\":\"" + vpcId + "\",\n" +
            "        \"subnet_id\":\"" + subnetId + "\",\n" +
            "        \"operation_type\":\"" + operationType + "\",\n" +
            "        \"old_subnet_ids\":[\"" + subnetId + "\",\"" + subnetId2 + "\"]\n" +
            "}";
}
