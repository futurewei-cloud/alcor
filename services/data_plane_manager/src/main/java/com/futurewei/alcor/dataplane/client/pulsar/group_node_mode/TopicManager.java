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
package com.futurewei.alcor.dataplane.client.pulsar.group_node_mode;

import com.futurewei.alcor.dataplane.exception.TopicParseFailureException;
import com.futurewei.alcor.web.entity.topic.NodeTopicInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.*;

@Configuration
//@ConditionalOnProperty(prefix = "mq", name = "type", havingValue = "pulsar")
@ConditionalOnProperty(prefix = "mq", name = "mode", havingValue = "group-node")
public class TopicManager {
    private static final Logger LOG = LoggerFactory.getLogger(TopicManager.class);

    @Autowired
    private PulsarConfiguration configuration;

    private Map<String, String> hostIpToGroupTopic;

    private Map<String, String> groupTopicToMulticastTopic;

    private Set<String> groupTopics;

    private Set<String> multicastTopics;

    @PostConstruct
    public void init() throws Exception {
        try {
            this.hostIpToGroupTopic = parseTopicConfig(configuration.getHostIpToGroupTopicMap());
            this.groupTopicToMulticastTopic = parseTopicConfig(configuration.getGroupTopicToMulticastTopicMap());
        }catch (Exception e) {
            throw new TopicParseFailureException("Parse topic config error: " + e);
        }

        LOG.info("Host ip to group topic map: {}", this.hostIpToGroupTopic);
        LOG.info("Group topic to multicast topic map: {}", this.groupTopicToMulticastTopic);

        this.groupTopics = new HashSet<>(this.hostIpToGroupTopic.values());
        this.multicastTopics = new HashSet<>(this.groupTopicToMulticastTopic.values());
    }

    private Map<String, String> parseTopicConfig(String config) {
        Map<String, String> result = new HashMap<>();
        String[] entries = config.split("\\s+");
        for (String entry: entries) {
            entry = entry.trim();
            String keyStr = entry.split(":")[1];
            String value = entry.split(":")[0];
            String[] keys = keyStr.split(",");
            for (String key: keys) {
                result.put(key, value);
            }
        }

        return result;
    }

    public String getGroupTopicByHostIp(String hostIp) {
        return hostIpToGroupTopic.get(hostIp);
    }

    public String getMulticastTopicByGroupTopic(String groupTopic) {
        return groupTopicToMulticastTopic.get(groupTopic);
    }

    public String getUnicastTopic() {
        return configuration.getUnicastTopic();
    }

    public String getGroupTopicByNodeId(String nodeId) {
        return "groupTopic-" + nodeId;
    }

    public NodeTopicInfo createNodeTopicInfo(String nodeId) {
        return new NodeTopicInfo(nodeId, getUnicastTopic(), getUnicastTopic(), getGroupTopicByNodeId(nodeId));
    }
}
