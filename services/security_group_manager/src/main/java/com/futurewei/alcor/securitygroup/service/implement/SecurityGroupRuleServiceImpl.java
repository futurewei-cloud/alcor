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
package com.futurewei.alcor.securitygroup.service.implement;

import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.securitygroup.exception.RemoteSecurityGroupNotFound;
import com.futurewei.alcor.securitygroup.exception.SecurityGroupNotFound;
import com.futurewei.alcor.securitygroup.exception.SecurityGroupRuleNotFound;
import com.futurewei.alcor.securitygroup.repo.SecurityGroupRepository;
import com.futurewei.alcor.securitygroup.service.SecurityGroupRuleService;
import com.futurewei.alcor.web.entity.securitygroup.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SecurityGroupRuleServiceImpl implements SecurityGroupRuleService {
    private static final Logger LOG = LoggerFactory.getLogger(SecurityGroupRuleServiceImpl.class);

    @Autowired
    SecurityGroupRepository securityGroupRepository;

    @Override
    @DurationStatistics
    public SecurityGroupRuleJson createSecurityGroupRule(SecurityGroupRuleJson securityGroupRuleJson) throws Exception {
        SecurityGroupRule securityGroupRule = securityGroupRuleJson.getSecurityGroupRule();
        String remoteGroupId = securityGroupRule.getRemoteGroupId();
        if (remoteGroupId != null) {
            if (securityGroupRepository.getSecurityGroup(remoteGroupId) == null) {
                throw new RemoteSecurityGroupNotFound();
            }
        }

        String securityGroupId = securityGroupRule.getSecurityGroupId();
        SecurityGroup securityGroup = securityGroupRepository.getSecurityGroup(securityGroupId);
        if (securityGroup == null) {
            throw new SecurityGroupNotFound();
        }

        //Generate uuid for securityGroupRule
        if (securityGroupRule.getId() == null) {
            securityGroupRule.setId(UUID.randomUUID().toString());
        }

        securityGroupRepository.addSecurityGroupRule(securityGroup, securityGroupRule);

        LOG.info("Create security group rule success, securityGroupRuleJson: {}", securityGroupRuleJson);

        return securityGroupRuleJson;
    }

    @Override
    @DurationStatistics
    public SecurityGroupRuleBulkJson createSecurityGroupRuleBulk(SecurityGroupRuleBulkJson securityGroupRuleBulkJson) throws Exception {
        List<SecurityGroupRule> securityGroupRules = securityGroupRuleBulkJson.getSecurityGroupRules();
        for (SecurityGroupRule securityGroupRule: securityGroupRules) {
            String remoteGroupId = securityGroupRule.getRemoteGroupId();
            if (remoteGroupId != null) {
                if (securityGroupRepository.getSecurityGroup(remoteGroupId) == null) {
                    throw new RemoteSecurityGroupNotFound();
                }
            }

            //Generate uuid for securityGroupRule
            if (securityGroupRule.getId() == null) {
                securityGroupRule.setId(UUID.randomUUID().toString());
            }
        }

        securityGroupRepository.addSecurityGroupRuleBulk(securityGroupRules);

        LOG.info("Create security group rule bulk success, securityGroupRuleBulkJson: {}", securityGroupRuleBulkJson);

        return securityGroupRuleBulkJson;
    }

    @Override
    @DurationStatistics
    public SecurityGroupRuleJson updateSecurityGroupRule(String securityGroupRuleId, SecurityGroupRuleJson securityGroupRuleJson) throws Exception {
        return null;
    }

    @Override
    @DurationStatistics
    public void deleteSecurityGroupRule(String securityGroupRuleId) throws Exception {
        SecurityGroupRule securityGroupRule = securityGroupRepository.getSecurityGroupRule(securityGroupRuleId);
        if (securityGroupRule == null) {
            throw new SecurityGroupRuleNotFound();
        }

        securityGroupRepository.deleteSecurityGroupRule(securityGroupRule);

        LOG.info("Delete security group rule success, securityGroupRuleId: {}", securityGroupRuleId);
    }

    @Override
    @DurationStatistics
    public SecurityGroupRuleJson getSecurityGroupRule(String securityGroupRuleId) throws Exception {
        SecurityGroupRule securityGroupRule = securityGroupRepository.getSecurityGroupRule(securityGroupRuleId);
        if (securityGroupRule == null) {
            throw new SecurityGroupRuleNotFound();
        }

        LOG.info("Get security group rule success, securityGroupRule: {}", securityGroupRule);

        return new SecurityGroupRuleJson(securityGroupRule);
    }

    @Override
    @DurationStatistics
    public SecurityGroupRulesJson listSecurityGroupRule() throws Exception {
        List<SecurityGroupRule> securityGroupRules = new ArrayList<>();

        Map<String, SecurityGroupRule> securityGroupRuleMap = securityGroupRepository.getAllSecurityGroupRules();
        if (securityGroupRuleMap == null) {
            return new SecurityGroupRulesJson(securityGroupRules);
        }

        for (Map.Entry<String, SecurityGroupRule> entry: securityGroupRuleMap.entrySet()) {
            securityGroupRules.add(entry.getValue());
        }

        LOG.info("List security group rule success");

        return new SecurityGroupRulesJson(securityGroupRules);
    }

    @Override
    public SecurityGroupRulesJson listSecurityGroupRule(Map<String, Object[]> queryParams) throws Exception {
        List<SecurityGroupRule> securityGroupRules = new ArrayList<>();

        Map<String, SecurityGroupRule> securityGroupRuleMap = securityGroupRepository.getAllSecurityGroupRules(queryParams);
        if (securityGroupRuleMap == null) {
            return new SecurityGroupRulesJson(securityGroupRules);
        }

        for (Map.Entry<String, SecurityGroupRule> entry: securityGroupRuleMap.entrySet()) {
            securityGroupRules.add(entry.getValue());
        }

        LOG.info("List security group rule success");

        return new SecurityGroupRulesJson(securityGroupRules);
    }
}
