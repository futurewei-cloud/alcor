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

package com.futurewei.alcor.securitygroup.controller;

import com.futurewei.alcor.common.utils.ControllerUtil;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.securitygroup.service.SecurityGroupRuleService;
import com.futurewei.alcor.web.entity.securitygroup.*;
import com.futurewei.alcor.web.json.annotation.FieldFilter;
import com.futurewei.alcor.web.rbac.aspect.Rbac;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static com.futurewei.alcor.common.constants.CommonConstants.QUERY_ATTR_HEADER;
import static com.futurewei.alcor.securitygroup.utils.RestParameterValidator.*;

@RestController
@ComponentScan(value = "com.futurewei.alcor.common.stats")
public class SecurityGroupRuleController {
    @Autowired
    private SecurityGroupRuleService securityGroupRuleService;

    @Autowired
    private HttpServletRequest request;

    @Rbac(resource ="security_group_rule")
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

    @Rbac(resource ="security_group_rule")
    @DeleteMapping({"/project/{project_id}/security-group-rules/{security_group_rule_id}", "v4/{project_id}/security-group-rules/{security_group_rule_id}"})
    @DurationStatistics
    public void deleteSecurityGroupRule(@PathVariable("project_id") String projectId,
                                    @PathVariable("security_group_rule_id") String securityGroupRuleId) throws Exception {
        checkProjectId(projectId);
        checkSecurityGroupRuleId(securityGroupRuleId);

        securityGroupRuleService.deleteSecurityGroupRule(securityGroupRuleId);
    }

    @Rbac(resource ="security_group_rule")
    @FieldFilter(type = SecurityGroupRule.class)
    @GetMapping({"/project/{project_id}/security-group-rules/{security_group_rule_id}", "v4/{project_id}/security-group-rules/{security_group_rule_id}"})
    @DurationStatistics
    public SecurityGroupRuleJson getSecurityGroupRule(@PathVariable("project_id") String projectId,
                                              @PathVariable("security_group_rule_id") String securityGroupRuleId) throws Exception {
        checkProjectId(projectId);
        checkSecurityGroupRuleId(securityGroupRuleId);

        return securityGroupRuleService.getSecurityGroupRule(securityGroupRuleId);
    }

    @Rbac(resource ="security_group_rule")
    @FieldFilter(type = SecurityGroupRule.class)
    @GetMapping({"/project/{project_id}/security-group-rules", "v4/{project_id}/security-group-rules"})
    @DurationStatistics
    public SecurityGroupRulesJson listSecurityGroupRule(@PathVariable("project_id") String projectId) throws Exception {
        checkProjectId(projectId);

        Map<String, String[]> requestParams = (Map<String, String[]>)request.getAttribute(QUERY_ATTR_HEADER);
        requestParams = requestParams == null ? request.getParameterMap():requestParams;
        Map<String, Object[]> queryParams =
                ControllerUtil.transformUrlPathParams(requestParams, SecurityGroupRule.class);

        return securityGroupRuleService.listSecurityGroupRule(queryParams);
    }
}
