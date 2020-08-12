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

import com.futurewei.alcor.common.utils.ControllerUtil;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.securitygroup.exception.*;
import com.futurewei.alcor.securitygroup.service.SecurityGroupService;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroup;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupBulkJson;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupJson;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupsJson;
import com.futurewei.alcor.web.json.annotation.FieldFilter;
import com.futurewei.alcor.web.entity.securitygroup.*;
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
public class SecurityGroupController {

    @Autowired
    private SecurityGroupService securityGroupService;

    @Autowired
    private HttpServletRequest request;

    @Rbac(resourceName="security_group")
    @PostMapping({"/project/{project_id}/security-groups", "v4/{project_id}/security-groups"})
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @DurationStatistics
    public SecurityGroupJson createSecurityGroup(@PathVariable("project_id") String projectId,
                                                 @RequestBody SecurityGroupJson securityGroupJson) throws Exception {
        checkProjectId(projectId);
        checkSecurityGroup(securityGroupJson);
        checkTenantId(securityGroupJson.getSecurityGroup().getTenantId());
        securityGroupJson.getSecurityGroup().setProjectId(projectId);

        return securityGroupService.createSecurityGroup(securityGroupJson);
    }

    @Rbac(resourceName="security_group")
    @PostMapping({"/project/{project_id}/security-groups/bulk", "v4/{project_id}/security-groups/bulk"})
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @DurationStatistics
    public SecurityGroupBulkJson createSecurityGroupBulk(@PathVariable("project_id") String projectId,
                                                         @RequestBody SecurityGroupBulkJson securityGroupBulkJson) throws Exception {
        checkProjectId(projectId);
        checkSecurityGroups(securityGroupBulkJson);

        String tenantId = null;
        for (SecurityGroup securityGroup : securityGroupBulkJson.getSecurityGroups()) {
            checkTenantId(securityGroup.getTenantId());
            if (tenantId == null) {
                tenantId = securityGroup.getTenantId();
            } else if (!tenantId.equals(securityGroup.getTenantId())) {
                throw new TenantIdInvalid();
            }

            securityGroup.setProjectId(projectId);
        }

        return securityGroupService.createSecurityGroupBulk(tenantId, projectId, securityGroupBulkJson);
    }

    @Rbac(resourceName="security_group")
    @PutMapping({"/project/{project_id}/security-groups/{security_group_id}", "v4/{project_id}/security-groups/{security_group_id}"})
    @DurationStatistics
    public SecurityGroupJson updateSecurityGroup(@PathVariable("project_id") String projectId,
                                                 @PathVariable("security_group_id") String securityGroupId,
                                                 @RequestBody SecurityGroupJson securityGroupJson) throws Exception {
        checkProjectId(projectId);
        checkSecurityGroup(securityGroupJson);
        checkSecurityGroupId(securityGroupId);

        return securityGroupService.updateSecurityGroup(securityGroupId, securityGroupJson);
    }

    @Rbac(resourceName="security_group")
    @DeleteMapping({"/project/{project_id}/security-groups/{security_group_id}", "v4/{project_id}/security-groups/{security_group_id}"})
    @DurationStatistics
    public void deleteSecurityGroup(@PathVariable("project_id") String projectId,
                                    @PathVariable("security_group_id") String securityGroupId) throws Exception {
        checkProjectId(projectId);
        checkSecurityGroupId(securityGroupId);

        securityGroupService.deleteSecurityGroup(securityGroupId);
    }

    @Rbac(resourceName="security_group")
    @FieldFilter(type=SecurityGroup.class)
    @GetMapping({"/project/{project_id}/security-groups/{security_group_id}", "v4/{project_id}/security-groups/{security_group_id}"})
    @DurationStatistics
    public SecurityGroupJson getSecurityGroup(@PathVariable("project_id") String projectId,
                                              @PathVariable("security_group_id") String securityGroupId) throws Exception {
        checkProjectId(projectId);
        checkSecurityGroupId(securityGroupId);

        return securityGroupService.getSecurityGroup(securityGroupId);
    }

    @GetMapping({"/project/{project_id}/security-groups/default/{tenant_id}", "v4/{project_id}/security-groups/default/{tenant_id}}"})
    @DurationStatistics
    public SecurityGroupJson getDefaultSecurityGroup(@PathVariable("project_id") String projectId,
                                                     @PathVariable("tenant_id") String tenantId) throws Exception {
        checkProjectId(projectId);
        checkTenantId(tenantId);

        return securityGroupService.getDefaultSecurityGroup(projectId, tenantId);
    }

    @Rbac(resourceName ="security_group")
    @FieldFilter(type = SecurityGroup.class)
    @GetMapping({"/project/{project_id}/security-groups", "v4/{project_id}/security-groups"})
    @DurationStatistics
    public SecurityGroupsJson listSecurityGroup(@PathVariable("project_id") String projectId) throws Exception {
        checkProjectId(projectId);

        Map<String, String[]> requestParams = (Map<String, String[]>)request.getAttribute(QUERY_ATTR_HEADER);
        Map<String, Object[]> queryParams =
                ControllerUtil.transformUrlPathParams(requestParams, SecurityGroup.class);

        return securityGroupService.listSecurityGroup(queryParams);
    }

    @PostMapping({"/project/{project_id}/bind-security-groups", "v4/{project_id}/bind-security-groups"})
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @DurationStatistics
    public PortBindingSecurityGroupsJson bindSecurityGroups(@PathVariable("project_id") String projectId,
                                                            @RequestBody PortBindingSecurityGroupsJson portBindingSecurityGroupsJson) throws Exception {
        checkProjectId(projectId);
        checkPortSecurityGroups(portBindingSecurityGroupsJson);

        return securityGroupService.bindSecurityGroups(portBindingSecurityGroupsJson);
    }

    @PostMapping({"/project/{project_id}/unbind-security-groups", "v4/{project_id}/unbind-security-groups"})
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @DurationStatistics
    public PortBindingSecurityGroupsJson unbindSecurityGroups(@PathVariable("project_id") String projectId,
                                                              @RequestBody PortBindingSecurityGroupsJson portBindingSecurityGroupsJson) throws Exception {
        checkProjectId(projectId);
        checkPortSecurityGroups(portBindingSecurityGroupsJson);

        return securityGroupService.unbindSecurityGroups(portBindingSecurityGroupsJson);
    }
}
