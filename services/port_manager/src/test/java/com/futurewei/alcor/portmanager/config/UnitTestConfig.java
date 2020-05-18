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
    public static String nodeId = "00:01:6C:08:B1:34";
    public static String nodeId2 = "00:01:6C:08:B1:34";
    public static String securityGroup = "3d53801c-32ce-4e97-9572-bb966f4d45ca";
    public static String portStateWithFixedIps = "{\n" +
            "    \"port\": {\n" +
            "        \"id\":\"" + portId + "\",\n" +
            "        \"vpc_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"" + subnetId + "\", \"ip_address\":\"" + ip1 + "\"}],\n" +
            "        \"security_groups\": [\""+ securityGroup +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip2 + "\", \"mac_address\":\"" + mac1 + "\"}]\n" +
            "    }\n" +
            "}";
    public static String portStateWithoutFixedIps = "{\n" +
            "    \"port\": {\n" +
            "        \"id\":\"" + portId + "\",\n" +
            "        \"vpc_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"security_groups\": [\""+ securityGroup +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip2 + "\", \"mac_address\":\"" + mac1 + "\"}]\n" +
            "    }\n" +
            "}";
    public static String portStateWithMacAddress = "{\n" +
            "    \"port\": {\n" +
            "        \"id\":\"" + portId + "\",\n" +
            "        \"vpc_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"mac_address\":\"" + mac1 + "\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"" + subnetId + "\", \"ip_address\":\"" + ip1 + "\"}],\n" +
            "        \"security_groups\": [\""+ securityGroup +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip2 + "\", \"mac_address\":\"" + mac1 + "\"}]\n" +
            "    }\n" +
            "}";
    public static String portStateWithoutMacAddress = "{\n" +
            "    \"port\": {\n" +
            "        \"id\":\"" + portId + "\",\n" +
            "        \"vpc_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"" + subnetId + "\", \"ip_address\":\"" + ip1 + "\"}],\n" +
            "        \"security_groups\": [\""+ securityGroup +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip2 + "\", \"mac_address\":\"" + mac1 + "\"}]\n" +
            "    }\n" +
            "}";

    public static String updateFixedIps = "{\n" +
            "    \"port\": {\n" +
            "        \"id\":\"" + portId + "\",\n" +
            "        \"vpc_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"" + subnetId + "\", \"ip_address\":\"" + ip2 + "\"}],\n" +
            "        \"security_groups\": [\""+ securityGroup +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip2 + "\", \"mac_address\":\"" + mac1 + "\"}]\n" +
            "    }\n" +
            "}";
    public static String createPortBulk = "{\n" +
            "    \"ports\": [{\n" +
            "        \"id\":\"" + portId + "\",\n" +
            "        \"vpc_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"" + subnetId + "\", \"ip_address\":\"" + ip1 + "\"}],\n" +
            "        \"security_groups\": [\""+ securityGroup +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip2 + "\", \"mac_address\":\"" + mac1 + "\"}]\n" +
            "    },{\n" +
            "        \"id\":\"" + portId2 + "\",\n" +
            "        \"vpc_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"" + subnetId + "\", \"ip_address\":\"" + ip2 + "\"}],\n" +
            "        \"security_groups\": [\""+ securityGroup +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip2 + "\", \"mac_address\":\"" + mac2 + "\"}]\n" +
            "    }]\n" +
            "}";
    public static String updatePortBulk = "{\n" +
            "    \"ports\": [{\n" +
            "        \"id\":\"" + portId + "\",\n" +
            "        \"vpc_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"mac_address\":\"" + mac2 + "\",\n" +
            "        \"binding:host_id\":\"" + nodeId2 + "\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"" + subnetId + "\", \"ip_address\":\"" + ip2 + "\"}],\n" +
            "        \"security_groups\": [\""+ securityGroup +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip2 + "\", \"mac_address\":\"" + mac1 + "\"}]\n" +
            "    },{\n" +
            "        \"id\":\"" + portId2 + "\",\n" +
            "        \"vpc_id\":\"" + vpcId + "\",\n" +
            "        \"tenant_id\":\"" + tenantId + "\",\n" +
            "        \"mac_address\":\"" + mac2 + "\",\n" +
            "        \"binding:host_id\":\"" + nodeId2 + "\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"" + subnetId + "\", \"ip_address\":\"" + ip2 + "\"}],\n" +
            "        \"security_groups\": [\""+ securityGroup +"\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"" + ip2 + "\", \"mac_address\":\"" + mac2 + "\"}]\n" +
            "    }]\n" +
            "}";
}
