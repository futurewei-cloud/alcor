package com.futurewei.alcor.controller.cache.repo;

import com.futurewei.alcor.controller.db.CacheFactory;
import com.futurewei.alcor.controller.db.ICache;
import com.futurewei.alcor.controller.exception.CacheException;
import com.futurewei.alcor.controller.logging.Logger;
import com.futurewei.alcor.controller.logging.LoggerFactory;
import com.futurewei.alcor.controller.model.SubnetState;
import com.futurewei.alcor.controller.schema.Subnet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.logging.Level;

@Repository
@ConditionalOnBean(CacheFactory.class)
public class SubnetRepository implements ICacheRepository<SubnetState> {
    private static final Logger logger = LoggerFactory.getLogger();

    private ICache<String, SubnetState> cache;

    @Autowired
    public SubnetRepository(CacheFactory cacheFactory) {
        cache = cacheFactory.getCache(Subnet.class);
    }

    @PostConstruct
    private void init() {
        logger.log(Level.INFO, "SubnetRepository init completed");
    }

    @Override
    public SubnetState findItem(String id) throws CacheException {
        return cache.get(id);
    }

    @Override
    public Map findAllItems() throws CacheException {
        return cache.getAll();
    }

    @Override
    public void addItem(SubnetState newItem) throws CacheException{
        logger.log(Level.INFO, "Subnet Id:" + newItem.getId());
        cache.put(newItem.getId(), newItem);
    }

    @Override
    public void deleteItem(String id) throws CacheException {
        cache.remove(id);
    }
}
