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
            if (securityGroupJson == null ||
                    securityGroupJson.getSecurityGroup() == null) {
                throw new GetSecurityGroupException();
            }

            securityGroups.add(securityGroupJson.getSecurityGroup());
        } else {
            SecurityGroupsJson securityGroupsJson = securityGroupManagerRestClient
                    .getSecurityGroupBulk(context.getProjectId(), securityGroupIds);
            if (securityGroupsJson == null ||
                    securityGroupsJson.getSecurityGroups() == null ||
                    securityGroupsJson.getSecurityGroups().size() != securityGroupIds.size()) {
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
