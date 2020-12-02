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

import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "mq", name = "type", havingValue = "pulsar")
public class PulsarConfiguration {
    @Value("${pulsar.url}")
    private String pulsarUrl;

    @Value("${pulsar.unicast.topic:#{null}}")
    private String unicastTopic;

    @Value("${host.ip.to.group.topic.map}")
    private String hostIpToGroupTopicMap;

    @Value("${group.topic.to.multicast.topic.map}")
    private String groupTopicToMulticastTopicMap;

    @Bean
    public PulsarClient pulsarClientInstance() throws PulsarClientException {
        return PulsarClient.builder().serviceUrl(pulsarUrl).build();
    }

    public String getPulsarUrl() {
        return pulsarUrl;
    }

    public String getUnicastTopic() {
        return unicastTopic;
    }

    public String getHostIpToGroupTopicMap() {
        return hostIpToGroupTopicMap;
    }

    public String getGroupTopicToMulticastTopicMap() {
        return groupTopicToMulticastTopicMap;
    }
}
