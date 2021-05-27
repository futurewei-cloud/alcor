/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.futurewei.alcor.portmanager.processor;

import com.futurewei.alcor.portmanager.exception.GetSecurityGroupException;
import com.futurewei.alcor.portmanager.exception.SecurityGroupEntityNotFound;
import com.futurewei.alcor.portmanager.request.BindSecurityGroupRequest;
import com.futurewei.alcor.portmanager.request.FetchSecurityGroupRequest;
import com.futurewei.alcor.portmanager.request.IRestRequest;
import com.futurewei.alcor.portmanager.request.UnbindSecurityGroupRequest;
import com.futurewei.alcor.portmanager.util.ArrayUtil;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.securitygroup.PortBindingSecurityGroup;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroup;

import java.util.*;

@AfterProcessor(PortProcessor.class)
public class SecurityGroupProcessor extends AbstractProcessor {
    private static final String defaultSgName = "default";

    public void fetchSecurityGroupCallback(IRestRequest request) throws Exception {
        List<SecurityGroup> securityGroups = ((FetchSecurityGroupRequest) request)
                .getSecurityGroups();

        List<SecurityGroup> existSecurityGroups = request.getContext()
                .getNetworkConfig().getSecurityGroups();
        if (existSecurityGroups == null) {
            request.getContext().getNetworkConfig().setSecurityGroups(securityGroups);
        } else {
            existSecurityGroups.addAll(securityGroups);
        }

        PortContext context = request.getContext();
        for (SecurityGroup securityGroup: securityGroups) {
            if (defaultSgName.equals(securityGroup.getName())) {
                context.setDefaultSgId(securityGroup.getId());
                break;
            }
        }

        List<PortEntity> portEntities = context.getPortEntities();
        if (portEntities == null) {
            portEntities = Collections.singletonList(context.getNewPortEntity());
        }

        for (PortEntity portEntity: portEntities) {
            List<String> securityGroupList = portEntity.getSecurityGroups();
            if (securityGroupList == null || securityGroupList.size() == 0) {
                if (context.getDefaultSgId() == null) {
                    throw new GetSecurityGroupException();
                }

                portEntity.setSecurityGroups(Collections.singletonList(context.getDefaultSgId()));
            }
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
