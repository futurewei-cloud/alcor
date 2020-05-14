package com.futurewei.alcor.vpcmanager.dao;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.repo.ICacheRepository;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.vpcmanager.entity.NetworkVlanType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.logging.Level;

@Repository
@ComponentScan(value="com.futurewei.alcor.common.db")
public class VlanRepository implements ICacheRepository<NetworkVlanType> {

    private static final Logger logger = LoggerFactory.getLogger();

    public ICache<String, NetworkVlanType> getCache() {
        return cache;
    }

    private ICache<String, NetworkVlanType> cache;


    @Autowired
    public VlanRepository(CacheFactory cacheFactory) {
        cache = cacheFactory.getCache(NetworkVlanType.class);
    }

    @PostConstruct
    private void init() {
        logger.log(Level.INFO, "VlanRepository init completed");
    }

    @Override
    public NetworkVlanType findItem(String id) throws CacheException {
        return cache.get(id);
    }

    @Override
    public Map<String, NetworkVlanType> findAllItems() throws CacheException {
        return cache.getAll();
    }

    @Override
    public void addItem(NetworkVlanType newItem) throws CacheException {
        logger.log(Level.INFO, "Add Vlan, Vlan Id:" + newItem.getVlanId());
        cache.put(newItem.getVlanId(), newItem);
    }

    @Override
    public void deleteItem(String id) throws CacheException {
        logger.log(Level.INFO, "Delete Vlan, Vlan Id:" + id);
        cache.remove(id);
    }
}
