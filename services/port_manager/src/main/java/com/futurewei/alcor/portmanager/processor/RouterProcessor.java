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
package com.futurewei.alcor.portmanager.processor;

import com.futurewei.alcor.portmanager.exception.AllocateIpAddrException;
import com.futurewei.alcor.portmanager.request.FetchRouterSubnetsRequest;
import com.futurewei.alcor.portmanager.request.IRestRequest;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.route.Router;

import java.util.*;

@AfterProcessor(FixedIpsProcessor.class)
public class RouterProcessor extends AbstractProcessor {
    private static final int TRY_TIMES = 100;
    private static final int SLEEP_TIME = 100;

    private void fetchConnectedSubnetIdsCallback(IRestRequest request) {
        FetchRouterSubnetsRequest fetchRouterSubnetsRequest = ((FetchRouterSubnetsRequest) request);

        String vpcId = fetchRouterSubnetsRequest.getVpcId();
        List<String> subnetIds = fetchRouterSubnetsRequest.getSubnetIds();
        request.getContext().addRouterSubnetIds(vpcId, subnetIds);

        Router router = fetchRouterSubnetsRequest.getRouter();
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
        PortEntity oldPortEntity = context.getOldPortEntity();
        getRouterSubnetIds(context, Collections.singletonList(oldPortEntity));
    }

    @Override
    void deleteProcess(PortContext context) throws Exception {
        getRouterSubnetIds(context, context.getPortEntities());
    }
}
