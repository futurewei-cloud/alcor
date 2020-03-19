package com.futurewei.alcor.controller.db.redis;

import com.futurewei.alcor.controller.db.ICache;
import com.futurewei.alcor.controller.db.Transaction;
import com.futurewei.alcor.controller.exception.CacheException;
import com.futurewei.alcor.controller.logging.Logger;
import com.futurewei.alcor.controller.logging.LoggerFactory;
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
    public Transaction getTransaction() {
        return transaction;
    }
}
