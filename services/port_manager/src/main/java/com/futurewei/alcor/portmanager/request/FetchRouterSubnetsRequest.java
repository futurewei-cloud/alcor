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
