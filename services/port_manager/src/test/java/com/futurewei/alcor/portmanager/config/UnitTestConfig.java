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
    public static String projectId = "3d53801c-32ce-4e97-9572-bb966f4de79c";
    public static int ipVersion = 4;
    public static String vpcId = "3d53801c-32ce-4e97-9572-bb966f4d175e";
    public static String tenantId = "3d53801c-32ce-4e97-9572-bb966f476ec";
    public static String subnetId = "3d53801c-32ce-4e97-9572-bb966f4056b";
    public static String rangeId = "3d53801c-32ce-4e97-9572-bb966f6ba29";
    public static String vpcCidr = "11.11.1.0/24";
    public static String ip1 = "11.11.11.100";
    public static String ip2 = "11.11.11.101";
    public static String mac1 = "00:01:6C:06:A6:29";
    public static String mac2 = "00:01:6C:06:A6:30";
    public static String securityGroup = "3d53801c-32ce-4e97-9572-bb966f4d45ca";
    public static String portStateStr = "{\n" +
            "    \"port_state\": {\n" +
            "        \"vpc_id\":\"vpc1\",\n" +
            "        \"tenant_id\":\"tenant1\",\n" +
            "        \"fixed_ips\":[{\"subnet_id\":\"range1\", \"ip_address\":\"11.11.11.101\"}],\n" +
            "        \"security_groups\": [\"securitygroup1\"],\n" +
            "        \"allowed_address_pairs\":[{\"ip_address\":\"11.11.11.1\", \"mac_address\":\"00:01:6C:06:A6:29\"}]\n" +
            "    }\n" +
            "}";
}
