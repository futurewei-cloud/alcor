package com.futurewei.alcor.netwconfigmanager.service;

import com.futurewei.alcor.web.entity.node.BulkNodeInfoJson;
import com.futurewei.alcor.web.entity.node.NodeInfo;
import com.futurewei.alcor.web.entity.node.NodeInfoJson;

import java.util.List;

public interface NodeService {

    void createNodeInfo(NodeInfoJson nodeInfoJson) throws Exception;

    void updateNodeInfo(NodeInfo nodeInfo) throws Exception;

    void deleteNodeInfo(String nodeId) throws Exception;

    void createNodeInfoBulk(BulkNodeInfoJson bulkNodeInfoJson) throws Exception;

    NodeInfo getNodeInfo(String nodeId) throws Exception;
}
