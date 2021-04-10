/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/

package com.futurewei.alcor.securitygroup.utils;

import com.futurewei.alcor.common.utils.Ipv4AddrUtil;
import com.futurewei.alcor.common.utils.Ipv6AddrUtil;
import com.futurewei.alcor.securitygroup.exception.*;
import com.futurewei.alcor.web.entity.securitygroup.PortBindingSecurityGroupsJson;
import com.futurewei.alcor.web.entity.securitygroup.*;
import org.thymeleaf.util.StringUtils;

import java.util.Arrays;
import java.util.List;

public class RestParameterValidator {
    public static void checkProjectId(String projectId) throws ProjectIdRequired {
        if (StringUtils.isEmpty(projectId)) {
            throw new ProjectIdRequired();
        }
    }

    public static String checkOrAssignTenantId(String tenantId, String projectId) throws ProjectIdRequired{
        checkProjectId(projectId);
        if (StringUtils.isEmpty(tenantId)) {
            return projectId;
        }
        return tenantId;
    }

    public static void checkSecurityGroup(SecurityGroupJson securityGroupJson) throws SecurityGroupRequired {
        if (securityGroupJson == null || securityGroupJson.getSecurityGroup() == null) {
            throw new SecurityGroupRequired();
        }
    }

    public static void checkSecurityGroupId(String securityGroupId) throws SecurityGroupIdRequired {
        if (StringUtils.isEmpty(securityGroupId)) {
            throw new SecurityGroupIdRequired();
        }
    }

    public static void checkPortId(String portId) throws PortIdRequired {
        if (StringUtils.isEmpty(portId)) {
            throw new PortIdRequired();
        }
    }

    public static void checkSecurityGroups(SecurityGroupBulkJson securityGroupBulkJson) throws SecurityGroupsRequired {
        if (securityGroupBulkJson == null ||
                securityGroupBulkJson.getSecurityGroups() == null ||
                securityGroupBulkJson.getSecurityGroups().size() == 0) {
            throw new SecurityGroupsRequired();
        }
    }

    public static void checkDirection(String direction) throws DirectionRequired {
        if (StringUtils.isEmpty(direction)) {
            throw new DirectionRequired();
        }
    }

    public static void checkSecurityGroupRule(SecurityGroupRuleJson securityGroupRuleJson) throws SecurityGroupRuleRequired {
        if (securityGroupRuleJson == null || securityGroupRuleJson.getSecurityGroupRule() == null) {
            throw new SecurityGroupRuleRequired();
        }
    }

    public static void checkSecurityGroupRules(SecurityGroupRuleBulkJson securityGroupRuleBulkJson) throws SecurityGroupRuleRequired {
        if (securityGroupRuleBulkJson == null ||
                securityGroupRuleBulkJson.getSecurityGroupRules() == null ||
                securityGroupRuleBulkJson.getSecurityGroupRules().size() == 0) {
            throw new SecurityGroupRuleRequired();
        }
    }

    public static void checkSecurityGroupRuleId(String securityGroupRuleId) throws SecurityGroupRuleIdRequired {
        if (StringUtils.isEmpty(securityGroupRuleId)) {
            throw new SecurityGroupRuleIdRequired();
        }
    }

    public static void checkPortRange(SecurityGroupRule securityGroupRule) throws Exception {
        Integer portRangeMax = securityGroupRule.getPortRangeMax();
        Integer portRangeMin = securityGroupRule.getPortRangeMin();
        String protocol = securityGroupRule.getProtocol();

        if (protocol == null) {
            throw new ProtocolInvalidException();
        }

        if (Arrays.asList("tcp", "udp").contains(protocol)) {
            if (portRangeMax == null || portRangeMin == null) {
                throw new PortRangeInvalid();
            }

            if (!(portRangeMax > 0 && portRangeMax < 65536)) {
                throw new PortRangeInvalid();
            }

            if (!(portRangeMin > 0 && portRangeMin < 65536)) {
                throw new PortRangeInvalid();
            }

            if (portRangeMin > portRangeMax) {
                throw new PortRangeInvalid();
            }
        }

        if (Arrays.asList("icmp", "icmpv6").contains(protocol)) {
            if (portRangeMin == null && portRangeMax != null) {
                throw new PortRangeInvalid();
            }

            if (portRangeMin != null && portRangeMax == null) {
                throw new PortRangeInvalid();
            }

            if (portRangeMin != null && !(0 <= portRangeMin && portRangeMin <= 255)) {
                throw new PortRangeInvalid();
            }

            if (portRangeMax != null && !(0 <= portRangeMax && portRangeMax <= 255)) {
                throw new PortRangeInvalid();
            }
        }
    }

    public static String getIpVersionByPrefix(String prefix) throws Exception {
        if (Ipv4AddrUtil.ipv4PrefixCheck(prefix)) {
            return "IPv4";
        } else if (Ipv6AddrUtil.ipv6PrefixCheck(prefix)) {
            return "IPv6";
        }

        throw new RemoteIpPrefixInvalid();
    }

    public static void checkRemoteIpPrefix(SecurityGroupRule securityGroupRule) throws Exception {
        String remoteIpPrefix = securityGroupRule.getRemoteIpPrefix();
        String etherType = securityGroupRule.getEtherType();

        if (!getIpVersionByPrefix(remoteIpPrefix).equals(etherType)) {
            throw new EtherTypeRemoteIpPrefixConflict();
        }
    }

    public static void checkProtocolAndEtherType(SecurityGroupRule securityGroupRule) throws Exception {
        String protocol = securityGroupRule.getProtocol();
        String etherType = securityGroupRule.getEtherType();

        List<String> ipv6Protocols = Arrays.asList("icmpv6");
        if (ipv6Protocols.contains(protocol) &&
                SecurityGroupRule.EtherType.IPV4.getType().equals(etherType)) {
            throw new ProtocolEtherTypeConflict();
        }
    }

    public static void checkSecurityGroupRule(SecurityGroupRule securityGroupRule) throws Exception {
        //checkTenantId(securityGroupRule.getTenantId());
        checkSecurityGroupId(securityGroupRule.getSecurityGroupId());
        checkDirection(securityGroupRule.getDirection());

        //Verify port range
        if (securityGroupRule.getPortRangeMax() != null || securityGroupRule.getPortRangeMin() != null) {
            checkPortRange(securityGroupRule);
        }

        //Verify ip prefix
        if (securityGroupRule.getRemoteIpPrefix() != null) {
            checkRemoteIpPrefix(securityGroupRule);
        }

        //Verify protocol and ether type
        if (securityGroupRule.getProtocol() != null && securityGroupRule.getEtherType() != null) {
            checkProtocolAndEtherType(securityGroupRule);
        }

        if (securityGroupRule.getRemoteGroupId() != null && securityGroupRule.getRemoteIpPrefix() != null) {
            throw new RemoteIpPrefixRemoteGroupIdConflict();
        }
    }

    public static void checkPortSecurityGroups(PortBindingSecurityGroupsJson portBindingSecurityGroupsJson) throws Exception {
        List<PortBindingSecurityGroup> portBindingSecurityGroups = portBindingSecurityGroupsJson.getPortBindingSecurityGroups();
        for (PortBindingSecurityGroup portBindingSecurityGroup: portBindingSecurityGroups) {

            checkPortId(portBindingSecurityGroup.getPortId());
            checkSecurityGroupId(portBindingSecurityGroup.getSecurityGroupId());
        }
    }
}
