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

package com.futurewei.alcor.common.db;

import com.futurewei.alcor.common.db.ignite.IgniteCache;
import com.futurewei.alcor.common.db.redis.RedisCache;
import com.futurewei.alcor.common.db.redis.RedisExpireCache;
import org.apache.ignite.client.IgniteClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;
import java.util.concurrent.TimeUnit;


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

    private <K, V> ICache<K, V> getIgniteCache(String cacheName) {
        return new IgniteCache<>(igniteClient, cacheName);
    }

    public <K, V> RedisTemplate<K, V> getRedisTemplate(Class<V> v){
        RedisTemplate<K, V> template = new RedisTemplate<>();
        template.setConnectionFactory(lettuceConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());

        template.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(v));
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(v));
        template.afterPropertiesSet();

        return template;
    }

    public <K, V> ICache<K, V> getRedisCache(Class<V> v, String cacheName) {
        RedisTemplate<K, V> template = getRedisTemplate(v);
        return new RedisCache<>(template, cacheName);
    }

    public <K, V> ICache<K, V> getRedisExpireCache(Class<V> v, long timeout, TimeUnit timeUnit){
        RedisTemplate<K, V> template = getRedisTemplate(v);
        return new RedisExpireCache<>(template, timeout, timeUnit);
    }

    public <K, V> ICache<K, V> getCache(Class<V> v) {
        if (igniteClient != null) {
            return getIgniteCache(v.getName());
        }

        return getRedisCache(v, v.getName());
    }

    public <K, V> ICache<K, V> getExpireCache(Class<V> v, long timeout, TimeUnit timeUnit){
        if (igniteClient != null) {
            ExpiryPolicy ep = CreatedExpiryPolicy.factoryOf(new Duration(timeUnit, timeout)).create();
            return new IgniteCache<>(igniteClient, v.getName(), ep);
        }

        return getRedisExpireCache(v, timeout, timeUnit);
    }
}
