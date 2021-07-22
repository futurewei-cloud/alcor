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

package com.futurewei.alcor.common.db.redis;

import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.ICacheFactory;
import com.futurewei.alcor.common.db.IDistributedLock;
import com.futurewei.alcor.common.db.Transaction;
import org.apache.ignite.configuration.CacheConfiguration;
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
    public <K, V> ICache<K, V> getCache(Class<V> v, CacheConfiguration cacheConfig) {
        return null;
    }

    @Override
    public <K, V> ICache<K, V> getExpireCache(Class<V> v, long timeout, TimeUnit timeUnit) {
        RedisTemplate<K, V> template = getRedisTemplate(v);
        return new RedisExpireCache<>(template, timeout, timeUnit);
    }

    private <K, V> RedisTemplate<K, V> getRedisTemplate(Class<?> v) {

        RedisTemplate<K, V> template = new RedisTemplate<>();
        template.setConnectionFactory(lettuceConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());

        template.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(v));
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(v));
        template.afterPropertiesSet();

        return template;
    }

    @Override
    public <V> IDistributedLock getDistributedLock(Class<V> t) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(lettuceConnectionFactory);

        return new RedisDistributedLock(template, LOCK_PREFIX + t.getName(), tryLockInterval, expireTime);
    }

    @Override
    public Transaction getTransaction() {
        RedisTemplate<?, ?> template = new RedisTemplate<>();
        template.setConnectionFactory(lettuceConnectionFactory);
        return new RedisTransaction(template);
    }
}
