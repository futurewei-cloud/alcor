package com.futurewei.alcor.vpcmanager.dao;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.repo.ICacheRepository;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.web.entity.SegmentEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.logging.Level;

@Repository
@ComponentScan(value="com.futurewei.alcor.common.db")
public class SegmentRepository implements ICacheRepository<SegmentEntity> {

    private static final Logger logger = LoggerFactory.getLogger();

    public ICache<String, SegmentEntity> getCache() {
        return cache;
    }

    private ICache<String, SegmentEntity> cache;

    @Autowired
    public SegmentRepository(CacheFactory cacheFactory) {
        cache = cacheFactory.getCache(SegmentEntity.class);
    }

    @PostConstruct
    private void init() {
        logger.log(Level.INFO, "SegmentRepository init completed");
    }

    @Override
    public SegmentEntity findItem(String id) throws CacheException {
        return cache.get(id);
    }

    @Override
    public Map<String, SegmentEntity> findAllItems() throws CacheException {
        return cache.getAll();
    }

    @Override
    public void addItem(SegmentEntity newItem) throws CacheException {
        logger.log(Level.INFO, "Add segment, Segment Id:" + newItem.getId());
        cache.put(newItem.getId(), newItem);
    }

    @Override
    public void deleteItem(String id) throws CacheException {
        logger.log(Level.INFO, "Delete segment, Segment Id:" + id);
        cache.remove(id);
    }
}

