package com.futurewei.alcor.dataplane.cache;

import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.dataplane.client.pulsar.TopicManager;
import com.futurewei.alcor.web.entity.topic.NodeTopicInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

@Repository
@ComponentScan(value = "com.futurewei.alcor.common.db")
public class NodeTopicCache {
    private ICache<String, NodeTopicInfo> nodeTopicInfoICache;

    @Autowired
    public NodeTopicCache(CacheFactory cacheFactory) {
        nodeTopicInfoICache = cacheFactory.getCache(NodeTopicInfo.class);
    }

    @Autowired
    TopicManager topicManager;

    @DurationStatistics
    public void addNodeTopicInfo(String nodeId, NodeTopicInfo nodeTopicInfo) throws Exception {
        nodeTopicInfoICache.put(nodeId, nodeTopicInfo);
    }

    @DurationStatistics
    public NodeTopicInfo getNodeTopicInfo(String nodeId) throws Exception {
        NodeTopicInfo nodeTopicInfo = nodeTopicInfoICache.get(nodeId);
        if (nodeTopicInfo == null) {
            nodeTopicInfo = topicManager.createNodeTopicInfo(nodeId);
            this.addNodeTopicInfo(nodeId, nodeTopicInfo);
        }
        return nodeTopicInfo;
    }

    @DurationStatistics
    public void updateNodeTopicInfo(String nodeId, NodeTopicInfo nodeTopicInfo) throws Exception {
        nodeTopicInfoICache.put(nodeId, nodeTopicInfo);
    }
}
