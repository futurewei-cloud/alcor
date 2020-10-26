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
import com.futurewei.alcor.portmanager.exception.GetConnectedRouterException;
import com.futurewei.alcor.portmanager.exception.GetConnectedSubnetException;
import com.futurewei.alcor.portmanager.processor.PortContext;
import com.futurewei.alcor.web.entity.route.ConnectedSubnetsWebResponse;
import com.futurewei.alcor.web.entity.route.InternalRouterInfo;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;
import com.futurewei.alcor.web.restclient.RouterManagerRestClient;

import java.util.ArrayList;
import java.util.List;

public class FetchRouterSubnetsRequest extends AbstractRequest {
    private String vpcId;
    private String subnetId;
    private List<SubnetEntity> associatedSubnetEntities;
    private InternalRouterInfo router;

    private RouterManagerRestClient routerManagerRestClient;

    public FetchRouterSubnetsRequest(PortContext context, String vpcId, String subnetId) {
        super(context);
        this.vpcId = vpcId;
        this.subnetId = subnetId;
        this.associatedSubnetEntities = new ArrayList<>();
        this.routerManagerRestClient = SpringContextUtil.getBean(RouterManagerRestClient.class);
    }

    public String getVpcId() {
        return this.vpcId;
    }

    public List<SubnetEntity> getAssociatedSubnetEntities() {
        return this.associatedSubnetEntities;
    }

    public InternalRouterInfo getRouter() { return this.router; }

    @Override
    public void send() throws Exception {
        ConnectedSubnetsWebResponse connectedRouterInfo = routerManagerRestClient.getRouterSubnets(context.getProjectId(), vpcId, subnetId);
        if (connectedRouterInfo == null || connectedRouterInfo.getSubnetEntities() == null) {
            throw new GetConnectedSubnetException();
        } else if (connectedRouterInfo.getInternalRouterInfo() == null) {
            throw new GetConnectedRouterException();
        }

        associatedSubnetEntities.addAll(connectedRouterInfo.getSubnetEntities());
        router = new InternalRouterInfo(connectedRouterInfo.getInternalRouterInfo());
    }

    @Override
    public void rollback() throws Exception {

    }
}
