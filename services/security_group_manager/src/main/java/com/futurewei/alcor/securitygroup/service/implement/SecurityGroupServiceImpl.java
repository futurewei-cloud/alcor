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

import com.futurewei.alcor.securitygroup.exception.*;
import com.futurewei.alcor.securitygroup.repo.SecurityGroupBindingsRepository;
import com.futurewei.alcor.securitygroup.repo.SecurityGroupRepository;
import com.futurewei.alcor.securitygroup.service.SecurityGroupService;
import com.futurewei.alcor.securitygroup.utils.TimeUtil;
import com.futurewei.alcor.web.entity.port.PortSecurityGroupsJson;
import com.futurewei.alcor.web.entity.securitygroup.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SecurityGroupServiceImpl implements SecurityGroupService {
    private static final Logger LOG = LoggerFactory.getLogger(SecurityGroupServiceImpl.class);

    @Autowired
    private SecurityGroupRepository securityGroupRepository;

    @Autowired
    private SecurityGroupBindingsRepository securityGroupBindingsRepository;

    private boolean isDefaultSecurityGroup(SecurityGroup securityGroup) {
        return "default".equals(securityGroup.getName());
    }

    private void createDefaultSecurityGroupRules(SecurityGroup securityGroup, SecurityGroupRule.Direction direction) {
        List<SecurityGroupRule> securityGroupRules = new ArrayList<>();
        List<SecurityGroupRule.EtherType> etherTypes = Arrays.asList(SecurityGroupRule.EtherType.IPV4, SecurityGroupRule.EtherType.IPV6);

        for (SecurityGroupRule.EtherType etherType : etherTypes) {
            SecurityGroupRule securityGroupRule = new SecurityGroupRule();

            securityGroupRule.setId(UUID.randomUUID().toString());
            securityGroupRule.setEtherType(etherType.getType());
            securityGroupRule.setTenantId(securityGroup.getTenantId());
            securityGroupRule.setSecurityGroupId(securityGroup.getId());
            securityGroupRule.setDirection(direction.getDirection());

            securityGroupRules.add(securityGroupRule);
        }

        securityGroup.setSecurityGroupRules(securityGroupRules);
    }

    private void createDefaultSecurityGroup(String tenantId, String projectId, String description) throws Exception {
        SecurityGroup defaultSecurityGroup = new SecurityGroup();
        defaultSecurityGroup.setId(UUID.randomUUID().toString());
        defaultSecurityGroup.setTenantId(tenantId);
        defaultSecurityGroup.setProjectId(projectId);
        defaultSecurityGroup.setDescription(description);
        defaultSecurityGroup.setName("default");

        //Create default security group rules
        createDefaultSecurityGroupRules(defaultSecurityGroup, SecurityGroupRule.Direction.INGRESS);

        //Set create and update time for securityGroup
        String currentTime = TimeUtil.getCurrentTime();
        defaultSecurityGroup.setCreateAt(currentTime);
        defaultSecurityGroup.setUpdateAt(currentTime);

        //Persist securityGroup
        securityGroupRepository.createDefaultSecurityGroup(defaultSecurityGroup);
    }

    @Override
    public SecurityGroupJson createSecurityGroup(SecurityGroupJson securityGroupJson) throws Exception {
        SecurityGroup securityGroup = securityGroupJson.getSecurityGroup();
        String tenantId = securityGroup.getTenantId();
        String projectId = securityGroup.getProjectId();
        String description = securityGroup.getDescription();

        //Create default security group if not exist
        SecurityGroup defaultSecurityGroup = securityGroupRepository.getSecurityGroup(tenantId);
        if (isDefaultSecurityGroup(securityGroup) && defaultSecurityGroup != null) {
            throw new DefaultSecurityGroupExists();
        }

        if (!isDefaultSecurityGroup(securityGroup)) {
            //Default security group not exists, create it
            if (defaultSecurityGroup == null) {
                createDefaultSecurityGroup(tenantId, projectId, null);
            }

            //Generate uuid for securityGroup
            if (securityGroup.getId() == null) {
                securityGroup.setId(UUID.randomUUID().toString());
            }

            //Create default security group rule for securityGroup
            createDefaultSecurityGroupRules(securityGroup, SecurityGroupRule.Direction.EGRESS);

            //Set create and update time for securityGroup
            String currentTime = TimeUtil.getCurrentTime();
            securityGroup.setCreateAt(currentTime);
            securityGroup.setUpdateAt(currentTime);

            //Persist securityGroup
            securityGroupRepository.addSecurityGroup(securityGroup);
        } else {
            createDefaultSecurityGroup(tenantId, projectId, description);
        }

        LOG.info("Create security group success, securityGroupJson: {}", securityGroupJson);

        return securityGroupJson;
    }

    @Override
    public SecurityGroupBulkJson createSecurityGroupBulk(String tenantId, String projectId, SecurityGroupBulkJson securityGroupBulkJson) throws Exception {
        List<SecurityGroup> securityGroups = securityGroupBulkJson.getSecurityGroups();
        String currentTime = TimeUtil.getCurrentTime();
        SecurityGroup defaultSecurityGroup = null;

        for (SecurityGroup securityGroup : securityGroups) {
            if (isDefaultSecurityGroup(securityGroup)) {
                if (defaultSecurityGroup != null) {
                    throw new DefaultSecurityGroupNotUnique();
                }

                defaultSecurityGroup = securityGroup;
            }

            //Generate uuid for securityGroup
            if (securityGroup.getId() == null) {
                securityGroup.setId(UUID.randomUUID().toString());
            }

            //Create default security group rule for securityGroup
            createDefaultSecurityGroupRules(securityGroup, SecurityGroupRule.Direction.EGRESS);

            //Set create and update time for securityGroup
            securityGroup.setCreateAt(currentTime);
            securityGroup.setUpdateAt(currentTime);
        }

        SecurityGroup oldDefaultSecurityGroup = securityGroupRepository.getSecurityGroup(tenantId);
        if (defaultSecurityGroup != null) {
            if (oldDefaultSecurityGroup != null) {
                throw new DefaultSecurityGroupExists();
            }

            String description = defaultSecurityGroup.getDescription();
            createDefaultSecurityGroup(tenantId, projectId, description);
        } else if (oldDefaultSecurityGroup == null) {
            createDefaultSecurityGroup(tenantId, projectId, null);
        }

        securityGroupRepository.addSecurityGroupBulk(securityGroups);

        LOG.info("Create security group bulk success, securityGroupBulkJson: {}", securityGroupBulkJson);

        return securityGroupBulkJson;
    }

    @Override
    public SecurityGroupJson updateSecurityGroup(String securityGroupId, SecurityGroupJson securityGroupJson) throws Exception {
        SecurityGroup securityGroup = securityGroupJson.getSecurityGroup();
        SecurityGroup oldSecurityGroup = securityGroupRepository.getSecurityGroup(securityGroupId);
        if (oldSecurityGroup == null) {
            throw new SecurityGroupRequired();
        }

        if (oldSecurityGroup.getName().equals("default") && securityGroup.getName() != null) {
            throw new ModificationNotAllowed();
        }

        //Only name and description updates are supported
        if (securityGroup.getName() != null) {
            oldSecurityGroup.setName(securityGroup.getName());
        }

        if (securityGroup.getDescription() != null) {
            oldSecurityGroup.setDescription(securityGroup.getDescription());
        }

        //Set update time for securityGroup
        String currentTime = TimeUtil.getCurrentTime();
        oldSecurityGroup.setUpdateAt(currentTime);

        securityGroupRepository.addSecurityGroup(oldSecurityGroup);
        securityGroupJson.setSecurityGroup(oldSecurityGroup);

        LOG.info("Update security group success, securityGroupJson: {}", securityGroupJson);

        return securityGroupJson;
    }

    private Set<String> getSecurityGroupBindings(String SecurityGroupId) throws Exception {
        SecurityGroupBindings securityGroupBindings = securityGroupBindingsRepository.getSecurityGroupBindings(SecurityGroupId);
        if (securityGroupBindings == null || securityGroupBindings.getBindings().size() == 0) {
            return null;
        }

        return securityGroupBindings.getBindings();
    }

    @Override
    public void deleteSecurityGroup(String securityGroupId) throws Exception {
        SecurityGroup securityGroup = securityGroupRepository.getSecurityGroup(securityGroupId);
        if (securityGroup == null) {
            throw new SecurityGroupRequired();
        }

        if (getSecurityGroupBindings(securityGroupId) != null) {
            throw new SecurityGroupHasBindings();
        }

        if (securityGroup.getName().equals("default")) {
            securityGroupRepository.deleteDefaultSecurityGroup(securityGroupId);
        } else {
            securityGroupRepository.deleteSecurityGroup(securityGroupId);
        }

        LOG.info("Delete security group success, securityGroupId: {}", securityGroupId);
    }

    @Override
    public SecurityGroupJson getSecurityGroup(String SecurityGroupId) throws Exception {
        SecurityGroup securityGroup = securityGroupRepository.getSecurityGroup(SecurityGroupId);
        if (securityGroup == null) {
            throw new SecurityGroupNotFound();
        }

        LOG.info("Get security group success, securityGroup: {}", securityGroup);

        return new SecurityGroupJson(securityGroup);
    }

    @Override
    public SecurityGroupJson getDefaultSecurityGroup(String projectId, String tenantId) throws Exception {
        SecurityGroup defaultSecurityGroup = securityGroupRepository.getSecurityGroup(tenantId);
        if (defaultSecurityGroup == null) {
            createDefaultSecurityGroup(tenantId, projectId, null);

            defaultSecurityGroup = securityGroupRepository.getSecurityGroup(tenantId);
            if (defaultSecurityGroup == null) {
                throw new DefaultSecurityGroupNotFound();
            }
        }

        LOG.info("Get default security group success, defaultSecurityGroup: {}", defaultSecurityGroup);

        return new SecurityGroupJson(defaultSecurityGroup);
    }

    @Override
    public SecurityGroupsJson listSecurityGroup() throws Exception {
        List<SecurityGroup> securityGroups = new ArrayList<>();

        Map<String, SecurityGroup> securityGroupMap = securityGroupRepository.getAllSecurityGroups();
        if (securityGroupMap == null) {
            return new SecurityGroupsJson();
        }

        for (Map.Entry<String, SecurityGroup> entry : securityGroupMap.entrySet()) {
            //Skip the internal default security group
            if (entry.getKey().equals(entry.getValue().getTenantId())) {
                continue;
            }

            securityGroups.add(entry.getValue());
        }

        LOG.info("List security group success");

        return new SecurityGroupsJson(securityGroups);
    }

    @Override
    public SecurityGroupsJson listSecurityGroup(Map<String, Object[]> queryParams) throws Exception {
        List<SecurityGroup> securityGroups = new ArrayList<>();
        Map<String, SecurityGroup> securityGroupMap = securityGroupRepository.getAllSecurityGroups(queryParams);

        if (securityGroupMap == null) {
            return new SecurityGroupsJson();
        }

        for (Map.Entry<String, SecurityGroup> entry : securityGroupMap.entrySet()) {
            //Skip the internal default security group
            if (entry.getKey().equals(entry.getValue().getTenantId())) {
                continue;
            }

            securityGroups.add(entry.getValue());
        }

        LOG.info("List security group success");
        return new SecurityGroupsJson(securityGroups);
    }

    @Override
    public PortSecurityGroupsJson bindSecurityGroups(PortSecurityGroupsJson portSecurityGroupsJson) throws Exception {
        securityGroupBindingsRepository.addSecurityGroupBinding(portSecurityGroupsJson);

        LOG.info("Bind security groups success, portSecurityGroupsJson: {}", portSecurityGroupsJson);

        return portSecurityGroupsJson;
    }

    @Override
    public PortSecurityGroupsJson unbindSecurityGroups(PortSecurityGroupsJson portSecurityGroupsJson) throws Exception {
        securityGroupBindingsRepository.deleteSecurityGroupBinding(portSecurityGroupsJson);

        LOG.info("Unbind security groups success, portSecurityGroupsJson: {}", portSecurityGroupsJson);

        return portSecurityGroupsJson;
    }
}
