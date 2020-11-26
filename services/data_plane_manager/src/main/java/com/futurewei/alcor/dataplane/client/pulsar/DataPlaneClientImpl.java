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
import com.futurewei.alcor.schema.Goalstate;
import com.futurewei.alcor.schema.Goalstateprovisioner;
import com.futurewei.alcor.web.entity.dataplane.MulticastGoalStateByte;
import com.futurewei.alcor.web.entity.dataplane.UnicastGoalStateByte;

import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.impl.schema.JSONSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@Component
@Service("pulsar")
public class DataPlaneClientImpl implements DataPlaneClient {
    private static final Logger LOG = LoggerFactory.getLogger(DataPlaneClientImpl.class);

    @Autowired
    private PulsarClient pulsarClient;

    @Autowired
    private TopicManager topicManager;

    @Override
    public List<String> createGoalStates(List<UnicastGoalState> unicastGoalStates) throws Exception {
        return null;
    }

    private List<String> getGroupTopics(List<String> hostIps) throws Exception {
        List<String> groupTopics = new ArrayList<>();

        for (String hostIp: hostIps) {
            String groupTopic = topicManager.getGroupTopicByHostIp(hostIp);
            if (StringUtils.isEmpty(groupTopic)) {
                LOG.error("Can not find group topic by host ip:{}", hostIp);
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
                LOG.error("Can not find multicast topic by group topic:{}", groupTopic);
                throw new MulticastTopicNotFound();
            }

            if (!multicastTopics.containsKey(multicastTopic)) {
                multicastTopics.put(multicastTopic, new ArrayList<>());
            }

            multicastTopics.get(multicastTopic).add(groupTopic);
        }

        return multicastTopics;
    }

    private void createGoalState(MulticastGoalState multicastGoalState) throws Exception {
        Map<String, List<String>> multicastTopics = getMulticastTopics(multicastGoalState.getHostIps());

        for (Map.Entry<String, List<String>> entry: multicastTopics.entrySet()) {
            String multicastTopic = entry.getKey();
            List<String> groupTopics = entry.getValue();

            multicastGoalState.setNextTopics(groupTopics);

            Producer<MulticastGoalStateByte> producer = pulsarClient
                    .newProducer(JSONSchema.of(MulticastGoalStateByte.class))
                    .topic(multicastTopic)
                    .enableBatching(false)
                    .create();

            producer.send(multicastGoalState.getMulticastGoalStateByte());

            LOG.info("Send multicastGoalState to topic:{} success, " +
                            "groupTopics: {}, unicastGoalStates: {}",
                    multicastTopic, groupTopics, multicastGoalState);
        }
    }

    @Override
    public List<Map<String, List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>>> createGoalStates(List<UnicastGoalState> unicastGoalStates) throws Exception {
        for (UnicastGoalState unicastGoalState: unicastGoalStates) {
            String nextTopic = topicManager.getGroupTopicByHostIp(unicastGoalState.getHostIp());
            if (StringUtils.isEmpty(nextTopic)) {
                LOG.error("Can not find next topic by host ip:{}", unicastGoalState.getHostIp());
                throw new GroupTopicNotFound();
            }

            String topic = nextTopic;
            String unicastTopic = topicManager.getUnicastTopic();
            if (!StringUtils.isEmpty(unicastTopic)) {
                unicastGoalState.setNextTopic(nextTopic);
                topic = unicastTopic;
            }

            Producer<UnicastGoalStateByte> producer = pulsarClient
                    .newProducer(JSONSchema.of(UnicastGoalStateByte.class))
                    .topic(topic)
                    .enableBatching(false)
                    .create();
            producer.send(unicastGoalState.getUnicastGoalStateByte());

            LOG.info("Send unicastGoalStates to topic:{} success, " +
                    "unicastGoalStates: {}", nextTopic, unicastGoalState);
        }

        List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus> tempList = new ArrayList<>();
        Map<String, List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>> tempMap = new HashMap<>();
        List<Map<String, List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>>> tempResult = new ArrayList<>();
        tempMap.put("temp", tempList);
        tempResult.add(tempMap);
        return tempResult;
    }

    @Override
    public List<Map<String, List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>>> updateGoalStates(List<UnicastGoalState> unicastGoalStates) throws Exception {
        return null;
    }

    @Override
    public List<Map<String, List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>>> deleteGoalStates(List<UnicastGoalState> unicastGoalStates) throws Exception {
        return null;
    }

    @Override
    public List<Map<String, List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>>> createGoalStates(List<UnicastGoalState> unicastGoalStates, MulticastGoalState multicastGoalState) throws Exception {
        createGoalStates(unicastGoalStates);
        createGoalState(multicastGoalState);


        List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus> tempList = new ArrayList<>();
        Map<String, List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>> tempMap = new HashMap<>();
        List<Map<String, List<Goalstateprovisioner.GoalStateOperationReply.GoalStateOperationStatus>>> tempResult = new ArrayList<>();
        tempMap.put("temp", tempList);
        tempResult.add(tempMap);
        return tempResult;
    }
}
