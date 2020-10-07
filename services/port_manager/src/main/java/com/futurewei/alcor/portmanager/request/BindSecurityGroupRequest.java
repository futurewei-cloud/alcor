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
import com.futurewei.alcor.portmanager.processor.PortContext;
import com.futurewei.alcor.web.entity.securitygroup.PortBindingSecurityGroup;
import com.futurewei.alcor.web.entity.securitygroup.PortBindingSecurityGroupsJson;
import com.futurewei.alcor.web.restclient.SecurityGroupManagerRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class BindSecurityGroupRequest extends AbstractRequest {
    private static final Logger LOG = LoggerFactory.getLogger(BindSecurityGroupRequest.class);

    private SecurityGroupManagerRestClient securityGroupManagerRestClient;
    private List<PortBindingSecurityGroup> bindSecurityGroups;

    public BindSecurityGroupRequest(PortContext context, List<PortBindingSecurityGroup> bindSecurityGroups) {
        super(context);
        this.bindSecurityGroups = bindSecurityGroups;
        this.securityGroupManagerRestClient = SpringContextUtil.getBean(SecurityGroupManagerRestClient.class);
    }

    @Override
    public void send() throws Exception {
        PortBindingSecurityGroupsJson portBindingSecurityGroupsJson = new PortBindingSecurityGroupsJson();
        portBindingSecurityGroupsJson.setPortBindingSecurityGroups(bindSecurityGroups);
        securityGroupManagerRestClient.bindSecurityGroup(context.getProjectId(), portBindingSecurityGroupsJson);
    }

    @Override
    public void rollback() throws Exception {
        LOG.info("BindSecurityGroupRequest rollback, portSecurityGroups: {}", bindSecurityGroups);
        PortBindingSecurityGroupsJson portBindingSecurityGroupsJson = new PortBindingSecurityGroupsJson();
        portBindingSecurityGroupsJson.setPortBindingSecurityGroups(bindSecurityGroups);
        securityGroupManagerRestClient.unbindSecurityGroup(context.getProjectId(), portBindingSecurityGroupsJson);
    }
}
