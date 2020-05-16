package com.futurewei.alcor.vpcmanager.dao;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.repo.ICacheRepository;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.vpcmanager.entity.NetworkVxlanType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.logging.Level;

@Repository
@ComponentScan(value="com.futurewei.alcor.common.db")
public class VxlanRepository implements ICacheRepository<NetworkVxlanType> {

    private static final Logger logger = LoggerFactory.getLogger();

    public ICache<String, NetworkVxlanType> getCache() {
        return cache;
    }

    private ICache<String, NetworkVxlanType> cache;

    @Autowired
    public VxlanRepository(CacheFactory cacheFactory) {
        cache = cacheFactory.getCache(NetworkVxlanType.class);
    }

    @PostConstruct
    private void init() {
        logger.log(Level.INFO, "VxlanRepository init completed");
    }

    @Override
    public NetworkVxlanType findItem(String id) throws CacheException {
        return cache.get(id);
    }

    @Override
    public Map<String, NetworkVxlanType> findAllItems() throws CacheException {
        return cache.getAll();
    }

    @Override
    public void addItem(NetworkVxlanType newItem) throws CacheException {
        logger.log(Level.INFO, "Add Vxlan, Vxlan Id:" + newItem.getVxlanId());
        cache.put(newItem.getVxlanId(), newItem);
    }

    @Override
    public void deleteItem(String id) throws CacheException {
        logger.log(Level.INFO, "Delete Vxlan, Vxlan Id:" + id);
        cache.remove(id);
    }

}
