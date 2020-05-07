package com.futurewei.alcor.nodemanager.service;

import com.futurewei.alcor.nodemanager.entity.NodeInfo;

import java.util.Hashtable;
import java.util.List;

public interface NodeService {
    int getNodeInfoFromFile(String path) throws Exception;

    NodeInfo getNodeInfoById(String nodeId) throws Exception;

    List getAllNodes() throws Exception;

    NodeInfo createNodeInfo(NodeInfo nodeInfo) throws Exception;

    NodeInfo updateNodeInfo(String nodeId, NodeInfo nodeInfo) throws Exception;

    String deleteNodeInfo(String nodeId) throws Exception;
}
