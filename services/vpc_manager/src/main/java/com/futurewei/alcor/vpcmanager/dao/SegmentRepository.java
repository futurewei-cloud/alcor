package com.futurewei.alcor.vpcmanager.dao;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.repo.ICacheRepository;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.web.entity.vpc.SegmentEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Repository
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
    @DurationStatistics
    public SegmentEntity findItem(String id) throws CacheException {
        return cache.get(id);
    }

    @Override
    @DurationStatistics
    public Map<String, SegmentEntity> findAllItems() throws CacheException {
        return cache.getAll();
    }

    @Override
    @DurationStatistics
    public Map<String, SegmentEntity> findAllItems(Map<String, Object[]> queryParams) throws CacheException {
        return cache.getAll(queryParams);
    }

    @Override
    @DurationStatistics
    public void addItem(SegmentEntity newItem) throws CacheException {
        logger.log(Level.INFO, "Add segment, Segment Id:" + newItem.getId());
        cache.put(newItem.getId(), newItem);
    }

    @Override
    @DurationStatistics
    public void addItems(List<SegmentEntity> items) throws CacheException {
        logger.log(Level.INFO, "Add segment batch: {}",items);
        Map<String, SegmentEntity> segmentEntityMap = items.stream().collect(Collectors.toMap(SegmentEntity::getId, Function.identity()));
        cache.putAll(segmentEntityMap);
    }

    @Override
    @DurationStatistics
    public void deleteItem(String id) throws CacheException {
        logger.log(Level.INFO, "Delete segment, Segment Id:" + id);
        cache.remove(id);
    }
}

