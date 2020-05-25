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
package com.futurewei.alcor.securitygroup.controller;

import com.futurewei.alcor.common.utils.Ipv4AddrUtil;
import com.futurewei.alcor.common.utils.Ipv6AddrUtil;
import com.futurewei.alcor.securitygroup.exception.*;
import com.futurewei.alcor.securitygroup.service.SecurityGroupRuleService;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupRule;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupRuleBulkJson;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupRuleJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.util.StringUtils;

import java.util.Arrays;
import java.util.List;

@RestController
public class SecurityGroupRuleController {
    @Autowired
    private SecurityGroupRuleService securityGroupRuleService;

    private void checkProjectId(String projectId) throws ProjectIdRequired {
        if (StringUtils.isEmpty(projectId)) {
            throw new ProjectIdRequired();
        }
    }

    private void checkTenantId(String tenantId) throws TenantIdRequired {
        if (StringUtils.isEmpty(tenantId)) {
            throw new TenantIdRequired();
        }
    }

    private void checkSecurityGroupId(String securityGroupId) throws SecurityGroupIdRequired {
        if (StringUtils.isEmpty(securityGroupId)) {
            throw new SecurityGroupIdRequired();
        }
    }

    private void checkDirection(String direction) throws DirectionRequired {
        if (StringUtils.isEmpty(direction)) {
            throw new DirectionRequired();
        }
    }

    private void checkSecurityGroupRule(SecurityGroupRuleJson securityGroupRuleJson) throws SecurityGroupRuleRequired {
        if (securityGroupRuleJson == null || securityGroupRuleJson.getSecurityGroupRule() == null) {
            throw new SecurityGroupRuleRequired();
        }
    }

    private void checkSecurityGroupRules(SecurityGroupRuleBulkJson securityGroupRuleBulkJson) throws SecurityGroupRuleRequired {
        if (securityGroupRuleBulkJson == null ||
                securityGroupRuleBulkJson.getSecurityGroupRules() == null ||
                securityGroupRuleBulkJson.getSecurityGroupRules().size() == 0) {
            throw new SecurityGroupRuleRequired();
        }
    }

    private void checkSecurityGroupRuleId(String securityGroupRuleId) throws SecurityGroupRuleIdRequired {
        if (StringUtils.isEmpty(securityGroupRuleId)) {
            throw new SecurityGroupRuleIdRequired();
        }
    }

    private void checkPortRange(SecurityGroupRule securityGroupRule) throws Exception {
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

    private String getIpVersionByPrefix(String prefix) throws Exception {
        if (Ipv4AddrUtil.ipv4PrefixCheck(prefix)) {
            return "IPv4";
        } else if (Ipv6AddrUtil.ipv6PrefixCheck(prefix)) {
            return "IPv6";
        }

        throw new RemoteIpPrefixInvalid();
    }
    private void checkRemoteIpPrefix(SecurityGroupRule securityGroupRule) throws Exception {
        String remoteIpPrefix = securityGroupRule.getRemoteIpPrefix();
        String etherType = securityGroupRule.getEtherType();

        if (!getIpVersionByPrefix(remoteIpPrefix).equals(etherType)) {
            throw new EtherTypeRemoteIpPrefixConflict();
        }
    }

    private void checkProtocolAndEtherType(SecurityGroupRule securityGroupRule) throws Exception {
        String protocol = securityGroupRule.getProtocol();
        String etherType = securityGroupRule.getEtherType();

        List<String> ipv6Protocols = Arrays.asList("icmpv6");
        if (ipv6Protocols.contains(protocol) &&
                SecurityGroupRule.EtherType.IPV4.getType().equals(etherType)) {
            throw new ProtocolEtherTypeConflict();
        }
    }

    private void checkSecurityGroupRule(SecurityGroupRule securityGroupRule) throws Exception {
        checkTenantId(securityGroupRule.getTenantId());
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

    @PostMapping({"/project/{project_id}/security-group-rules", "v4/{project_id}/security-group-rules"})
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public SecurityGroupRuleJson createSecurityGroupRule(@PathVariable("project_id") String projectId,
                                                     @RequestBody SecurityGroupRuleJson securityGroupRuleJson) throws Exception {
        checkProjectId(projectId);
        checkSecurityGroupRule(securityGroupRuleJson);
        SecurityGroupRule securityGroupRule = securityGroupRuleJson.getSecurityGroupRule();
        checkSecurityGroupRule(securityGroupRule);
        securityGroupRule.setProjectId(projectId);

        return securityGroupRuleService.createSecurityGroupRule(securityGroupRuleJson);
    }

    @PostMapping({"/project/{project_id}/security-group-rules/bulk", "v4/{project_id}/security-group-rules/bulk"})
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public SecurityGroupRuleBulkJson createSecurityGroupRuleBulk(@PathVariable("project_id") String projectId,
                                                         @RequestBody SecurityGroupRuleBulkJson securityGroupRuleBulkJson) throws Exception {
        checkProjectId(projectId);
        checkSecurityGroupRules(securityGroupRuleBulkJson);

        for (SecurityGroupRule securityGroupRule: securityGroupRuleBulkJson.getSecurityGroupRules()) {
            checkSecurityGroupRule(securityGroupRule);
            securityGroupRule.setProjectId(projectId);
        }

        return securityGroupRuleService.createSecurityGroupRuleBulk(securityGroupRuleBulkJson);
    }

    /*
    @PutMapping({"/project/{project_id}/security-group-rules/{security_group_rule_id}", "v4/{project_id}/security-group-rules/{security_group_rule_id}"})
    public SecurityGroupRuleJson updateSecurityGroupRule(@PathVariable("project_id") String projectId,
                                                 @PathVariable("security_group_rule_id") String securityGroupRuleId,
                                                 @RequestBody SecurityGroupRuleJson securityGroupRuleJson) throws Exception {
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(securityGroupRuleJson);
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(securityGroupRuleId);
        SecurityGroupRule securityGroupRule = securityGroupRuleJson.getSecurityGroupRule();
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(securityGroupRule);

        return securityGroupRuleService.updateSecurityGroupRule(projectId, securityGroupRuleId, securityGroupRuleJson);
    }*/

    @DeleteMapping({"/project/{project_id}/security-group-rules/{security_group_rule_id}", "v4/{project_id}/security-group-rules/{security_group_rule_id}"})
    public void deleteSecurityGroupRule(@PathVariable("project_id") String projectId,
                                    @PathVariable("security_group_rule_id") String securityGroupRuleId) throws Exception {
        checkProjectId(projectId);
        checkSecurityGroupRuleId(securityGroupRuleId);

        securityGroupRuleService.deleteSecurityGroupRule(securityGroupRuleId);
    }

    @GetMapping({"/project/{project_id}/security-group-rules/{security_group_rule_id}", "v4/{project_id}/security-group-rules/{security_group_rule_id}"})
    public SecurityGroupRuleJson getSecurityGroupRule(@PathVariable("project_id") String projectId,
                                              @PathVariable("security_group_rule_id") String securityGroupRuleId) throws Exception {
        checkProjectId(projectId);
        checkSecurityGroupRuleId(securityGroupRuleId);

        return securityGroupRuleService.getSecurityGroupRule(securityGroupRuleId);
    }

    @GetMapping({"/project/{project_id}/security-group-rules", "v4/{project_id}/security-group-rules"})
    public List<SecurityGroupRuleJson> listSecurityGroupRule(@PathVariable("project_id") String projectId) throws Exception {
        checkProjectId(projectId);

        return securityGroupRuleService.listSecurityGroupRule();
    }
}
