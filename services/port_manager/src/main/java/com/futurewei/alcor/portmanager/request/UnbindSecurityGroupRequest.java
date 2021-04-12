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
package com.futurewei.alcor.portmanager.request;

import com.futurewei.alcor.common.utils.SpringContextUtil;
import com.futurewei.alcor.portmanager.processor.PortContext;
import com.futurewei.alcor.web.entity.securitygroup.PortBindingSecurityGroup;
import com.futurewei.alcor.web.entity.securitygroup.PortBindingSecurityGroupsJson;
import com.futurewei.alcor.web.restclient.SecurityGroupManagerRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class UnbindSecurityGroupRequest extends AbstractRequest {
    private static final Logger LOG = LoggerFactory.getLogger(UnbindSecurityGroupRequest.class);

    private SecurityGroupManagerRestClient securityGroupManagerRestClient;
    private List<PortBindingSecurityGroup> unbindSecurityGroups;

    public UnbindSecurityGroupRequest(PortContext context, List<PortBindingSecurityGroup> unbindSecurityGroups) {
        super(context);
        this.unbindSecurityGroups = unbindSecurityGroups;
        this.securityGroupManagerRestClient = SpringContextUtil.getBean(SecurityGroupManagerRestClient.class);

    }

    @Override
    public void send() throws Exception {
        PortBindingSecurityGroupsJson portBindingSecurityGroupsJson = new PortBindingSecurityGroupsJson();
        portBindingSecurityGroupsJson.setPortBindingSecurityGroups(unbindSecurityGroups);
        securityGroupManagerRestClient.unbindSecurityGroup(context.getProjectId(), portBindingSecurityGroupsJson);
    }

    @Override
    public void rollback() throws Exception {
        LOG.info("UnbindSecurityGroupRequest rollback, unbindSecurityGroups: {}", unbindSecurityGroups);
        PortBindingSecurityGroupsJson portBindingSecurityGroupsJson = new PortBindingSecurityGroupsJson();
        portBindingSecurityGroupsJson.setPortBindingSecurityGroups(unbindSecurityGroups);
        securityGroupManagerRestClient.bindSecurityGroup(context.getProjectId(), portBindingSecurityGroupsJson);
    }
}
