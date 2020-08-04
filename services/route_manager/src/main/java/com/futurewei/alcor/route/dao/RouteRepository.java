package com.futurewei.alcor.route.dao;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.Transaction;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.repo.ICacheRepository;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.web.entity.route.RouteEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.logging.Level;

@Repository
public class RouteRepository implements ICacheRepository<RouteEntity> {
    private static final Logger logger = LoggerFactory.getLogger();

    public ICache<String, RouteEntity> getCache() {
        return cache;
    }

    private ICache<String, RouteEntity> cache;

    @Autowired
    public RouteRepository (CacheFactory cacheFactory) {
        cache = cacheFactory.getCache(RouteEntity.class);
    }

    @PostConstruct
    private void init() {
        logger.log(Level.INFO, "RouteRepository init completed");
    }

    @Override
    public RouteEntity findItem(String id) throws CacheException {
        return cache.get(id);
    }

    @Override
    public Map<String, RouteEntity> findAllItems() throws CacheException {
        return cache.getAll();
    }

    @Override
    public Map<String, RouteEntity> findAllItems(Map<String, Object[]> queryParams) throws CacheException {
        return cache.getAll(queryParams);
    }

    @Override
    public void addItem(RouteEntity routeEntity) throws CacheException {
        logger.log(Level.INFO, "Add route, route Id:" + routeEntity.getId());
        cache.put(routeEntity.getId(), routeEntity);

    }

    @Override
    public void deleteItem(String id) throws CacheException {
        logger.log(Level.INFO, "Delete route, route Id:" + id);
        cache.remove(id);
    }
}
