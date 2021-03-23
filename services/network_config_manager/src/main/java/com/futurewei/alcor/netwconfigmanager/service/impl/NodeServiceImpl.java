package com.futurewei.alcor.netwconfigmanager.service.impl;

import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.netwconfigmanager.cache.NodeInfoCache;
import com.futurewei.alcor.netwconfigmanager.service.NodeService;
import com.futurewei.alcor.web.entity.node.BulkNodeInfoJson;
import com.futurewei.alcor.web.entity.node.NodeInfo;
import com.futurewei.alcor.web.entity.node.NodeInfoJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Level;

/**
 * DPM, NMM and now NCM all are holding onto NodeInfo, by making copy of the code
 * instead of sharing. Let this get working first and the re-factor as much as possible into
 * common as shared module/package.
 */
@Service
public class NodeServiceImpl implements NodeService {
    private static final Logger LOG = LoggerFactory.getLogger();
    @Autowired
    private NodeInfoCache nodeInfoCache;
    @Override
    public void createNodeInfo(NodeInfoJson nodeInfoJson) throws Exception {
        nodeInfoCache.addNodeInfo(nodeInfoJson.getNodeInfo());
    }

    @Override
    public void updateNodeInfo(NodeInfo nodeInfo) throws Exception {
        nodeInfoCache.updateNodeInfo(nodeInfo);
    }

    @Override
    public void deleteNodeInfo(String nodeId) throws Exception {
        nodeInfoCache.deleteNodeInfo(nodeId);
    }

    @Override
    public void createNodeInfoBulk(BulkNodeInfoJson bulkNodeInfoJson) throws Exception {
        List<NodeInfo> nodeInfos = bulkNodeInfoJson.getNodeInfos();
        nodeInfoCache.addNodeInfoBulk(nodeInfos);
    }

    @Override
    public NodeInfo getNodeInfo(String nodeId) throws Exception {
        return nodeInfoCache.getNodeInfo(nodeId);
    }
}