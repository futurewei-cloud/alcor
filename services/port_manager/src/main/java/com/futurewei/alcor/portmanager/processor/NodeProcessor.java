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
import com.futurewei.alcor.portmanager.request.UpstreamRequest;
import com.futurewei.alcor.web.entity.NodeInfo;
import com.futurewei.alcor.web.entity.port.PortEntity;

import java.util.*;

public class NodeProcessor extends AbstractProcessor {
    private void fetchNodeCallback(UpstreamRequest request) {
        List<NodeInfo> nodeInfoList = ((FetchNodeRequest) request).getNodeInfoList();
        List<NetworkConfig.ExtendPortEntity> internalPortEntities = networkConfig.getPortEntities();

        for (NetworkConfig.ExtendPortEntity extendPortEntity : internalPortEntities) {
            for (NodeInfo node: nodeInfoList) {
                if (extendPortEntity.getBindingHostId().equals(node.getId())) {
                    extendPortEntity.getInternalPortEntity().setBindingHostIp(node.getLocalIp());
                    extendPortEntity.setBindingHostId(node.getId());
                }
            }
        }
    }

    @Override
    void createProcess(List<PortEntity> portEntities) {
        Set<String> nodeIds = new HashSet<>();

        portEntities.stream().forEach((p) -> {
            String nodeId = p.getBindingHostId();
            nodeIds.add(nodeId);
        });

        if (nodeIds.size() > 0) {
            UpstreamRequest fetchNodeRequest = new FetchNodeRequest(new ArrayList<>(nodeIds));
            sendRequest(fetchNodeRequest, this::fetchNodeCallback);
        }
    }

    @Override
    void updateProcess(String portId, PortEntity portEntity) {

    }
}
