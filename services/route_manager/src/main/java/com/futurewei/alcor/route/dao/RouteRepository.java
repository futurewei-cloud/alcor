package com.futurewei.alcor.route.dao;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.repo.ICacheRepository;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.web.entity.route.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.logging.Level;

@Repository
@ComponentScan(value="com.futurewei.alcor.common.db")
public class RouteRepository implements ICacheRepository<Route> {
    private static final Logger logger = LoggerFactory.getLogger();

    public ICache<String, Route> getCache() {
        return cache;
    }

    private ICache<String, Route> cache;

    @Autowired
    public RouteRepository (CacheFactory cacheFactory) {
        cache = cacheFactory.getCache(Route.class);
    }

    @PostConstruct
    private void init() {
        logger.log(Level.INFO, "RouteRepository init completed");
    }

    @Override
    public Route findItem(String id) throws CacheException {
        return cache.get(id);
    }

    @Override
    public Map<String, Route> findAllItems() throws CacheException {
        return cache.getAll();
    }

    @Override
    public void addItem(Route routeState) throws CacheException {
        logger.log(Level.INFO, "Add route, route Id:" + routeState.getId());
        cache.put(routeState.getId(), routeState);
    }

    @Override
    public void deleteItem(String id) throws CacheException {
        logger.log(Level.INFO, "Delete route, route Id:" + id);
        cache.remove(id);
    }
}
