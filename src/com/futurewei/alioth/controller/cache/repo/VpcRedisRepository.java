package com.futurewei.alioth.controller.cache.repo;

import com.futurewei.alioth.controller.model.VpcState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

public class VpcRedisRepository implements RedisRepository<VpcState> {

    private static final String KEY = "VpcState";

    private RedisTemplate<String, VpcState> redisTemplate;

    private HashOperations hashOperations;

    @Autowired
    public VpcRedisRepository(RedisTemplate<String, VpcState> redisTemplate){
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    private void init(){
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
        hashOperations.put(KEY, newItem.getId(), newItem);
    }

    @Override
    public void deleteItem(String id) {
        hashOperations.delete(KEY, id);
    }
}
