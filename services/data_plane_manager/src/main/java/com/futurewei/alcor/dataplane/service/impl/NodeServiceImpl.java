package com.futurewei.alcor.dataplane.service.impl;

import com.futurewei.alcor.dataplane.cache.LocalCache;
import com.futurewei.alcor.dataplane.service.NodeService;
import com.futurewei.alcor.web.entity.node.BulkNodeInfoJson;
import com.futurewei.alcor.web.entity.node.NodeInfo;
import com.futurewei.alcor.web.entity.node.NodeInfoJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NodeServiceImpl implements NodeService {
    @Autowired
    private LocalCache localCache;

    @Override
    public void createNodeInfo(NodeInfoJson nodeInfoJson) throws Exception {
        localCache.addNodeInfo(nodeInfoJson.getNodeInfo());
    }

    @Override
    public void updateNodeInfo(NodeInfoJson nodeInfoJson) throws Exception {
        localCache.updateNodeInfo(nodeInfoJson.getNodeInfo());
    }

    @Override
    public void deleteNodeInfo(NodeInfoJson nodeInfoJson) throws Exception {
        localCache.deleteNodeInfo(nodeInfoJson.getNodeInfo().getId());
    }

    @Override
    public void createNodeInfoBulk(BulkNodeInfoJson bulkNodeInfoJson) throws Exception {
        List<NodeInfo> nodeInfos = bulkNodeInfoJson.getNodeInfos();
        for (NodeInfo nodeInfo : nodeInfos) {
            localCache.addNodeInfo(nodeInfo);
        }
    }
}
