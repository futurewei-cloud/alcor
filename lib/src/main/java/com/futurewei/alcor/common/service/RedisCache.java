/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/


package com.futurewei.alcor.common.service;

import com.futurewei.alcor.common.exception.CacheException;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.common.repo.ICache;
import com.futurewei.alcor.common.repo.Transaction;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

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
    public Transaction getTransaction() {
        return transaction;
    }
}
