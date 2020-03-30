package com.futurewei.route.dao;

import com.futurewei.alcor.common.exception.CacheException;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.common.repo.ICacheRepository;
import com.futurewei.route.entity.RouteState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.logging.Level;

@Repository
public class RouteRedisRepository implements ICacheRepository<RouteState> {

    private static final String KEY = "RouteState";

    private RedisTemplate<String, RouteState> redisTemplate;

    private HashOperations hashOperations;

    @Autowired
    public RouteRedisRepository(RedisTemplate<String, RouteState> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    private void init() {
        hashOperations = redisTemplate.opsForHash();
    }

    @Override
    public RouteState findItem(String id) throws CacheException {
        return (RouteState) hashOperations.get(KEY, id);
    }

    @Override
    public Map<String, RouteState> findAllItems() throws CacheException {
        return hashOperations.entries(KEY);
    }

    @Override
    public void addItem(RouteState routeState) throws CacheException {
        Logger logger = LoggerFactory.getLogger();
        logger.log(Level.INFO, "Route Id:" + routeState.getId());
        hashOperations.put(KEY, routeState.getId(), routeState);
    }

    @Override
    public void deleteItem(String id) throws CacheException {
        hashOperations.delete(KEY, id);
    }
}
