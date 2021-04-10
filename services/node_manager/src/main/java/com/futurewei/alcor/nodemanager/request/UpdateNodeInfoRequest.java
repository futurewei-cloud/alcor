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

package com.futurewei.alcor.nodemanager.request;

import com.futurewei.alcor.common.utils.SpringContextUtil;
import com.futurewei.alcor.nodemanager.processor.NodeContext;
import com.futurewei.alcor.web.entity.node.NodeInfo;
import com.futurewei.alcor.web.entity.node.NodeInfoJson;
import com.futurewei.alcor.web.restclient.DataPlaneManagerRestClient;
import com.futurewei.alcor.web.restclient.NetworkConfigManagerRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateNodeInfoRequest extends AbstractRequest{
    private static final Logger LOG = LoggerFactory.getLogger(UpdateNodeInfoRequest.class);

    private NodeInfo nodeInfo;
    private DataPlaneManagerRestClient dataPlaneManagerRestClient;
    private NetworkConfigManagerRestClient ncmRestClient;

    public UpdateNodeInfoRequest(NodeContext context, NodeInfo nodeInfo) {
        super(context);
        this.nodeInfo = nodeInfo;
        this.dataPlaneManagerRestClient = SpringContextUtil.getBean(DataPlaneManagerRestClient.class);
        this.ncmRestClient = SpringContextUtil.getBean(NetworkConfigManagerRestClient.class);
    }

    @Override
    public void send() throws Exception {
        NodeInfoJson jsonData = new NodeInfoJson(nodeInfo);
        dataPlaneManagerRestClient.updateNodeInfo(jsonData);
        ncmRestClient.updateNodeInfo(jsonData);
    }
}