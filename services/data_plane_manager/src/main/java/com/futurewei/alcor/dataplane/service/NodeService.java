package com.futurewei.alcor.dataplane.service;

import com.futurewei.alcor.web.entity.node.BulkNodeInfoJson;
import com.futurewei.alcor.web.entity.node.NodeInfo;
import com.futurewei.alcor.web.entity.node.NodeInfoJson;

import java.util.List;

public interface NodeService {

    void createNodeInfo(NodeInfoJson nodeInfoJson) throws Exception;

    void updateNodeInfo(NodeInfoJson nodeInfoJson) throws Exception;

    void deleteNodeInfo(NodeInfoJson nodeInfoJson) throws Exception;

    void createNodeInfoBulk(BulkNodeInfoJson bulkNodeInfoJson) throws Exception;
}
