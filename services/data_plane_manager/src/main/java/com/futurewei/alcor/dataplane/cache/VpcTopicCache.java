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
import com.futurewei.alcor.dataplane.client.pulsar.vpc_mode.TopicManager;
import com.futurewei.alcor.schema.Subscribeinfoprovisioner.NodeSubscribeInfo;
import com.futurewei.alcor.web.entity.topic.VpcTopicInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

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
    public NodeSubscribeInfo getNodeSubscribeInfoByVpcId(String vpcId, String hostIp) throws Exception {
        NodeSubscribeInfo.Builder nodeSubscribeBuilder = NodeSubscribeInfo.newBuilder();
        VpcTopicInfo vpcTopicInfo = vpcTopicInfoICache.get(vpcId);
        if (vpcTopicInfo == null) {
            return null;
        }
        nodeSubscribeBuilder.setTopic(vpcTopicInfo.getTopicName());
        String key = vpcTopicInfo.getSubscribeMapping().get(hostIp);
        if (key == null) {
            return null;
        }
        nodeSubscribeBuilder.setKey(key);
        return nodeSubscribeBuilder.build();
    }

    @DurationStatistics
    public synchronized void addSubscribedNodeForVpc(String vpcId, String topic, String hostIp, String key) throws Exception {
        try (Transaction tx = vpcTopicInfoICache.getTransaction().start()) {
            VpcTopicInfo vpcTopicInfo = vpcTopicInfoICache.get(vpcId);
            if (vpcTopicInfo == null) {
                vpcTopicInfo = new VpcTopicInfo(topic);
            }
            vpcTopicInfo.getSubscribeMapping().put(hostIp, key);
            vpcTopicInfoICache.put(vpcId, vpcTopicInfo);
            tx.commit();
        }
    }

    @DurationStatistics
    public synchronized void deleteNodeSubscribeInfo(String vpcId, String hostIp) throws Exception {
        try (Transaction tx = vpcTopicInfoICache.getTransaction().start()) {
            VpcTopicInfo vpcTopicInfo = vpcTopicInfoICache.get(vpcId);
            if (vpcTopicInfo != null) {
                vpcTopicInfo.getSubscribeMapping().remove(hostIp);
            }
            vpcTopicInfoICache.put(vpcId, vpcTopicInfo);
            tx.commit();
        }
    }
}
