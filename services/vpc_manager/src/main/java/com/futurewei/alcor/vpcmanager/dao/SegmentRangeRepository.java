package com.futurewei.alcor.vpcmanager.dao;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.repo.ICacheRepository;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.web.entity.NetworkSegmentRangeWebResponseJson;
import com.futurewei.alcor.web.entity.NetworkSegmentRangeWebResponseObject;
import com.futurewei.alcor.web.entity.SegmentWebResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.logging.Level;

@Repository
@ComponentScan(value="com.futurewei.alcor.common.db")
public class SegmentRangeRepository implements ICacheRepository<NetworkSegmentRangeWebResponseObject> {

    private static final Logger logger = LoggerFactory.getLogger();

    public ICache<String, NetworkSegmentRangeWebResponseObject> getCache() {
        return cache;
    }

    private ICache<String, NetworkSegmentRangeWebResponseObject> cache;

    @Autowired
    public SegmentRangeRepository(CacheFactory cacheFactory) {
        cache = cacheFactory.getCache(NetworkSegmentRangeWebResponseObject.class);
    }

    @PostConstruct
    private void init() {
        logger.log(Level.INFO, "SegmentRangeRepository init completed");
    }

    @Override
    public NetworkSegmentRangeWebResponseObject findItem(String id) throws CacheException {
        return cache.get(id);
    }

    @Override
    public Map<String, NetworkSegmentRangeWebResponseObject> findAllItems() throws CacheException {
        return cache.getAll();
    }

    @Override
    public void addItem(NetworkSegmentRangeWebResponseObject newItem) throws CacheException {
        logger.log(Level.INFO, "Add segment range, Segment Range Id:" + newItem.getId());
        cache.put(newItem.getId(), newItem);
    }

    @Override
    public void deleteItem(String id) throws CacheException {
        logger.log(Level.INFO, "Delete segment range, Segment Range Id:" + id);
        cache.remove(id);
    }
}
