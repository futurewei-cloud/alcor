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
import com.futurewei.alcor.common.utils.RestPreconditionsUtil;
import com.futurewei.alcor.securitygroup.exception.*;
import com.futurewei.alcor.securitygroup.service.SecurityGroupRuleService;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupRule;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupRuleJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
public class SecurityGroupRuleController {
    @Autowired
    private SecurityGroupRuleService securityGroupRuleService;

    private void verifyPortRange(SecurityGroupRule securityGroupRule) throws Exception {
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
    private void verifyRemoteIpPrefix(SecurityGroupRule securityGroupRule) throws Exception {
        String remoteIpPrefix = securityGroupRule.getRemoteIpPrefix();
        String etherType = securityGroupRule.getEtherType();

        if (!getIpVersionByPrefix(remoteIpPrefix).equals(etherType)) {
            throw new EtherTypeRemoteIpPrefixConflict();
        }
    }

    private void verifyProtocolAndEtherType(SecurityGroupRule securityGroupRule) throws Exception {
        String protocol = securityGroupRule.getProtocol();
        String etherType = securityGroupRule.getEtherType();

        List<String> ipv6Protocols = Arrays.asList("icmpv6");
        if (ipv6Protocols.contains(protocol) &&
                SecurityGroupRule.EtherType.IPV4.getType().equals(etherType)) {
            throw new ProtocolEtherTypeConflict();
        }
    }

    @PostMapping({"/project/{project_id}/security-group-rules", "v4/{project_id}/security-group-rules"})
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public SecurityGroupRuleJson createSecurityGroupRule(@PathVariable("project_id") String projectId,
                                                     @RequestBody SecurityGroupRuleJson securityGroupRuleJson) throws Exception {
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(securityGroupRuleJson);
        SecurityGroupRule securityGroupRule = securityGroupRuleJson.getSecurityGroupRule();
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(securityGroupRule);
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(securityGroupRule.getTenantId());
        securityGroupRule.setProjectId(projectId);

        //Verify port range
        if (securityGroupRule.getPortRangeMax() != null || securityGroupRule.getPortRangeMin() != null) {
            verifyPortRange(securityGroupRule);
        }

        //Verify ip prefix
        if (securityGroupRule.getRemoteIpPrefix() != null) {
            verifyRemoteIpPrefix(securityGroupRule);
        }

        //Verify protocol and ether type
        if (securityGroupRule.getProtocol() != null && securityGroupRule.getEtherType() != null) {
            verifyProtocolAndEtherType(securityGroupRule);
        }

        if (securityGroupRule.getRemoteGroupId() != null && securityGroupRule.getRemoteIpPrefix() != null) {
            throw new RemoteIpPrefixRemoteGroupIdConflict();
        }

        return securityGroupRuleService.createSecurityGroupRule(securityGroupRuleJson);
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
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(securityGroupRuleId);

        securityGroupRuleService.deleteSecurityGroupRule(securityGroupRuleId);
    }

    @GetMapping({"/project/{project_id}/security-group-rules/{security_group_rule_id}", "v4/{project_id}/security-group-rules/{security_group_rule_id}"})
    public SecurityGroupRuleJson getSecurityGroupRule(@PathVariable("project_id") String projectId,
                                              @PathVariable("security_group_rule_id") String securityGroupRuleId) throws Exception {
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(securityGroupRuleId);

        return securityGroupRuleService.getSecurityGroupRule(securityGroupRuleId);
    }

    @GetMapping({"/project/{project_id}/security-group-rules", "v4/{project_id}/security-group-rules"})
    public List<SecurityGroupRuleJson> listSecurityGroupRule(@PathVariable("project_id") String projectId) throws Exception {
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);

        return securityGroupRuleService.listSecurityGroupRule();
    }
}
