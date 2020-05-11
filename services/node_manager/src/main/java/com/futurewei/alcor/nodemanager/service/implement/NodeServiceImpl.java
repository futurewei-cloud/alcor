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
import com.futurewei.alcor.nodemanager.dao.NodeRepository;
import com.futurewei.alcor.nodemanager.entity.NodeInfo;
import com.futurewei.alcor.nodemanager.service.NodeService;
import com.futurewei.alcor.nodemanager.utils.NodeManagerConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@Service
public class NodeServiceImpl implements NodeService {
    private static final Logger logger = LoggerFactory.getLogger(NodeServiceImpl.class);

    @Autowired
    private NodeRepository nodeRepository;

    public int getNodeInfoFromUpload(MultipartFile file) throws Exception {
        int nReturn = 0;
        List<NodeInfo> nodeList = new ArrayList<NodeInfo>();
        try {
            Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
            NodeFileLoader dataCenterConfigLoader = new NodeFileLoader();
            nodeList = dataCenterConfigLoader.getHostNodeListFromUpload(reader);
            if (nodeList != null) {
                nodeRepository.addItemBulkTransaction(nodeList);
                nReturn = nodeList.size();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return nReturn;
    }

    @Override
    public int getNodeInfoFromFile(String path) throws Exception {
        int nReturn = 0;
        NodeFileLoader dataCenterConfigLoader = new NodeFileLoader();
        List<NodeInfo> nodeList = dataCenterConfigLoader.loadAndGetHostNodeList(path);
        if (nodeList != null) {
            nodeRepository.addItemBulkTransaction(nodeList);
            nReturn = nodeList.size();
        }
        return nReturn;
    }

    @Override
    public NodeInfo getNodeInfoById(String nodeId) throws Exception {
        if (nodeId == null)
            throw (new ParameterNullOrEmptyException(NodeManagerConstant.NODE_EXCEPTION_PARAMETER_NULL_EMPTY));
        NodeInfo nodeInfo = null;
        try {
            nodeInfo = nodeRepository.findItem(nodeId);
        } catch (Exception e) {
            throw e;
        }
        return nodeInfo;
    }

    @Override
    public List<NodeInfo> getAllNodes() throws Exception {
        List<NodeInfo> nodes = new ArrayList<NodeInfo>();
        try {
            nodes = new ArrayList(nodeRepository.findAllItems().values());
        } catch (Exception e) {
            throw e;
        }
        return nodes;
    }

    @Override
    public NodeInfo createNodeInfo(NodeInfo nodeInfo) throws Exception {
        if (nodeInfo == null)
            throw (new ParameterNullOrEmptyException(NodeManagerConstant.NODE_EXCEPTION_PARAMETER_NULL_EMPTY));
        NodeInfo node = getNodeInfoById(nodeInfo.getId());
        if (nodeInfo != null) {
            try {
                nodeRepository.addItem(nodeInfo);
            } catch (Exception e) {
                throw e;
            }
        }
        return nodeInfo;
    }

    @Override
    public NodeInfo updateNodeInfo(String nodeId, NodeInfo nodeInfo) throws Exception {
        if (nodeId == null || nodeInfo == null)
            throw (new ParameterNullOrEmptyException(NodeManagerConstant.NODE_EXCEPTION_PARAMETER_NULL_EMPTY));
        NodeInfo node = getNodeInfoById(nodeId);
        if (node == null)
            throw (new ParameterNullOrEmptyException(NodeManagerConstant.NODE_EXCEPTION_NODE_NOT_EXISTING));
        else if (nodeId.equals(node.getId()) == false) {
            throw (new ParameterNullOrEmptyException(NodeManagerConstant.NODE_EXCEPTION_NODE_NOT_EXISTING));
        }
        if (nodeInfo != null) {
            try {
                nodeRepository.addItem(nodeInfo);
            } catch (Exception e) {
                throw e;
            }
        }
        return nodeInfo;
    }

    @Override
    public String deleteNodeInfo(String nodeId) throws Exception {
        if (nodeId == null)
            throw (new ParameterNullOrEmptyException(NodeManagerConstant.NODE_EXCEPTION_PARAMETER_NULL_EMPTY));
        NodeInfo node = getNodeInfoById(nodeId);
        if (node == null)
            throw (new ParameterNullOrEmptyException(NodeManagerConstant.NODE_EXCEPTION_NODE_NOT_EXISTING));
        else if (nodeId.equals(node.getId()) == false)
            throw (new ParameterNullOrEmptyException(NodeManagerConstant.NODE_EXCEPTION_NODE_NOT_EXISTING));
        try {
            nodeRepository.deleteItem(nodeId);
        } catch (Exception e) {
            throw e;
        }
        return nodeId;
    }
}
