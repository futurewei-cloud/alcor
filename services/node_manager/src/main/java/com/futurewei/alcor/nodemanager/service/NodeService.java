package com.futurewei.alcor.nodemanager.service;

import com.futurewei.alcor.nodemanager.entity.NodeInfo;

import java.util.Hashtable;
import java.util.List;

public interface NodeService {
    NodeInfo getNodeInfoById(String nodeId) throws Exception;

    Hashtable getAllNodes() throws Exception;

    List getAllNodesList() throws Exception;

    NodeInfo createNodeInfo(NodeInfo nodeInfo) throws Exception;

    NodeInfo updateNodeInfo(String nodeId, NodeInfo nodeInfo) throws Exception;

    String deleteNodeInfo(String nodeId) throws Exception;
}
