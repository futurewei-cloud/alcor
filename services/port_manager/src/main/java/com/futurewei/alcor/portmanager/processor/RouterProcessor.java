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

import com.futurewei.alcor.portmanager.request.FetchRouterSubnetsRequest;
import com.futurewei.alcor.portmanager.request.IRestRequest;
import com.futurewei.alcor.web.entity.port.PortEntity;

import java.util.*;

public class RouterProcessor extends AbstractProcessor {
    private void fetchConnectedSubnetIdsCallback(IRestRequest request) {
        List<String> subnetIds = ((FetchRouterSubnetsRequest) request).getSubnetIds();
        request.getContext().setRouterSubnetIds(subnetIds);
    }

    private void getRouterSubnetIds(PortContext context, String vpcId, String subnetId) {
        FetchRouterSubnetsRequest fetchRouterSubnetsRequest =
                new FetchRouterSubnetsRequest(context, vpcId, subnetId);
        context.getRequestManager().sendRequestAsync(
                fetchRouterSubnetsRequest, this::fetchConnectedSubnetIdsCallback);
    }

    private void getRouterSubnetIds(PortContext context, List<PortEntity> portEntities) {
        Set<String> vpcIds = new HashSet<>();
        for (PortEntity portEntity: portEntities) {
            if (portEntity.getFixedIps() == null) {
                continue;
            }

            if (!vpcIds.contains(portEntity.getVpcId())) {
                getRouterSubnetIds(context, portEntity.getVpcId(),
                        portEntity.getFixedIps().get(0).getSubnetId());
                vpcIds.add(portEntity.getVpcId());
            }
        }
    }

    @Override
    void createProcess(PortContext context) {
        getRouterSubnetIds(context, context.getPortEntities());
    }

    @Override
    void updateProcess(PortContext context) {
        PortEntity oldPortEntity = context.getOldPortEntity();
        getRouterSubnetIds(context, Collections.singletonList(oldPortEntity));
    }

    @Override
    void deleteProcess(PortContext context) {
        getRouterSubnetIds(context, context.getPortEntities());
    }
}
