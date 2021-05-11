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
import com.futurewei.alcor.dataplane.cache.TopicMappingCache;
import com.futurewei.alcor.dataplane.cache.VpcNodeIdsCache;
import com.futurewei.alcor.dataplane.cache.VpcTopicCache;
import com.futurewei.alcor.dataplane.client.pulsar.vpc_mode.PulsarConfiguration;
import com.futurewei.alcor.dataplane.exception.TopicParseFailureException;
import com.futurewei.alcor.web.entity.port.PortEntity;
import com.futurewei.alcor.web.entity.topic.TopicInfo;
import com.futurewei.alcor.web.entity.topic.VpcTopicInfo;
import org.apache.pulsar.common.util.Murmur3_32Hash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Configuration
@ConditionalOnProperty(prefix = "mq", name = "mode", havingValue = "vpc")
public class TopicManager {
    private static final Logger LOG = LoggerFactory.getLogger(TopicManager.class);

    @Autowired
    private PulsarConfiguration configuration;

    @Autowired
    LocalCache localCache;

    @Autowired
    private VpcTopicCache vpcTopicCache;

    @Autowired
    private VpcNodeIdsCache vpcNodeIdsCache;

    public void updateTopicInfoByPortEntity(PortEntity portEntity) throws Exception{
        String vpcId = portEntity.getVpcId();
        if (vpcTopicCache.getTopicByVpcId(vpcId) != null) {
            if (!vpcTopicCache.isNodeSubscribeToVpcId(portEntity.getBindingHostId(), vpcId)) {
                vpcTopicCache.addSubscribedNodeForVpcId(portEntity.getBindingHostId(), vpcId);
                String nodeKey = generateKeyByNodeId(portEntity.getBindingHostId());
                if (vpcTopicCache.getTopicByVpcId(vpcId).getSubscribeMapping().containsValue(nodeKey)) {
                    nodeKey = generateKeyByNodeId(portEntity.getBindingHostId() + portEntity.getBindingHostId());
                }
                vpcTopicCache.getTopicByVpcId(vpcId).getSubscribeMapping().put(portEntity.getBindingHostId(), nodeKey);
            }
        } else {
            vpcTopicCache.addTopicMapping(
                    vpcId,
                    new VpcTopicInfo(generateTopicByVpcId(vpcId))
            );
            vpcTopicCache.getTopicByVpcId(vpcId).getSubscribeMapping().put(portEntity.getBindingHostId(), generateKeyByNodeId(portEntity.getBindingHostId()));
        }
    }

    private VpcTopicInfo getTopicInfoByNodeIp(String nodeIp) throws Exception {
//        String nodeId = localCache.getNodeInfoByNodeIp(nodeIp).get(0).getId();
//        String vpcId = localCache.
//
//                 topicInfo = new TopicInfo(
//                localCache.getNodeInfoByNodeIp(nodeIp).get(0).getId(),
//                new HashMap<>()
//        )
        return null;
    }

    private String generateTopicByVpcId(String vpcId) {
        return vpcId;
    }

    private String generateKeyByNodeId(String nodeId) {
        int hashCode = Murmur3_32Hash.getInstance().makeHash(nodeId.getBytes(StandardCharsets.UTF_8));
        return Integer.toString(hashCode);
    }
}