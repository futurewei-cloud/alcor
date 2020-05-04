package com.futurewei.alcor.vpcmanager.dao;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.repo.ICacheRepository;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.web.entity.NetworkVGREType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.logging.Level;

@Repository
@ComponentScan(value="com.futurewei.alcor.common.db")
public class GreRepository implements ICacheRepository<NetworkVGREType> {

    private static final Logger logger = LoggerFactory.getLogger();

    public ICache<String, NetworkVGREType> getCache() {
        return cache;
    }

    private ICache<String, NetworkVGREType> cache;

    @Autowired
    public GreRepository(CacheFactory cacheFactory) {
        cache = cacheFactory.getCache(NetworkVGREType.class);
    }

    @PostConstruct
    private void init() {
        logger.log(Level.INFO, "GreRepository init completed");
    }

    @Override
    public NetworkVGREType findItem(String id) throws CacheException {
        return cache.get(id);
    }

    @Override
    public Map<String, NetworkVGREType> findAllItems() throws CacheException {
        return cache.getAll();
    }

    @Override
    public void addItem(NetworkVGREType newItem) throws CacheException {
        logger.log(Level.INFO, "Add Gre, Gre Id:" + newItem.getId());
        cache.put(newItem.getId(), newItem);
    }

    @Override
    public void deleteItem(String id) throws CacheException {
        logger.log(Level.INFO, "Delete Gre, Gre Id:" + id);
        cache.remove(id);
    }

}
