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
package com.futurewei.alcor.dataplane.client.pulsar.vpc_mode;

import com.futurewei.alcor.dataplane.cache.VpcTopicCache;
import com.futurewei.alcor.dataplane.client.NodeSubscribeClient;
import com.futurewei.alcor.dataplane.rollback.SendTopicInfoRollback;
import com.futurewei.alcor.schema.Subscribeinfoprovisioner.NodeSubscribeInfo;
import org.apache.pulsar.common.util.Murmur3_32Hash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Configuration
@ConditionalOnProperty(prefix = "mq", name = "mode", havingValue = "vpc")
public class TopicManager {
    private static final Logger LOG = LoggerFactory.getLogger(TopicManager.class);

    @Autowired
    private VpcTopicCache vpcTopicCache;

    @Autowired
    private NodeSubscribeClient nodeSubscribeClient;

    public NodeSubscribeInfo getNodeSubscribeInfoByVpcId(String vpcId, String hostIp) throws Exception {
        NodeSubscribeInfo nodeSubscribeInfo = vpcTopicCache.getNodeSubscribeInfoByVpcId(vpcId, hostIp);
        if (nodeSubscribeInfo == null) {
            try {
                String topic = this.generateTopicByVpcId(vpcId);
                String key = this.generateKeyByHostIp(hostIp);
                vpcTopicCache.addSubscribedNodeForVpc(
                        vpcId,
                        this.generateTopicByVpcId(vpcId),
                        hostIp,
                        this.generateKeyByHostIp(hostIp)
                );
                this.sendSubscribeInfo(
                        hostIp,
                        topic,
                        this.generateHashKeyByKey(key)
                );
                NodeSubscribeInfo.Builder nodeSubscribeInfoBuilder = NodeSubscribeInfo.newBuilder();
                nodeSubscribeInfoBuilder.setTopic(topic);
                nodeSubscribeInfoBuilder.setKey(key);
                return nodeSubscribeInfoBuilder.build();
            } catch (Exception e) {
                LOG.error("{} node subscribe topic failed for VPC {}", hostIp, vpcId);
                SendTopicInfoRollback sendTopicInfoRollback = new SendTopicInfoRollback(vpcId, hostIp);
                handleException(sendTopicInfoRollback);
            }

        }
        return nodeSubscribeInfo;
    }

    public void sendSubscribeInfo(String hostIp, String topic, String key) throws Exception {
        NodeSubscribeInfo.Builder infoBuilder = NodeSubscribeInfo.newBuilder();
        infoBuilder.setSubscribeOperationValue(0);
        infoBuilder.setTopic(topic);
        infoBuilder.setKey(this.generateHashKeyByKey(key));
        Map infoMap = new HashMap<>();
        infoMap.put(hostIp, infoBuilder.build());
        nodeSubscribeClient.asyncSendSubscribeInfos(infoMap);
    }

    public String generateTopicByVpcId(String vpcId) {
        return vpcId;
    }

    public String generateKeyByHostIp(String hostIp) {
        return hostIp;
    }

    public String generateHashKeyByKey(String key) {
        int hashCode = Murmur3_32Hash.getInstance().makeHash(key.getBytes(StandardCharsets.UTF_8)) % 65536;
        return Integer.toString(hashCode);
    }

    private void handleException(SendTopicInfoRollback sendTopicInfoRollback) {
        try {
            sendTopicInfoRollback.doRollback();
        } catch (Exception e) {
            LOG.error("{} roll back failed: {}", sendTopicInfoRollback, e);
        }
    }
}