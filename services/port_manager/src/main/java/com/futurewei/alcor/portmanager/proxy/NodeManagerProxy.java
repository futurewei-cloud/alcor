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
package com.futurewei.alcor.portmanager.proxy;

import com.futurewei.alcor.common.utils.SpringContextUtil;
import com.futurewei.alcor.portmanager.entity.PortBindingHost;
import com.futurewei.alcor.portmanager.exception.GetNodeInfoException;
import com.futurewei.alcor.portmanager.exception.MultipleNodeInfosHaveSameNodeName;
import com.futurewei.alcor.portmanager.rollback.Rollback;
import com.futurewei.alcor.web.entity.NodeInfo;
import com.futurewei.alcor.web.entity.NodeInfoJson;
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
                nodeInfos.get(0).getgRPCServerPort());

        return new PortBindingHost(portEntity.getId(), node);
    }
}
