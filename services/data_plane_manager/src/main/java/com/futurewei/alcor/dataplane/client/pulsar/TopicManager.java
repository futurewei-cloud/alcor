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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
public class TopicManager {
    @Autowired
    private PulsarConfiguration configuration;

    private Map<String, String> groupTopics;

    @PostConstruct
    public void init() {
        //create topics

        //build hostIp to group topic
        groupTopics = new HashMap<>();
    }

    public String getTopicByHostIp(String hostIp) {
        return groupTopics.get(hostIp);
    }

    public String getUnicastTopic() {
        return configuration.getUnicastTopic();
    }

    public String getMulticastTopic() {
        return configuration.getMulticastTopic();
    }
}
