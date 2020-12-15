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

import com.futurewei.alcor.portmanager.request.FetchPortNeighborRequest;
import com.futurewei.alcor.portmanager.request.IRestRequest;
import com.futurewei.alcor.web.entity.dataplane.NeighborInfo;
import com.futurewei.alcor.web.entity.port.PortEntity;

import java.util.*;
import java.util.stream.Collectors;

@AfterProcessor(PortProcessor.class)
public class NeighborProcessor extends AbstractProcessor {
    private void fetchPortNeighborCallback(IRestRequest request) {
        Map<String, NeighborInfo> neighborInfoMap = ((FetchPortNeighborRequest) request).getNeighborInfos();
        if (neighborInfoMap == null || neighborInfoMap.size() == 0) {
            return;
        }

        Map<String, NeighborInfo> neighborInfos = new HashMap<>();
        for (Map.Entry<String, NeighborInfo> entry: neighborInfoMap.entrySet()) {
            NeighborInfo neighborInfo = entry.getValue();
            neighborInfos.put(neighborInfo.getPortIp(), neighborInfo);
        }

        NetworkConfig networkConfig = request.getContext().getNetworkConfig();
        networkConfig.setNeighborInfos(neighborInfos);
    }

    private void getNeighbors(PortContext context, List<PortEntity> portEntities) {
        Set<String> vpcIds = portEntities
                .stream()
                .filter(p -> p.getVpcId() != null)
                .map(PortEntity::getVpcId)
                .collect(Collectors.toSet());

        IRestRequest fetchPortNeighborRequest =
                new FetchPortNeighborRequest(context, new ArrayList<>(vpcIds));
        context.getRequestManager().sendRequestAsync(
                fetchPortNeighborRequest, this::fetchPortNeighborCallback);
    }

    @Override
    void createProcess(PortContext context) {
        getNeighbors(context, context.getPortEntities());
    }

    @Override
    void updateProcess(PortContext context) {
        PortEntity oldPortEntity = context.getOldPortEntity();
        getNeighbors(context, Collections.singletonList(oldPortEntity));
    }

    @Override
    void deleteProcess(PortContext context) {
        getNeighbors(context, context.getPortEntities());
    }
}
