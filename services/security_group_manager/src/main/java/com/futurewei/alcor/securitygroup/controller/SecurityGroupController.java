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

import com.futurewei.alcor.common.utils.RestPreconditionsUtil;
import com.futurewei.alcor.securitygroup.service.SecurityGroupService;
import com.futurewei.alcor.web.entity.port.PortSecurityGroupsJson;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroup;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class SecurityGroupController {

    @Autowired
    private SecurityGroupService securityGroupService;

    @PostMapping({"/project/{project_id}/security-groups", "v4/{project_id}/security-groups"})
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public SecurityGroupJson createSecurityGroup(@PathVariable("project_id") String projectId,
                                             @RequestBody SecurityGroupJson securityGroupJson) throws Exception {
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(securityGroupJson);
        SecurityGroup securityGroup = securityGroupJson.getSecurityGroup();
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(securityGroup);
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(securityGroup.getTenantId());
        securityGroup.setProjectId(projectId);

        return securityGroupService.createSecurityGroup(securityGroupJson);
    }

    @PutMapping({"/project/{project_id}/security-groups/{security_group_id}", "v4/{project_id}/security-groups/{security_group_id}"})
    public SecurityGroupJson updateSecurityGroup(@PathVariable("project_id") String projectId,
                                         @PathVariable("security_group_id") String securityGroupId,
                                         @RequestBody SecurityGroupJson securityGroupJson) throws Exception {
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(securityGroupJson);
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(securityGroupId);
        SecurityGroup securityGroup = securityGroupJson.getSecurityGroup();
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(securityGroup);

        return securityGroupService.updateSecurityGroup(securityGroupId, securityGroupJson);
    }

    @DeleteMapping({"/project/{project_id}/security-groups/{security_group_id}", "v4/{project_id}/security-groups/{security_group_id}"})
    public void deleteSecurityGroup(@PathVariable("project_id") String projectId,
                                @PathVariable("security_group_id") String securityGroupId) throws Exception {
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(securityGroupId);

        securityGroupService.deleteSecurityGroup(securityGroupId);
    }

    @GetMapping({"/project/{project_id}/security-groups/{security_group_id}", "v4/{project_id}/security-groups/{security_group_id}"})
    public SecurityGroupJson getSecurityGroup(@PathVariable("project_id") String projectId,
                                      @PathVariable("security_group_id") String securityGroupId) throws Exception {
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(securityGroupId);

        return securityGroupService.getSecurityGroup(securityGroupId);
    }

    @GetMapping({"/project/{project_id}/security-groups", "v4/{project_id}/security-groups"})
    public List<SecurityGroupJson> listSecurityGroup(@PathVariable("project_id") String projectId) throws Exception {
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);

        return securityGroupService.listSecurityGroup();
    }

    private void verifyPortSecurityGroups(String projectId, PortSecurityGroupsJson portSecurityGroupsJson) throws Exception {
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(projectId);
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(portSecurityGroupsJson);
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(portSecurityGroupsJson.getPortId());
        RestPreconditionsUtil.verifyParameterNotNullorEmpty(portSecurityGroupsJson.getSecurityGroups());

        for (String securityGroup: portSecurityGroupsJson.getSecurityGroups()) {
            RestPreconditionsUtil.verifyParameterNotNullorEmpty(securityGroup);
        }
    }

    @PostMapping({"/project/{project_id}/bind-security-groups", "v4/{project_id}/bind-security-groups"})
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public PortSecurityGroupsJson bindSecurityGroups(@PathVariable("project_id") String projectId,
                                                              @RequestBody PortSecurityGroupsJson portSecurityGroupsJson) throws Exception {
        verifyPortSecurityGroups(projectId, portSecurityGroupsJson);

        return securityGroupService.bindSecurityGroups(portSecurityGroupsJson);
    }

    @PostMapping({"/project/{project_id}/unbind-security-groups", "v4/{project_id}/unbind-security-groups"})
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public PortSecurityGroupsJson unbindSecurityGroups(@PathVariable("project_id") String projectId,
                                                              @RequestBody PortSecurityGroupsJson portSecurityGroupsJson) throws Exception {
        verifyPortSecurityGroups(projectId, portSecurityGroupsJson);

        return securityGroupService.unbindSecurityGroups(portSecurityGroupsJson);
    }
}
