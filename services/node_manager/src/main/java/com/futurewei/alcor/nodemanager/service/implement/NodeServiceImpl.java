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

import com.futurewei.alcor.common.exception.ParameterNullOrEmptyException;
import com.futurewei.alcor.nodemanager.service.datacenter.DataCenterConfigLoader;
import com.futurewei.alcor.nodemanager.dao.NodeRepository;
import com.futurewei.alcor.nodemanager.entity.NodeInfo;
import com.futurewei.alcor.nodemanager.service.NodeService;
import com.futurewei.alcor.nodemanager.service.datacenter.NodeManager;
import com.futurewei.alcor.nodemanager.utils.NodeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

@Service
public class NodeServiceImpl implements NodeService {
    private static final Logger logger = LoggerFactory.getLogger(NodeServiceImpl.class);
    public Hashtable<String, NodeInfo> Nodes = new Hashtable<String, NodeInfo>();

    @Value("${nodemanager.nodeinfo.location}")
    private int nodeInfoLocation;

    @Autowired
    private NodeRepository nodeRepository;

    @Autowired
    private NodeManager nodeManager;

    @Override
    public NodeInfo getNodeInfoById(String nodeId) throws Exception {
        NodeInfo nodeInfo = null;
        try {
            switch(nodeInfoLocation){
                case NodeUtil.NODE_INFO_FILE: {
                    nodeInfo = nodeManager.getNodeInfoById(nodeId);
                }
                case NodeUtil.NODE_INFO_REPOSITOTY: {
                    nodeInfo = nodeRepository.findItem(nodeId);
                }
            }
        } catch (Exception e) {
            throw e;
        }
        return nodeInfo;
    }

    @Override
    public Hashtable<String, NodeInfo> getAllNodes() throws Exception {
        Hashtable<String, NodeInfo> nodes = new Hashtable<String, NodeInfo>();
        try {
            switch(nodeInfoLocation){
                case NodeUtil.NODE_INFO_FILE: {
                    nodes = nodeManager.getAllNodes();
                    break;
                }
                case NodeUtil.NODE_INFO_REPOSITOTY: {
                    nodes.putAll(nodeRepository.findAllItems());
                    break;
                }
            }
        } catch (Exception e) {
            throw e;
        }
        return nodes;
    }

    public List<NodeInfo> getAllNodesList() throws Exception {
        List<NodeInfo> nodes = new ArrayList<NodeInfo>();
        try {
            switch(nodeInfoLocation){
                case NodeUtil.NODE_INFO_FILE: {
                    nodes = nodeManager.getAllNodesList();
                    break;
                }
                case NodeUtil.NODE_INFO_REPOSITOTY: {
                    nodes = new ArrayList(nodeRepository.findAllItems().values());
                    break;
                }
            }
        } catch (Exception e) {
            throw e;
        }
        return nodes;
    }

    @Override
    public NodeInfo createNodeInfo(NodeInfo nodeInfo) throws Exception {
        if (nodeInfo != null) {
            try {
                switch(nodeInfoLocation){
                    case NodeUtil.NODE_INFO_FILE: {
                        nodeManager.putNode(nodeInfo);
                    }
                    case NodeUtil.NODE_INFO_REPOSITOTY: {
                        nodeRepository.addItem(nodeInfo);
                    }
                }
            } catch (Exception e) {
                throw e;
            }
        }
        return nodeInfo;
    }

    @Override
    public NodeInfo updateNodeInfo(String nodeId, NodeInfo nodeInfo) throws Exception {
        if (nodeInfo != null) {
            try {
                switch(nodeInfoLocation){
                    case NodeUtil.NODE_INFO_FILE: {
                        nodeManager.putNode(nodeInfo);
                    }
                    case NodeUtil.NODE_INFO_REPOSITOTY: {
                        nodeRepository.addItem(nodeInfo);
                    }
                }
            } catch (Exception e) {
                throw e;
            }
        }
        return nodeInfo;
    }

    @Override
    public String deleteNodeInfo(String nodeId) throws Exception {
        if (nodeId == null)
            throw (new ParameterNullOrEmptyException(NodeUtil.NODE_EXCEPTION_PARAMETER_NULL_EMPTY));
        try {
            switch(nodeInfoLocation){
                case NodeUtil.NODE_INFO_FILE: {
                    nodeManager.deleteNode(nodeId);
                }
                case NodeUtil.NODE_INFO_REPOSITOTY: {
                    nodeRepository.deleteItem(nodeId);
                }
            }
        } catch (Exception e) {
            throw e;
        }
        return nodeId;
    }
}
