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
package com.futurewei.alcor.portmanager.request;

import com.futurewei.alcor.common.utils.SpringContextUtil;
import com.futurewei.alcor.portmanager.exception.GetSecurityGroupException;
import com.futurewei.alcor.portmanager.processor.PortContext;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroup;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupJson;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupsJson;
import com.futurewei.alcor.web.restclient.SecurityGroupManagerRestClient;

import java.util.ArrayList;
import java.util.List;

public class FetchSecurityGroupRequest extends AbstractRequest {
    private SecurityGroupManagerRestClient securityGroupManagerRestClient;
    private List<String> securityGroupIds;
    private List<String> defaultSecurityGroupIds;
    private List<SecurityGroup> securityGroups;

    public FetchSecurityGroupRequest(PortContext context, List<String> securityGroupIds, List<String> defaultSecurityGroupIds) {
        super(context);
        this.securityGroupIds = securityGroupIds;
        this.defaultSecurityGroupIds = defaultSecurityGroupIds;
        this.securityGroups = new ArrayList<>();
        this.securityGroupManagerRestClient = SpringContextUtil.getBean(SecurityGroupManagerRestClient.class);
    }

    public List<SecurityGroup> getSecurityGroups() {
        return securityGroups;
    }

    private void getSecurityGroup() throws Exception {
        if (securityGroupIds.size() == 1) {
            SecurityGroupJson securityGroupJson = securityGroupManagerRestClient
                    .getSecurityGroup(context.getProjectId(), securityGroupIds.get(0));
            if (securityGroupJson == null || securityGroupJson.getSecurityGroup() == null) {
                throw new GetSecurityGroupException();
            }

            securityGroups.add(securityGroupJson.getSecurityGroup());
        } else {
            SecurityGroupsJson securityGroupsJson = securityGroupManagerRestClient
                    .getSecurityGroupBulk(context.getProjectId(), securityGroupIds);
            if (securityGroupsJson == null || securityGroupsJson.getSecurityGroups() == null) {
                throw new GetSecurityGroupException();
            }

            securityGroups.addAll(securityGroupsJson.getSecurityGroups());
        }
    }

    private void getDefaultSecurityGroup() throws Exception {
        for (String securityGroupId : defaultSecurityGroupIds) {
            SecurityGroupJson defaultSecurityGroup = securityGroupManagerRestClient
                    .getDefaultSecurityGroup(context.getProjectId(), securityGroupId);
            if (defaultSecurityGroup == null || defaultSecurityGroup.getSecurityGroup() == null) {
                throw new GetSecurityGroupException();
            }

            securityGroups.add(defaultSecurityGroup.getSecurityGroup());
        }
    }

    @Override
    public void send() throws Exception {
        if (securityGroupIds != null &&
                securityGroupIds.size() > 0) {
            getSecurityGroup();
        }

        if (defaultSecurityGroupIds != null &&
                defaultSecurityGroupIds.size() > 0) {
            getDefaultSecurityGroup();
        }
    }

    @Override
    public void rollback() {

    }
}
