/*
 *
 * MIT License
 * Copyright(c) 2020 Futurewei Cloud
 *
 *     Permission is hereby granted,
 *     free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
 *     including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
 *     to whom the Software is furnished to do so, subject to the following conditions:
 *
 *     The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 *     THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *     FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *     WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * /
 */

package com.futurewei.alcor.dataplane.client.pulsar.vpc_mode;

import com.futurewei.alcor.dataplane.client.DataPlaneClient;
import com.futurewei.alcor.dataplane.entity.MulticastGoalStateV2;
import com.futurewei.alcor.dataplane.entity.UnicastGoalStateV2;
import com.futurewei.alcor.schema.Subscribeinfoprovisioner.NodeSubscribeInfo;
import com.futurewei.alcor.web.entity.dataplane.MulticastGoalStateByte;
import com.futurewei.alcor.web.entity.topic.VpcTopicInfo;
import org.apache.pulsar.client.api.BatcherBuilder;
import org.apache.pulsar.client.api.HashingScheme;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.impl.schema.JSONSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service("pulsarDataPlaneClient")
@ConditionalOnProperty(prefix = "protobuf.goal-state-message", name = "version", havingValue = "102")
public class DataPlaneClientImplV2 implements DataPlaneClient<UnicastGoalStateV2, MulticastGoalStateV2> {
    private static final Logger LOG = LoggerFactory.getLogger(DataPlaneClientImpl.class);

    @Autowired
    private PulsarClient pulsarClient;

    @Autowired
    private TopicManager topicManager;

    private List<String> createGoalState(MulticastGoalStateV2 multicastGoalState) throws Exception {
        List<String> failedHosts = new ArrayList<>();
        if (multicastGoalState != null &&
                multicastGoalState.getHostIps().size() != 0 &&
                multicastGoalState.getGoalState() != null) {
            Map<String, Set<String>> hostVpcMap = multicastGoalState.getHostVpcMap();
            for (Map.Entry<String, Set<String>> hostVpcPair : hostVpcMap.entrySet()) {
                String hostIp = hostVpcPair.getKey();
                for (String vpcId : hostVpcPair.getValue()) {
                    NodeSubscribeInfo nodeSubscribeInfo = topicManager.getNodeSubscribeInfoByVpcId(vpcId, hostIp);
                    String multicastTopic = nodeSubscribeInfo.getTopic();
                    String multicastKey = nodeSubscribeInfo.getKey();

                    try {
                        Producer<byte[]> producer = pulsarClient
                                .newProducer()
                                .topic(multicastTopic)
                                .batcherBuilder(BatcherBuilder.KEY_BASED)
                                .hashingScheme(HashingScheme.Murmur3_32Hash)
                                .create();
                        producer.newMessage()
                                .key(multicastKey)
                                .value(multicastGoalState.getGoalState().toByteArray())
                                .send();
                    } catch (Exception e) {
                        LOG.error("Send multicastGoalState to topic:{} failed: ", multicastTopic, e);
                        failedHosts.addAll(multicastGoalState.getHostIps());
                        continue;
                    }

                    LOG.info("Send multicastGoalState to topic:{} success, " +
                                    " unicastGoalStates: {}",
                            multicastTopic, multicastGoalState);
                }
            }
        }

        return failedHosts;
    }

    @Override
    public List<String> sendGoalStates(List<UnicastGoalStateV2> unicastGoalStates) throws Exception {
        List<String> failedHosts = new ArrayList<>();

        for (UnicastGoalStateV2 unicastGoalState : unicastGoalStates) {
            if (unicastGoalState.getVpcId() == null) {
                throw new Exception("The VpcId of unicast goalState is null.");
            }

            NodeSubscribeInfo nodeSubscribeInfo = topicManager.getNodeSubscribeInfoByVpcId(unicastGoalState.getVpcId(), unicastGoalState.getHostIp());
            String unicastTopic = nodeSubscribeInfo.getTopic();
            String unicastKey = nodeSubscribeInfo.getKey();

            try {
                Producer<byte[]> producer = pulsarClient
                        .newProducer()
                        .topic(unicastTopic)
                        .batcherBuilder(BatcherBuilder.KEY_BASED)
                        .hashingScheme(HashingScheme.Murmur3_32Hash)
                        .create();
                producer.newMessage()
                        .key(unicastKey)
                        .value(unicastGoalState.getGoalState().toByteArray())
                        .send();
            } catch (Exception e) {
                LOG.error("Send unicastGoalStates to topic:{} failed: ", unicastTopic, e);
                failedHosts.add(unicastGoalState.getHostIp());
                continue;
            }

            LOG.info("Send unicastGoalStates to topic:{} success, " +
                    "unicastGoalStates: {}", unicastTopic, unicastGoalState);
        }

        return failedHosts;
    }

    @Override
    public List<String> sendGoalStates(List<UnicastGoalStateV2> unicastGoalStates, MulticastGoalStateV2 multicastGoalState) throws Exception {
        List<String> failedHosts = new ArrayList<>();

        failedHosts.addAll(sendGoalStates(unicastGoalStates));
        failedHosts.addAll(createGoalState(multicastGoalState));

        return failedHosts;
    }
}
