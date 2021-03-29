package com.futurewei.alcor.nodemanager.service;


import com.futurewei.alcor.common.exception.ParameterNullOrEmptyException;
import com.futurewei.alcor.nodemanager.exception.InvalidDataException;
import com.futurewei.alcor.web.entity.node.NodeInfo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface NodeService {
    int getNodeInfoFromUpload(MultipartFile file) throws IOException, Exception;

    NodeInfo getNodeInfoById(String nodeId) throws ParameterNullOrEmptyException, Exception;

    List getAllNodes() throws Exception;

    List<NodeInfo> getAllNodes(Map<String, Object[]> queryParams) throws Exception;

    NodeInfo createNodeInfo(NodeInfo nodeInfo) throws ParameterNullOrEmptyException, Exception;

    List<NodeInfo> createNodeInfoBulk(List<NodeInfo> nodeInfo) throws Exception;

    NodeInfo updateNodeInfo(String nodeId, NodeInfo nodeInfo) throws ParameterNullOrEmptyException, InvalidDataException, Exception;

    String deleteNodeInfo(String nodeId) throws ParameterNullOrEmptyException, Exception;
 
    // TEMP: URI will be provided by the Admin but these are for testing purpose.
    static String makeUpNcmId(String hostIP, int localPort) { return "ncm_" + hostIP + "_" + String.valueOf(localPort); }
    static String makeUpNcmUri(String hostIP, int localPort) { return "ncm/" + hostIP + "/" + String.valueOf(localPort); }
}
