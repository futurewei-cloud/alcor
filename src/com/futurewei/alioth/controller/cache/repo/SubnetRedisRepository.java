package com.futurewei.alioth.controller.cache.repo;

import com.futurewei.alioth.controller.model.SubnetState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Map;

@Repository
public class SubnetRedisRepository implements ICacheRepository<SubnetState> {

    private static final String KEY = "SubnetState";

    private RedisTemplate<String, SubnetState> redisTemplate;

    private HashOperations hashOperations;

    @Autowired
    public SubnetRedisRepository(RedisTemplate<String, SubnetState> redisTemplate){
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    private void init(){
        hashOperations = redisTemplate.opsForHash();
    }

    @Override
    public SubnetState findItem(String id) {
        return (SubnetState) hashOperations.get(KEY, id);
    }

    @Override
    public Map findAllItems() {
        return hashOperations.entries(KEY);
    }

    @Override
    public void addItem(SubnetState newItem) {
        System.out.println("Id:" + newItem.getId());
        hashOperations.put(KEY, newItem.getId(), newItem);
    }

    @Override
    public void deleteItem(String id) {
        hashOperations.delete(KEY, id);
    }
}
