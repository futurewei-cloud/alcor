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
    @DurationStatistics
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
