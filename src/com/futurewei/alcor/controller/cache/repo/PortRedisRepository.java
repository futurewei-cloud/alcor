package com.futurewei.alcor.controller.cache.repo;

import com.futurewei.alcor.controller.model.PortState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Map;

@Repository
public class PortRedisRepository implements ICacheRepository<PortState> {

    private static final String KEY = "PortState";

    private RedisTemplate<String, PortState> redisTemplate;

    private HashOperations hashOperations;

    @Autowired
    public PortRedisRepository(RedisTemplate<String, PortState> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    private void init() {
        hashOperations = redisTemplate.opsForHash();
    }

    @Override
    public PortState findItem(String id) {
        return (PortState) hashOperations.get(KEY, id);
    }

    @Override
    public Map findAllItems() {
        return hashOperations.entries(KEY);
    }

    @Override
    public void addItem(PortState newItem) {
        System.out.println("Port Id:" + newItem.getId());
        hashOperations.put(KEY, newItem.getId(), newItem);
    }

    @Override
    public void deleteItem(String id) {
        hashOperations.delete(KEY, id);
    }
}
