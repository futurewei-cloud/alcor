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

import com.futurewei.alcor.web.entity.dataplane.InternalPortEntity;
import com.futurewei.alcor.web.entity.dataplane.NeighborInfo;
import com.futurewei.alcor.web.entity.port.PortEntity;

import java.util.*;

public class DatabaseProcessor extends AbstractProcessor {
    private NeighborInfo getNeighborInfo(InternalPortEntity internalPortEntity) {
        String bindingHostIp = internalPortEntity.getBindingHostIp();
        if (bindingHostIp == null) {
            return null;
        }

        NeighborInfo neighborInfo = new NeighborInfo(bindingHostIp,
                internalPortEntity.getBindingHostId(),
                internalPortEntity.getId(),
                internalPortEntity.getMacAddress());

        return neighborInfo;
    }

    @Override
    void createProcess(PortContext context) throws Exception {
        Map<String, List<NeighborInfo>> portNeighbors = new HashMap<>();

        NetworkConfig networkConfig = context.getNetworkConfig();
        List<InternalPortEntity> internalPortEntities = networkConfig.getPortEntities();
        for (InternalPortEntity internalPortEntity : internalPortEntities) {
            NeighborInfo neighborInfo = getNeighborInfo(internalPortEntity);
            if (!portNeighbors.containsKey(internalPortEntity.getVpcId())) {
                List<NeighborInfo> neighborInfos = new ArrayList<>();
                portNeighbors.put(internalPortEntity.getVpcId(), neighborInfos);
            }

            portNeighbors.get(internalPortEntity.getVpcId()).add(neighborInfo);
        }

        /**
         * TODO:
         CreateNetworkConfig may fail, in that case we need to rollback the database
         operation, or wait for CreateNetworkConfig to succeed before writing to the database
         */
        List<PortEntity> portEntities = context.getPortEntities();
        context.getPortRepository().createPortAndNeighborBulk(portEntities, portNeighbors);
    }

    @Override
    void updateProcess(PortContext context) throws Exception {
        List<InternalPortEntity> internalPortEntities = context.getNetworkConfig().getPortEntities();
        NeighborInfo neighborInfo = getNeighborInfo(internalPortEntities.get(0));
        PortEntity oldPortEntity = context.getOldPortEntity();
        context.getPortRepository().updatePortAndNeighbor(oldPortEntity, neighborInfo);
    }

    @Override
    void deleteProcess(PortContext context) throws Exception {
        //TODO: support batch deletion
        for (PortEntity portEntity: context.getPortEntities()) {
            context.getPortRepository().deletePortAndNeighbor(portEntity);
        }
    }
}
