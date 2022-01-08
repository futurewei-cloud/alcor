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

package com.futurewei.alcor.dataplane.client.pulsar.group_node_mode;
import com.futurewei.alcor.dataplane.cache.NodeTopicCache;
import com.futurewei.alcor.dataplane.client.DataPlaneClient;
import com.futurewei.alcor.dataplane.entity.*;
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

public class DataPlaneClientImplV2 implements DataPlaneClient<UnicastGoalStateV2, MulticastGoalStateV2> {
    private static final Logger LOG = LoggerFactory.getLogger(DataPlaneClientImplV2.class);


    @Autowired
    private PulsarClient pulsarClient;

    @Autowired
    NodeTopicCache nodeTopicCache;

    private Map<String, List<String>> getMulticastTopics(List<String> hostIps) throws Exception {
        Map<String, List<String>> multicastTopics = new HashMap<>();
        for (String hostIp : hostIps) {
            String groupTopic = nodeTopicCache.getNodeTopicInfoByNodeIp(hostIp).getGroupTopic();
            if (StringUtils.isEmpty(groupTopic)) {
                LOG.error("Can not find group topic by host ip:{}", hostIp);
                throw new GroupTopicNotFound();
            }

            String multicastTopic = nodeTopicCache.getNodeTopicInfoByNodeIp(hostIp).getMulticastTopic();
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

    private List<String> createGoalState(MulticastGoalStateV2 multicastGoalState) throws Exception {
        List<String> failedHosts = new ArrayList<>();

        Map<String, List<String>> multicastTopics = getMulticastTopics(new ArrayList<>(multicastGoalState.getHostIps()));

        for (Map.Entry<String, List<String>> entry: multicastTopics.entrySet()) {
            String multicastTopic = entry.getKey();
            List<String> groupTopics = entry.getValue();

            FunctionMulticastGoalStateV2 functionMulticastGoalStateV2 = new FunctionMulticastGoalStateV2(
                    multicastGoalState.getGoalState(),
                    groupTopics
            );

            try {
                Producer<MulticastGoalStateByte> producer = pulsarClient
                        .newProducer(JSONSchema.of(MulticastGoalStateByte.class))
                        .topic(multicastTopic)
                        .enableBatching(false)
                        .create();

                producer.send(functionMulticastGoalStateV2.getMulticastGoalStateByte());
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
    public List<String> sendGoalStates(List<UnicastGoalStateV2> unicastGoalStates) throws Exception {
        List<String> failedHosts = new ArrayList<>();

        for (UnicastGoalStateV2 unicastGoalState: unicastGoalStates) {
            String nextTopic = nodeTopicCache.getNodeTopicInfoByNodeIp(unicastGoalState.getHostIp()).getGroupTopic();
            if (StringUtils.isEmpty(nextTopic)) {
                LOG.error("Can not find next topic by host ip:{}", unicastGoalState.getHostIp());
                throw new GroupTopicNotFound();
            }

            String topic = nextTopic;
            String unicastTopic = nodeTopicCache.getNodeTopicInfoByNodeIp(unicastGoalState.getHostIp()).getUnicastTopic();

            FunctionUnicastGoalStateV2 functionUnicastGoalStateV2 = new FunctionUnicastGoalStateV2(unicastGoalState.getGoalState());

            if (!StringUtils.isEmpty(unicastTopic)) {
                functionUnicastGoalStateV2.setTopic(nextTopic);
                topic = unicastTopic;
            }

            try {
                Producer<UnicastGoalStateByte> producer = pulsarClient
                        .newProducer(JSONSchema.of(UnicastGoalStateByte.class))
                        .topic(topic)
                        .enableBatching(false)
                        .create();
                producer.send(functionUnicastGoalStateV2.getUnicastGoalStateByte());
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
    public List<String> sendGoalStates(List<UnicastGoalStateV2> unicastGoalStates, MulticastGoalStateV2 multicastGoalState) throws Exception {
        List<String> failedHosts = new ArrayList<>();

        failedHosts.addAll(sendGoalStates(unicastGoalStates));
        failedHosts.addAll(createGoalState(multicastGoalState));

        return failedHosts;
    }
}
