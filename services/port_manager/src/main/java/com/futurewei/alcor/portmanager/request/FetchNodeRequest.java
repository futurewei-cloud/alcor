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
package com.futurewei.alcor.portmanager.request;

import com.futurewei.alcor.common.utils.SpringContextUtil;
import com.futurewei.alcor.portmanager.exception.GetNodeInfoException;
import com.futurewei.alcor.portmanager.processor.PortContext;
import com.futurewei.alcor.web.entity.node.NodeInfo;
import com.futurewei.alcor.web.entity.node.NodeInfoJson;
import com.futurewei.alcor.web.entity.node.NodesWebJson;
import com.futurewei.alcor.web.restclient.NodeManagerRestClient;

import java.util.ArrayList;
import java.util.List;

public class FetchNodeRequest extends AbstractRequest {
    private NodeManagerRestClient nodeManagerRestClient;
    private List<String> nodeIds;
    private List<NodeInfo> nodeInfoList;

    public FetchNodeRequest(PortContext context, List<String> nodeIds) {
        super(context);
        this.nodeIds = nodeIds;
        this.nodeInfoList = new ArrayList<>();
        this.nodeManagerRestClient = SpringContextUtil.getBean(NodeManagerRestClient.class);
    }

    public List<NodeInfo> getNodeInfoList() {
        return nodeInfoList;
    }

    @Override
    public void send() throws Exception {
        if (nodeIds.size() == 1) {
            NodeInfoJson nodeInfoJson = nodeManagerRestClient.getNodeInfo(nodeIds.get(0));
            if (nodeInfoJson == null || nodeInfoJson.getNodeInfo() == null) {
                throw new GetNodeInfoException();
            }

            nodeInfoList.add(nodeInfoJson.getNodeInfo());
        } else {
            NodesWebJson nodesWebJson = nodeManagerRestClient.getNodeInfoBulk(nodeIds);
            if (nodesWebJson == null ||
                    nodesWebJson.getNodeInfos() == null ||
                    nodesWebJson.getNodeInfos().size() != nodeIds.size()) {
                throw new GetNodeInfoException();
            }

            nodeInfoList.addAll(nodesWebJson.getNodeInfos());
        }
    }

    @Override
    public void rollback() {

    }
}
