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
import com.futurewei.alcor.portmanager.request.UpstreamRequest;
import com.futurewei.alcor.web.entity.dataplane.NeighborInfo;
import com.futurewei.alcor.web.entity.port.PortEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NeighborProcessor extends AbstractProcessor {
    private void fetchPortNeighborCallback(UpstreamRequest request) {
        List<PortNeighbors> portNeighborsList = ((FetchPortNeighborRequest) request).getPortNeighborsList();
        List<NetworkConfig.ExtendPortEntity> internalPortEntities = networkConfig.getPortEntities();
        for (NetworkConfig.ExtendPortEntity extendPortEntity : internalPortEntities) {
            for (PortNeighbors portNeighbors: portNeighborsList) {
                if (extendPortEntity.getVpcId().equals(portNeighbors.getVpcId())) {
                    List<NeighborInfo> neighborInfos = new ArrayList<>(portNeighbors.getNeighbors().values());
                    extendPortEntity.getInternalPortEntity().setNeighborInfos(neighborInfos);
                }
            }
        }
    }

    @Override
    void createProcess(List<PortEntity> portEntities) {
        Set<String> vpcIds = new HashSet<>();

        portEntities.stream().forEach((p) -> {
            String vpcId = p.getVpcId();
            vpcIds.add(vpcId);
        });

        UpstreamRequest fetchPortNeighborRequest = new FetchPortNeighborRequest(
                portRepository, new ArrayList<>(vpcIds));
        sendRequest(fetchPortNeighborRequest, this::fetchPortNeighborCallback);
    }

    @Override
    void updateProcess(String portId, PortEntity portEntity) throws Exception {

    }
}
