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


package com.futurewei.alcor.macmanager.config;


import com.futurewei.alcor.common.repo.ICachePublisher;
import com.futurewei.alcor.common.service.RedisListener;
import com.futurewei.alcor.macmanager.entity.MacRange;
import com.futurewei.alcor.macmanager.entity.MacState;
import com.futurewei.alcor.macmanager.service.implement.redis.RedisMacRangePublisherServiceImpl;
import com.futurewei.alcor.macmanager.service.implement.redis.RedisPublisherServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@ComponentScan({"com.futurewei.alcor.macmanager.service", "com.futurewei.alcor.common.service"})
@EntityScan({"com.futurewei.alcor.macmanager.entity}", "com.futurewei.common.entity"})
public class RedisConfiguration {

    @Value("${spring.redis.host}")
    private String redisHostName;

    @Value("${spring.redis.port}")
    private int redisHostPort;

    @Bean
    LettuceConnectionFactory lettuceConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(redisHostName);
        configuration.setPort(redisHostPort);
        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    public RedisTemplate<String, MacState> redisMacTemplate() {
        final RedisTemplate<String, MacState> template = new RedisTemplate<String, MacState>();
        template.setConnectionFactory(lettuceConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new Jackson2JsonRedisSerializer<MacState>(MacState.class));
        template.setValueSerializer(new Jackson2JsonRedisSerializer<MacState>(MacState.class));
        template.setEnableTransactionSupport(true);
        return template;
    }

    @Bean
    public RedisTemplate<String, MacRange> redisMacRangeTemplate() {
        final RedisTemplate<String, MacRange> templateMacRange = new RedisTemplate<String, MacRange>();
        templateMacRange.setConnectionFactory(lettuceConnectionFactory());
        templateMacRange.setKeySerializer(new StringRedisSerializer());
        templateMacRange.setHashValueSerializer(new Jackson2JsonRedisSerializer<MacRange>(MacRange.class));
        templateMacRange.setValueSerializer(new Jackson2JsonRedisSerializer<MacRange>(MacRange.class));
        templateMacRange.setEnableTransactionSupport(true);
        return templateMacRange;
    }

    @Bean
    MessageListenerAdapter redisListenerInstance() {
        return new MessageListenerAdapter(new RedisListener());
    }

    @Bean
    RedisMessageListenerContainer redisContainer() {
        final RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(lettuceConnectionFactory());
        container.addMessageListener(redisListenerInstance(), topic());
        return container;
    }

    @Bean
    ICachePublisher redisMacPublisherInstance() {
        return new RedisPublisherServiceImpl(redisMacTemplate(), topic());
    }

    @Bean
    ICachePublisher redisMacRangePublisherInstance() {
        return new RedisMacRangePublisherServiceImpl(redisMacRangeTemplate(), topic());
    }

    @Bean
    ChannelTopic topic() {
        return new ChannelTopic("pubsub:queue");
    }
}

