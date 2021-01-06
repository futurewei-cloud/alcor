package com.futurewei.alcor.dataplane.cache;

import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.web.entity.node.NodeInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

@Repository
@ComponentScan(value = "com.futurewei.alcor.common.db")

public class NodeInfoCache {
    private ICache<String, NodeInfo> nodeInfoCache;

    @Autowired
    public NodeInfoCache(CacheFactory cacheFactory) {
        nodeInfoCache = cacheFactory.getCache(NodeInfo.class);
    }

    @DurationStatistics
    public NodeInfo getNodeInfo(String nodeId) throws Exception {
        return nodeInfoCache.get(nodeId);
    }

    @DurationStatistics
    public synchronized void addNodeInfo(NodeInfo nodeInfo) throws Exception {
        nodeInfoCache.put(nodeInfo.getId(), nodeInfo);
    }

    @DurationStatistics
    public void updateNodeInfo(NodeInfo nodeInfo) throws Exception {
        nodeInfoCache.put(nodeInfo.getId(), nodeInfo);
    }

    @DurationStatistics
    public void deleteNodeInfo(String nodeId) throws Exception {
        nodeInfoCache.remove(nodeId);
    }
}
