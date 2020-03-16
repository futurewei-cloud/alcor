package com.futurewei.alcor.controller.db.redis;

import com.futurewei.alcor.controller.db.ICache;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Map;

public class RedisCache<K, V> implements ICache<K, V> {
    private HashOperations hashOperations;
    private String key;

    public RedisCache(RedisTemplate<K, V> redisTemplate, String key) {
        hashOperations = redisTemplate.opsForHash();
        this.key = key;
    }

    @Override
    public V get(K var1) {
        return (V) hashOperations.get(key, var1);
    }

    @Override
    public void put(K var1, V var2) {
        hashOperations.put(key, var1, var2);
    }

    @Override
    public boolean containsKey(K var1) {
        return hashOperations.hasKey(key, var1);
    }

    @Override
    public Map<K, V> getAll() {
        return hashOperations.entries(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> var1) {
        hashOperations.putAll(key, var1);
    }

    @Override
    public boolean remove(K var1) {
        return hashOperations.delete(key, var1) == 1;
    }
}
