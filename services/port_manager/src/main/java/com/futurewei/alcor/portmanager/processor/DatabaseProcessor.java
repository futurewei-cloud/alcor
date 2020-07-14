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

import com.futurewei.alcor.web.entity.dataplane.NeighborInfo;
import com.futurewei.alcor.web.entity.port.PortEntity;

import java.util.*;

public class DatabaseProcessor extends AbstractProcessor {
    @Override
    void createProcess(List<PortEntity> portEntities) throws Exception {
        Map<String, List<NeighborInfo>> portNeighbors = new HashMap<>();

        List<NetworkConfig.ExtendPortEntity> internalPortEntities = networkConfig.getPortEntities();
        for (NetworkConfig.ExtendPortEntity extendPortEntity : internalPortEntities) {
            String bindingHostIp = extendPortEntity.getInternalPortEntity().getBindingHostIp();
            if (bindingHostIp == null) {
                continue;
            }

            NeighborInfo neighborInfo = new NeighborInfo(bindingHostIp,
                    extendPortEntity.getBindingHostId(),
                    extendPortEntity.getId(),
                    extendPortEntity.getMacAddress());

            if (!portNeighbors.containsKey(extendPortEntity.getVpcId())) {
                List<NeighborInfo> neighborInfos = new ArrayList<>();
                portNeighbors.put(extendPortEntity.getVpcId(), neighborInfos);
            }

            portNeighbors.get(extendPortEntity.getVpcId()).add(neighborInfo);
        }

        portRepository.createPortAndNeighborBulk(portEntities, portNeighbors);
    }

    @Override
    void updateProcess(String portId, PortEntity portEntity) {

    }
}
