package com.futurewei.alcor.vpcmanager.dao;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.repo.ICacheRepository;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.web.entity.vpc.NetworkSegmentRangeEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Repository
public class SegmentRangeRepository implements ICacheRepository<NetworkSegmentRangeEntity> {

    private static final Logger logger = LoggerFactory.getLogger();

    public ICache<String, NetworkSegmentRangeEntity> getCache() {
        return cache;
    }

    private ICache<String, NetworkSegmentRangeEntity> cache;

    @Autowired
    public SegmentRangeRepository(CacheFactory cacheFactory) {
        cache = cacheFactory.getCache(NetworkSegmentRangeEntity.class);
    }

    @PostConstruct
    private void init() {
        logger.log(Level.INFO, "SegmentRangeRepository init completed");
    }

    @Override
    @DurationStatistics
    public NetworkSegmentRangeEntity findItem(String id) throws CacheException {
        return cache.get(id);
    }

    @Override
    @DurationStatistics
    public Map<String, NetworkSegmentRangeEntity> findAllItems() throws CacheException {
        return cache.getAll();
    }

    @Override
    @DurationStatistics
    public Map<String, NetworkSegmentRangeEntity> findAllItems(Map<String, Object[]> queryParams) throws CacheException {
        return cache.getAll(queryParams);
    }

    @Override
    @DurationStatistics
    public void addItem(NetworkSegmentRangeEntity newItem) throws CacheException {
        logger.log(Level.INFO, "Add segment range, Segment Range Id:" + newItem.getId());
        cache.put(newItem.getId(), newItem);
    }

    @Override
    @DurationStatistics
    public void addItems(List<NetworkSegmentRangeEntity> items) throws CacheException {
        logger.log(Level.INFO, "Add segment range batch: {}",items);
        Map<String, NetworkSegmentRangeEntity> networkSegmentRangeEntityMap = items.stream().collect(Collectors.toMap(NetworkSegmentRangeEntity::getId, Function.identity()));
        cache.putAll(networkSegmentRangeEntityMap);
    }

    @Override
    @DurationStatistics
    public void deleteItem(String id) throws CacheException {
        logger.log(Level.INFO, "Delete segment range, Segment Range Id:" + id);
        cache.remove(id);
    }
}
