package com.futurewei.alcor.nodemanager.service;


import com.futurewei.alcor.web.entity.NodeInfo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface NodeService {
    int getNodeInfoFromUpload(MultipartFile file) throws Exception;

    NodeInfo getNodeInfoById(String nodeId) throws Exception;

    List getAllNodes() throws Exception;

    NodeInfo createNodeInfo(NodeInfo nodeInfo) throws Exception;

    NodeInfo updateNodeInfo(String nodeId, NodeInfo nodeInfo) throws Exception;

    String deleteNodeInfo(String nodeId) throws Exception;
}
