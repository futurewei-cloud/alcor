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
import com.futurewei.alcor.portmanager.util.ArrayUtil;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.securitygroup.PortBindingSecurityGroup;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroup;

import java.util.*;

public class SecurityGroupProcessor extends AbstractProcessor {
    public void fetchSecurityGroupCallback(IRestRequest request) {
        List<SecurityGroup> securityGroups = ((FetchSecurityGroupRequest) request)
                .getSecurityGroups();

        List<SecurityGroup> existSecurityGroups = request.getContext()
                .getNetworkConfig().getSecurityGroups();
        if (existSecurityGroups == null) {
            request.getContext().getNetworkConfig().setSecurityGroups(securityGroups);
        } else {
            existSecurityGroups.addAll(securityGroups);
        }
    }

    private void getSecurityGroups(PortContext context, List<String> securityGroupIds,
                                   List<String> defaultSecurityGroupIds) {
        IRestRequest fetchSecurityGroupRequest = new FetchSecurityGroupRequest(
                context, securityGroupIds, defaultSecurityGroupIds);

        context.getRequestManager().sendRequestAsync(
                fetchSecurityGroupRequest, this::fetchSecurityGroupCallback);
    }

    @FunctionalInterface
    private interface BindOrUnbindSecurityGroup {
        void apply(PortContext context, List<PortBindingSecurityGroup> bindSecurityGroups) throws Exception;
    }

    private void bindSecurityGroups(PortContext context,
                                    List<PortBindingSecurityGroup> bindSecurityGroups) {
        IRestRequest bindSecurityGroupRequest =
                new BindSecurityGroupRequest(context, bindSecurityGroups);
        context.getRequestManager().sendRequestAsync(bindSecurityGroupRequest, null);
    }

    private void unbindSecurityGroups(PortContext context,
                                      List<PortBindingSecurityGroup> unbindSecurityGroups) {
        IRestRequest unbindSecurityGroupRequest =
                new UnbindSecurityGroupRequest(context, unbindSecurityGroups);
        context.getRequestManager().sendRequestAsync(unbindSecurityGroupRequest, null);
    }

    private void securityGroupProcess(PortContext context, BindOrUnbindSecurityGroup function) throws Exception {
        List<PortBindingSecurityGroup> bindSecurityGroups = new ArrayList<>();
        Set<String> securityGroupIdSet = new HashSet<>();
        Set<String> defaultSecurityGroupIdSet = new HashSet<>();

        for (PortEntity portEntity: context.getPortEntities()){
            List<String> securityGroupIds = portEntity.getSecurityGroups();
            if (securityGroupIds != null) {
                for (String securityGroupId: securityGroupIds) {
                    securityGroupIdSet.addAll(securityGroupIds);
                    bindSecurityGroups.add(new PortBindingSecurityGroup(portEntity.getId(), securityGroupId));
                }
            } else {
                defaultSecurityGroupIdSet.add(portEntity.getTenantId());
            }
        }

        //Bind security groups
        if (bindSecurityGroups.size() > 0) {
            function.apply(context, bindSecurityGroups);
        }

        //Get security groups (include default security group)
        if (securityGroupIdSet.size() > 0 || defaultSecurityGroupIdSet.size() > 0) {
            getSecurityGroups(context, new ArrayList<>(securityGroupIdSet),
                    new ArrayList<>(defaultSecurityGroupIdSet));
        }
    }

    @Override
    void createProcess(PortContext context) throws Exception {
        securityGroupProcess(context, this::bindSecurityGroups);
    }

    @Override
    void updateProcess(PortContext context) {
        PortEntity newPortEntity = context.getNewPortEntity();
        PortEntity oldPortEntity = context.getOldPortEntity();

        List<String> newSecurityGroups = newPortEntity.getSecurityGroups();
        List<String> oldSecurityGroups = oldPortEntity.getSecurityGroups();

        if (newSecurityGroups != null && !newSecurityGroups.equals(oldSecurityGroups)) {
            oldPortEntity.setSecurityGroups(newSecurityGroups);

            List<String> commonSecurityGroups = ArrayUtil.findCommonItems(
                    newSecurityGroups, oldSecurityGroups);

            if (newSecurityGroups.size() > 0) {
                List<PortBindingSecurityGroup> bindSecurityGroups = new ArrayList<>();
                for (String securityGroupId: newSecurityGroups) {
                    bindSecurityGroups.add(new PortBindingSecurityGroup(
                            newPortEntity.getId(), securityGroupId));
                }

                getSecurityGroups(context, newSecurityGroups, null);
                bindSecurityGroups(context, bindSecurityGroups);
            } else {
                getSecurityGroups(context, null,
                        Collections.singletonList(newPortEntity.getTenantId()));
            }

            if (oldSecurityGroups != null && oldSecurityGroups.size() > 0) {
                List<PortBindingSecurityGroup> unbindSecurityGroups = new ArrayList<>();
                for (String securityGroupId: oldSecurityGroups) {
                    unbindSecurityGroups.add(new PortBindingSecurityGroup(
                            newPortEntity.getId(), securityGroupId));
                }

                unbindSecurityGroups(context, unbindSecurityGroups);
            }

            if (commonSecurityGroups.size() > 0) {
                getSecurityGroups(context, commonSecurityGroups, null);
            }
        } else if (oldSecurityGroups != null && oldSecurityGroups.size() > 0){
            getSecurityGroups(context, oldSecurityGroups, null);
        } else {
            getSecurityGroups(context, null,
                    Collections.singletonList(newPortEntity.getTenantId()));
        }
    }

    @Override
    void deleteProcess(PortContext context) throws Exception {
        securityGroupProcess(context, this::unbindSecurityGroups);
    }
}
