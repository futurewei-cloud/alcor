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

package com.futurewei.alcor.controller.db.redis;

import com.futurewei.alcor.controller.cache.message.ICachePublisher;
import com.futurewei.alcor.controller.cache.message.RedisListener;
import com.futurewei.alcor.controller.cache.message.RedisPublisher;
import com.futurewei.alcor.controller.model.PortState;
import com.futurewei.alcor.controller.model.SubnetState;
import com.futurewei.alcor.controller.model.VpcState;
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
@ComponentScan("com.futurewei.alcor.controller.db")
@EntityScan("com.futurewei.alcor.controller.db")
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
    public RedisTemplate<String, SubnetState> redisSubnetTemplate() {
        final RedisTemplate<String, SubnetState> template = new RedisTemplate<String, SubnetState>();
        template.setConnectionFactory(lettuceConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new Jackson2JsonRedisSerializer<SubnetState>(SubnetState.class));
        template.setValueSerializer(new Jackson2JsonRedisSerializer<SubnetState>(SubnetState.class));
        return template;
    }

    @Bean
    public RedisTemplate<String, PortState> redisPortTemplate() {
        final RedisTemplate<String, PortState> template = new RedisTemplate<String, PortState>();
        template.setConnectionFactory(lettuceConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new Jackson2JsonRedisSerializer<PortState>(PortState.class));
        template.setValueSerializer(new Jackson2JsonRedisSerializer<PortState>(PortState.class));
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

