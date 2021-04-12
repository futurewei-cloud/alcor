/*
Copyright 2019 The Alcor Authors.

Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/
package com.futurewei.alcor.dataplane.client.pulsar.vpc_mode;

import com.futurewei.alcor.dataplane.cache.LocalCache;
import com.futurewei.alcor.dataplane.cache.NodeTopicCache;
import com.futurewei.alcor.dataplane.cache.VpcTopicCache;
import com.futurewei.alcor.dataplane.client.DataPlaneClient;
import com.futurewei.alcor.dataplane.entity.MulticastGoalState;
import com.futurewei.alcor.dataplane.entity.UnicastGoalState;
import com.futurewei.alcor.dataplane.exception.GroupTopicNotFound;
import com.futurewei.alcor.dataplane.exception.MulticastTopicNotFound;
import com.futurewei.alcor.web.entity.dataplane.MulticastGoalStateByte;
import com.futurewei.alcor.web.entity.dataplane.UnicastGoalStateByte;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.impl.schema.JSONSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@Component
@Service("pulsarDataPlaneClient")
@ConditionalOnProperty(prefix = "mq", name = "mode", havingValue = "vpc")
public class DataPlaneClientImpl extends com.futurewei.alcor.dataplane.client.pulsar.group_node_mode.DataPlaneClientImpl {
    private static final Logger LOG = LoggerFactory.getLogger(DataPlaneClientImpl.class);

    @Autowired
    private PulsarClient pulsarClient;

    @Autowired
    LocalCache localCache;

    @Autowired
    private VpcTopicCache vpcTopicCache;

    private Map<String, List<String>> getMulticastTopics(List<String> hostIps) throws Exception {
        Map<String, List<String>> multicastTopics = new HashMap<>();

        for (String hostIp : hostIps) {
//            String groupTopic = localCache.getNodeInfoByNodeIp(hostIp).get(0).getGroupTopic();
            String groupTopic = vpcTopicCache.getTopicByVpcId(unicastGoalState.getVpcId()).getTopicName();
            if (StringUtils.isEmpty(groupTopic)) {
                LOG.error("Can not find group topic by host ip:{}", hostIp);
                throw new GroupTopicNotFound();
            }

            String multicastTopic = localCache.getNodeInfoByNodeIp(hostIp).get(0).getMulticastTopic();
            if (StringUtils.isEmpty(multicastTopic)) {
                LOG.error("Can not find multicast topic by host ip:{}", hostIp);
                throw new MulticastTopicNotFound();
            }

            if (!multicastTopics.containsKey(multicastTopic)) {
                multicastTopics.put(multicastTopic, new ArrayList<>());
            }

            multicastTopics.get(multicastTopic).add(groupTopic);
        }

        return multicastTopics;
    }

    private List<String> createGoalState(MulticastGoalState multicastGoalState) throws Exception {
        List<String> failedHosts = new ArrayList<>();

        Map<String, List<String>> multicastTopics = getMulticastTopics(multicastGoalState.getHostIps());

        for (Map.Entry<String, List<String>> entry: multicastTopics.entrySet()) {
            String multicastTopic = entry.getKey();
            List<String> groupTopics = entry.getValue();

            multicastGoalState.setNextTopics(groupTopics);

            try {
                Producer<MulticastGoalStateByte> producer = pulsarClient
                        .newProducer(JSONSchema.of(MulticastGoalStateByte.class))
                        .topic(multicastTopic)
                        .enableBatching(false)
                        .create();

                producer.send(multicastGoalState.getMulticastGoalStateByte());
            } catch (Exception e) {
                LOG.error("Send multicastGoalState to topic:{} failed: ", multicastTopic, e);
                failedHosts.addAll(multicastGoalState.getHostIps());
                continue;
            }

            LOG.info("Send multicastGoalState to topic:{} success, " +
                            "groupTopics: {}, unicastGoalStates: {}",
                    multicastTopic, groupTopics, multicastGoalState);
        }

        return failedHosts;
    }

    @Override
    public List<String> sendGoalStates(List<UnicastGoalState> unicastGoalStates) throws Exception {
        List<String> failedHosts = new ArrayList<>();

        for (UnicastGoalState unicastGoalState: unicastGoalStates) {
            String nextTopic = unicastGoalState.getVpcId();
            if (StringUtils.isEmpty(nextTopic)) {
                LOG.error("Can not find next topic by host ip:{}", unicastGoalState.getHostIp());
                throw new GroupTopicNotFound();
            }

            String topic = nextTopic;
//            String unicastTopic = localCache.getNodeInfoByNodeIp(unicastGoalState.getHostIp()).get(0).getUnicastTopic();
            String unicastTopic = topicMappingCache.getTopicByVpcId(unicastGoalState.getVpcId()).getTopicName();
            if (!StringUtils.isEmpty(unicastTopic)) {
                unicastGoalState.setNextTopic(nextTopic);
                topic = unicastTopic;
            }

            try {
                Producer<UnicastGoalStateByte> producer = pulsarClient
                        .newProducer(JSONSchema.of(UnicastGoalStateByte.class))
                        .topic(topic)
                        .enableBatching(false)
                        .create();
                producer.send(unicastGoalState.getUnicastGoalStateByte());
            } catch (Exception e) {
                LOG.error("Send unicastGoalStates to topic:{} failed: ", topic, e);
                failedHosts.add(unicastGoalState.getHostIp());
                continue;
            }

            LOG.info("Send unicastGoalStates to topic:{} success, " +
                    "unicastGoalStates: {}", nextTopic, unicastGoalState);
        }

        return failedHosts;
    }

    @Override
    public List<String> sendGoalStates(List<UnicastGoalState> unicastGoalStates, MulticastGoalState multicastGoalState) throws Exception {
        List<String> failedHosts = new ArrayList<>();

        failedHosts.addAll(sendGoalStates(unicastGoalStates));
        failedHosts.addAll(createGoalState(multicastGoalState));

        return failedHosts;
    }
}