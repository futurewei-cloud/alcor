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
package com.futurewei.alcor.dataplane.client.pulsar;

import com.futurewei.alcor.dataplane.client.DataPlaneClient;
import com.futurewei.alcor.dataplane.entity.MulticastGoalState;
import com.futurewei.alcor.dataplane.entity.UnicastGoalState;
import com.futurewei.alcor.dataplane.exception.GroupTopicNotFound;
import com.futurewei.alcor.dataplane.exception.MulticastTopicNotFound;
import com.futurewei.alcor.schema.Goalstate.GoalState;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.impl.schema.JSONSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@ConditionalOnProperty(prefix = "mq", name = "type", havingValue = "pulsar")
public class DataPlaneClientImpl implements DataPlaneClient {
    private static final Logger LOG = LoggerFactory.getLogger(DataPlaneClientImpl.class);

    @Autowired
    private TopicManager topicManager;

    @Autowired
    private PulsarClient pulsarClient;

    @Override
    public void createGoalState(GoalState goalState, String hostIp) throws Exception {

    }

    @Override
    public void createGoalState(List<UnicastGoalState> unicastGoalStates) throws Exception {
        for (UnicastGoalState unicastGoalState: unicastGoalStates) {
            String nextTopic = topicManager.getGroupTopicByHostIp(unicastGoalState.getHostIp());
            if (StringUtils.isEmpty(nextTopic)) {
                throw new GroupTopicNotFound();
            }

            String topic = nextTopic;
            String unicastTopic = topicManager.getUnicastTopic();
            if (!StringUtils.isEmpty(unicastTopic)) {
                unicastGoalState.setNextTopic(nextTopic);
                topic = unicastTopic;
            }

            Producer<UnicastGoalState> producer = pulsarClient
                    .newProducer(JSONSchema.of(UnicastGoalState.class))
                    .topic(topic)
                    .enableBatching(false)
                    .create();
            producer.send(unicastGoalState);

            LOG.info("Send unicastGoalStates to topic:{} success, " +
                    "unicastGoalStates: {}", nextTopic, unicastGoalState);
        }
    }

    @Override
    public void updateGoalState(List<UnicastGoalState> unicastGoalStates) throws Exception {

    }

    @Override
    public void deleteGoalState(List<UnicastGoalState> unicastGoalStates) throws Exception {

    }

    private List<String> getGroupTopics(List<String> hostIps) throws Exception {
        List<String> groupTopics = new ArrayList<>();

        for (String hostIp: hostIps) {
            String groupTopic = topicManager.getGroupTopicByHostIp(hostIp);
            if (StringUtils.isEmpty(groupTopic)) {
                throw new GroupTopicNotFound();
            }

            groupTopics.add(groupTopic);
        }

        return groupTopics;
    }

    private Map<String, List<String>> getMulticastTopics(List<String> hostIps) throws Exception {
        Map<String, List<String>> multicastTopics = new HashMap<>();

        List<String> groupTopics = this.getGroupTopics(hostIps);
        for (String groupTopic: groupTopics) {
            String multicastTopic = topicManager.getMulticastTopicByGroupTopic(groupTopic);
            if (StringUtils.isEmpty(multicastTopic)) {
                throw new MulticastTopicNotFound();
            }

            if (!multicastTopic.contains(multicastTopic)) {
                multicastTopics.put(multicastTopic, new ArrayList<>());
            }

            multicastTopics.get(multicastTopic).add(groupTopic);
        }

        return multicastTopics;
    }

    @Override
    public void createGoalState(MulticastGoalState multicastGoalState) throws Exception {
        Map<String, List<String>> multicastTopics = getMulticastTopics(multicastGoalState.getHostIps());

        for (Map.Entry<String, List<String>> entry: multicastTopics.entrySet()) {
            String multicastTopic = entry.getKey();
            List<String> groupTopics = entry.getValue();

            multicastGoalState.setNextTopics(groupTopics);

            Producer<MulticastGoalState> producer = pulsarClient
                    .newProducer(JSONSchema.of(MulticastGoalState.class))
                    .topic(multicastTopic)
                    .enableBatching(false)
                    .create();
            producer.send(multicastGoalState);

            LOG.info("Send multicastGoalState to topic:{} success, " +
                    "groupTopics: {}, unicastGoalStates: {}",
                    multicastTopic, groupTopics, multicastGoalState);
        }
    }

    @Override
    public void updateGoalState(MulticastGoalState multicastGoalState) throws Exception {

    }

    @Override
    public void deleteGoalState(MulticastGoalState multicastGoalState) throws Exception {

    }
}
