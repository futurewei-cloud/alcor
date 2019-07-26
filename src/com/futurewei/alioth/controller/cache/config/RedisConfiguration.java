package com.futurewei.alioth.controller.cache.config;

import com.futurewei.alioth.controller.cache.message.RedisPublisher;
import com.futurewei.alioth.controller.cache.message.RedisListener;
import com.futurewei.alioth.controller.cache.message.ICachePublisher;

import com.futurewei.alioth.controller.model.VpcState;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
@Configuration
@ComponentScan("com.futurewei.alioth.controller.cache")
@EntityScan("com.futurewei.alioth.controller.cache")
public class RedisConfiguration {

//    @Bean
//    JedisConnectionFactory jedisConnectionFactory() {
//        return new JedisConnectionFactory();
//    }

    @Bean
    LettuceConnectionFactory lettuceConnectionFactory() {
        return new LettuceConnectionFactory();
    }

    @Bean
    public RedisTemplate<String, VpcState> redisTemplate() {
        final RedisTemplate<String, VpcState> template = new RedisTemplate<String, VpcState>();
        template.setConnectionFactory(lettuceConnectionFactory());
        template.setKeySerializer( new StringRedisSerializer() );
        template.setHashValueSerializer( new Jackson2JsonRedisSerializer < VpcState >( VpcState.class ) );
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
    ICachePublisher redisPublisherInstance() {
        return new RedisPublisher(redisTemplate(), topic());
    }

    @Bean
    ChannelTopic topic() {
        return new ChannelTopic("pubsub:queue");
    }
}

