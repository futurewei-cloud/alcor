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

    @Rbac(resource = "security_group")
    @PostMapping({"/project/{project_id}/security-groups", "v4/{project_id}/security-groups"})
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @DurationStatistics
    public SecurityGroupJson createSecurityGroup(@PathVariable("project_id") String projectId,
                                                 @RequestBody SecurityGroupJson securityGroupJson) throws Exception {
        checkProjectId(projectId);
        checkSecurityGroup(securityGroupJson);
        String tenantId = checkOrAssignTenantId(securityGroupJson.getSecurityGroup().getTenantId(), projectId);
        securityGroupJson.getSecurityGroup().setProjectId(projectId);
        securityGroupJson.getSecurityGroup().setTenantId(tenantId);

        return securityGroupService.createSecurityGroup(securityGroupJson);
    }

    @Rbac(resource = "security_group")
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
            tenantId = checkOrAssignTenantId(securityGroup.getTenantId(), projectId);
            if (tenantId == null) {
                tenantId = securityGroup.getTenantId();
            } else if (!tenantId.equals(securityGroup.getTenantId())) {
                throw new TenantIdInvalid();
            }

            securityGroup.setProjectId(projectId);
        }

        return securityGroupService.createSecurityGroupBulk(tenantId, projectId, securityGroupBulkJson);
    }

    @Rbac(resource = "security_group")
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

    @Rbac(resource = "security_group")
    @DeleteMapping({"/project/{project_id}/security-groups/{security_group_id}", "v4/{project_id}/security-groups/{security_group_id}"})
    @DurationStatistics
    public void deleteSecurityGroup(@PathVariable("project_id") String projectId,
                                    @PathVariable("security_group_id") String securityGroupId) throws Exception {
        checkProjectId(projectId);
        checkSecurityGroupId(securityGroupId);

        securityGroupService.deleteSecurityGroup(securityGroupId);
    }

    @Rbac(resource = "security_group")
    @FieldFilter(type = SecurityGroup.class)
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
        String assignedTenantId = checkOrAssignTenantId(tenantId, projectId);

        return securityGroupService.getDefaultSecurityGroup(projectId, assignedTenantId);
    }

    @Rbac(resource = "security_group")
    @FieldFilter(type = SecurityGroup.class)
    @GetMapping({"/project/{project_id}/security-groups", "v4/{project_id}/security-groups"})
    @DurationStatistics
    public SecurityGroupsJson listSecurityGroup(@PathVariable("project_id") String projectId) throws Exception {
        checkProjectId(projectId);

        Map<String, String[]> requestParams = (Map<String, String[]>) request.getAttribute(QUERY_ATTR_HEADER);
        requestParams = requestParams == null ? request.getParameterMap() : requestParams;
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
