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

import com.futurewei.alcor.portmanager.request.FetchNodeRequest;
import com.futurewei.alcor.portmanager.request.IRestRequest;
import com.futurewei.alcor.web.entity.NodeInfo;
import com.futurewei.alcor.web.entity.dataplane.InternalPortEntity;
import com.futurewei.alcor.web.entity.port.PortEntity;

import java.util.*;
import java.util.stream.Collectors;

@AfterProcessor(PortProcessor.class)
public class NodeProcessor extends AbstractProcessor {
    private void fetchNodeCallback(IRestRequest request) {
        List<NodeInfo> nodeInfoList = ((FetchNodeRequest) request).getNodeInfoList();
        List<InternalPortEntity> internalPortEntities =
                request.getContext().getNetworkConfig().getPortEntities();

        for (InternalPortEntity internalPortEntity : internalPortEntities) {
            for (NodeInfo node: nodeInfoList) {
                if (node.getId() == null) {
                    continue;
                }

                if (node.getName().equals(internalPortEntity.getBindingHostId())) {
                    internalPortEntity.setBindingHostIP(node.getLocalIp());
                    internalPortEntity.setBindingHostId(node.getName());
                }
            }
        }

        request.getContext().setNodeInfos(nodeInfoList);
    }

    private void getNodeInfo(PortContext context, List<PortEntity> portEntities) {
        Set<String> nodeIds = portEntities
                .stream()
                .filter(p -> p.getBindingHostId() != null)
                .map(PortEntity::getBindingHostId)
                .collect(Collectors.toSet());

        if (nodeIds.size() > 0) {
            IRestRequest fetchNodeRequest = new FetchNodeRequest(context, new ArrayList<>(nodeIds));
            context.getRequestManager().sendRequestAsync(fetchNodeRequest, this::fetchNodeCallback);
        }
    }

    @Override
    void createProcess(PortContext context) {
        getNodeInfo(context, context.getPortEntities());
    }

    @Override
    void updateProcess(PortContext context) {
        PortEntity oldPortEntity = context.getOldPortEntity();
        getNodeInfo(context, Collections.singletonList(oldPortEntity));
    }

    @Override
    void deleteProcess(PortContext context) throws Exception {
        getNodeInfo(context, context.getPortEntities());
    }
}
