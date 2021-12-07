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

import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
//@ConditionalOnProperty(prefix = "mq", name = "type", havingValue = "pulsar")
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
