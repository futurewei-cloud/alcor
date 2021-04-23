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
package com.futurewei.alcor.dataplane.client.pulsar;

import com.futurewei.alcor.dataplane.exception.TopicParseFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.*;

@Configuration
@ConditionalOnProperty(prefix = "mq", name = "type", havingValue = "pulsar")
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
}
