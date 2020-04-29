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
package com.futurewei.alcor.nodemanager.service.datacenter;

//import com.futurewei.alcor.controller.utilities.Common;

import com.futurewei.alcor.nodemanager.entity.NodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class NodeManager {
    private static final Logger logger = LoggerFactory.getLogger(NodeManager.class);

    @Autowired
    public DataCenterConfigLoader dataCenterConfigLoader;

    //private List<NodeInfo> nodes;
    private Hashtable<String, NodeInfo> nodeTable;

    public NodeManager() {
        nodeTable = null;
    }

    public NodeManager(List<NodeInfo> nodes) {
        nodeTable = null;
        for (NodeInfo node : nodes) {
            logger.info("Log:" + node);
        }
        this.BuildTableFromNodeIdToInfo(nodes);
    }

    private List<NodeInfo> LoadNodes() {
        if (nodeTable == null) {
            List<NodeInfo> nodes = dataCenterConfigLoader.loadAndGetHostNodeList();
            BuildTableFromNodeIdToInfo(nodes);
        }
        return new ArrayList(nodeTable.values());
    }

    public NodeInfo getNodeInfoById(String nodeId) {
        LoadNodes();
        if (this.nodeTable != null) {
            return this.nodeTable.get(nodeId);
        } else
            return null;
    }

    public Hashtable<String, NodeInfo> getAllNodes() {
        LoadNodes();
        return this.nodeTable;
    }

    public List<NodeInfo> getAllNodesList() {
        LoadNodes();
        return new ArrayList(nodeTable.values());
    }

    public Collection<NodeInfo> getAllNodes2() {
        LoadNodes();
        if (this.nodeTable != null) {
            return this.nodeTable.values();
        } else
            return null;
    }

    public NodeInfo[] getRandomHosts(int count) {
        LoadNodes();
        NodeInfo[] randomHosts = new NodeInfo[count];
        for (int i = 0; i < count; i++) {
            int index = ThreadLocalRandom.current().nextInt(0, this.getAllNodes().size() - 1);
            randomHosts[i] = this.getAllNodes().get(index);
        }
        return randomHosts;
    }

    public void putNode(NodeInfo nodeInfo) {
        LoadNodes();
        if (this.nodeTable != null) {
            logger.info("Log:" + "[NodeManager] Add Host id: " + nodeInfo.getId());
            this.nodeTable.put(nodeInfo.getId(), nodeInfo);
        }
    }

    public String deleteNode(String nodeId) {
        LoadNodes();
        if (this.nodeTable != null) {
            logger.info("Log:" + "[NodeManager] Delete Host id: " + nodeId);
            this.nodeTable.remove(nodeId);
        }
        return nodeId;
    }

    private void BuildTableFromNodeIdToInfo(List<NodeInfo> nodes) {
        nodeTable = new Hashtable<String, NodeInfo>();
        if (nodes != null) {
            logger.info("nodes size : " + nodes.size());
            for (NodeInfo node : nodes) {
                this.nodeTable.put(node.getId(), node);
            }
        }
    }
}
