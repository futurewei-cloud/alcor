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
package com.futurewei.alcor.securitygroup.config;

public class UnitTestConfig {
    public static String projectId = "3d53801c-32ce-4e97-9572-bb966fab628";
    public static String securityGroupId = "3d53801c-32ce-4e97-9572-bb966f476ec";
    public static String securityGroupId2 = "3d53801c-32ce-4e97-9572-bb966f7a5f1";
    public static String securityGroupName = "securityGroup1";
    public static String securityGroupName2 = "securityGroup2";
    public static String defaultSecurityGroupName = "default";
    public static String securityGroupDescription = "This is a security group";
    public static String securityGroupDescription2 = "This is another security group";
    public static String tenantId = "3d53801c-32ce-4e97-9572-bb966fab628";
    public static String securityGroupUrl = "/project/" + projectId + "/security-groups";
    public static String securityGroupBulkUrl = "/project/" + projectId + "/security-groups/bulk";
    public static String securityGroupRuleId = "3d53801c-32ce-4e97-9572-bb966fa6b23";
    public static String securityGroupRuleUrl = "/project/" + projectId + "/security-group-rules";
    public static String securityGroupRuleBulkUrl = "/project/" + projectId + "/security-group-rules/bulk";
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
    public static String portId = "3d53801c-32ce-4e97-9572-bb966fbb66e";
    public static String bindSecurityGroupUrl = "/project/" + projectId + "/bind-security-groups";
    public static String unbindSecurityGroupUrl = "/project/" + projectId + "/unbind-security-groups";
}
