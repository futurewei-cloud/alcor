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

import com.futurewei.alcor.dataplane.cache.LocalCache;
import com.futurewei.alcor.dataplane.cache.VpcTopicCache;
import com.futurewei.alcor.dataplane.client.NodeSubscribeClient;
import com.futurewei.alcor.schema.Subscribeinfoprovisioner;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.topic.VpcTopicInfo;
import org.apache.kafka.common.protocol.types.Field;
import org.apache.pulsar.common.util.Murmur3_32Hash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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

    public static String generateTopicByVpcId(String vpcId) {
        if (vpcId == null) {
            vpcId = "9192a4d4-ffff-4ece-b3f0-8d36e3d88038";
        }
        return vpcId;
    }

    public static String generateKeyByNodeIp(String nodeIp) {
        int hashCode = Murmur3_32Hash.getInstance().makeHash(nodeIp.getBytes(StandardCharsets.UTF_8)) % 65536;
        return Integer.toString(hashCode);
    }

    public VpcTopicInfo getTopicInfoByVpcId(String vpcId) throws Exception {
        VpcTopicInfo vpcTopicInfo = vpcTopicCache.getTopicInfoByVpcId(vpcId);
        if (vpcTopicInfo == null) {
            vpcTopicInfo = new VpcTopicInfo(TopicManager.generateTopicByVpcId(vpcId));
            try {
                vpcTopicCache.addTopicMapping(
                        vpcId,
                        vpcTopicInfo
                );
            } catch (Exception e) {

            }
        }

//        if (multicastKey == null || StringUtils.isEmpty(multicastKey)) {
//            multicastKey = topicManager.generateKeyByNodeId(new ArrayList<>(multicastGoalState.getHostIps()).get(multiGsIndex));
//
//            try {
//                vpcTopicCache.addSubscribedNodeForVpcId(
//                        multicastGoalState.getVpcIds().get(multiGsIndex),
//                        new ArrayList<>(multicastGoalState.getHostIps()).get(multiGsIndex),
//                        multicastKey
//                );
//            } catch (Exception e) {
//
//            }
//        }

        return vpcTopicInfo;
    }

    public void sendSubscribeInfo(String hostIp, String topic, String key) throws Exception {
        Subscribeinfoprovisioner.NodeSubscribeInfo.Builder infoBuilder = Subscribeinfoprovisioner.NodeSubscribeInfo.newBuilder();
        infoBuilder.setSubscribeOperationValue(1);
        infoBuilder.setTopic(topic);
        infoBuilder.setKey(key);
        Map infoMap = new HashMap<>();
        infoMap.put(
                hostIp,
                infoBuilder.build()
        );
        nodeSubscribeClient.asyncSendSubscribeInfos(infoMap);
    }

}