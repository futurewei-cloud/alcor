/*Copyright 2019 The Alcor Authors.

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
package com.futurewei.alcor.nodemanager.service.implement;

import com.futurewei.alcor.controller.utilities.Common;
import com.futurewei.alcor.nodemanager.dao.file.DataCenterConfigLoader;
import com.futurewei.alcor.nodemanager.entity.NodeInfo;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Data
public class NodeManager {
    private static final Logger logger = LoggerFactory.getLogger(NodeManager.class);
    private static int GRPC_SERVER_PORT = 50001;
    private List<NodeInfo> nodes;
    private HashMap<String, NodeInfo> nodeMap;

    public NodeManager(List<NodeInfo> hosts) {
        this.nodes = NodeManager.LoadNodes(hosts);
        for (NodeInfo host : hosts) {
            logger.info("Log:" + host);
        }
        this.BuildMapFromNodeIdToInfo(this.nodes);
    }

    private static List<NodeInfo> LoadNodes(List<NodeInfo> hosts) {
        List<NodeInfo> nodeInfoList = new ArrayList<>(hosts);
        for (int i = 0; i < nodeInfoList.size(); i++) {
            NodeInfo host = nodeInfoList.get(i);
            host.setGRPCServerPort(NodeManager.GRPC_SERVER_PORT);
        }
        return nodeInfoList;
    }

    public NodeInfo getNodeInfoById(String hostId) {
        if (this.nodeMap != null) {
            logger.info("Log:" + "[NodeManager] Host id: " + hostId + " info:" + this.nodeMap.get(hostId));
            return this.nodeMap.get(hostId);
        }
        logger.info("[NodeManager] node map is empty");
        return null;
    }

    public NodeInfo[] getRandomHosts(int count) {
        NodeInfo[] randomHosts = new NodeInfo[count];
        for (int i = 0; i < count; i++) {
            int index = Common.getRandomNumberInRange(0, this.getNodes().size() - 1);
            randomHosts[i] = this.getNodes().get(index);
        }
        return randomHosts;
    }

    private void BuildMapFromNodeIdToInfo(List<NodeInfo> hosts) {
        if (hosts != null) {
            if (this.nodeMap == null) {
                this.nodeMap = new HashMap<>();
            }
            logger.info("hosts size : " + hosts.size());
            for (NodeInfo host : hosts) {
                this.nodeMap.put(host.getId(), host);
            }
        }
    }
}
