package com.futurewei.alcor.controller.cache.repo;

import com.futurewei.alcor.controller.db.CacheFactory;
import com.futurewei.alcor.controller.db.ICache;
import com.futurewei.alcor.controller.exception.CacheException;
import com.futurewei.alcor.controller.logging.Logger;
import com.futurewei.alcor.controller.logging.LoggerFactory;
import com.futurewei.alcor.controller.model.PortState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.logging.Level;

@Repository
@ConditionalOnBean(CacheFactory.class)
public class PortRepository implements ICacheRepository<PortState> {
    private static final Logger logger = LoggerFactory.getLogger();

    private ICache<String, PortState> cache;

    @Autowired
    public PortRepository(CacheFactory cacheFactory) {
        cache = cacheFactory.getCache(PortState.class);
    }

    @PostConstruct
    private void init() {
        logger.log(Level.INFO, "PortRepository init completed");
    }

    @Override
    public PortState findItem(String id) throws CacheException {
        return cache.get(id);
    }

    @Override
    public Map findAllItems() throws CacheException {
        return cache.getAll();
    }

    @Override
    public void addItem(PortState newItem) throws CacheException {
        logger.log(Level.INFO, "Port Id:" + newItem.getId());
        cache.put(newItem.getId(), newItem);
    }

    @Override
    public void deleteItem(String id) throws CacheException {
        cache.remove(id);
    }
}
