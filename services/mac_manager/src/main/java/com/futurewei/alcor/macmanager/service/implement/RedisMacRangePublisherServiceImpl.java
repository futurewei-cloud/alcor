/*Copyright 2019 The Alcor Authors.

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
package com.futurewei.alcor.macmanager.service.implement;

import com.futurewei.alcor.common.repo.ICachePublisher;
import com.futurewei.alcor.macmanager.entity.MacRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
public class RedisMacRangePublisherServiceImpl implements ICachePublisher {
    @Autowired
    private RedisTemplate<String, MacRange> redisTemplate;

    @Autowired
    private ChannelTopic topic;

    public RedisMacRangePublisherServiceImpl() {
    }

    public RedisMacRangePublisherServiceImpl(final RedisTemplate<String, MacRange> redisTemplate, final ChannelTopic topic2) {
        this.redisTemplate = redisTemplate;
        this.topic = topic2;
    }

    public void publish(final String message) {
        redisTemplate.convertAndSend(topic.getTopic(), message);
    }
}
