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

import java.util.*;

public class SecurityGroupProcessor extends AbstractProcessor {
    public void fetchSecurityGroupCallback(IRestRequest request) {
        List<SecurityGroup> securityGroups = ((FetchSecurityGroupRequest) request)
                .getSecurityGroups();
        request.getContext().getNetworkConfig().setSecurityGroups(securityGroups);
    }

    private void getSecurityGroups(PortContext context, List<String> securityGroupIds,
                                   List<String> defaultSecurityGroupIds) {
        IRestRequest fetchSecurityGroupRequest = new FetchSecurityGroupRequest(
                context, securityGroupIds, defaultSecurityGroupIds);

        context.getRequestManager().sendRequestAsync(
                fetchSecurityGroupRequest, this::fetchSecurityGroupCallback);
    }

    private void bindSecurityGroups(PortContext context,
                                    List<PortSecurityGroupsJson> bindSecurityGroups) {
        IRestRequest bindSecurityGroupRequest =
                new BindSecurityGroupRequest(context, bindSecurityGroups);
        context.getRequestManager().sendRequestAsync(bindSecurityGroupRequest, null);
    }

    private void unbindSecurityGroups(PortContext context,
                                      List<PortSecurityGroupsJson> unbindSecurityGroups) {
        IRestRequest unbindSecurityGroupRequest =
                new UnbindSecurityGroupRequest(context, unbindSecurityGroups);
        context.getRequestManager().sendRequestAsync(unbindSecurityGroupRequest, null);
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
            bindSecurityGroups(context, portSecurityGroups);
        }

        //Get security groups (include default security group)
        if (securityGroupIdSet.size() > 0 || defaultSecurityGroupIdSet.size() > 0) {
            getSecurityGroups(context, new ArrayList<>(securityGroupIdSet),
                    new ArrayList<>(defaultSecurityGroupIdSet));
        }
    }

    @Override
    void updateProcess(PortContext context) {
        PortEntity newPortEntity = context.getNewPortEntity();
        PortEntity oldPortEntity = context.getOldPortEntity();

        List<String> newSecurityGroups = newPortEntity.getSecurityGroups();
        List<String> oldSecurityGroups = oldPortEntity.getSecurityGroups();

        if (newSecurityGroups != null && !newSecurityGroups.equals(oldSecurityGroups)) {
            List<PortSecurityGroupsJson> bindSecurityGroups = new ArrayList<>();
            bindSecurityGroups.add(new PortSecurityGroupsJson(newPortEntity.getId(), newSecurityGroups));

            if (newSecurityGroups.size() > 0) {
                //Bind new security group
                bindSecurityGroups(context, bindSecurityGroups);

                //Get new security group
                getSecurityGroups(context, newSecurityGroups, null);
            } else { //Get default security group
                getSecurityGroups(context, null,
                        Collections.singletonList(newPortEntity.getTenantId()));
            }

            //Unbind old security group
            if (oldSecurityGroups != null && oldSecurityGroups.size() > 0) {
                List<PortSecurityGroupsJson> unbindSecurityGroups = new ArrayList<>();
                unbindSecurityGroups.add(new PortSecurityGroupsJson(
                        oldPortEntity.getId(), oldSecurityGroups));

                unbindSecurityGroups(context, unbindSecurityGroups);
            }

            oldPortEntity.setSecurityGroups(newSecurityGroups);
        }
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
            unbindSecurityGroups(context, portSecurityGroupsJsons);
        }

        //Get security groups (include default security group)
        if (securityGroupIdSet.size() > 0 || defaultSecurityGroupIdSet.size() > 0) {
            getSecurityGroups(context, new ArrayList<>(securityGroupIdSet),
                    new ArrayList<>(defaultSecurityGroupIdSet));
        }
    }
}
