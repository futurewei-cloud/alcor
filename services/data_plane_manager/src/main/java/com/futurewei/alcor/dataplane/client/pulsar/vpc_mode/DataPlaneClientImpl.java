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
import com.futurewei.alcor.dataplane.client.DataPlaneClient;
import com.futurewei.alcor.dataplane.entity.MulticastGoalState;
import com.futurewei.alcor.dataplane.entity.UnicastGoalState;
import com.futurewei.alcor.dataplane.entity.VpcMulticastGoalState;
import com.futurewei.alcor.dataplane.entity.VpcUnicastGoalState;
import com.futurewei.alcor.dataplane.exception.GroupTopicNotFound;
import com.futurewei.alcor.dataplane.exception.MulticastTopicNotFound;
import com.futurewei.alcor.web.entity.dataplane.MulticastGoalStateByte;
import com.futurewei.alcor.web.entity.dataplane.UnicastGoalStateByte;
import com.futurewei.alcor.web.entity.topic.VpcTopicInfo;
import org.apache.ignite.stream.StreamAdapter;
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
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@Component
@Service("pulsarDataPlaneClient")
@ConditionalOnProperty(prefix = "mq", name = "mode", havingValue = "vpc")
public class DataPlaneClientImpl implements DataPlaneClient {
    private static final Logger LOG = LoggerFactory.getLogger(DataPlaneClientImpl.class);

    @Autowired
    private PulsarClient pulsarClient;

    @Autowired
    private VpcTopicCache vpcTopicCache;

    private List<String> createVpcGoalState(VpcMulticastGoalState multicastGoalState) throws Exception {
        List<String> failedHosts = new ArrayList<>();

        for (int multiGsIndex = 0; multiGsIndex < multicastGoalState.getVpcIds().size(); multiGsIndex++) {
            VpcTopicInfo vpcTopicInfo = vpcTopicCache.getTopicInfoByVpcId(multicastGoalState.getVpcIds().get(multiGsIndex));
            if (vpcTopicInfo == null) {
                vpcTopicInfo = new VpcTopicInfo(TopicManager.generateTopicByVpcId(multicastGoalState.getVpcIds().get(multiGsIndex)));
                try {
                    vpcTopicCache.addTopicMapping(
                            multicastGoalState.getVpcIds().get(multiGsIndex),
                            vpcTopicInfo
                    );
                } catch (Exception e) {

                }
            }
            String multicastTopic = vpcTopicInfo.getTopicName();


            String multicastKey = vpcTopicInfo.getSubscribeMapping().get(multicastGoalState.getHostIps().get(multiGsIndex));

            if (multicastKey == null || StringUtils.isEmpty(multicastKey)) {
                multicastKey = TopicManager.generateKeyByNodeId(multicastGoalState.getHostIps().get(multiGsIndex));

                try {
                    vpcTopicCache.addSubscribedNodeForVpcId(
                            multicastGoalState.getVpcIds().get(multiGsIndex),
                            multicastGoalState.getHostIps().get(multiGsIndex),
                            multicastKey
                    );
                } catch (Exception e) {

                }
            }

            try {
                Producer<MulticastGoalStateByte> producer = pulsarClient
                        .newProducer(JSONSchema.of(MulticastGoalStateByte.class))
                        .topic(multicastTopic)
                        .batcherBuilder(BatcherBuilder.KEY_BASED)
                        .hashingScheme(HashingScheme.Murmur3_32Hash)
                        .create();
                producer.newMessage()
                        .key(multicastKey)
                        .value(multicastGoalState.getMulticastGoalStateByte())
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

        return failedHosts;
    }

    @Override
    public List<String> sendVpcGoalStates(List<VpcUnicastGoalState> unicastGoalStates) throws Exception {
        List<String> failedHosts = new ArrayList<>();

        for (VpcUnicastGoalState unicastGoalState : unicastGoalStates) {
            VpcTopicInfo vpcTopicInfo = vpcTopicCache.getTopicInfoByVpcId(unicastGoalState.getVpcId());
            if (vpcTopicInfo == null) {
                vpcTopicInfo = new VpcTopicInfo(TopicManager.generateTopicByVpcId(unicastGoalState.getVpcId()));
                try {
                    vpcTopicCache.addTopicMapping(
                            unicastGoalState.getVpcId(),
                            vpcTopicInfo
                    );
                } catch (Exception e) {

                }
            }
            String unicastTopic = vpcTopicInfo.getTopicName();

            String unicastKey = vpcTopicInfo.getSubscribeMapping().get(unicastGoalState.getHostIp());
            if (unicastKey == null || StringUtils.isEmpty(unicastKey)) {
                unicastKey = TopicManager.generateKeyByNodeId(unicastGoalState.getHostIp());

                try {
                    vpcTopicCache.addSubscribedNodeForVpcId(
                            unicastGoalState.getVpcId(),
                            unicastGoalState.getHostIp(),
                            unicastKey
                    );
                } catch (Exception e) {

                }
            }

            try {
                Producer<UnicastGoalStateByte> producer = pulsarClient
                        .newProducer(JSONSchema.of(UnicastGoalStateByte.class))
                        .topic(unicastTopic)
                        .batcherBuilder(BatcherBuilder.KEY_BASED)
                        .hashingScheme(HashingScheme.Murmur3_32Hash)
                        .create();
                producer.newMessage()
                        .key(unicastKey)
                        .value(unicastGoalState.getUnicastGoalStateByte())
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
    public List<String> sendVpcGoalStates(List<VpcUnicastGoalState> unicastGoalStates, VpcMulticastGoalState multicastGoalState) throws Exception {
        List<String> failedHosts = new ArrayList<>();

        failedHosts.addAll(sendVpcGoalStates(unicastGoalStates));
        failedHosts.addAll(createVpcGoalState(multicastGoalState));

        return failedHosts;
    }

    @Override
    public List<String> sendGoalStates(List<UnicastGoalState> unicastGoalStates) throws Exception {
        return null;
    }

    @Override
    public List<String> sendGoalStates(List<UnicastGoalState> unicastGoalStates, MulticastGoalState multicastGoalState) throws Exception {
        return null;
    }
}