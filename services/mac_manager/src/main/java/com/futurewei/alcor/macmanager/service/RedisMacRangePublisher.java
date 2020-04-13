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
package com.futurewei.alcor.macmanager.service;

import com.futurewei.alcor.common.repo.ICachePublisher;
import com.futurewei.alcor.macmanager.entity.MacRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
public class RedisMacRangePublisher implements ICachePublisher {
    @Autowired
    private RedisTemplate<String, MacRange> redisTemplate2;

    @Autowired
    private ChannelTopic topic2;

    public RedisMacRangePublisher() {
    }

    public RedisMacRangePublisher(final RedisTemplate<String, MacRange> redisTemplate, final ChannelTopic topic2) {
        this.redisTemplate2 = redisTemplate;
        this.topic2 = topic2;
    }

    public void publish(final String message) {
        redisTemplate2.convertAndSend(topic2.getTopic(), message);
    }
}
