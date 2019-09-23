package com.futurewei.alcor.controller.cache.message;

import com.futurewei.alcor.controller.model.VpcState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
public class RedisPublisher implements ICachePublisher {

    @Autowired
    private RedisTemplate<String, VpcState> redisTemplate;

    @Autowired
    private ChannelTopic topic;

    public RedisPublisher() {
    }

    public RedisPublisher(final RedisTemplate<String, VpcState> redisTemplate, final ChannelTopic topic) {
        this.redisTemplate = redisTemplate;
        this.topic = topic;
    }

    public void publish(final String message) {
        redisTemplate.convertAndSend(topic.getTopic(), message);
    }
}
