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
import com.futurewei.alcor.portmanager.request.IRestRequest;
import com.futurewei.alcor.portmanager.request.UnbindSecurityGroupRequest;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.port.PortSecurityGroupsJson;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SecurityGroupProcessor extends AbstractProcessor {
    public void fetchSecurityGroupCallback(IRestRequest request) {
        List<SecurityGroup> securityGroups = ((FetchSecurityGroupRequest) request)
                .getSecurityGroups();
        request.getContext().getNetworkConfig().setSecurityGroups(securityGroups);
    }

    @Override
    void createProcess(PortContext context) {
        List<PortSecurityGroupsJson> portSecurityGroups = new ArrayList<>();
        Set<String> securityGroupIdSet = new HashSet<>();
        Set<String> defaultSecurityGroupIdSet = new HashSet<>();

        for (PortEntity portEntity: context.getPortEntities()){
            List<String> securityGroupIds = portEntity.getSecurityGroups();
            if (securityGroupIds != null) {
                securityGroupIdSet.addAll(securityGroupIds);
                portSecurityGroups.add(new PortSecurityGroupsJson(portEntity.getId(), securityGroupIds));
            } else {
                defaultSecurityGroupIdSet.add(portEntity.getTenantId());
            }
        }

        //Bind security groups
        if (portSecurityGroups.size() > 0) {
            IRestRequest bindSecurityGroupRequest =
                    new BindSecurityGroupRequest(context, portSecurityGroups);
            context.getRequestManager().sendRequestAsync(bindSecurityGroupRequest, null);
        }

        //Get security groups (include default security group)
        if (securityGroupIdSet.size() > 0 || defaultSecurityGroupIdSet.size() > 0) {
            IRestRequest fetchSecurityGroupRequest =
                    new FetchSecurityGroupRequest(context,
                            new ArrayList<>(securityGroupIdSet),
                            new ArrayList<>(defaultSecurityGroupIdSet));
            context.getRequestManager().sendRequestAsync(
                    fetchSecurityGroupRequest, this::fetchSecurityGroupCallback);
        }
    }

    @Override
    void updateProcess(PortContext context) {

    }

    @Override
    void deleteProcess(PortContext context) {
        List<PortSecurityGroupsJson> portSecurityGroupsJsons = new ArrayList<>();
        Set<String> securityGroupIdSet = new HashSet<>();
        Set<String> defaultSecurityGroupIdSet = new HashSet<>();

        for (PortEntity portEntity: context.getPortEntities()) {
            List<String> securityGroupIds = portEntity.getSecurityGroups();
            if (securityGroupIds != null) {
                securityGroupIdSet.addAll(securityGroupIds);

                PortSecurityGroupsJson portSecurityGroupsJson = new PortSecurityGroupsJson();
                portSecurityGroupsJson.setPortId(portEntity.getId());
                portSecurityGroupsJson.setSecurityGroups(portEntity.getSecurityGroups());
                portSecurityGroupsJsons.add(portSecurityGroupsJson);
            } else {
                defaultSecurityGroupIdSet.add(portEntity.getTenantId());
            }
        }

        //Unbind security group
        if (portSecurityGroupsJsons.size() > 0) {
            IRestRequest unbindSecurityGroupRequest =
                    new UnbindSecurityGroupRequest(context, portSecurityGroupsJsons);
            context.getRequestManager().sendRequestAsync(unbindSecurityGroupRequest, null);
        }

        //Get security groups (include default security group)
        if (securityGroupIdSet.size() > 0 || defaultSecurityGroupIdSet.size() > 0) {
            IRestRequest fetchSecurityGroupRequest =
                    new FetchSecurityGroupRequest(context,
                            new ArrayList<>(securityGroupIdSet),
                            new ArrayList<>(defaultSecurityGroupIdSet));
            context.getRequestManager().sendRequestAsync(
                    fetchSecurityGroupRequest, this::fetchSecurityGroupCallback);
        }
    }
}
