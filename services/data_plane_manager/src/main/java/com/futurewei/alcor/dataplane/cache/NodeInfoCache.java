package com.futurewei.alcor.dataplane.cache;

import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.common.utils.SpringContextUtil;
import com.futurewei.alcor.web.entity.node.NodeInfo;
import com.futurewei.alcor.web.restclient.NodeManagerRestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

@Repository
@ComponentScan(value = "com.futurewei.alcor.common.db")
public class NodeInfoCache {
    private ICache<String, NodeInfo> nodeInfoCache;
    private NodeManagerRestClient nodeManagerRestClient;

    @Autowired
    public NodeInfoCache(CacheFactory cacheFactory) {
        nodeInfoCache = cacheFactory.getCache(NodeInfo.class);
        nodeManagerRestClient = SpringContextUtil.getBean(NodeManagerRestClient.class);
    }


    @DurationStatistics
    public NodeInfo getNodeInfo(String nodeIp) throws Exception {
        NodeInfo nodeInfo;
        try {
            nodeInfo = nodeInfoCache.get(nodeIp);
            assert nodeInfo != null;
        } catch (Exception e) {
            NodeInfo newNodeInfo = nodeManagerRestClient.getNodeInfo(nodeIp).getNodeInfo();
            nodeInfoCache.put(newNodeInfo.getLocalIp(), newNodeInfo);
            nodeInfo = newNodeInfo;
        }
        return nodeInfo;
    }

    @DurationStatistics
    public synchronized void addNodeInfo(NodeInfo nodeInfo) throws Exception {
        nodeInfoCache.put(nodeInfo.getLocalIp(), nodeInfo);
    }

    @DurationStatistics
    public void updateNodeInfo(NodeInfo nodeInfo) throws Exception {
        nodeInfoCache.put(nodeInfo.getLocalIp(), nodeInfo);
    }

    @DurationStatistics
    public void deleteNodeInfo(String nodeId) throws Exception {
        nodeInfoCache.remove(nodeId);
    }
}
