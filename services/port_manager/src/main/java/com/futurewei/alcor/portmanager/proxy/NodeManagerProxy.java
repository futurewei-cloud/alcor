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
package com.futurewei.alcor.portmanager.proxy;

import com.futurewei.alcor.common.utils.SpringContextUtil;
import com.futurewei.alcor.portmanager.entity.PortBindingHost;
import com.futurewei.alcor.portmanager.exception.GetNodeInfoException;
import com.futurewei.alcor.portmanager.exception.MultipleNodeInfosHaveSameNodeName;
import com.futurewei.alcor.portmanager.rollback.Rollback;
import com.futurewei.alcor.web.entity.node.NodeInfo;
import com.futurewei.alcor.web.entity.node.NodeInfoJson;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.restclient.NodeManagerRestClient;

import java.util.List;
import java.util.Stack;

public class NodeManagerProxy {
    private NodeManagerRestClient nodeManagerRestClient;
    private Stack<Rollback> rollbacks;

    public NodeManagerProxy(Stack<Rollback> rollbacks) {
        nodeManagerRestClient = SpringContextUtil.getBean(NodeManagerRestClient.class);
        this.rollbacks = rollbacks;
    }

    /**
     * Verify and get host info from Node manager
     * @param args Id of host/node
     * @return The information of host/node
     * @throws Exception Rest request exception
     */
    public PortBindingHost getNodeInfo(Object args) throws Exception {
        PortEntity portEntity = (PortEntity)args;
        NodeInfoJson nodeInfoJson = nodeManagerRestClient.getNodeInfo(portEntity.getBindingHostId());
        if (nodeInfoJson == null || nodeInfoJson.getNodeInfo() == null) {
            throw new GetNodeInfoException();
        }

        return new PortBindingHost(portEntity.getId(), nodeInfoJson.getNodeInfo());
    }

    /**
     * Verify and get host info from Node manager by node name
     * @param args name of host/node
     * @return The information of host/node
     * @throws Exception Rest request exception
     */
    public PortBindingHost getNodeInfoByNodeName(Object args) throws Exception {
        PortEntity portEntity = (PortEntity)args;
        // Binding Host Id is Node Name
        List<NodeInfo> nodeInfos = nodeManagerRestClient.getNodeInfoByNodeName(portEntity.getBindingHostId());
        if (nodeInfos == null || nodeInfos.size() == 0) {
            throw new GetNodeInfoException();
        }

        if (nodeInfos.size() >= 2) {
            throw new MultipleNodeInfosHaveSameNodeName();
        }
        NodeInfo node = new NodeInfo(nodeInfos.get(0).getId(),
                nodeInfos.get(0).getName(),
                nodeInfos.get(0).getLocalIp(),
                nodeInfos.get(0).getMacAddress(),
                nodeInfos.get(0).getVeth(),
                nodeInfos.get(0).getgRPCServerPort()
        );
        node.setNcmId(nodeInfos.get(0).getNcmId());

        return new PortBindingHost(portEntity.getId(), node);
    }
}
