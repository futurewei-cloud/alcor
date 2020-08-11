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

import com.futurewei.alcor.common.rbac.aspect.Rbac;
import com.futurewei.alcor.common.utils.ControllerUtil;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.securitygroup.service.SecurityGroupRuleService;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupRule;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupRuleBulkJson;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupRuleJson;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupRulesJson;
import com.futurewei.alcor.web.json.annotation.FieldFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static com.futurewei.alcor.securitygroup.utils.RestParameterValidator.*;

@RestController
@ComponentScan(value = "com.futurewei.alcor.common.stats")
public class SecurityGroupRuleController {
    @Autowired
    private SecurityGroupRuleService securityGroupRuleService;

    @Autowired
    private HttpServletRequest request;

    @Rbac(name="security_group_rule")
    @PostMapping({"/project/{project_id}/security-group-rules", "v4/{project_id}/security-group-rules"})
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @DurationStatistics
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
    @DurationStatistics
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

    @Rbac(name="security_group_rule")
    @DeleteMapping({"/project/{project_id}/security-group-rules/{security_group_rule_id}", "v4/{project_id}/security-group-rules/{security_group_rule_id}"})
    @DurationStatistics
    public void deleteSecurityGroupRule(@PathVariable("project_id") String projectId,
                                    @PathVariable("security_group_rule_id") String securityGroupRuleId) throws Exception {
        checkProjectId(projectId);
        checkSecurityGroupRuleId(securityGroupRuleId);

        securityGroupRuleService.deleteSecurityGroupRule(securityGroupRuleId);
    }

    @Rbac(name="security_group_rule")
    @FieldFilter(type = SecurityGroupRule.class)
    @GetMapping({"/project/{project_id}/security-group-rules/{security_group_rule_id}", "v4/{project_id}/security-group-rules/{security_group_rule_id}"})
    @DurationStatistics
    public SecurityGroupRuleJson getSecurityGroupRule(@PathVariable("project_id") String projectId,
                                              @PathVariable("security_group_rule_id") String securityGroupRuleId) throws Exception {
        checkProjectId(projectId);
        checkSecurityGroupRuleId(securityGroupRuleId);

        return securityGroupRuleService.getSecurityGroupRule(securityGroupRuleId);
    }

    @Rbac(name="security_group_rule")
    @FieldFilter(type = SecurityGroupRule.class)
    @GetMapping({"/project/{project_id}/security-group-rules", "v4/{project_id}/security-group-rules"})
    @DurationStatistics
    public SecurityGroupRulesJson listSecurityGroupRule(@PathVariable("project_id") String projectId) throws Exception {
        checkProjectId(projectId);

        Map<String, Object[]> queryParams =
                ControllerUtil.transformUrlPathParams(request.getParameterMap(), PortEntity.class);
        ControllerUtil.handleUserRoles(request.getHeader(ControllerUtil.TOKEN_INFO_HEADER), queryParams);

        return securityGroupRuleService.listSecurityGroupRule(queryParams);
    }
}
