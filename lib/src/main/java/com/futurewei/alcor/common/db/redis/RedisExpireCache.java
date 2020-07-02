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

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.Transaction;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import org.apache.ignite.lang.IgniteBiPredicate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class RedisExpireCache<K, V> implements ICache<K, V> {
    private static final Logger logger = LoggerFactory.getLogger();

    private RedisTemplate<K, V> redisTemplate;
    private ValueOperations<K, V> valueOperations;
    private long timeout;
    private TimeUnit timeUnit;
    private RedisTransaction transaction;

    /**
     * return a new redis cache client with auto set expire time
     * eg:
     *     RedisTemplate<K, V> template = new RedisTemplate<>();
     *     ICache cache = new RedisExpireCache(redisTemplate, 2, TimeUnit.HOURS)
     * @param redisTemplate a RedisTemplate instance
     * @param timeout the key expiration timeout.
     * @param timeUnit must not be {@literal null}.
     */
    public RedisExpireCache(RedisTemplate<K, V> redisTemplate, long timeout, TimeUnit timeUnit) {
        this.redisTemplate = redisTemplate;
        this.valueOperations = redisTemplate.opsForValue();
        this.timeout = timeout;
        this.timeUnit = timeUnit;

        this.transaction = new RedisTransaction(redisTemplate);
    }


    /**
     * put a cache to redis, it will set auto expire time with this instance expire timeout
     * @param key
     * @param value
     * @throws CacheException
     */
    @Override
    public void put(K key, V value) throws CacheException {
        try {
            valueOperations.set(key, value, timeout, timeUnit);
        } catch (Exception e) {
            logger.log(Level.WARNING, "RedisCache put operation error:" + e.getMessage());
            throw new CacheException(e.getMessage());
        }
    }

    @Override
    public V get(K key) throws CacheException {
        try {
            return valueOperations.get(key);
        } catch (Exception e) {
            logger.log(Level.WARNING, "RedisCache get operation error:" + e.getMessage());
            throw new CacheException(e.getMessage());
        }
    }

    @Override
    public Map<K, V> getAll() throws CacheException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> items) throws CacheException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsKey(K key) throws CacheException {
        try {
            V v = valueOperations.get(key);
            return v != null;
        } catch (Exception e) {
            logger.log(Level.WARNING, "RedisCache containsKey operation error:" + e.getMessage());
            throw new CacheException(e.getMessage());
        }
    }

    @Override
    public boolean remove(K key) throws CacheException {
        try {
            return redisTemplate.delete(key);
        } catch (Exception e) {
            logger.log(Level.WARNING, "RedisCache remove operation error:" + e.getMessage());
            throw new CacheException(e.getMessage());
        }
    }

    @Override
    public V get(Map<String, Object[]> filterParams) throws CacheException {
        return null;
    }

    @Override
    public <E1, E2> Map<K, V> getAll(Map<String, Object[]> filterParams) throws CacheException {
        return null;
    }

    @Override
    public Transaction getTransaction() {
        return transaction;
    }
}
