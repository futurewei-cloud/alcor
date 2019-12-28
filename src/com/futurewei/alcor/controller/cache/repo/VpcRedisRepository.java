package com.futurewei.alcor.controller.cache.repo;

import com.futurewei.alcor.controller.model.VpcState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Map;

@Repository
public class VpcRedisRepository implements ICacheRepository<VpcState> {

    private static final String KEY = "VpcState";

    private RedisTemplate<String, VpcState> redisTemplate;

    private HashOperations hashOperations;

    @Autowired
    public VpcRedisRepository(RedisTemplate<String, VpcState> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    private void init() {
        hashOperations = redisTemplate.opsForHash();
    }

    @Override
    public VpcState findItem(String id) {
        return (VpcState) hashOperations.get(KEY, id);
    }

    @Override
    public Map findAllItems() {
        return hashOperations.entries(KEY);
    }

    @Override
    public void addItem(VpcState newItem) {
        System.out.println("Vpc Id:" + newItem.getId());
        hashOperations.put(KEY, newItem.getId(), newItem);
    }

    @Override
    public void deleteItem(String id) {
        hashOperations.delete(KEY, id);
    }
}
