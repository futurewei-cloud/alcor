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

import com.futurewei.alcor.web.entity.securitygroup.PortBindingSecurityGroupsJson;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupBulkJson;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupJson;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupsJson;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface SecurityGroupService {
    SecurityGroupJson createSecurityGroup(SecurityGroupJson securityGroupJson) throws Exception;

    SecurityGroupBulkJson createSecurityGroupBulk(String tenantId, String projectId, SecurityGroupBulkJson securityGroupBulkJson) throws Exception;

    SecurityGroupJson updateSecurityGroup(String securityGroupId, SecurityGroupJson securityGroupJson) throws Exception;

    void deleteSecurityGroup(String securityGroupId) throws Exception;

    SecurityGroupJson getSecurityGroup(String securityGroupId) throws Exception;

    SecurityGroupJson getDefaultSecurityGroup(String projectId, String tenantId) throws Exception;

    SecurityGroupsJson listSecurityGroup(Map<String, Object[]> queryParams) throws Exception;

<<<<<<< HEAD
    SecurityGroupsJson listSecurityGroup(Map<String, Object[]> queryParams) throws Exception;

    PortSecurityGroupsJson bindSecurityGroups(PortSecurityGroupsJson portSecurityGroupsJson) throws Exception;
=======
    PortBindingSecurityGroupsJson bindSecurityGroups(PortBindingSecurityGroupsJson portBindingSecurityGroupsJson) throws Exception;
>>>>>>> Batch port binding security groups are supported

    PortBindingSecurityGroupsJson unbindSecurityGroups(PortBindingSecurityGroupsJson portBindingSecurityGroupsJson) throws Exception;
}
