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
import com.futurewei.alcor.web.entity.NodeInfo;
import com.futurewei.alcor.web.entity.NodeInfoJson;
import com.futurewei.alcor.web.restclient.NodeManagerRestClient;

import java.util.ArrayList;
import java.util.List;

public class FetchNodeRequest implements UpstreamRequest {
    private NodeManagerRestClient nodeManagerRestClient;
    private List<String> nodeIds;
    private List<NodeInfo> nodeInfoList;

    public FetchNodeRequest(List<String> nodeIds) {
        this.nodeIds = nodeIds;
        this.nodeInfoList = new ArrayList<>();
        this.nodeManagerRestClient = SpringContextUtil.getBean(NodeManagerRestClient.class);
    }

    public List<NodeInfo> getNodeInfoList() {
        return nodeInfoList;
    }

    @Override
    public void send() throws Exception {
        for (String nodeId: nodeIds) {
            NodeInfoJson nodeInfoJson = nodeManagerRestClient.getNodeInfo(nodeId);
            if (nodeInfoJson == null || nodeInfoJson.getNodeInfo() == null) {
                throw new GetNodeInfoException();
            }

            nodeInfoList.add(nodeInfoJson.getNodeInfo());
        }
    }

    @Override
    public void rollback() {

    }
}
