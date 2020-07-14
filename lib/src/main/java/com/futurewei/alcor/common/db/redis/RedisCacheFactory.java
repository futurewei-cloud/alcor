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

package com.futurewei.alcor.common.db.redis;

import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.ICacheFactory;
import com.futurewei.alcor.common.db.IDistributedLock;
import com.futurewei.alcor.common.db.Transaction;
import com.futurewei.alcor.common.entity.TokenEntity;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.concurrent.TimeUnit;

public class RedisCacheFactory implements ICacheFactory {

    private final LettuceConnectionFactory lettuceConnectionFactory;
    private final int tryLockInterval;
    private final int expireTime;

    public RedisCacheFactory(LettuceConnectionFactory lettuceConnectionFactory, int interval, int expire) {
        this.lettuceConnectionFactory = lettuceConnectionFactory;
        this.tryLockInterval = interval;
        this.expireTime = expire;
    }

    @Override
    public <K, V> ICache<K, V> getCache(Class<V> v) {
        RedisTemplate<String, Object> template = getRedisTemplate(v);
        return new RedisCache<>(template, v.getName());
    }

    @Override
    public <K, V> ICache<K, V> getCache(Class<V> v, String cacheName) {
        RedisTemplate<String, Object> template = getRedisTemplate(v);
        return new RedisCache<>(template, cacheName);
    }

    @Override
    public <K, V> ICache<K, V> getExpireCache(Class<V> v, long timeout, TimeUnit timeUnit) {
        RedisTemplate<K, V> template = getRedisTemplate(v);
        return new RedisExpireCache<>(template, timeout, timeUnit);
    }

    private <K, V> RedisTemplate<K, V> getRedisTemplate(Class<?> v){

        RedisTemplate<K, V> template = new RedisTemplate<>();
        template.setConnectionFactory(lettuceConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());

        template.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(v));
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(v));
        template.afterPropertiesSet();

        return template;
    }

    @Override
    public <T> IDistributedLock getDistributedLock(Class<T> t) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(lettuceConnectionFactory);

        return new RedisDistributedLock(template, LOCK_PREFIX + t.getName(), tryLockInterval, expireTime);
    }
}
