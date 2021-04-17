/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
