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

package com.futurewei.alcor.portmanager.processor;

import com.futurewei.alcor.portmanager.exception.AllocateIpAddrException;
import com.futurewei.alcor.portmanager.request.FetchRouterSubnetsRequest;
import com.futurewei.alcor.portmanager.request.IRestRequest;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.route.InternalRouterInfo;
import com.futurewei.alcor.web.entity.subnet.SubnetEntity;

import java.util.*;

@AfterProcessor(FixedIpsProcessor.class)
public class RouterProcessor extends AbstractProcessor {
    private static final int TRY_TIMES = 100;
    private static final int SLEEP_TIME = 100;

    private void fetchConnectedSubnetIdsCallback(IRestRequest request) {
        FetchRouterSubnetsRequest fetchRouterSubnetsRequest = ((FetchRouterSubnetsRequest) request);

        String vpcId = fetchRouterSubnetsRequest.getVpcId();
        List<SubnetEntity> associatedSubnetEntities = fetchRouterSubnetsRequest.getAssociatedSubnetEntities();
        request.getContext().addRouterSubnetEntities(vpcId, associatedSubnetEntities);

        InternalRouterInfo router = fetchRouterSubnetsRequest.getRouter();
        // Current implementation supports Neutron router. VPC router will be in next release.
        request.getContext().addRouter(vpcId, router);
    }

    private void getRouterSubnetIds(PortContext context, String vpcId, String subnetId) {
        FetchRouterSubnetsRequest fetchRouterSubnetsRequest =
                new FetchRouterSubnetsRequest(context, vpcId, subnetId);
        context.getRequestManager().sendRequestAsync(
                fetchRouterSubnetsRequest, this::fetchConnectedSubnetIdsCallback);
    }

    private void getRouterSubnetIds(PortContext context, List<PortEntity> portEntities) throws Exception {
        Set<String> vpcIds = new HashSet<>();
        int tryTimes = TRY_TIMES;
        for (PortEntity portEntity : portEntities) {
            if (portEntity.getFixedIps() == null) {
                continue;
            }

            //Waiting for random ip address to be assigned
            while (portEntity.getFixedIps() == null && tryTimes > 0) {
                Thread.sleep(SLEEP_TIME);
                tryTimes--;
            }

            if (portEntity.getFixedIps() == null) {
                throw new AllocateIpAddrException();
            }

            if (!vpcIds.contains(portEntity.getVpcId())) {
                getRouterSubnetIds(context, portEntity.getVpcId(),
                        portEntity.getFixedIps().get(0).getSubnetId());
                vpcIds.add(portEntity.getVpcId());
            }
        }
    }

    @Override
    void createProcess(PortContext context) throws Exception {
        getRouterSubnetIds(context, context.getPortEntities());
    }

    @Override
    void updateProcess(PortContext context) throws Exception {
        PortEntity portEntity = context.getNewPortEntity();
        getRouterSubnetIds(context, Collections.singletonList(portEntity));
    }

    @Override
    void deleteProcess(PortContext context) throws Exception {
        getRouterSubnetIds(context, context.getPortEntities());
    }
}
