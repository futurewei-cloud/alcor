package com.futurewei.alcor.dataplane.service;

import com.futurewei.alcor.web.entity.node.NodeInfo;
import com.futurewei.alcor.web.entity.node.NodeInfoJson;

public interface NodeService {

    void createNodeInfo(NodeInfoJson nodeInfoJson) throws Exception;

    void updateNodeInfo(NodeInfoJson nodeInfoJson) throws Exception;

    void deleteNodeInfo(NodeInfoJson nodeInfoJson) throws Exception;
}
