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

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.nodemanager.config.Config;
import com.futurewei.alcor.nodemanager.dao.NodeRepository;
import com.futurewei.alcor.nodemanager.exception.UpdateNonExistingNodeException;
import com.futurewei.alcor.nodemanager.service.NodeService;
import com.futurewei.alcor.nodemanager.utils.MacAddrUtils;
import com.futurewei.alcor.web.entity.node.NodeInfo;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class NodeServiceImpl implements NodeService {
    private static final Logger logger = LoggerFactory.getLogger(NodeServiceImpl.class);

    @Autowired
    private NodeRepository nodeRepository;

    @Autowired
    private Config nodeManagerConfig;

    @DurationStatistics
    public int getNodeInfoFromUpload(MultipartFile file) throws FileNotFoundException, IOException, ParseException, CacheException {
        String strMethodName = "getNodeInfoFromUpload";
        int result = 0;
        List<NodeInfo> nodeList;

        Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
        nodeList = NodeFileLoader.getHostNodeListFromUpload(reader);
        if (nodeList != null) {
            nodeRepository.addItemBulkTransaction(nodeList);
            result = nodeList.size();
        }

        return result;
    }

    @Override
    @DurationStatistics
    public NodeInfo getNodeInfoById(String nodeId) throws CacheException {
        NodeInfo nodeInfo = nodeRepository.findItem(nodeId);
        return nodeInfo;
    }

    @Override
    @DurationStatistics
    public List<NodeInfo> getAllNodes(Map<String, Object[]> queryParams) throws CacheException {
        List<NodeInfo> result = new ArrayList<>();

        Map<String, NodeInfo> nodeInfoMap = nodeRepository.findAllItems(queryParams);
        if (nodeInfoMap == null) {
            return result;
        }

        for (Map.Entry<String, NodeInfo> entry : nodeInfoMap.entrySet()) {
            NodeInfo nodeInfo = new NodeInfo(entry.getValue());
            result.add(nodeInfo);
        }

        return result;
    }

    @Override
    @DurationStatistics
    public NodeInfo createNodeInfo(NodeInfo nodeInfo) throws CacheException {
        String strMethodName = "createNodeInfo";
        return this.setNodeInfo(nodeInfo, strMethodName);
    }

    @Override
    public void createNodeInfoBulk(List<NodeInfo> nodeInfo) throws CacheException {
        nodeRepository.addItemBulkTransaction(nodeInfo);
    }

    @Override
    @DurationStatistics
    public NodeInfo updateNodeInfo(String nodeId, NodeInfo nodeInfo) throws CacheException, UpdateNonExistingNodeException {
        String strMethodName = "updateNodeInfo";
        NodeInfo node = getNodeInfoById(nodeId);
        if (node == null) {
            throw new UpdateNonExistingNodeException();
        }

        this.setNodeInfo(nodeInfo, strMethodName);

        return getNodeInfoById(nodeId);
    }

    @Override
    @DurationStatistics
    public void deleteNodeInfo(String nodeId) throws CacheException, UpdateNonExistingNodeException {
        NodeInfo node = getNodeInfoById(nodeId);
        if (node == null) {
            throw new UpdateNonExistingNodeException();
        }

        nodeRepository.deleteItem(nodeId);
    }

    private NodeInfo setNodeInfo(NodeInfo nodeInfo, String strMethodName) throws CacheException {
        try {
            this.populateHostDvrMacAddr(nodeInfo);
            nodeRepository.addItem(nodeInfo);

            return nodeInfo;
        } catch (CacheException e) {
            logger.error(strMethodName + e.getMessage());
            throw e;
        }
    }

    private void populateHostDvrMacAddr(NodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return;
        }

        String originalMacAddr = nodeInfo.getHostDvrMac();
        if (MacAddrUtils.verifyMacAddress(originalMacAddr)) {
            return;
        }

        String generatedMacFromHostIp = "";
        nodeInfo.setHostDvrMac(generatedMacFromHostIp);
    }
}
