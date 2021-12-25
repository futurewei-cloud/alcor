package com.futurewei.alcor.dataplane.cache;

import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.Transaction;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.dataplane.client.pulsar.vpc_mode.TopicManager;
import com.futurewei.alcor.web.entity.topic.VpcTopicInfo;
import org.apache.pulsar.common.util.Murmur3_32Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
@ComponentScan(value = "com.futurewei.alcor.common.db")
@ConditionalOnProperty(prefix = "mq", name = "mode", havingValue = "vpc")
public class VpcTopicCache {
    private ICache<String, VpcTopicInfo> vpcTopicInfoICache;

    @Autowired
    public VpcTopicCache(CacheFactory cacheFactory) {
        vpcTopicInfoICache = cacheFactory.getCache(VpcTopicInfo.class);
    }

    @DurationStatistics
    public void addTopicMapping(String vpcId, VpcTopicInfo vpcTopicInfo) throws Exception {
        vpcTopicInfoICache.put(vpcId, vpcTopicInfo);
    }

    @DurationStatistics
    public void updateTopicMapping(String vpcId, VpcTopicInfo vpcTopicInfo) throws Exception {
        vpcTopicInfoICache.put(vpcId, vpcTopicInfo);
    }

    @DurationStatistics
    public VpcTopicInfo getTopicInfoByVpcId(String vpcId) throws Exception {
        try (Transaction tx = vpcTopicInfoICache.getTransaction().start()) {
            VpcTopicInfo vpcTopicInfo = vpcTopicInfoICache.get(vpcId);
            if (vpcTopicInfo == null) {
                vpcTopicInfo = new VpcTopicInfo(TopicManager.generateTopicByVpcId(vpcId));
                vpcTopicInfoICache.put(
                            vpcId,
                            vpcTopicInfo
                    );
            }
            tx.commit();
            return vpcTopicInfoICache.get(vpcId);
        }
    }

    @DurationStatistics
    public String getTopicNameByVpcId(String vpcId) throws Exception {
        try (Transaction tx = vpcTopicInfoICache.getTransaction().start()) {
            VpcTopicInfo vpcTopicInfo = vpcTopicInfoICache.get(vpcId);
            String topicName = null;
            if (vpcTopicInfo == null) {
                topicName = TopicManager.generateTopicByVpcId(vpcId);
                vpcTopicInfo = new VpcTopicInfo(topicName);
                vpcTopicInfoICache.put(
                        vpcId,
                        vpcTopicInfo
                );
            } else {
                topicName = vpcTopicInfo.getTopicName();
            }
            tx.commit();
            return topicName;
        }
    }

    @DurationStatistics
    public String getKeyByHostIp(String vpcId, String hostIp) throws Exception {
        VpcTopicInfo vpcTopicInfo = vpcTopicInfoICache.get(vpcId);
        Map<String, String> keyMap = vpcTopicInfo.getSubscribeMapping();
        return keyMap.get(hostIp);
    }

    @DurationStatistics
    public boolean isNodeSubscribeToVpcId(String nodeId, String vpcId) throws Exception {
        Set<String> nodeIdSet = vpcTopicInfoICache.get(vpcId).getSubscribeMapping().keySet();
        return nodeIdSet.contains(nodeId);
    }

    @DurationStatistics
    public boolean isNodeSubscribeToTopicName(String nodeId, String topicName) throws Exception {
        Map<String, Object[]> queryParams = new HashMap<>();
        Object[] values = new Object[1];
        values[0] = topicName;
        queryParams.put("topic_name", values);
        Map<String, VpcTopicInfo> topicMap = vpcTopicInfoICache.getAll(queryParams);
        Boolean result = false;
        if (topicMap.size() != 0) {
            for (Map.Entry<String, VpcTopicInfo> entry: topicMap.entrySet()) {
                result = entry.getValue().getSubscribeMapping().keySet().contains(nodeId);
            }
        }
        return result;
    }

    @DurationStatistics
    public void addSubscribedNodeForVpcId(String nodeId, String vpcId, String key) throws Exception {
        VpcTopicInfo vpcTopicInfo = vpcTopicInfoICache.get(vpcId);
        vpcTopicInfo.getSubscribeMapping().put(nodeId, key);
        vpcTopicInfoICache.put(vpcId, vpcTopicInfo);
    }
}
