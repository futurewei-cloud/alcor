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

package com.futurewei.alcor.vpcmanager.config;

import com.futurewei.common.repo.ICachePublisher;
import com.futurewei.common.service.RedisListener;
import com.futurewei.alcor.vpcmanager.service.RedisPublisher;

import com.futurewei.alcor.vpcmanager.entity.VpcState;
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
@ComponentScan({"com.futurewei.alcor.vpcmanager.service", "com.futurewei.common.service"})
@EntityScan({"com.futurewei.alcor.vpcmanager.entity}", "com.futurewei.common.entity"})
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
    public RedisTemplate<String, VpcState> redisVpcTemplate() {
        final RedisTemplate<String, VpcState> template = new RedisTemplate<String, VpcState>();
        template.setConnectionFactory(lettuceConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new Jackson2JsonRedisSerializer<VpcState>(VpcState.class));
        template.setValueSerializer(new Jackson2JsonRedisSerializer<VpcState>(VpcState.class));
        return template;
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
    ICachePublisher redisVpcPublisherInstance() {
        return new RedisPublisher(redisVpcTemplate(), topic());
    }

//    @Bean
//    ICachePublisher redisSubnetPublisherInstance() {
//        return new RedisPublisher(redisSubnetTemplate(), topic());
//    }

    @Bean
    ChannelTopic topic() {
        return new ChannelTopic("pubsub:queue");
    }
}

