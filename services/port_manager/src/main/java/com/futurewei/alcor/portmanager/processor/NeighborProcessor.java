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

import com.futurewei.alcor.portmanager.entity.PortNeighbors;
import com.futurewei.alcor.portmanager.request.FetchPortNeighborRequest;
import com.futurewei.alcor.portmanager.request.IRestRequest;
import com.futurewei.alcor.web.entity.dataplane.InternalPortEntity;
import com.futurewei.alcor.web.entity.dataplane.NeighborInfo;
import com.futurewei.alcor.web.entity.port.PortEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class NeighborProcessor extends AbstractProcessor {
    private void fetchPortNeighborCallback(IRestRequest request) {
        List<PortNeighbors> portNeighborsList = ((FetchPortNeighborRequest) request).getPortNeighborsList();
        List<InternalPortEntity> internalPortEntities =
                request.getContext().getNetworkConfig().getPortEntities();

        for (InternalPortEntity internalPortEntity : internalPortEntities) {
            for (PortNeighbors portNeighbors: portNeighborsList) {
                if (internalPortEntity.getVpcId().equals(portNeighbors.getVpcId())) {
                    List<NeighborInfo> neighborInfos = new ArrayList<>(portNeighbors.getNeighbors().values());
                    internalPortEntity.setNeighborInfos(neighborInfos);
                }
            }
        }
    }

    private void getNeighbors(PortContext context) {
        Set<String> vpcIds = context.getPortEntities()
                .stream()
                .map(PortEntity::getVpcId)
                .collect(Collectors.toSet());

        IRestRequest fetchPortNeighborRequest =
                new FetchPortNeighborRequest(context, new ArrayList<>(vpcIds));
        context.getRequestManager().sendRequestAsync(
                fetchPortNeighborRequest, this::fetchPortNeighborCallback);
    }

    @Override
    void createProcess(PortContext context) {
        getNeighbors(context);
    }

    @Override
    void updateProcess(PortContext context) {

    }

    @Override
    void deleteProcess(PortContext context) {
        getNeighbors(context);
    }
}
