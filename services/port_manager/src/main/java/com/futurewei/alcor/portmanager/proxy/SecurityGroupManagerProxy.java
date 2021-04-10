/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/

package com.futurewei.alcor.portmanager.proxy;

import com.futurewei.alcor.common.utils.SpringContextUtil;
import com.futurewei.alcor.portmanager.exception.GetSecurityGroupException;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.securitygroup.PortBindingSecurityGroup;
import com.futurewei.alcor.web.entity.securitygroup.PortBindingSecurityGroupsJson;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroup;
import com.futurewei.alcor.web.entity.securitygroup.SecurityGroupJson;
import com.futurewei.alcor.web.restclient.SecurityGroupManagerRestClient;

import java.util.ArrayList;
import java.util.List;

public class SecurityGroupManagerProxy {

    private SecurityGroupManagerRestClient securityGroupManagerRestClient;
    private String projectId;

    public SecurityGroupManagerProxy(String projectId) {
        securityGroupManagerRestClient = SpringContextUtil.getBean(SecurityGroupManagerRestClient.class);
        this.projectId = projectId;
    }

    public SecurityGroup getSecurityGroup(Object args) throws Exception {
        String securityGroupId = (String) args;
        SecurityGroupJson securityGroupJson = securityGroupManagerRestClient.getSecurityGroup(projectId, securityGroupId);
        if (securityGroupJson == null || securityGroupJson.getSecurityGroup() == null) {
            throw new GetSecurityGroupException();
        }

        return securityGroupJson.getSecurityGroup();
    }

    public SecurityGroup getDefaultSecurityGroupEntity(Object args) throws Exception {
        String tenantId = (String) args;
        SecurityGroupJson securityGroupJson = securityGroupManagerRestClient.getDefaultSecurityGroup(projectId, tenantId);
        if (securityGroupJson == null || securityGroupJson.getSecurityGroup() == null) {
            throw new GetSecurityGroupException();
        }

        return securityGroupJson.getSecurityGroup();
    }

    private PortBindingSecurityGroupsJson buildPortBindingSecurityGroupsJson(PortEntity portEntity) {
        PortBindingSecurityGroupsJson portBindingSecurityGroupsJson = new PortBindingSecurityGroupsJson();
        List<PortBindingSecurityGroup> portBindingSecurityGroups = new ArrayList<>();
        for (String securityGroupId: portEntity.getSecurityGroups()) {
            portBindingSecurityGroups.add(new PortBindingSecurityGroup(portEntity.getId(), securityGroupId));
        }

        portBindingSecurityGroupsJson.setPortBindingSecurityGroups(portBindingSecurityGroups);
        return portBindingSecurityGroupsJson;
    }

    public PortBindingSecurityGroupsJson bindSecurityGroup(Object args) throws Exception {
        return securityGroupManagerRestClient.bindSecurityGroup(projectId,
                buildPortBindingSecurityGroupsJson((PortEntity) args));
    }

    public PortBindingSecurityGroupsJson unbindSecurityGroup(Object args) throws Exception {
        return securityGroupManagerRestClient.unbindSecurityGroup(projectId,
                buildPortBindingSecurityGroupsJson((PortEntity) args));
    }
}
