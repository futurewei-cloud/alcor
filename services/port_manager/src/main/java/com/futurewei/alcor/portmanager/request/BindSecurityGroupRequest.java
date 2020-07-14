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
import com.futurewei.alcor.web.entity.port.PortSecurityGroupsJson;
import com.futurewei.alcor.web.restclient.SecurityGroupManagerRestClient;

import java.util.List;

public class BindSecurityGroupRequest implements UpstreamRequest {
    private SecurityGroupManagerRestClient securityGroupManagerRestClient;
    private String projectId;
    private List<PortSecurityGroupsJson> portSecurityGroups;

    public BindSecurityGroupRequest(String projectId, List<PortSecurityGroupsJson> portSecurityGroups) {
        this.projectId = projectId;
        this.portSecurityGroups = portSecurityGroups;
        this.securityGroupManagerRestClient = SpringContextUtil.getBean(SecurityGroupManagerRestClient.class);
    }

    @Override
    public void send() throws Exception {
        for (PortSecurityGroupsJson portSecurityGroups: portSecurityGroups) {
            securityGroupManagerRestClient.bindSecurityGroups(projectId, portSecurityGroups);
        }
    }

    @Override
    public void rollback() throws Exception {
        for (PortSecurityGroupsJson portSecurityGroups: portSecurityGroups) {
            securityGroupManagerRestClient.unbindSecurityGroups(projectId, portSecurityGroups);
        }
    }
}
