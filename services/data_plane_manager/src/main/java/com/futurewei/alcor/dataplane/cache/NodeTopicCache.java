package com.futurewei.alcor.dataplane.cache;
/*
 *
 * Copyright 2019 The Alcor Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *         you may not use this file except in compliance with the License.
 *         You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *         See the License for the specific language governing permissions and
 *         limitations under the License.
 * /
 */

import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.dataplane.client.pulsar.TopicManager;
import com.futurewei.alcor.web.entity.topic.NodeTopicInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional(rollbackFor = Exception.class)
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

    @DurationStatistics
    @Transactional(rollbackFor = Exception.class)
    public NodeTopicInfo getNodeTopicInfoByNodeIp(String nodeIp) throws Exception {
        String nodeId = localCache.getNodeInfoByNodeIp(nodeIp).get(0).getId();
        NodeTopicInfo nodeTopicInfo = nodeTopicInfoICache.get(nodeId);
        if (nodeTopicInfo == null) {
            nodeTopicInfo = topicManager.createNodeTopicInfo(nodeId);
            this.addNodeTopicInfo(nodeId, nodeTopicInfo);
        }
        return nodeTopicInfo;
    }
}
