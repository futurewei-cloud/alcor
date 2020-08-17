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
package com.futurewei.alcor.securitygroup.service;

import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupRuleBulkJson;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupRuleJson;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupRulesJson;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface SecurityGroupRuleService {
    SecurityGroupRuleJson createSecurityGroupRule(SecurityGroupRuleJson securityGroupRuleJson) throws Exception;

    SecurityGroupRuleBulkJson createSecurityGroupRuleBulk(SecurityGroupRuleBulkJson securityGroupRuleBulkJson) throws Exception;

    SecurityGroupRuleJson updateSecurityGroupRule(String securityGroupRuleId, SecurityGroupRuleJson securityGroupRuleJson) throws Exception;

    void deleteSecurityGroupRule(String securityGroupRuleId) throws Exception;

    SecurityGroupRuleJson getSecurityGroupRule(String securityGroupRuleId) throws Exception;

    SecurityGroupRulesJson listSecurityGroupRule() throws Exception;

    SecurityGroupRulesJson listSecurityGroupRule(Map<String, Object[]> queryParams) throws Exception;
}
