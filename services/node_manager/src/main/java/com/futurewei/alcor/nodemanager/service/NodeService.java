package com.futurewei.alcor.nodemanager.service;

import com.futurewei.alcor.nodemanager.entity.NodeInfo;

public interface NodeService {
    NodeInfo getNodeInfoById(String nodeId) throws Exception;

    NodeInfo createNodeInfo(NodeInfo nodeInfo) throws Exception;

    NodeInfo updateNodeInfo(String nodeId, NodeInfo nodeInfo) throws Exception;

    String deleteNodeInfo(String nodeId) throws Exception;
}
