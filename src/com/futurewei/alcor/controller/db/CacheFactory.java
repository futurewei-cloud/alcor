package com.futurewei.alcor.controller.db;

import com.futurewei.alcor.controller.db.ignite.IgniteCache;
import com.futurewei.alcor.controller.db.redis.RedisCache;
import org.apache.ignite.client.IgniteClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;


@ComponentScan
@Component
public class CacheFactory {
    @Autowired(required = false)
    private IgniteClient igniteClient;

    @Autowired
    LettuceConnectionFactory lettuceConnectionFactory;

    @Bean
    CacheFactory cacheFactoryInstance() {
        return new CacheFactory();
    }

    private ICache getIgniteCache(String cacheName) {
        return new IgniteCache<>(igniteClient, cacheName);
    }

    public <K, V>ICache getRedisCache(Class<V> v, String cacheName) {

        RedisTemplate<K, V> template = new RedisTemplate<K, V>();
        template.setConnectionFactory(lettuceConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());

        template.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(v));
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(v));
        template.afterPropertiesSet();

        return new RedisCache<>(template, cacheName);
    }

    public <K, V>ICache getCache(Class<V> v) {
        if (igniteClient != null) {
            return getIgniteCache(v.getName());
        }

        return getRedisCache(v, v.getName());
    }
}
