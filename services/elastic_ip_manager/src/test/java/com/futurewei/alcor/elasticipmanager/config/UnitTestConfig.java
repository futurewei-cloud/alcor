/*
Copyright 2020 The Alcor Authors.

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
package com.futurewei.alcor.elasticipmanager.config;

public class UnitTestConfig {
    public static String elasticIp1 = "11223344-1122-1122-1122-112233440001";
    public static String elasticIp2 = "11223344-1122-1122-1122-112233440002";
    public static String elasticIpv4Address1 = "200.1.1.10";
    public static String elasticIpv6Address1 = "2002:100::10";
    public static String elasticIpName1 = "eip1";
    public static String elasticIpName2 = "eip2";
    public static String elasticIpDescription1 = "elastic ip 1";
    public static String elasticIpDescription2 = "elastic ip 2";
    public static String elasticIpRange1 = "11223344-1122-1122-1122-112233440003";
    public static String elasticIpRange2 = "11223344-1122-1122-1122-112233440004";
    public static Integer elasticIpVersion1 = 4;
    public static Integer elasticIpVersion2 = 6;
    public static String elasticIpPort1 = "11223344-1122-1122-1122-112233440005";
    public static String elasticIpPort2 = "11223344-1122-1122-1122-112233440006";
    public static String elasticIpPrivateIp1 = "192.168.10.10";
    public static String elasticIpPrivateIp2 = "192.168.20.20";
    public static String elasticIpPrivateIp3 = "2002:200::20";
    public static Integer elasticIpPrivateIpVersion1 = 4;
    public static Integer elasticIpPrivateIpVersion2 = 6;
    public static String elasticIpDnsDomain1 = "futurewei.com.";
    public static String elasticIpDnsDomain2 = "github.com.";
    public static String elasticIpDnsName1 = "eip1";
    public static String elasticIpDnsName2 = "eip2";
    public static String projectId1 = "11223344-1122-1122-1122-112233440007";
    public static String projectId2 = "11223344-1122-1122-1122-112233440008";
    public static String state1 = "activated";
    public static String state2 = "deactivated";

    public static String ElasticIpInfoWithPort = "{\n" +
            "    \"elasticip\": {\n" +
            "        \"id\":\"" + elasticIp1 + "\",\n" +
            "        \"project_id\":\"" + projectId1 + "\",\n" +
            "        \"name\":\"" + elasticIpName1 + "\",\n" +
            "        \"description\":\"" + elasticIpDescription1 + "\",\n" +
            "        \"range_id\":\"" + elasticIpRange1 + "\",\n" +
            "        \"elastic_ip_version\":\"" + elasticIpVersion1 + "\",\n" +
            "        \"elastic_ip\":\"" + elasticIpv4Address1 + "\",\n" +
            "        \"port_id\":\"" + elasticIpPort1 + "\",\n" +
            "        \"private_ip_version\":\"" + elasticIpPrivateIpVersion1 + "\",\n" +
            "        \"private_ip\":\"" + elasticIpPrivateIp1 + "\",\n" +
            "        \"dns_name\":\"" + elasticIpDnsName1 + "\",\n" +
            "        \"dns_domain\":\"" + elasticIpDnsDomain1 + "\",\n" +
            "        \"state\":\"" + state1 + "\"\n" +
            "    }\n" +
            "}";
}
