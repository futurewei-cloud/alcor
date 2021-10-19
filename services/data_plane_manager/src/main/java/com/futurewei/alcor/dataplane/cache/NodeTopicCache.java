/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.futurewei.alcor.dataplane.cache;

import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.Transaction;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.dataplane.client.pulsar.group_node_mode.TopicManager;
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

    @Autowired
    LocalCache localCache;


    @DurationStatistics
    public void addNodeTopicInfo(String nodeId, NodeTopicInfo nodeTopicInfo) throws Exception {
        nodeTopicInfoICache.put(nodeId, nodeTopicInfo);
    }

    @DurationStatistics
    public NodeTopicInfo getNodeTopicInfo(String nodeId) throws Exception {
        try (Transaction tx = nodeTopicInfoICache.getTransaction().start()) {
            NodeTopicInfo nodeTopicInfo = nodeTopicInfoICache.get(nodeId);
            if (nodeTopicInfo == null) {
                nodeTopicInfo = topicManager.createNodeTopicInfo(nodeId);
                this.addNodeTopicInfo(nodeId, nodeTopicInfo);
            }

            tx.commit();
            return nodeTopicInfo;
        }

    }

    @DurationStatistics
    public void updateNodeTopicInfo(String nodeId, NodeTopicInfo nodeTopicInfo) throws Exception {
        nodeTopicInfoICache.put(nodeId, nodeTopicInfo);
    }

    @DurationStatistics
    public NodeTopicInfo getNodeTopicInfoByNodeIp(String nodeIp) throws Exception {
        try (Transaction tx = nodeTopicInfoICache.getTransaction().start()) {
            String nodeId = localCache.getNodeInfoByNodeIp(nodeIp).get(0).getId();
            NodeTopicInfo nodeTopicInfo = nodeTopicInfoICache.get(nodeId);
            if (nodeTopicInfo == null) {
                nodeTopicInfo = topicManager.createNodeTopicInfo(nodeId);
                this.addNodeTopicInfo(nodeId, nodeTopicInfo);
            }

            tx.commit();
            return nodeTopicInfo;
        }
    }
}
