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

import com.futurewei.alcor.common.db.query.CachePredicate;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.Transaction;
import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Map;
import java.util.logging.Level;

public class RedisCache<K, V> implements ICache<K, V> {
    private static final Logger logger = LoggerFactory.getLogger();

    private RedisTemplate<K, V> redisTemplate;
    private HashOperations hashOperations;
    private RedisTransaction transaction;
    private String name;

    public RedisCache(RedisTemplate<K, V> redisTemplate, String name) {
        this.redisTemplate = redisTemplate;
        hashOperations = redisTemplate.opsForHash();
        this.name = name;

        transaction = new RedisTransaction(redisTemplate);
    }

    @Override
    public V get(K key) throws CacheException {
        try {
            return (V) hashOperations.get(name, key);
        } catch (Exception e) {
            logger.log(Level.WARNING, "RedisCache get operation error:" + e.getMessage());
            throw new CacheException(e.getMessage());
        }
    }

    @Override
    public void put(K key, V value) throws CacheException {
        try {
            hashOperations.put(name, key, value);
        } catch (Exception e) {
            logger.log(Level.WARNING, "RedisCache put operation error:" + e.getMessage());
            throw new CacheException(e.getMessage());
        }
    }

    @Override
    public boolean containsKey(K key) throws CacheException {
        try {
            return hashOperations.hasKey(name, key);
        } catch (Exception e) {
            logger.log(Level.WARNING, "RedisCache containsKey operation error:" + e.getMessage());
            throw new CacheException(e.getMessage());
        }
    }

    @Override
    public Map<K, V> getAll() throws CacheException {
        try {
            return hashOperations.entries(name);
        } catch (Exception e) {
            logger.log(Level.WARNING, "RedisCache getAll operation error:" + e.getMessage());
            throw new CacheException(e.getMessage());
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> items) throws CacheException {
        try {
            hashOperations.putAll(name, items);
        } catch (Exception e) {
            logger.log(Level.WARNING, "RedisCache putAll operation error:" + e.getMessage());
            throw new CacheException(e.getMessage());
        }
    }

    @Override
    public boolean remove(K key) throws CacheException {
        try {
            return hashOperations.delete(name, key) == 1;
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
    public <E1, E2> V get(CachePredicate<E1, E2> cachePredicate) throws CacheException {
        return null;
    }

    @Override
    public <E1, E2> Map<K, V> getAll(Map<String, Object[]> filterParams) throws CacheException {
        return null;
    }

    @Override
    public <E1, E2> Map<K, V> getAll(CachePredicate<E1, E2> cachePredicate) throws CacheException {
        return null;
    }

    @Override
    public Transaction getTransaction() {
        return transaction;
    }
}
