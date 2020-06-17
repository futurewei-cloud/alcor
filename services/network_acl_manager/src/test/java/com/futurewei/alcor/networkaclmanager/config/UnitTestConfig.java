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
package com.futurewei.alcor.networkaclmanager.config;

public class UnitTestConfig {
    public static String projectId = "3d53801c-32ce-4e97-9572-bb966fab628";

    //Network ACL config
    public static String subnetId1 = "3d53801c-32ce-4e97-9572-bb966fab628";
    public static String subnetId2 = "3d53801c-32ce-4e97-9572-bb966f76ae1";
    public static String networkAclUrl = "/project/" + projectId + "/network-acls";
    public static String vpcId1 = "3d53801c-32ce-4e97-9572-bb966fac7632";
    public static String vpcId2 = "3d53801c-32ce-4e97-9572-bb966faab241";
    public static String networkAclId1 = "3d53801c-32ce-4e97-9572-bb966f77ab4";
    public static String networkAclId2 = "3d53801c-32ce-4e97-9572-bb966f678e1";
    public static String networkAclName1 = "test_network_acl1";
    public static String networkAclName2 = "test_network_acl2";

    //Network ACL Rule config
    public static String networkAclRuleId = "3d53801c-32ce-4e97-9572-bb966f88ca4";
    public static String networkAclRuleName1 = "test_network_acl_rule1";
    public static String networkAclRuleName2 = "test_network_acl_rule2";
    public static String networkAclRuleUrl = "/project/" + projectId + "/network-acl-rules";
    public static Integer number1 = 10;
    public static Integer number2 = 20;
    public static Integer numberInvalid1 = 32767;
    public static Integer numberInvalid2 = -1;
    public static String ipv4Prefix1 = "192.168.1.0/24";
    public static String ipv4Prefix2 = "192.168.2.0/24";
    public static String ipv6Prefix = "2001::/16";
    public static String directionIngress = "ingress";
    public static String directionEgress = "egress";
    public static String directionInvalid = "invalid";
    public static String actionDeny = "deny";
    public static String actionAllow = "allow";
    public static String actionInvalid = "invalid";
    public static String protocolTcp = "tcp";
    public static String protocolUdp = "udp";
    public static String protocolIcmp = "icmp";
    public static String protocolIcmpv6 = "icmpv6";
    public static String protocolInvalid = "icmpv7";
    public static int portRangeMin1 = 1;
    public static int portRangeMin2 = 10;
    public static int portRangeMinInvalid1 = 0;
    public static int portRangeMinInvalid2 = 65536;
    public static int portRangeMax1 = 65535;
    public static int portRangeMax2 = 600;
    public static int portRangeMaxInvalid1 = 0;
    public static int portRangeMaxInvalid2 = 65536;
    public static int icmpType1 = 0;
    public static int icmpType2 = 1;
    public static int icmpTypeInvalid1 = -1;
    public static int icmpTypeInvalid2 = 256;
    public static int icmpCode1 = 254;
    public static int icmpCode2 = 255;
    public static int icmpCodeInvalid1 = -1;
    public static int icmpCodeInvalid2 = 256;
    public static String etherTypeIpv4 = "ipv4";
    public static String etherTypeIpv6 = "ipv6";
    public static String etherTypeInvalid = "ipv7";
}
