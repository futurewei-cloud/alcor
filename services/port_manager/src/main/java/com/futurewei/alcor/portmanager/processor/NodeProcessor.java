/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/

package com.futurewei.alcor.portmanager.processor;

import com.futurewei.alcor.portmanager.request.FetchNodeRequest;
import com.futurewei.alcor.portmanager.request.IRestRequest;
import com.futurewei.alcor.web.entity.node.NodeInfo;
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
        PortEntity portEntity = context.getNewPortEntity();
        getNodeInfo(context, Collections.singletonList(portEntity));
    }

    @Override
    void deleteProcess(PortContext context) throws Exception {
        getNodeInfo(context, context.getPortEntities());
    }
}
