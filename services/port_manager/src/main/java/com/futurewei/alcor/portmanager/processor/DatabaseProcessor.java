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

@AfterProcessor(DataPlaneProcessor.class)
public class DatabaseProcessor extends AbstractProcessor {
    private List<NeighborInfo> buildNeighborInfos(InternalPortEntity internalPortEntity) {
        List<NeighborInfo> neighborInfos = new ArrayList<>();
        String bindingHostIp = internalPortEntity.getBindingHostIP();
        if (bindingHostIp == null) {
            return neighborInfos;
        }

        for (PortEntity.FixedIp fixedIp : internalPortEntity.getFixedIps()) {
            NeighborInfo neighborInfo = new NeighborInfo(bindingHostIp,
                    internalPortEntity.getBindingHostId(),
                    internalPortEntity.getId(),
                    internalPortEntity.getMacAddress(),
                    fixedIp.getIpAddress(),
                    internalPortEntity.getVpcId(),
                    fixedIp.getSubnetId());
            neighborInfos.add(neighborInfo);
        }

        return neighborInfos;
    }

    @Override
    void createProcess(PortContext context) throws Exception {
        Map<String, List<NeighborInfo>> portNeighbors = new HashMap<>();

        NetworkConfig networkConfig = context.getNetworkConfig();
        List<InternalPortEntity> internalPortEntities = networkConfig.getPortEntities();
        for (InternalPortEntity internalPortEntity : internalPortEntities) {
            List<NeighborInfo> neighborInfoList = buildNeighborInfos(internalPortEntity);
            if (neighborInfoList.size() == 0) {
                continue;
            }
            if (!portNeighbors.containsKey(internalPortEntity.getVpcId())) {
                portNeighbors.put(internalPortEntity.getVpcId(), new ArrayList<>());
            }

            portNeighbors.get(internalPortEntity.getVpcId()).addAll(neighborInfoList);
        }

        /**
         * TODO:
         CreateNetworkConfig may fail, in that case we need to rollback the database
         operation, or wait for CreateNetworkConfig to succeed before writing to the database
         */
        List<PortEntity> portEntities = context.getPortEntities();
        context.getPortRepository().createPortBulk(portEntities, portNeighbors);
    }

    @Override
    void updateProcess(PortContext context) throws Exception {
        List<InternalPortEntity> internalPortEntities = context.getNetworkConfig().getPortEntities();
        List<NeighborInfo> neighborInfos = null;
        if (internalPortEntities.size() > 0) {
            neighborInfos = buildNeighborInfos(internalPortEntities.get(0));
        }

        PortEntity oldPortEntity = context.getOldPortEntity();

        //TODO: A port may have more than one ip address,
        // for one ip address we should create one neighborInfo
        context.getPortRepository().updatePort(oldPortEntity, neighborInfos != null ? neighborInfos.get(0) : null);
    }

    @Override
    void deleteProcess(PortContext context) throws Exception {
        //TODO: support batch deletion
        for (PortEntity portEntity : context.getPortEntities()) {
            context.getPortRepository().deletePort(portEntity);
        }
    }
}
