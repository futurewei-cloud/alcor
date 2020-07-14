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
package com.futurewei.alcor.portmanager.processor;

import com.futurewei.alcor.portmanager.request.BindSecurityGroupRequest;
import com.futurewei.alcor.portmanager.request.FetchSecurityGroupRequest;
import com.futurewei.alcor.portmanager.request.UpstreamRequest;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.port.PortSecurityGroupsJson;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SecurityGroupProcessor extends AbstractProcessor {
    public void fetchSecurityGroupCallback(UpstreamRequest request) {
        List<SecurityGroup> securityGroups = ((FetchSecurityGroupRequest) request)
                .getSecurityGroups();
        networkConfig.setSecurityGroups(securityGroups);
    }

    @Override
    void createProcess(List<PortEntity> portEntities) {
        List<PortSecurityGroupsJson> portSecurityGroups = new ArrayList<>();
        Set<String> securityGroupIdSet = new HashSet<>();
        Set<String> defaultSecurityGroupIdSet = new HashSet<>();

        portEntities.stream().forEach((p) -> {
            List<String> securityGroupIds = p.getSecurityGroups();
            if (securityGroupIds != null) {
                securityGroupIdSet.addAll(securityGroupIds);
                portSecurityGroups.add(new PortSecurityGroupsJson(p.getId(), securityGroupIds));
            } else {
                defaultSecurityGroupIdSet.add(p.getTenantId()); //default security group
            }
        });

        //Bind security groups
        if (portSecurityGroups.size() > 0) {
            UpstreamRequest bindSecurityGroupRequest =
                    new BindSecurityGroupRequest(projectId, portSecurityGroups);
            sendRequest(bindSecurityGroupRequest, null);
        }

        //Get security groups (include default security group)
        if (securityGroupIdSet.size() > 0 || defaultSecurityGroupIdSet.size() > 0) {
            UpstreamRequest fetchSecurityGroupRequest =
                    new FetchSecurityGroupRequest(projectId,
                            new ArrayList<>(securityGroupIdSet),
                            new ArrayList<>(defaultSecurityGroupIdSet));
            sendRequest(fetchSecurityGroupRequest, this::fetchSecurityGroupCallback);
        }
    }

    @Override
    void updateProcess(String portId, PortEntity portEntity) {

    }
}
