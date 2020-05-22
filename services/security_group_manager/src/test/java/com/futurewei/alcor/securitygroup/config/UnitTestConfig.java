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
package com.futurewei.alcor.securitygroup.config;

public class UnitTestConfig {
    public static String projectId = "3d53801c-32ce-4e97-9572-bb966fab628";
    public static String securityGroupId = "3d53801c-32ce-4e97-9572-bb966f476ec";
    public static String securityGroupName = "securityGroup1";
    public static String securityGroupName2 = "securityGroup2";
    public static String defaultSecurityGroupName = "default";
    public static String securityGroupDescription = "This is a security group";
    public static String securityGroupDescription2 = "This is another security group";
    public static String tenantId = "3d53801c-32ce-4e97-9572-bb966fab628";
    public static String securityGroupUrl = "/project/" + projectId + "/security-groups";
    public static String securityGroupRuleId = "3d53801c-32ce-4e97-9572-bb966fa6b23";
    public static String securityGroupRuleUrl = "/project/" + projectId + "/security-group-rules";
    public static String remoteIpPrefix = "192.168.1.0/24";
    public static String direction = "ingress";
    public static String protocolTcp = "tcp";
    public static String protocolIcmp = "icmp";
    public static int portRangeMin = 1;
    public static int portRangeMax = 65535;
    public static int portRangeMinIcmp = 0;
    public static int portRangeMaxIcmp = 255;
    public static String etherType = "IPv4";
    public static String portId = "3d53801c-32ce-4e97-9572-bb966fbb66e";
    public static String bindSecurityGroupUrl = "/project/" + projectId + "/bind-security-groups";
    public static String unbindSecurityGroupUrl = "/project/" + projectId + "/unbind-security-groups";
}
