package com.futurewei.alcor.route.dao;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.repo.ICacheRepository;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.route.entity.RouteState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.logging.Level;

@Repository
@ComponentScan(value="com.futurewei.alcor.common.db")
public class RouteRepository implements ICacheRepository<RouteState> {
    private static final Logger logger = LoggerFactory.getLogger();

    public ICache<String, RouteState> getCache() {
        return cache;
    }

    private ICache<String, RouteState> cache;

    @Autowired
    public RouteRepository (CacheFactory cacheFactory) {
        cache = cacheFactory.getCache(RouteState.class);
    }

    @PostConstruct
    private void init() {
        logger.log(Level.INFO, "RouteRepository init completed");
    }

    @Override
    public RouteState findItem(String id) throws CacheException {
        return cache.get(id);
    }

    @Override
    public Map<String, RouteState> findAllItems() throws CacheException {
        return cache.getAll();
    }

    @Override
    public void addItem(RouteState routeState) throws CacheException {
        logger.log(Level.INFO, "Add route, route Id:" + routeState.getId());
        cache.put(routeState.getId(), routeState);
    }

    @Override
    public void deleteItem(String id) throws CacheException {
        logger.log(Level.INFO, "Delete route, route Id:" + id);
        cache.remove(id);
    }
}
