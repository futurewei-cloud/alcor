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
package com.futurewei.alcor.portmanager.proxy;

import com.futurewei.alcor.common.utils.SpringContextUtil;
import com.futurewei.alcor.portmanager.exception.GetSecurityGroupEntityException;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.port.PortSecurityGroupsJson;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupEntity;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupWebJson;
import com.futurewei.alcor.web.restclient.SecurityGroupManagerRestClient;

public class SecurityGroupManagerProxy {
    private SecurityGroupManagerRestClient securityGroupManagerRestClient;
    private String projectId;

    public SecurityGroupManagerProxy(String projectId) {
        securityGroupManagerRestClient = SpringContextUtil.getBean(SecurityGroupManagerRestClient.class);
        this.projectId = projectId;
    }

    public SecurityGroupEntity getSecurityGroupEntity(Object args) throws Exception {
        String securityGroupId = (String)args;
        SecurityGroupWebJson securityGroupWebJson = securityGroupManagerRestClient.getSecurityGroup(projectId, securityGroupId);
        if (securityGroupWebJson == null || securityGroupWebJson.getSecurityGroupEntity() == null) {
            throw new GetSecurityGroupEntityException();
        }

        return securityGroupWebJson.getSecurityGroupEntity();
    }

    public SecurityGroupEntity getDefaultSecurityGroupEntity(Object args) throws Exception {
        SecurityGroupWebJson securityGroupWebJson = securityGroupManagerRestClient.getDefaultSecurityGroup(projectId);
        if (securityGroupWebJson == null || securityGroupWebJson.getSecurityGroupEntity() == null) {
            throw new GetSecurityGroupEntityException();
        }

        return securityGroupWebJson.getSecurityGroupEntity();
    }

    public PortSecurityGroupsJson bindSecurityGroup(Object args) throws Exception {
        PortEntity portEntity = (PortEntity)args;

        PortSecurityGroupsJson portSecurityGroupsJson = new PortSecurityGroupsJson();
        portSecurityGroupsJson.setPortId(portEntity.getId());
        portSecurityGroupsJson.setSecurityGroups(portEntity.getSecurityGroups());

        return securityGroupManagerRestClient.bindSecurityGroups(projectId, portSecurityGroupsJson);
    }

    public PortSecurityGroupsJson unbindSecurityGroup(Object args) throws Exception {
        PortEntity portEntity = (PortEntity)args;

        PortSecurityGroupsJson portSecurityGroupsJson = new PortSecurityGroupsJson();
        portSecurityGroupsJson.setPortId(portEntity.getId());
        portSecurityGroupsJson.setSecurityGroups(portEntity.getSecurityGroups());

        return securityGroupManagerRestClient.bindSecurityGroups(projectId, portSecurityGroupsJson);
    }
}
